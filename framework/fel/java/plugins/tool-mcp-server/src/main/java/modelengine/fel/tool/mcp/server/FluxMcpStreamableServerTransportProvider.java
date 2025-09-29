package modelengine.fel.tool.mcp.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.spec.*;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import modelengine.fit.http.annotation.DeleteMapping;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/mcp/streamable")
public class FluxMcpStreamableServerTransportProvider implements McpStreamableServerTransportProvider {

    private static final Logger logger = LoggerFactory.getLogger(FluxMcpStreamableServerTransportProvider.class);

    public static final String MESSAGE_EVENT_TYPE = "message";

    private final ObjectMapper objectMapper;

    private final boolean disallowDelete;

    private McpStreamableServerSession.Factory sessionFactory;

    private final ConcurrentHashMap<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();

    private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor;

    private volatile boolean isClosing = false;

    private KeepAliveScheduler keepAliveScheduler;

    private FluxMcpStreamableServerTransportProvider(ObjectMapper objectMapper,
                                                     McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor, boolean disallowDelete,
                                                     Duration keepAliveInterval) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        Assert.notNull(contextExtractor, "Context extractor must not be null");

        this.objectMapper = objectMapper;
        this.contextExtractor = contextExtractor;
        this.disallowDelete = disallowDelete;

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

    @Override
    public Mono<Void> notifyClients(String method, Object params) {
        if (sessions.isEmpty()) {
            logger.debug("No active sessions to broadcast message to");
            return Mono.empty();
        }

        logger.debug("Attempting to broadcast message to {} active sessions", sessions.size());

        return Flux.fromIterable(sessions.values())
                .flatMap(session -> session.sendNotification(method, params)
                        .doOnError(
                                e -> logger.error("Failed to send message to session {}: {}", session.getId(), e.getMessage()))
                        .onErrorComplete())
                .then();
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.defer(() -> {
            this.isClosing = true;
            return Flux.fromIterable(sessions.values())
                    .doFirst(() -> logger.debug("Initiating graceful shutdown with {} active sessions", sessions.size()))
                    .flatMap(McpStreamableServerSession::closeGracefully)
                    .then();
        }).then().doOnSuccess(v -> {
            sessions.clear();
            if (this.keepAliveScheduler != null) {
                this.keepAliveScheduler.shutdown();
            }
        });
    }

    /**
     * Opens the listening SSE streams for clients.
     * @param request The incoming server request
     */
    @GetMapping
    private void handleGet(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            response.reasonPhrase("Server is shutting down");
            return;
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        return Mono.defer(() -> {
            String acceptHeader = request.headers().first("Accept");
            List<MediaType> acceptHeaders = request.headers().asHttpHeaders().getAccept();
            if (!acceptHeaders.contains(MediaType.TEXT_EVENT_STREAM)) {
                return HttpClassicServerResponse.badRequest().build();
            }

            if (!request.headers().asHttpHeaders().containsKey(HttpHeaders.MCP_SESSION_ID)) {
                return HttpClassicServerResponse.badRequest().build(); // TODO: say we need a session
                // id
            }

            String sessionId = request.headers().asHttpHeaders().getFirst(HttpHeaders.MCP_SESSION_ID);

            McpStreamableServerSession session = this.sessions.get(sessionId);

            if (session == null) {
                return HttpClassicServerResponse.notFound().build();
            }

            if (request.headers().asHttpHeaders().containsKey(HttpHeaders.LAST_EVENT_ID)) {
                String lastId = request.headers().asHttpHeaders().getFirst(HttpHeaders.LAST_EVENT_ID);
                return HttpClassicServerResponse.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(session.replay(lastId)
                                        .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)),
                                ServerSentEvent.class);
            }

            return HttpClassicServerResponse.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(Flux.<ServerSentEvent<?>>create(sink -> {
                        FluxStreamableMcpSessionTransport sessionTransport = new FluxStreamableMcpSessionTransport(
                                sink);
                        McpStreamableServerSession.McpStreamableServerSessionStream listeningStream = session
                                .listeningStream(sessionTransport);
                        sink.onDispose(listeningStream::close);
                        // TODO Clarify why the outer context is not present in the
                        // Flux.create sink?
                    }).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)), ServerSentEvent.class);

        }).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext));
    }

    /**
     * Handles incoming JSON-RPC messages from clients.
     * @param request The incoming server request containing the JSON-RPC message
     * @return A Mono with the response appropriate to a particular Streamable HTTP flow.
     */
    @PostMapping
    private void handlePost(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            response.reasonPhrase("Server is shutting down");
            return;
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        List<MediaType> acceptHeaders = request.headers().asHttpHeaders().getAccept();
        if (!(acceptHeaders.contains(MediaType.APPLICATION_JSON)
                && acceptHeaders.contains(MediaType.TEXT_EVENT_STREAM))) {
            return HttpClassicServerResponse.badRequest().build();
        }

        return request.bodyToMono(String.class).<HttpClassicServerResponse>flatMap(body -> {
                    try {
                        McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(objectMapper, body);
                        if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest
                                && jsonrpcRequest.method().equals(McpSchema.METHOD_INITIALIZE)) {
                            McpSchema.InitializeRequest initializeRequest = objectMapper.convertValue(jsonrpcRequest.params(),
                                    new TypeReference<McpSchema.InitializeRequest>() {
                                    });
                            McpStreamableServerSession.McpStreamableServerSessionInit init = this.sessionFactory
                                    .startSession(initializeRequest);
                            sessions.put(init.session().getId(), init.session());
                            return init.initResult().map(initializeResult -> {
                                        McpSchema.JSONRPCResponse jsonrpcResponse = new McpSchema.JSONRPCResponse(
                                                McpSchema.JSONRPC_VERSION, jsonrpcRequest.id(), initializeResult, null);
                                        try {
                                            return this.objectMapper.writeValueAsString(jsonrpcResponse);
                                        }
                                        catch (IOException e) {
                                            logger.warn("Failed to serialize initResponse", e);
                                            throw Exceptions.propagate(e);
                                        }
                                    })
                                    .flatMap(initResult -> HttpClassicServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header(HttpHeaders.MCP_SESSION_ID, init.session().getId())
                                            .bodyValue(initResult));
                        }

                        if (!request.headers().asHttpHeaders().containsKey(HttpHeaders.MCP_SESSION_ID)) {
                            return HttpClassicServerResponse.badRequest().bodyValue(new McpError("Session ID missing"));
                        }

                        String sessionId = request.headers().asHttpHeaders().getFirst(HttpHeaders.MCP_SESSION_ID);
                        McpStreamableServerSession session = sessions.get(sessionId);

                        if (session == null) {
                            return HttpClassicServerResponse.status(HttpStatus.NOT_FOUND)
                                    .bodyValue(new McpError("Session not found: " + sessionId));
                        }

                        if (message instanceof McpSchema.JSONRPCResponse jsonrpcResponse) {
                            return session.accept(jsonrpcResponse).then(HttpClassicServerResponse.accepted().build());
                        }
                        else if (message instanceof McpSchema.JSONRPCNotification jsonrpcNotification) {
                            return session.accept(jsonrpcNotification).then(HttpClassicServerResponse.accepted().build());
                        }
                        else if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest) {
                            return HttpClassicServerResponse.ok()
                                    .contentType(MediaType.TEXT_EVENT_STREAM)
                                    .body(Flux.<ServerSentEvent<?>>create(sink -> {
                                                FluxStreamableMcpSessionTransport st = new FluxStreamableMcpSessionTransport(sink);
                                                Mono<Void> stream = session.responseStream(jsonrpcRequest, st);
                                                Disposable streamSubscription = stream.onErrorComplete(err -> {
                                                    sink.error(err);
                                                    return true;
                                                }).contextWrite(sink.contextView()).subscribe();
                                                sink.onCancel(streamSubscription);
                                                // TODO Clarify why the outer context is not present in the
                                                // Flux.create sink?
                                            }).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)),
                                            ServerSentEvent.class);
                        }
                        else {
                            return HttpClassicServerResponse.badRequest().bodyValue(new McpError("Unknown message type"));
                        }
                    }
                    catch (IllegalArgumentException | IOException e) {
                        logger.error("Failed to deserialize message: {}", e.getMessage());
                        return HttpClassicServerResponse.badRequest().bodyValue(new McpError("Invalid message format"));
                    }
                })
                .switchIfEmpty(HttpClassicServerResponse.badRequest().build())
                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext));
    }

    @DeleteMapping
    private void handleDelete(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            response.reasonPhrase("Server is shutting down");
            return;
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);

        return Mono.defer(() -> {
            if (!request.headers().asHttpHeaders().containsKey(HttpHeaders.MCP_SESSION_ID)) {
                return HttpClassicServerResponse.badRequest().build(); // TODO: say we need a session
                // id
            }

            if (this.disallowDelete) {
                return HttpClassicServerResponse.status(HttpStatus.METHOD_NOT_ALLOWED).build();
            }

            String sessionId = request.headers().asHttpHeaders().getFirst(HttpHeaders.MCP_SESSION_ID);

            McpStreamableServerSession session = this.sessions.get(sessionId);

            if (session == null) {
                return HttpClassicServerResponse.notFound().build();
            }

            return session.delete().then(HttpClassicServerResponse.ok().build());
        }).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext));
    }

    private class FluxStreamableMcpSessionTransport implements McpStreamableServerTransport {

        private final FluxSink<ServerSentEvent<?>> sink;

        public FluxStreamableMcpSessionTransport(FluxSink<ServerSentEvent<?>> sink) {
            this.sink = sink;
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return this.sendMessage(message, null);
        }



        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return Mono.fromSupplier(() -> {
                try {
                    return objectMapper.writeValueAsString(message);
                }
                catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }).doOnNext(jsonText -> {
                ServerSentEvent<Object> event = ServerSentEvent.builder()
                        .id(messageId)
                        .event(MESSAGE_EVENT_TYPE)
                        .data(jsonText)
                        .build();
                sink.next(event);
            }).doOnError(e -> {
                // TODO log with sessionid
                Throwable exception = Exceptions.unwrap(e);
                sink.error(exception);
            }).then();
        }

        @Override
        public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
            return objectMapper.convertValue(data, typeRef);
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(sink::complete);
        }

        @Override
        public void close() {
            sink.complete();
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating instances of {@link FluxMcpStreamableServerTransportProvider}.
     * <p>
     * This builder provides a fluent API for configuring and creating instances of
     * DefaultMcpStreamableServerTransportProvider with custom settings.
     */
    public static class Builder {

        private ObjectMapper objectMapper;

        private McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor = (
                serverRequest) -> McpTransportContext.EMPTY;

        private boolean disallowDelete;

        private Duration keepAliveInterval;

        private Builder() {
            // used by a static method
        }

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
         * Sets whether the session removal capability is disabled.
         * @param disallowDelete if {@code true}, the DELETE endpoint will not be
         * supported and sessions won't be deleted.
         * @return this builder instance
         */
        public Builder disallowDelete(boolean disallowDelete) {
            this.disallowDelete = disallowDelete;
            return this;
        }

        /**
         * Sets the keep-alive interval for the server transport.
         * @param keepAliveInterval The interval for sending keep-alive messages. If null,
         * no keep-alive will be scheduled.
         * @return this builder instance
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Builds a new instance of {@link FluxMcpStreamableServerTransportProvider} with
         * the configured settings.
         * @return A new DefaultMcpStreamableServerTransportProvider instance
         * @throws IllegalStateException if required parameters are not set
         */
        public FluxMcpStreamableServerTransportProvider build() {
            Assert.notNull(objectMapper, "ObjectMapper must be set");

            return new FluxMcpStreamableServerTransportProvider(objectMapper, contextExtractor,
                    disallowDelete, keepAliveInterval);
        }

    }

}
