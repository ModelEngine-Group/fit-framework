/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.transport;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import modelengine.fel.tool.mcp.server.FitMcpServerTransportProvider;
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
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * The default implementation of {@link McpServerTransportProvider}.
 * The FIT transport provider for MCP SSE Server, according to {@code HttpServletSseServerTransportProvider} in MCP
 * SDK.
 *
 * @author 黄可欣
 * @since 2025-11-19
 */
public class FitMcpSseServerTransportProvider extends FitMcpServerTransportProvider<McpServerSession>
        implements McpServerTransportProvider {
    private static final Logger logger = Logger.get(FitMcpSseServerTransportProvider.class);
    private static final String MESSAGE_ENDPOINT = "/mcp/message";
    private static final String SSE_ENDPOINT = "/mcp/sse";
    public static final String ENDPOINT_EVENT_TYPE = "endpoint";
    private McpServerSession.Factory sessionFactory;

    /**
     * Constructs a new FitMcpSseServerTransportProvider instance.
     *
     * @param jsonMapper The McpJsonMapper to use for JSON serialization/deserialization
     * of messages.
     * @param keepAliveInterval The interval for sending keep-alive messages to clients.
     * @param contextExtractor The contextExtractor to fill in a
     * {@link McpTransportContext}.
     * @throws IllegalArgumentException if any parameter is null.
     */
    private FitMcpSseServerTransportProvider(McpJsonMapper jsonMapper, Duration keepAliveInterval,
            McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor) {
        super(jsonMapper, contextExtractor, keepAliveInterval);
    }

    @Override
    protected void initKeepAliveScheduler(Duration keepAliveInterval) {
        this.keepAliveScheduler = KeepAliveScheduler.builder(() -> this.isClosing
                        ? Flux.empty()
                        : Flux.fromIterable(this.sessions.values()))
                .initialDelay(keepAliveInterval)
                .interval(keepAliveInterval)
                .build();
        this.keepAliveScheduler.start();
    }

    @Override
    protected String getSessionId(McpServerSession session) {
        return session.getId();
    }

    @Override
    protected Mono<Void> closeSession(McpServerSession session) {
        return session.closeGracefully();
    }

    @Override
    protected Mono<Void> sendNotificationToSession(McpServerSession session, String method, Object params) {
        return session.sendNotification(method, params);
    }

    /**
     * Returns the list of supported MCP protocol versions.
     *
     * @return A list of supported protocol version strings.
     */
    @Override
    public List<String> protocolVersions() {
        return List.of(ProtocolVersions.MCP_2024_11_05);
    }

    /**
     * Sets the session factory used to create new MCP server sessions.
     *
     * @param sessionFactory The factory for creating server sessions.
     */
    @Override
    public void setSessionFactory(McpServerSession.Factory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Handles new SSE connection requests from clients by creating a new session and
     * establishing an SSE connection. This method:
     * <ul>
     * <li>Generates a unique session ID</li>
     * <li>Creates a new session with a {@link FitSseMcpSessionTransport}</li>
     * <li>Sends an initial endpoint event to inform the client where to send messages</li>
     * <li>Maintains the session in the sessions map</li>
     * </ul>
     *
     * @param request The incoming server request.
     * @param response The HTTP response for SSE communication.
     * @return A {@link Choir}{@code <}{@link TextEvent}{@code >} object for SSE streaming,
     * or an error response if the server is shutting down or the connection fails.
     */
    @GetMapping(path = SSE_ENDPOINT)
    public Object handleSseConnection(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            return this.createShuttingDownResponse(response);
        }

        String sessionId = UUID.randomUUID().toString();
        logger.debug("Creating new SSE connection. [sessionId={}]", sessionId);
        try {
            return Choir.<TextEvent>create(emitter -> {
                this.addEmitterObserver(emitter, sessionId);
                FitSseMcpSessionTransport sessionTransport =
                        new FitSseMcpSessionTransport(sessionId, emitter, response);
                McpServerSession session = this.sessionFactory.create(sessionTransport);
                this.sessions.put(sessionId, session);
                try {
                    String initData = MESSAGE_ENDPOINT + "?sessionId=" + sessionId;
                    TextEvent textEvent =
                            TextEvent.custom().id(sessionId).event(ENDPOINT_EVENT_TYPE).data(initData).build();
                    emitter.emit(textEvent);
                    logger.info("[SSE] Sending init data to session. [sessionId={}, initData={}]", sessionId, initData);

                } catch (Exception e) {
                    logger.error("Failed to send initial endpoint event. [error={}]", e.getMessage(), e);
                    emitter.fail(e);
                }
            });
        } catch (Exception e) {
            logger.error("[GET] Failed to handle GET request. [sessionId={}, error={}]", sessionId, e.getMessage(), e);
            this.sessions.remove(sessionId);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return null;
        }
    }

    /**
     * Handles incoming JSON-RPC messages from clients. This method:
     * <ul>
     * <li>Validates the session ID from the request parameter</li>
     * <li>Deserializes the request body into a JSON-RPC message</li>
     * <li>Processes the message through the session's handle method</li>
     * <li>Returns appropriate HTTP responses based on the processing result</li>
     * </ul>
     *
     * @param request The incoming server request containing the JSON-RPC message.
     * @param response The HTTP response to set status code and return data.
     * @param sessionId The session ID from the request parameter.
     * @return An error {@link Entity} if validation fails, or {@code null} on success.
     */
    @PostMapping(path = MESSAGE_ENDPOINT)
    public Object handleMessage(HttpClassicServerRequest request, HttpClassicServerResponse response,
            @RequestParam("sessionId") String sessionId) {
        if (this.isClosing) {
            return this.createShuttingDownResponse(response);
        }
        Object sessionError = this.validateRequestSessionId(sessionId, response);
        if (sessionError != null) {
            return sessionError;
        }
        McpServerSession session = this.sessions.get(sessionId);

        String requestBody = new String(request.entityBytes(), StandardCharsets.UTF_8);
        McpSchema.JSONRPCMessage message = this.deserializeMessage(requestBody, response);
        if (message == null) {
            logger.error("[POST] Invalid message format. [sessionId={}, requestBody={}]", sessionId, requestBody);
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.PARSE_ERROR).message("Invalid message format.").build());
        }
        logger.info("[POST] Receiving message from session. [sessionId={}, requestBody={}]", sessionId, requestBody);
        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            session.handle(message).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
            response.statusCode(HttpResponseStatus.OK.statusCode());
            return null;
        } catch (Exception e) {
            logger.error("[POST] Error handling message. [error={}]", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Adds an observer to the SSE emitter to handle connection lifecycle events.
     * The observer removes the session from the sessions map when the connection
     * completes or fails.
     *
     * @param emitter The SSE emitter to observe.
     * @param sessionId The session ID associated with this emitter.
     */
    private void addEmitterObserver(Emitter<TextEvent> emitter, String sessionId) {
        emitter.observe(new Emitter.Observer<TextEvent>() {
            @Override
            public void onEmittedData(TextEvent data) {
                // No action needed
            }

            @Override
            public void onCompleted() {
                FitMcpSseServerTransportProvider.this.sessions.remove(sessionId);
                FitMcpSseServerTransportProvider.logger.info(
                        "[SSE] Completed SSE emitting and closed session successfully. [sessionId={}]",
                        sessionId);
            }

            @Override
            public void onFailed(Exception cause) {
                FitMcpSseServerTransportProvider.this.sessions.remove(sessionId);
                FitMcpSseServerTransportProvider.logger.warn(
                        "[SSE] SSE failed, session closed. [sessionId={}, cause={}]",
                        sessionId,
                        cause.getMessage());
            }
        });
    }

    /**
     * Validates the MCP session ID in the request headers and verifies the session exists.
     * This method checks both the presence of the {@code mcp-session-id} header and
     * the existence of the corresponding session in the active sessions map.
     *
     * @param sessionId The {@link String} session ID in request parameter.
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails.
     * @return An error {@link Entity} if validation fails (either missing session ID or session not found),
     * {@code null} if validation succeeds.
     */
    private Object validateRequestSessionId(String sessionId, HttpClassicServerResponse response) {
        if (sessionId.isEmpty()) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID missing in message endpoint.");
        }
        return this.validateSessionExists(sessionId, response);
    }

    /**
     * Implementation of {@link McpServerTransport} for FIT SSE sessions.
     * This class handles the transport-level communication for a specific client session.
     *
     * <p>
     * This class is thread-safe and uses a {@link java.util.concurrent.locks.ReentrantLock} to synchronize access to
     * the
     * underlying SSE emitter to prevent race conditions when multiple threads attempt to
     * send messages concurrently.
     */
    private class FitSseMcpSessionTransport extends AbstractFitMcpSessionTransport implements McpServerTransport {
        FitSseMcpSessionTransport(String sessionId, Emitter<TextEvent> emitter, HttpClassicServerResponse response) {
            super(sessionId, emitter, response);
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return this.doSendMessage(message, null);
        }

        @Override
        public void close() {
            this.doClose();
        }
    }

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
         * @param jsonMapper The object mapper to use.
         * @return This builder instance for method chaining.
         */
        public Builder jsonMapper(McpJsonMapper jsonMapper) {
            Validation.notNull(jsonMapper, "MCP Json mapper must not be null.");
            this.jsonMapper = jsonMapper;
            return this;
        }

        /**
         * Sets the interval for keep-alive pings.
         * <p>
         * If not specified, keep-alive pings will be disabled.
         *
         * @param keepAliveInterval The interval duration for keep-alive pings.
         * @return This builder instance for method chaining.
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
         * @return This builder instance.
         * @throws IllegalArgumentException if contextExtractor is null.
         */
        public Builder contextExtractor(McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor) {
            Validation.notNull(contextExtractor, "Context extractor must not be null.");
            this.contextExtractor = contextExtractor;
            return this;
        }

        /**
         * Builds a new instance of FitMcpSseServerTransportProvider with the configured
         * settings.
         *
         * @return A new FitMcpSseServerTransportProvider instance.
         * @throws IllegalStateException if jsonMapper or messageEndpoint is not set.
         */
        public FitMcpSseServerTransportProvider build() {
            Validation.notNull(this.jsonMapper, "Json mapper must be set.");

            return new FitMcpSseServerTransportProvider(
                    this.jsonMapper == null ? McpJsonMapper.getDefault() : this.jsonMapper,
                    this.keepAliveInterval,
                    this.contextExtractor);
        }
    }
}
