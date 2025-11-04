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
import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransport;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import modelengine.fel.tool.mcp.entity.Event;
import modelengine.fit.http.annotation.DeleteMapping;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.protocol.MessageHeaderNames;
import modelengine.fit.http.protocol.MimeType;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Emitter;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.log.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default implementation of {@link McpStreamableServerTransportProvider}.
 * The FIT transport provider for MCP Server, according to {@code WebMvcStreamableServerTransportProvider} in MCP SDK.
 *
 * @author 黄可欣
 * @since 2025-09-30
 */
public class FitMcpStreamableServerTransportProvider implements McpStreamableServerTransportProvider {
    private static final Logger logger = Logger.get(FitMcpStreamableServerTransportProvider.class);

    private static final String MESSAGE_ENDPOINT = "/mcp/streamable";

    /**
     * Flag indicating whether DELETE requests are disallowed on the endpoint.
     */
    private final boolean disallowDelete;
    private final McpJsonMapper jsonMapper;
    private final McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor;
    private KeepAliveScheduler keepAliveScheduler;

    private McpStreamableServerSession.Factory sessionFactory;

    /**
     * Map of active client sessions, keyed by mcp-session-id.
     */
    private final Map<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();

    /**
     * Flag indicating if the transport is shutting down.
     */
    private volatile boolean isClosing = false;

    /**
     * Constructs a new FitMcpStreamableServerTransportProvider instance,
     * for {@link FitMcpStreamableServerTransportProvider.Builder}.
     *
     * @param jsonMapper The jsonMapper to use for JSON serialization/deserialization
     * of messages.
     * @param disallowDelete Whether to disallow DELETE requests on the endpoint.
     * @param contextExtractor The context extractor to fill in a {@link McpTransportContext}.
     * @param keepAliveInterval The interval for sending keep-alive messages to clients.
     * @throws IllegalArgumentException if any parameter is null
     */
    private FitMcpStreamableServerTransportProvider(McpJsonMapper jsonMapper, boolean disallowDelete,
            McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor, Duration keepAliveInterval) {
        Validation.notNull(jsonMapper, "jsonMapper must not be null");
        Validation.notNull(contextExtractor, "McpTransportContextExtractor must not be null");

        this.jsonMapper = jsonMapper;
        this.disallowDelete = disallowDelete;
        this.contextExtractor = contextExtractor;

        if (keepAliveInterval != null) {
            this.keepAliveScheduler = KeepAliveScheduler.builder(() -> (isClosing)
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
        return List.of(ProtocolVersions.MCP_2024_11_05,
                ProtocolVersions.MCP_2025_03_26,
                ProtocolVersions.MCP_2025_06_18);
    }

    @Override
    public void setSessionFactory(McpStreamableServerSession.Factory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Broadcasts a notification to all connected clients through their SSE connections.
     * If any errors occur during sending to a particular client, they are logged but
     * don't prevent sending to other clients.
     *
     * @param method The method name for the notification
     * @param params The parameters for the notification
     * @return A Mono that completes when the broadcast attempt is finished
     */
    @Override
    public Mono<Void> notifyClients(String method, Object params) {
        if (this.sessions.isEmpty()) {
            logger.debug("No active sessions to broadcast message to");
            return Mono.empty();
        }

        logger.info("Attempting to broadcast message to {} active sessions", this.sessions.size());

        return Mono.fromRunnable(() -> {
            this.sessions.values().parallelStream().forEach(session -> {
                try {
                    session.sendNotification(method, params).block();
                } catch (Exception e) {
                    logger.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                }
            });
        });
    }

    /**
     * Initiates a graceful shutdown of the transport.
     *
     * @return A Mono that completes when all cleanup operations are finished
     */
    @Override
    public Mono<Void> closeGracefully() {
        return Mono.fromRunnable(() -> {
            this.isClosing = true;
            logger.info("Initiating graceful shutdown with {} active sessions", this.sessions.size());

            this.sessions.values().parallelStream().forEach(session -> {
                try {
                    session.closeGracefully().block();
                } catch (Exception e) {
                    logger.error("Failed to close session {}: {}", session.getId(), e.getMessage());
                }
            });

            this.sessions.clear();
            logger.info("Graceful shutdown completed");
        }).then().doOnSuccess(v -> {
            if (this.keepAliveScheduler != null) {
                this.keepAliveScheduler.shutdown();
            }
        });
    }

    /**
     * Set up the listening SSE connections and message replay.
     *
     * @param request The incoming server request
     * @param response The HTTP response
     * @return Return the HTTP response body {@link Entity} or a {@link Choir}{@code <}{@link TextEvent}{@code >} object
     */
    @GetMapping(path = MESSAGE_ENDPOINT)
    public Object handleGet(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            return Entity.createText(response, "Server is shutting down");
        }

        String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Invalid Accept header. Expected TEXT_EVENT_STREAM");
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID required in mcp-session-id header");
        }

        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);

        if (session == null) {
            response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
                            .message("Session not found: " + sessionId)
                            .build());
        }

        logger.info("[GET] Handling GET request for session: {}", sessionId);

        try {
            return Choir.<TextEvent>create(emitter -> {
                // TODO emitter.onTimeout() logger.info()

                FitStreamableMcpSessionTransport sessionTransport =
                        new FitStreamableMcpSessionTransport(sessionId, emitter, response);

                // Check if this is a replay request
                if (request.headers().contains(HttpHeaders.LAST_EVENT_ID)) {
                    String lastId = request.headers().first(HttpHeaders.LAST_EVENT_ID).orElse("0");

                    logger.info("[GET] Receiving replay request from session: {}", sessionId);
                    try {
                        session.replay(lastId)
                                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                                .toIterable()
                                .forEach(message -> {
                                    try {
                                        sessionTransport.sendMessage(message)
                                                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                                                .block();
                                    } catch (Exception e) {
                                        logger.error("Failed to replay message: {}", e.getMessage());
                                        emitter.fail(e);
                                    }
                                });
                    } catch (Exception e) {
                        logger.error("Failed to replay messages: {}", e.getMessage());
                        emitter.fail(e);
                    }
                } else {
                    // Establish new listening stream
                    logger.info("[GET] Receiving Get request to establish new SSE for session: {}", sessionId);
                    McpStreamableServerSession.McpStreamableServerSessionStream listeningStream =
                            session.listeningStream(sessionTransport);

                    emitter.observe(new Emitter.Observer<TextEvent>() {
                        @Override
                        public void onEmittedData(TextEvent data) {
                            // No action needed
                        }

                        @Override
                        public void onCompleted() {
                            logger.info("[SSE] Completed SSE emitting for session: {}", sessionId);
                            try {
                                listeningStream.close();
                            } catch (Exception e) {
                                logger.warn("[SSE] Error closing listeningStream on complete: {}", e.getMessage());
                            }
                        }

                        @Override
                        public void onFailed(Exception cause) {
                            logger.warn("[SSE] SSE failed for session: {}, cause: {}", sessionId, cause.getMessage());
                            try {
                                listeningStream.close();
                            } catch (Exception e) {
                                logger.warn("[SSE] Error closing listeningStream on failure: {}", e.getMessage());
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            logger.error("Failed to handle GET request for session {}: {}", sessionId, e.getMessage());
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return null;
        }
    }

    /**
     * Handles POST requests for incoming JSON-RPC messages from clients.
     *
     * @param request The incoming server request containing the JSON-RPC message
     * @param response The HTTP response
     * @param requestBody the map of JSON-RPC message
     * @return Return the HTTP response body {@link Entity} or a {@link Choir}{@code <}{@link TextEvent}{@code >} object
     */
    @PostMapping(path = MESSAGE_ENDPOINT)
    public Object handlePost(HttpClassicServerRequest request, HttpClassicServerResponse response,
            @RequestBody Map<String, Object> requestBody) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            return Entity.createText(response, "Server is shutting down");
        }

        String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())
                || !acceptHeaders.contains(MimeType.APPLICATION_JSON.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST)
                            .message("Invalid Accept headers. Expected TEXT_EVENT_STREAM and APPLICATION_JSON")
                            .build());
        }
        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            McpSchema.JSONRPCMessage message = this.deserializeJsonRpcMessage(requestBody);

            // Handle initialization request
            if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest && jsonrpcRequest.method()
                    .equals(McpSchema.METHOD_INITIALIZE)) {
                logger.info("[POST] Handling initialize method, with receiving message: {}", requestBody.toString());
                McpSchema.InitializeRequest initializeRequest =
                        jsonMapper.convertValue(jsonrpcRequest.params(), new TypeRef<McpSchema.InitializeRequest>() {});
                McpStreamableServerSession.McpStreamableServerSessionInit init =
                        this.sessionFactory.startSession(initializeRequest);
                this.sessions.put(init.session().getId(), init.session());

                try {
                    McpSchema.InitializeResult initResult = init.initResult().block();
                    response.statusCode(HttpResponseStatus.OK.statusCode());
                    response.headers().set("Content-Type", MimeType.APPLICATION_JSON.value());
                    response.headers().set(HttpHeaders.MCP_SESSION_ID, init.session().getId());
                    logger.info("[POST] Sending initialize message via HTTP response to session {}",
                            init.session().getId());
                    return Entity.createObject(response,
                            new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION,
                                    jsonrpcRequest.id(),
                                    initResult,
                                    null));
                } catch (Exception e) {
                    logger.error("Failed to initialize session: {}", e.getMessage());
                    response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
                    return Entity.createObject(response,
                            McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
                }
            }

            // Handle other messages that require a session
            if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
                response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
                return Entity.createObject(response,
                        McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST).message("Session ID missing").build());
            }

            String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
            McpStreamableServerSession session = this.sessions.get(sessionId);
            logger.info("[POST] Receiving message from session {}: {}", sessionId, requestBody.toString());

            if (session == null) {
                response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
                return Entity.createObject(response,
                        McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
                                .message("Session not found: " + sessionId)
                                .build());
            }

            if (message instanceof McpSchema.JSONRPCResponse jsonrpcResponse) {
                session.accept(jsonrpcResponse)
                        .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                        .block();
                response.statusCode(HttpResponseStatus.ACCEPTED.statusCode());
                return null;
            } else if (message instanceof McpSchema.JSONRPCNotification jsonrpcNotification) {
                session.accept(jsonrpcNotification)
                        .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                        .block();
                response.statusCode(HttpResponseStatus.ACCEPTED.statusCode());
                return null;
            } else if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest) {
                // For streaming responses, we need to return SSE
                return Choir.<TextEvent>create(emitter -> {
                    // TODO emitter.onTimeout() logger.info
                    emitter.observe(new Emitter.Observer<TextEvent>() {
                        @Override
                        public void onEmittedData(TextEvent data) {
                            // No action needed
                        }

                        @Override
                        public void onCompleted() {
                            logger.info("[SSE] Completed SSE emitting for session: {}", sessionId);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            logger.warn("[SSE] SSE failed for session: {}, cause: {}", sessionId, e.getMessage());
                        }
                    });

                    FitStreamableMcpSessionTransport sessionTransport =
                            new FitStreamableMcpSessionTransport(sessionId, emitter, response);

                    try {
                        session.responseStream(jsonrpcRequest, sessionTransport)
                                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                                .block();
                    } catch (Exception e) {
                        logger.error("Failed to handle request stream: {}", e.getMessage());
                        emitter.fail(e);
                    }
                });
            } else {
                response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
                return Entity.createObject(response,
                        McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message("Unknown message type").build());
            }
        } catch (IllegalArgumentException | IOException e) {
            logger.error("Failed to deserialize message: {}", e.getMessage());
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.PARSE_ERROR).message("Invalid message format").build());
        } catch (Exception e) {
            logger.error("Error handling message: {}", e.getMessage());
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Handles DELETE requests for session deletion.
     *
     * @param request The incoming server request
     * @param response The HTTP response
     * @return Return HTTP response body {@link Entity}.
     */
    @DeleteMapping(path = MESSAGE_ENDPOINT)
    public Object handleDelete(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            return Entity.createText(response, "Server is shutting down");
        }

        if (this.disallowDelete) {
            response.statusCode(HttpResponseStatus.METHOD_NOT_ALLOWED.statusCode());
            return null;
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID required in mcp-session-id header");
        }

        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);

        logger.info("[DELETE] Receiving delete request from session: {}", sessionId);
        if (session == null) {
            response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
                            .message("Session not found: " + sessionId)
                            .build());
        }

        try {
            session.delete().contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
            this.sessions.remove(sessionId);
            response.statusCode(HttpResponseStatus.OK.statusCode());
            return null;
        } catch (Exception e) {
            logger.error("Failed to delete session {}: {}", sessionId, e.getMessage());
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * deserialize Map to JsonRpcMessage
     *
     * @param map the map of JSON-RPC message
     * @return The corresponding {@link McpSchema.JSONRPCMessage} class
     * @throws IOException when cannot deserialize JSONRPCMessage
     */
    public McpSchema.JSONRPCMessage deserializeJsonRpcMessage(Map<String, Object> map) throws IOException {
        if (map.containsKey("method") && map.containsKey("id")) {
            return jsonMapper.convertValue(map, McpSchema.JSONRPCRequest.class);
        } else if (map.containsKey("method") && !map.containsKey("id")) {
            return jsonMapper.convertValue(map, McpSchema.JSONRPCNotification.class);
        } else if (map.containsKey("result") || map.containsKey("error")) {
            return jsonMapper.convertValue(map, McpSchema.JSONRPCResponse.class);
        }

        throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + map.toString());
    }

    /**
     * Implementation of McpStreamableServerTransport for WebMVC SSE sessions. This class
     * handles the transport-level communication for a specific client session.
     *
     * <p>
     * This class is thread-safe and uses a ReentrantLock to synchronize access to the
     * underlying SSE builder to prevent race conditions when multiple threads attempt to
     * send messages concurrently.
     */
    private class FitStreamableMcpSessionTransport implements McpStreamableServerTransport {
        private final String sessionId;
        private final Emitter<TextEvent> emitter;
        private final HttpClassicServerResponse response;

        private final ReentrantLock lock = new ReentrantLock();

        private volatile boolean closed = false;

        /**
         * Creates a new session transport with the specified ID and SSE builder.
         *
         * @param sessionId The unique identifier for this session
         * @param emitter The emitter for sending events
         * @param response The HTTP response for checking connection status
         */
        FitStreamableMcpSessionTransport(String sessionId, Emitter<TextEvent> emitter,
                HttpClassicServerResponse response) {
            this.sessionId = sessionId;
            this.emitter = emitter;
            this.response = response;
            logger.info("[SSE] Building SSE for session: {} ", sessionId);
        }

        /**
         * Sends a JSON-RPC message to the client through the SSE connection.
         *
         * @param message The JSON-RPC message to send
         * @return A Mono that completes when the message has been sent
         */
        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return sendMessage(message, null);
        }

        /**
         * Sends a JSON-RPC message to the client through the SSE connection with a
         * specific message ID.
         *
         * @param message The JSON-RPC message to send
         * @param messageId The message ID for SSE event identification
         * @return A Mono that completes when the message has been sent
         */
        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return Mono.fromRunnable(() -> {
                if (this.closed) {
                    logger.info("Attempted to send message to closed session: {}", this.sessionId);
                    return;
                }

                this.lock.lock();
                try {
                    if (this.closed) {
                        logger.info("Session {} was closed during message send attempt", this.sessionId);
                        return;
                    }

                    // Check if connection is still active before sending
                    if (!this.response.isActive()) {
                        logger.warn("[SSE] Connection inactive detected while sending message for session: {}",
                                this.sessionId);
                        this.close();
                        return;
                    }

                    String jsonText = jsonMapper.writeValueAsString(message);
                    TextEvent textEvent =
                            TextEvent.custom().id(this.sessionId).event(Event.MESSAGE.code()).data(jsonText).build();
                    this.emitter.emit(textEvent);

                    logger.info("[SSE] Sending message to session {}: {}", this.sessionId, jsonText);
                } catch (Exception e) {
                    logger.error("Failed to send message to session {}: {}", this.sessionId, e.getMessage());
                    try {
                        this.emitter.fail(e);
                    } catch (Exception errorException) {
                        logger.error("Failed to send error to SSE builder for session {}: {}",
                                this.sessionId,
                                errorException.getMessage());
                    }
                } finally {
                    this.lock.unlock();
                }
            });
        }

        /**
         * Converts data from one type to another using the configured jsonMapper.
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
            return Mono.fromRunnable(FitStreamableMcpSessionTransport.this::close);
        }

        /**
         * Closes the transport immediately.
         */
        @Override
        public void close() {
            this.lock.lock();
            try {
                if (this.closed) {
                    logger.info("Session transport {} already closed", this.sessionId);
                    return;
                }

                this.closed = true;

                this.emitter.complete();
                logger.info("[SSE] Closed SSE builder successfully for session {}", sessionId);
            } catch (Exception e) {
                logger.warn("Failed to complete SSE builder for session {}: {}", sessionId, e.getMessage());
            } finally {
                this.lock.unlock();
            }
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating instances of {@link FitMcpStreamableServerTransportProvider}.
     */
    public static class Builder {
        private McpJsonMapper jsonMapper;
        private boolean disallowDelete = false;
        private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor =
                (HttpClassicServerRequest) -> McpTransportContext.EMPTY;
        private Duration keepAliveInterval;

        /**
         * Sets the jsonMapper to use for JSON serialization/deserialization of MCP messages.
         *
         * @param jsonMapper The jsonMapper instance. Must not be null.
         * @return this builder instance
         * @throws IllegalArgumentException if jsonMapper is null
         */
        public Builder jsonMapper(McpJsonMapper jsonMapper) {
            Validation.notNull(jsonMapper, "jsonMapper must not be null");
            this.jsonMapper = jsonMapper;
            return this;
        }

        /**
         * Sets whether to disallow DELETE requests on the endpoint.
         *
         * @param disallowDelete true to disallow DELETE requests, false otherwise
         * @return this builder instance
         */
        public Builder disallowDelete(boolean disallowDelete) {
            this.disallowDelete = disallowDelete;
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
            Validation.notNull(contextExtractor, "contextExtractor must not be null");
            this.contextExtractor = contextExtractor;
            return this;
        }

        /**
         * Sets the keep-alive interval for the transport. If set, a keep-alive scheduler
         * will be created to periodically check and send keep-alive messages to clients.
         *
         * @param keepAliveInterval The interval duration for keep-alive messages, or null
         * to disable keep-alive
         * @return this builder instance
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Builds a new instance of {@link FitMcpStreamableServerTransportProvider} with
         * the configured settings.
         *
         * @return A new FitMcpStreamableServerTransportProvider instance
         * @throws IllegalStateException if required parameters are not set
         */
        public FitMcpStreamableServerTransportProvider build() {
            Validation.notNull(this.jsonMapper, "jsonMapper must be set");

            return new FitMcpStreamableServerTransportProvider(this.jsonMapper,
                    this.disallowDelete,
                    this.contextExtractor,
                    this.keepAliveInterval);
        }
    }
}
