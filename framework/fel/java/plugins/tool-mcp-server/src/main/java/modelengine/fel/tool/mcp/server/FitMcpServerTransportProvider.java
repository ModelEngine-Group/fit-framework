/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import modelengine.fel.tool.mcp.entity.Event;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fitframework.flowable.Emitter;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class for FIT MCP Server Transport Providers.
 * This class provides common functionality for both SSE and Streamable transport implementations.
 *
 * @param <S> The session type
 * @author 黄可欣
 * @since 2025-11-19
 */
public abstract class FitMcpServerTransportProvider<S> {
    private static final Logger logger = Logger.get(FitMcpServerTransportProvider.class);
    protected final McpJsonMapper jsonMapper;
    protected final McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor;
    protected KeepAliveScheduler keepAliveScheduler;

    protected volatile boolean isClosing = false;
    protected final Map<String, S> sessions = new ConcurrentHashMap<>();

    /**
     * Constructs a new FitMcpServerTransportProvider instance.
     *
     * @param jsonMapper The JSON mapper for serialization/deserialization.
     * @param contextExtractor The context extractor for HTTP requests.
     * @param keepAliveInterval The interval for keep-alive messages, or null to disable.
     */
    protected FitMcpServerTransportProvider(McpJsonMapper jsonMapper,
            McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor, Duration keepAliveInterval) {
        Validation.notNull(jsonMapper, "MCP Json mapper must not be null.");
        Validation.notNull(contextExtractor, "Context extractor must not be null.");

        this.jsonMapper = jsonMapper;
        this.contextExtractor = contextExtractor;
        if (keepAliveInterval != null) {
            this.initKeepAliveScheduler(keepAliveInterval);
        }
    }

    /**
     * Initializes the keep-alive scheduler with the specified interval.
     *
     * @param keepAliveInterval The interval for keep-alive messages.
     */
    protected abstract void initKeepAliveScheduler(Duration keepAliveInterval);

    /**
     * Gets the session ID from a session object.
     *
     * @param session The session object.
     * @return The session ID.
     */
    protected abstract String getSessionId(S session);

    /**
     * Closes a session gracefully.
     *
     * @param session The session to close.
     * @return A Mono that completes when the session is closed.
     */
    protected abstract Mono<Void> closeSession(S session);

    /**
     * Sends a notification to a specific session.
     *
     * @param session The session to send to.
     * @param method The notification method name.
     * @param params The notification parameters.
     * @return A Mono that completes when the notification is sent.
     */
    protected abstract Mono<Void> sendNotificationToSession(S session, String method, Object params);

    /**
     * Broadcasts a notification to all connected clients.
     * If any errors occur during sending to a particular client, they are logged but
     * don't prevent sending to other clients.
     *
     * @param method The method name for the notification.
     * @param params The parameters for the notification.
     * @return A Mono that completes when the broadcast attempt is finished.
     */
    public Mono<Void> notifyClients(String method, Object params) {
        if (this.sessions.isEmpty()) {
            logger.debug("No active sessions to broadcast message to.");
            return Mono.empty();
        }

        logger.debug("Attempting to broadcast message. [activeSessions={}]", this.sessions.size());

        return Mono.fromRunnable(() -> this.sessions.values().parallelStream().forEach(session -> {
            try {
                this.sendNotificationToSession(session, method, params).block();
            } catch (Exception e) {
                logger.error("Failed to send message to session. [sessionId={}, error={}]",
                        this.getSessionId(session),
                        e.getMessage(),
                        e);
            }
        }));
    }

    /**
     * Initiates a graceful shutdown of the transport.
     *
     * @return A Mono that completes when all cleanup operations are finished.
     */
    public Mono<Void> closeGracefully() {
        this.isClosing = true;
        logger.debug("Initiating graceful shutdown. [activeSessions={}]", this.sessions.size());

        return Mono.fromRunnable(() -> {
            this.sessions.values().parallelStream().forEach(session -> {
                try {
                    this.closeSession(session).block();
                } catch (Exception e) {
                    logger.error("Failed to close session. [sessionId={}, error={}]",
                            this.getSessionId(session),
                            e.getMessage(),
                            e);
                }
            });

            logger.debug("Graceful shutdown completed.");
            this.sessions.clear();
            if (this.keepAliveScheduler != null) {
                this.keepAliveScheduler.shutdown();
            }
        });
    }

    /**
     * Creates a response indicating the server is shutting down.
     *
     * @param response The HTTP response.
     * @return An Entity with the shutdown message.
     */
    protected Object createShuttingDownResponse(HttpClassicServerResponse response) {
        response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
        return Entity.createText(response, "Server is shutting down.");
    }

    /**
     * Validates that a session exists for the given session ID.
     *
     * @param sessionId The session ID to validate.
     * @param response The HTTP response to set status code if validation fails.
     * @return An error Entity if validation fails, null if validation succeeds.
     */
    protected Object validateSessionExists(String sessionId, HttpClassicServerResponse response) {
        if (sessionId == null || sessionId.isEmpty()) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID missing.");
        }
        if (this.sessions.get(sessionId) == null) {
            response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
                            .message("Session not found: " + sessionId)
                            .build());
        }
        return null;
    }

    /**
     * Deserializes a JSON-RPC message from the request body.
     *
     * @param requestBody The request body string to deserialize.
     * @param response The HTTP response to set error status if deserialization fails.
     * @return The deserialized {@link McpSchema.JSONRPCMessage}, or {@code null} if deserialization fails.
     */
    protected McpSchema.JSONRPCMessage deserializeMessage(String requestBody, HttpClassicServerResponse response) {
        try {
            return McpSchema.deserializeJsonRpcMessage(this.jsonMapper, requestBody);
        } catch (IllegalArgumentException | IOException e) {
            logger.error("Failed to deserialize message. [error={}]", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return null;
        }
    }

    /**
     * Abstract base class for session transport implementations.
     * Provides common functionality for sending messages over SSE connections.
     */
    protected abstract class AbstractFitMcpSessionTransport {
        protected final String sessionId;
        protected final Emitter<TextEvent> emitter;
        protected final HttpClassicServerResponse response;

        protected final ReentrantLock lock = new ReentrantLock();
        protected volatile boolean closed = false;

        /**
         * Creates a new session transport.
         *
         * @param sessionId The unique identifier for this session.
         * @param emitter The emitter for sending SSE events.
         * @param response The HTTP response for checking connection status.
         */
        protected AbstractFitMcpSessionTransport(String sessionId, Emitter<TextEvent> emitter,
                HttpClassicServerResponse response) {
            this.sessionId = sessionId;
            this.emitter = emitter;
            this.response = response;
            FitMcpServerTransportProvider.logger.info("[SSE] Building SSE emitter. [sessionId={}]", sessionId);
        }

        /**
         * Sends a JSON-RPC message to the client through the SSE connection.
         * This method is thread-safe and checks if the connection is still active before sending.
         *
         * @param message The JSON-RPC message to send.
         * @return A Mono that completes when the message has been sent.
         */
        protected Mono<Void> doSendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return Mono.fromRunnable(() -> {
                if (this.closed) {
                    FitMcpServerTransportProvider.logger.info(
                            "[SSE] Attempted to send message to closed session. [sessionId={}]",
                            this.sessionId);
                    return;
                }
                this.lock.lock();
                try {
                    if (this.closed) {
                        FitMcpServerTransportProvider.logger.info(
                                "[SSE] Session was closed during message send attempt. [sessionId={}]",
                                this.sessionId);
                        return;
                    }

                    if (!this.response.isActive()) {
                        FitMcpServerTransportProvider.logger.warn(
                                "[SSE] Connection inactive detected while sending message. [sessionId={}]",
                                this.sessionId);
                        this.doClose();
                        return;
                    }

                    String jsonText = FitMcpServerTransportProvider.this.jsonMapper.writeValueAsString(message);
                    TextEvent textEvent = TextEvent.custom()
                            .id(messageId != null ? messageId : this.sessionId)
                            .event(Event.MESSAGE.code())
                            .data(jsonText)
                            .build();
                    this.emitter.emit(textEvent);

                    FitMcpServerTransportProvider.logger.info(
                            "[SSE] Sending message to session. [sessionId={}, eventId={}, jsonText={}]",
                            this.sessionId,
                            messageId != null ? messageId : this.sessionId,
                            jsonText);
                } catch (Exception e) {
                    FitMcpServerTransportProvider.logger.error(
                            "[SSE] Failed to send message to session. [sessionId={}, error={}]",
                            this.sessionId,
                            e.getMessage(),
                            e);
                    try {
                        this.emitter.fail(e);
                    } catch (Exception errorException) {
                        FitMcpServerTransportProvider.logger.error(
                                "[SSE] Failed to send error to SSE builder. [sessionId={}, error={}]",
                                this.sessionId,
                                errorException.getMessage(),
                                errorException);
                    }
                } finally {
                    this.lock.unlock();
                }
            });
        }

        /**
         * Converts data from one type to another using the configured McpJsonMapper.
         *
         * @param data The source data object to convert.
         * @param typeRef The target type reference.
         * @param <T> The target type.
         * @return The converted object of type T.
         */
        public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
            return FitMcpServerTransportProvider.this.jsonMapper.convertValue(data, typeRef);
        }

        /**
         * Initiates a graceful shutdown of the transport.
         *
         * @return A Mono that completes when the shutdown is complete.
         */
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(this::doClose);
        }

        /**
         * Closes the transport immediately.
         * Completes the SSE emitter and releases any associated resources.
         */
        protected void doClose() {
            this.lock.lock();
            try {
                if (this.closed) {
                    FitMcpServerTransportProvider.logger.info("[SSE] Session transport already closed. [sessionId={}]",
                            this.sessionId);
                    return;
                }

                this.closed = true;
                FitMcpServerTransportProvider.logger.debug("[SSE] Closing session transport. [sessionId={}]",
                        this.sessionId);

                this.emitter.complete();
                FitMcpServerTransportProvider.logger.info("[SSE] Closed SSE builder successfully. [sessionId={}]",
                        this.sessionId);
            } catch (Exception e) {
                FitMcpServerTransportProvider.logger.warn(
                        "[SSE] Failed to complete SSE builder. [sessionId={}, error={}]",
                        this.sessionId,
                        e.getMessage());
            } finally {
                this.lock.unlock();
            }
        }
    }
}
