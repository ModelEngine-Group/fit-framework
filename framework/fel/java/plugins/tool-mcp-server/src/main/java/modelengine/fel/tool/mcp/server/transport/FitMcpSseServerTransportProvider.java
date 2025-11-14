/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.transport;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import modelengine.fel.tool.mcp.entity.Event;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestParam;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Emitter;
import modelengine.fitframework.log.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Server-side implementation of the Model Context Protocol (MCP) transport layer using
 * HTTP with Server-Sent Events (SSE) through Spring WebMVC. This implementation provides
 * a bridge between synchronous WebMVC operations and reactive programming patterns to
 * maintain compatibility with the reactive transport interface.
 *
 * <p>
 * Key features:
 * <ul>
 * <li>Implements bidirectional communication using HTTP POST for client-to-server
 * messages and SSE for server-to-client messages</li>
 * <li>Manages client sessions with unique IDs for reliable message delivery</li>
 * <li>Supports graceful shutdown with proper session cleanup</li>
 * <li>Provides JSON-RPC message handling through configured endpoints</li>
 * <li>Includes built-in error handling and logging</li>
 * </ul>
 *
 * <p>
 * The transport operates on two main endpoints:
 * <ul>
 * <li>{@code /sse} - The SSE endpoint where clients establish their event stream
 * connection</li>
 * <li>A configurable message endpoint where clients send their JSON-RPC messages via HTTP
 * POST</li>
 * </ul>
 *
 * <p>
 * This implementation uses {@link ConcurrentHashMap} to safely manage multiple client
 * sessions in a thread-safe manner. Each client session is assigned a unique ID and
 * maintains its own SSE connection.
 *
 * @author 黄可欣
 * @since 2025-11-10
 * @see McpServerTransportProvider
 */
public class FitMcpSseServerTransportProvider implements McpServerTransportProvider {
    private static final Logger logger = Logger.get(FitMcpSseServerTransportProvider.class);
    private static final String MESSAGE_ENDPOINT = "/mcp/message";
    private static final String SSE_ENDPOINT = "/mcp/sse";
    /**
     * Event type for sending the message endpoint URI to clients.
     */
    public static final String ENDPOINT_EVENT_TYPE = "endpoint";

    private final McpJsonMapper jsonMapper;
    private McpServerSession.Factory sessionFactory;
    private final Map<String, McpServerSession> sessions = new ConcurrentHashMap();
    private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor;
    private volatile boolean isClosing = false;
    private KeepAliveScheduler keepAliveScheduler;

    /**
     * Constructs a new FitMcpSseServerTransportProvider instance.
     *
     * @param jsonMapper The McpJsonMapper to use for JSON serialization/deserialization
     * of messages.
     * @param keepAliveInterval The interval for sending keep-alive messages to clients.
     * @param contextExtractor The contextExtractor to fill in a
     * {@link McpTransportContext}.
     * @throws IllegalArgumentException if any parameter is null
     */
    private FitMcpSseServerTransportProvider(McpJsonMapper jsonMapper, Duration keepAliveInterval,
            McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor) {
        Assert.notNull(jsonMapper, "McpJsonMapper must not be null");
        Assert.notNull(contextExtractor, "Context extractor must not be null");
        this.jsonMapper = jsonMapper;
        this.contextExtractor = contextExtractor;
        if (keepAliveInterval != null) {
            this.keepAliveScheduler = KeepAliveScheduler.builder(() -> this.isClosing
                            ? Flux.empty()
                            : Flux.fromIterable(this.sessions.values()))
                    .initialDelay(keepAliveInterval)
                    .interval(keepAliveInterval)
                    .build();
            this.keepAliveScheduler.start();
        }
    }

    @Override
    public List<String> protocolVersions() {
        return List.of(ProtocolVersions.MCP_2024_11_05);
    }

    @Override
    public void setSessionFactory(McpServerSession.Factory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Broadcasts a notification to all connected clients through their SSE connections.
     * The message is serialized to JSON and sent as an SSE event with type "message". If
     * any errors occur during sending to a particular client, they are logged but don't
     * prevent sending to other clients.
     *
     * @param method The method name for the notification
     * @param params The parameters for the notification
     * @return A Mono that completes when the broadcast attempt is finished
     */
    @Override
    public Mono<Void> notifyClients(String method, Object params) {
        if (sessions.isEmpty()) {
            logger.debug("No active sessions to broadcast message to");
            return Mono.empty();
        }

        logger.debug("Attempting to broadcast message to {} active sessions", sessions.size());

        return Flux.fromIterable(sessions.values())
                .flatMap(session -> session.sendNotification(method, params)
                        .doOnError(e -> logger.error("Failed to send message to session {}: {}",
                                session.getId(),
                                e.getMessage()))
                        .onErrorComplete())
                .then();
    }

    /**
     * Initiates a graceful shutdown of the transport. This method:
     * <ul>
     * <li>Sets the closing flag to prevent new connections</li>
     * <li>Closes all active SSE connections</li>
     * <li>Removes all session records</li>
     * </ul>
     *
     * @return A Mono that completes when all cleanup operations are finished
     */
    @Override
    public Mono<Void> closeGracefully() {
        return Flux.fromIterable(sessions.values()).doFirst(() -> {
            this.isClosing = true;
            logger.debug("Initiating graceful shutdown with {} active sessions", sessions.size());
        }).flatMap(McpServerSession::closeGracefully).then().doOnSuccess(v -> {
            logger.debug("Graceful shutdown completed");
            sessions.clear();
            if (this.keepAliveScheduler != null) {
                this.keepAliveScheduler.shutdown();
            }
        });
    }

    /**
     * Handles new SSE connection requests from clients by creating a new session and
     * establishing an SSE connection. This method:
     * <ul>
     * <li>Generates a unique session ID</li>
     * <li>Creates a new session with a FitMcpSessionTransport</li>
     * <li>Sends an initial endpoint event to inform the client where to send
     * messages</li>
     * <li>Maintains the session in the sessions map</li>
     * </ul>
     *
     * @param request The incoming server request
     * @return A ServerResponse configured for SSE communication, or an error response if
     * the server is shutting down or the connection fails
     */
    @GetMapping(path = SSE_ENDPOINT)
    private Object handleSseConnection(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            return Entity.createText(response, "Server is shutting down");
        }

        String sessionId = UUID.randomUUID().toString();
        logger.debug("Creating new SSE connection for session: {}", sessionId);
        try {
            return Choir.<TextEvent>create(emitter -> {
                this.addEmitterObserver(emitter, sessionId);
                FitMcpSessionTransport sessionTransport = new FitMcpSessionTransport(sessionId, emitter);
                McpServerSession session = sessionFactory.create(sessionTransport);
                this.sessions.put(sessionId, session);

                try {
                    String initData = MESSAGE_ENDPOINT + "?sessionId=" + sessionId;
                    TextEvent textEvent =
                            TextEvent.custom().id(sessionId).event(ENDPOINT_EVENT_TYPE).data(initData).build();
                    emitter.emit(textEvent);
                    logger.info("[SSE] Sending init data to session. [sessionId={}, initData={}]", sessionId, initData);

                } catch (Exception e) {
                    logger.error("Failed to send initial endpoint event: {}", e.getMessage());
                    emitter.fail(e);
                }
            });
        } catch (Exception e) {
            logger.error("[GET] Failed to handle GET request. [sessionId={}, error={}]", sessionId, e.getMessage(), e);
            sessions.remove(sessionId);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return null;
        }
    }

    /**
     * Handles incoming JSON-RPC messages from clients. This method:
     * <ul>
     * <li>Deserializes the request body into a JSON-RPC message</li>
     * <li>Processes the message through the session's handle method</li>
     * <li>Returns appropriate HTTP responses based on the processing result</li>
     * </ul>
     *
     * @param request The incoming server request containing the JSON-RPC message
     * @return A ServerResponse indicating success (200 OK) or appropriate error status
     * with error details in case of failures
     */
    @PostMapping(path = MESSAGE_ENDPOINT)
    private Object handleMessage(HttpClassicServerRequest request, HttpClassicServerResponse response,
            @RequestParam("sessionId") String sessionId) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            return Entity.createText(response, "Server is shutting down");
        }
        Object sessionError = validateRequestSessionId(sessionId, response);
        if (sessionError != null) {
            return sessionError;
        }

        McpServerSession session = this.sessions.get(sessionId);
        logger.info("[POST] Receiving delete request. [sessionId={}]", sessionId);
        try {
            final McpTransportContext transportContext = this.contextExtractor.extract(request);

            String requestBody = new String(request.entityBytes(), StandardCharsets.UTF_8);
            McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(jsonMapper, requestBody);
            session.handle(message).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
            response.statusCode(HttpResponseStatus.OK.statusCode());
            return null;
        } catch (IllegalArgumentException | IOException e) {
            logger.error("[POST] Failed to deserialize message. [error={}]", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.PARSE_ERROR).message("Invalid message format").build());
        } catch (Exception e) {
            logger.error("[POST] Error handling message. [error={}]", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    private void addEmitterObserver(Emitter<TextEvent> emitter, String sessionId) {
        emitter.observe(new Emitter.Observer<TextEvent>() {
            @Override
            public void onEmittedData(TextEvent data) {
                // No action needed
            }

            @Override
            public void onCompleted() {
                logger.info("[SSE] Completed SSE emitting. [sessionId={}]", sessionId);
                try {
                    FitMcpSseServerTransportProvider.this.sessions.remove(sessionId);
                } catch (Exception e) {
                    logger.warn("[SSE] Error closing listeningStream on complete. [sessionId={}, error={}]",
                            sessionId,
                            e.getMessage());
                }
            }

            @Override
            public void onFailed(Exception cause) {
                logger.warn("[SSE] SSE failed. [sessionId={}, cause={}]", sessionId, cause.getMessage());
                try {
                    FitMcpSseServerTransportProvider.this.sessions.remove(sessionId);
                } catch (Exception e) {
                    logger.warn("[SSE] Error closing listeningStream on failure. [sessionId={}, error={}]",
                            sessionId,
                            e.getMessage());
                }
            }
        });
    }

    /**
     * Validates the MCP session ID in the request headers and verifies the session exists.
     * This method checks both the presence of the {@code mcp-session-id} header and
     * the existence of the corresponding session in the active sessions map.
     *
     * @param sessionId The {@link String} session ID in request parameter.
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails
     * @return An error {@link Entity} if validation fails (either missing session ID or session not found),
     * {@code null} if validation succeeds
     */
    private Object validateRequestSessionId(String sessionId, HttpClassicServerResponse response) {
        if (sessionId.isEmpty()) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID missing in message endpoint");
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
     * Implementation of McpServerTransport for WebMVC SSE sessions. This class handles
     * the transport-level communication for a specific client session.
     */
    private class FitMcpSessionTransport implements McpServerTransport {
        private final String sessionId;
        private final Emitter<TextEvent> emitter;

        /**
         * Lock to ensure thread-safe access to the SSE builder when sending messages.
         * This prevents concurrent modifications that could lead to corrupted SSE events.
         */
        private final ReentrantLock sseBuilderLock = new ReentrantLock();

        /**
         * Creates a new session transport with the specified ID and SSE builder.
         *
         * @param sessionId The unique identifier for this session
         * @param emitter The emitter for sending events
         */
        FitMcpSessionTransport(String sessionId, Emitter<TextEvent> emitter) {
            this.sessionId = sessionId;
            this.emitter = emitter;
            logger.info("[SSE] Building SSE emitter. [sessionId={}]", sessionId);
        }

        /**
         * Sends a JSON-RPC message to the client through the SSE connection.
         *
         * @param message The JSON-RPC message to send
         * @return A Mono that completes when the message has been sent
         */
        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return Mono.fromRunnable(() -> {
                sseBuilderLock.lock();
                try {
                    String jsonText = jsonMapper.writeValueAsString(message);
                    TextEvent textEvent =
                            TextEvent.custom().id(this.sessionId).event(Event.MESSAGE.code()).data(jsonText).build();
                    this.emitter.emit(textEvent);
                    FitMcpSseServerTransportProvider.logger.debug("Message sent to session {}", this.sessionId);
                } catch (Exception e) {
                    logger.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
                    this.emitter.fail(e);
                } finally {
                    sseBuilderLock.unlock();
                }
            });
        }

        /**
         * Converts data from one type to another using the configured McpJsonMapper.
         *
         * @param data The source data object to convert
         * @param typeRef The target type reference
         * @param <T> The target type
         * @return The converted object of type T
         */
        @Override
        public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
            return jsonMapper.convertValue(data, typeRef);
        }

        /**
         * Initiates a graceful shutdown of the transport.
         *
         * @return A Mono that completes when the shutdown is complete
         */
        @Override
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(() -> {
                logger.debug("Closing session transport: {}", sessionId);
                sseBuilderLock.lock();
                try {
                    this.emitter.complete();
                    logger.debug("Successfully completed SSE builder for session {}", sessionId);
                } catch (Exception e) {
                    logger.warn("Failed to complete SSE builder for session {}: {}", sessionId, e.getMessage());
                } finally {
                    sseBuilderLock.unlock();
                }
            });
        }

        /**
         * Closes the transport immediately.
         */
        @Override
        public void close() {
            sseBuilderLock.lock();
            try {
                this.emitter.complete();
                logger.debug("Successfully completed SSE builder for session {}", sessionId);
            } catch (Exception e) {
                logger.warn("Failed to complete SSE builder for session {}: {}", sessionId, e.getMessage());
            } finally {
                sseBuilderLock.unlock();
            }
        }

    }

    /**
     * Creates a new Builder instance for configuring and creating instances of
     * FitMcpSseServerTransportProvider.
     *
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating instances of FitMcpSseServerTransportProvider.
     * <p>
     * This builder provides a fluent API for configuring and creating instances of
     * FitMcpSseServerTransportProvider with custom settings.
     */
    public static class Builder {
        private McpJsonMapper jsonMapper;
        private Duration keepAliveInterval;
        private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor =
                (serverRequest) -> McpTransportContext.EMPTY;

        /**
         * Sets the JSON object mapper to use for message serialization/deserialization.
         *
         * @param jsonMapper The object mapper to use
         * @return This builder instance for method chaining
         */
        public Builder jsonMapper(McpJsonMapper jsonMapper) {
            Assert.notNull(jsonMapper, "McpJsonMapper must not be null");
            this.jsonMapper = jsonMapper;
            return this;
        }

        /**
         * Sets the interval for keep-alive pings.
         * <p>
         * If not specified, keep-alive pings will be disabled.
         *
         * @param keepAliveInterval The interval duration for keep-alive pings
         * @return This builder instance for method chaining
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Sets the context extractor that allows providing the MCP feature
         * implementations to inspect HTTP transport level metadata that was present at
         * HTTP request processing time. This allows to extract custom headers and other
         * useful data for use during execution later on in the process.
         *
         * @param contextExtractor The contextExtractor to fill in a
         * {@link McpTransportContext}.
         * @return this builder instance
         * @throws IllegalArgumentException if contextExtractor is null
         */
        public Builder contextExtractor(McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor) {
            Assert.notNull(contextExtractor, "contextExtractor must not be null");
            this.contextExtractor = contextExtractor;
            return this;
        }

        /**
         * Builds a new instance of FitMcpSseServerTransportProvider with the configured
         * settings.
         *
         * @return A new FitMcpSseServerTransportProvider instance
         * @throws IllegalStateException if jsonMapper or messageEndpoint is not set
         */
        public FitMcpSseServerTransportProvider build() {
            return new FitMcpSseServerTransportProvider(
                    this.jsonMapper == null ? McpJsonMapper.getDefault() : this.jsonMapper,
                    this.keepAliveInterval,
                    this.contextExtractor);
        }
    }
}
