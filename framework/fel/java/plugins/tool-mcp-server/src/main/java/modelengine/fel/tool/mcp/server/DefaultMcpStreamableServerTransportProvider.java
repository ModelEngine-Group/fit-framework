package modelengine.fel.tool.mcp.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.spec.*;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import modelengine.fel.tool.mcp.entity.Event;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.annotation.*;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.protocol.MessageHeaderNames;
import modelengine.fit.http.protocol.MimeType;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RequestMapping("/mcp/streamable")
public class DefaultMcpStreamableServerTransportProvider implements McpStreamableServerTransportProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMcpStreamableServerTransportProvider.class);

    /**
     * Event type for JSON-RPC messages sent through the SSE connection.
     */
    public static final String MESSAGE_EVENT_TYPE = "message";

    /**
     * Event type for sending the message endpoint URI to clients.
     */
    public static final String ENDPOINT_EVENT_TYPE = "endpoint";

    /**
     * Default base URL for the message endpoint.
     */
    public static final String DEFAULT_BASE_URL = "";

    /**
     * Flag indicating whether DELETE requests are disallowed on the endpoint.
     */
    private final boolean disallowDelete;

    private final ObjectMapper objectMapper;

    private McpStreamableServerSession.Factory sessionFactory;

    /**
     * Map of active client sessions, keyed by mcp-session-id.
     */
    private final ConcurrentHashMap<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();

    private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor;

    /**
     * Flag indicating if the transport is shutting down.
     */
    private volatile boolean isClosing = false;

    private KeepAliveScheduler keepAliveScheduler;

    /**
     * Constructs a new DefaultMcpStreamableServerTransportProvider instance.
     * @param objectMapper The ObjectMapper to use for JSON serialization/deserialization
     * of messages.
     * @param disallowDelete Whether to disallow DELETE requests on the endpoint.
     * @throws IllegalArgumentException if any parameter is null
     */
    private DefaultMcpStreamableServerTransportProvider(ObjectMapper objectMapper,
                                                    boolean disallowDelete, McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor,
                                                    Duration keepAliveInterval) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        Assert.notNull(contextExtractor, "McpTransportContextExtractor must not be null");

        this.objectMapper = objectMapper;
        this.disallowDelete = disallowDelete;
        this.contextExtractor = contextExtractor;

        if (keepAliveInterval != null) {
            this.keepAliveScheduler = KeepAliveScheduler
                    .builder(() -> (isClosing) ? Flux.empty() : Flux.fromIterable(this.sessions.values()))
                    .initialDelay(keepAliveInterval)
                    .interval(keepAliveInterval)
                    .build();

            this.keepAliveScheduler.start();
        }
    }

    @Override
    public List<String> protocolVersions() {
        return List.of(ProtocolVersions.MCP_2024_11_05, ProtocolVersions.MCP_2025_03_26);
    }

    @Override
    public void setSessionFactory(McpStreamableServerSession.Factory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Broadcasts a notification to all connected clients through their SSE connections.
     * If any errors occur during sending to a particular client, they are logged but
     * don't prevent sending to other clients.
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

        logger.debug("Attempting to broadcast message to {} active sessions", this.sessions.size());

        return Mono.fromRunnable(() -> {
            this.sessions.values().parallelStream().forEach(session -> {
                try {
                    session.sendNotification(method, params).block();
                }
                catch (Exception e) {
                    logger.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                }
            });
        });
    }

    /**
     * Initiates a graceful shutdown of the transport.
     * @return A Mono that completes when all cleanup operations are finished
     */
    @Override
    public Mono<Void> closeGracefully() {
        return Mono.fromRunnable(() -> {
            this.isClosing = true;
            logger.debug("Initiating graceful shutdown with {} active sessions", this.sessions.size());

            this.sessions.values().parallelStream().forEach(session -> {
                try {
                    session.closeGracefully().block();
                }
                catch (Exception e) {
                    logger.error("Failed to close session {}: {}", session.getId(), e.getMessage());
                }
            });

            this.sessions.clear();
            logger.debug("Graceful shutdown completed");
        }).then().doOnSuccess(v -> {
            if (this.keepAliveScheduler != null) {
                this.keepAliveScheduler.shutdown();
            }
        });
    }


    /**
     * Setup the listening SSE connections and message replay.
     * @param request The incoming server request
     */
    @GetMapping
    private Choir<TextEvent> handleGet(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            response.entity(Entity.createText(response, "Server is shutting down"));
            return Choir.empty();
        }

        List<String> acceptHeaders = request.headers().all(MessageHeaderNames.ACCEPT);
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            response.entity(Entity.createText(response, "Invalid Accept header. Expected TEXT_EVENT_STREAM"));
            return Choir.empty();
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            response.entity(Entity.createText(response, "Session ID required in mcp-session-id header"));
            return Choir.empty();
        }

        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);

        if (session == null) {
            response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
            return Choir.empty();
        }

        logger.debug("Handling GET request for session: {}", sessionId);

        try {
            return Choir.create(emitter -> {
                // TODO onTimeout()
//                emitter.onTimeout(() -> {
//                    logger.debug("SSE connection timed out for session: {}", sessionId);
//                });

                DefaultStreamableMcpSessionTransport sessionTransport = new DefaultStreamableMcpSessionTransport(
                        sessionId, emitter);

                // Check if this is a replay request
                if (request.headers().contains(HttpHeaders.LAST_EVENT_ID)) {
                    String lastId = request.headers().first(HttpHeaders.LAST_EVENT_ID).orElse("");

                    try {
                        session.replay(lastId)
                                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                                .toIterable()
                                .forEach(message -> {
                                    try {
                                        sessionTransport.sendMessage(message)
                                                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                                                .block();
                                    }
                                    catch (Exception e) {
                                        logger.error("Failed to replay message: {}", e.getMessage());
                                        emitter.fail(e);
                                    }
                                });
                    }
                    catch (Exception e) {
                        logger.error("Failed to replay messages: {}", e.getMessage());
                        emitter.fail(e);
                    }
                }
                else {
                    // Establish new listening stream
                    McpStreamableServerSession.McpStreamableServerSessionStream listeningStream = session
                            .listeningStream(sessionTransport);

                    emitter.observe(new Emitter.Observer<TextEvent>() {
                        @Override
                        public void onEmittedData(TextEvent data) {
                            // No action needed on emitted data
                        }

                        @Override
                        public void onCompleted() {
                            logger.debug("SSE connection completed for session: {}", sessionId);
                            listeningStream.close();
                        }

                        @Override
                        public void onFailed(Exception cause) {
                            // Close the listening stream on failure
                        }
                    });
                }
            });
        }
        catch (Exception e) {
            logger.error("Failed to handle GET request for session {}: {}", sessionId, e.getMessage());
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Choir.empty();
        }
    }

    /**
     * Handles POST requests for incoming JSON-RPC messages from clients.
     * @param request The incoming server request containing the JSON-RPC message
     */
    @PostMapping
    private Choir<TextEvent> handlePost(@RequestBody String body,HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            response.entity(Entity.createText(response, "Server is shutting down"));
            return Choir.empty();
        }

        List<String> acceptHeaders = request.headers().all(MessageHeaderNames.ACCEPT);
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())
                || !acceptHeaders.contains(MimeType.APPLICATION_JSON.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            response.entity(Entity.createObject(response, new McpError("Invalid Accept headers. Expected TEXT_EVENT_STREAM and APPLICATION_JSON")));
            return Choir.empty();
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        try {

            McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(objectMapper, body);

            // Handle initialization request
            if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest
                    && jsonrpcRequest.method().equals(McpSchema.METHOD_INITIALIZE)) {
                McpSchema.InitializeRequest initializeRequest = objectMapper.convertValue(jsonrpcRequest.params(),
                        new TypeReference<McpSchema.InitializeRequest>() {
                        });
                McpStreamableServerSession.McpStreamableServerSessionInit init = this.sessionFactory
                        .startSession(initializeRequest);
                this.sessions.put(init.session().getId(), init.session());

                try {
                    McpSchema.InitializeResult initResult = init.initResult().block();
                    response.statusCode(HttpResponseStatus.OK.statusCode());
                    response.headers().set("Content-Type", MimeType.APPLICATION_JSON.value());
                    response.headers().set(HttpHeaders.MCP_SESSION_ID, init.session().getId());
                    response.entity(Entity.createObject(response,
                            new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, jsonrpcRequest.id(), initResult, null)));
                    return Choir.empty();
                }
                catch (Exception e) {
                    logger.error("Failed to initialize session: {}", e.getMessage());
                    response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
                    response.entity(Entity.createObject(response, new McpError(e.getMessage())));
                    return Choir.empty();
                }
            }

            // Handle other messages that require a session
            if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
                response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
                response.entity(Entity.createObject(response, new McpError("Session ID missing")));
                return Choir.empty();
            }

            String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
            McpStreamableServerSession session = this.sessions.get(sessionId);

            if (session == null) {
                response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
                response.entity(Entity.createObject(response, new McpError("Session not found: " + sessionId)));
                return Choir.empty();
            }

            if (message instanceof McpSchema.JSONRPCResponse jsonrpcResponse) {
                session.accept(jsonrpcResponse)
                        .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                        .block();
                response.statusCode(HttpResponseStatus.ACCEPTED.statusCode());
                return Choir.empty();

            }
            else if (message instanceof McpSchema.JSONRPCNotification jsonrpcNotification) {
                session.accept(jsonrpcNotification)
                        .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                        .block();
                response.statusCode(HttpResponseStatus.ACCEPTED.statusCode());
                return Choir.empty();
            }
            else if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest) {
                // For streaming responses, we need to return SSE
                return Choir.create(emitter -> {
                    // TODO onComplete() and onTimeout()
//                    emitter.onComplete(() -> {
//                        logger.debug("Request response stream completed for session: {}", sessionId);
//                    });
//                    emitter.onTimeout(() -> {
//                        logger.debug("Request response stream timed out for session: {}", sessionId);
//                    });

                    DefaultStreamableMcpSessionTransport sessionTransport = new DefaultStreamableMcpSessionTransport(sessionId, emitter);

                    try {
                        session.responseStream(jsonrpcRequest, sessionTransport)
                                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                                .block();
                        logger.debug("Request response stream completed for session: {}", sessionId);
                    } catch (Exception e) {
                        logger.error("Failed to handle request stream: {}", e.getMessage());
                        emitter.fail(e);
                    }});
            }
            else {
                response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
                response.entity(Entity.createObject(response, new McpError("Unknown message type")));
                return Choir.empty();
            }
        }
        catch (IllegalArgumentException | IOException e) {
            logger.error("Failed to deserialize message: {}", e.getMessage());
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            response.entity(Entity.createObject(response, new McpError("Invalid message format")));
            return Choir.empty();
        }
        catch (Exception e) {
            logger.error("Error handling message: {}", e.getMessage());
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            response.entity(Entity.createObject(response, new McpError(e.getMessage())));
            return Choir.empty();
        }
    }

    /**
     * Handles DELETE requests for session deletion.
     * @param request The incoming server request
     */
    @DeleteMapping
    private void handleDelete(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            response.entity(Entity.createText(response, "Server is shutting down"));
            return;
        }

        if (this.disallowDelete) {
            response.statusCode(HttpResponseStatus.METHOD_NOT_ALLOWED.statusCode());
            return;
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            response.entity(Entity.createText(response, "Session ID required in mcp-session-id header"));
            return;
        }

        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);

        if (session == null) {
            response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
            return;
        }

        try {
            session.delete().contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
            this.sessions.remove(sessionId);
            response.statusCode(HttpResponseStatus.OK.statusCode());
        }
        catch (Exception e) {
            logger.error("Failed to delete session {}: {}", sessionId, e.getMessage());
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            response.entity(Entity.createObject(response, new McpError(e.getMessage())));
        }
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
    private class DefaultStreamableMcpSessionTransport implements McpStreamableServerTransport {

        private final String sessionId;

        private final Emitter<TextEvent> emitter;

        private final ReentrantLock lock = new ReentrantLock();

        private volatile boolean closed = false;

        /**
         * Creates a new session transport with the specified ID and SSE builder.
         * @param sessionId The unique identifier for this session
         */
        DefaultStreamableMcpSessionTransport(String sessionId, Emitter<TextEvent> emitter) {
            this.sessionId = sessionId;
            this.emitter = emitter;
            logger.debug("Streamable session transport {} initialized with SSE builder", sessionId);
        }

        /**
         * Sends a JSON-RPC message to the client through the SSE connection.
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
         * @param message The JSON-RPC message to send
         * @param messageId The message ID for SSE event identification
         * @return A Mono that completes when the message has been sent
         */
        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return Mono.fromRunnable(() -> {
                if (this.closed) {
                    logger.debug("Attempted to send message to closed session: {}", this.sessionId);
                    return;
                }

                this.lock.lock();
                try {
                    if (this.closed) {
                        logger.debug("Session {} was closed during message send attempt", this.sessionId);
                        return;
                    }

                    String jsonText = objectMapper.writeValueAsString(message);
                    TextEvent textEvent = TextEvent.custom().id(this.sessionId).event(Event.MESSAGE.code()).data(jsonText).build();
                    this.emitter.emit(textEvent);

                    logger.debug("Message sent to session {} with ID {}", this.sessionId, messageId);
                }
                catch (Exception e) {
                    logger.error("Failed to send message to session {}: {}", this.sessionId, e.getMessage());
                    try {
                        this.emitter.fail(e);
                    }
                    catch (Exception errorException) {
                        logger.error("Failed to send error to SSE builder for session {}: {}", this.sessionId,
                                errorException.getMessage());
                    }
                }
                finally {
                    this.lock.unlock();
                }
            });
        }

        /**
         * Converts data from one type to another using the configured ObjectMapper.
         * @param data The source data object to convert
         * @param typeRef The target type reference
         * @return The converted object of type T
         * @param <T> The target type
         */
        @Override
        public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
            return objectMapper.convertValue(data, typeRef);
        }

        /**
         * Initiates a graceful shutdown of the transport.
         * @return A Mono that completes when the shutdown is complete
         */
        @Override
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(() -> {
                DefaultStreamableMcpSessionTransport.this.close();
            });
        }

        /**
         * Closes the transport immediately.
         */
        @Override
        public void close() {
            this.lock.lock();
            try {
                if (this.closed) {
                    logger.debug("Session transport {} already closed", this.sessionId);
                    return;
                }

                this.closed = true;

                this.emitter.complete();
                logger.debug("Successfully completed SSE builder for session {}", sessionId);
            }
            catch (Exception e) {
                logger.warn("Failed to complete SSE builder for session {}: {}", sessionId, e.getMessage());
            }
            finally {
                this.lock.unlock();
            }
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating instances of {@link DefaultMcpStreamableServerTransportProvider}.
     */
    public static class Builder {

        private ObjectMapper objectMapper;

        private boolean disallowDelete = false;

        private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor = (
                HttpClassicServerRequest) -> McpTransportContext.EMPTY;

        private Duration keepAliveInterval;

        /**
         * Sets the ObjectMapper to use for JSON serialization/deserialization of MCP
         * messages.
         * @param objectMapper The ObjectMapper instance. Must not be null.
         * @return this builder instance
         * @throws IllegalArgumentException if objectMapper is null
         */
        public Builder objectMapper(ObjectMapper objectMapper) {
            Assert.notNull(objectMapper, "ObjectMapper must not be null");
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Sets whether to disallow DELETE requests on the endpoint.
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
         * Sets the keep-alive interval for the transport. If set, a keep-alive scheduler
         * will be created to periodically check and send keep-alive messages to clients.
         * @param keepAliveInterval The interval duration for keep-alive messages, or null
         * to disable keep-alive
         * @return this builder instance
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Builds a new instance of {@link DefaultMcpStreamableServerTransportProvider} with
         * the configured settings.
         * @return A new DefaultMcpStreamableServerTransportProvider instance
         * @throws IllegalStateException if required parameters are not set
         */
        public DefaultMcpStreamableServerTransportProvider build() {
            Assert.notNull(this.objectMapper, "ObjectMapper must be set");

            return new DefaultMcpStreamableServerTransportProvider(this.objectMapper, this.disallowDelete,
                    this.contextExtractor, this.keepAliveInterval);
        }

    }

}
