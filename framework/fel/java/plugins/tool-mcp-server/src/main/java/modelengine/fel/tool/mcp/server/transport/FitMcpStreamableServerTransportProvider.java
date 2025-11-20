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
import modelengine.fel.tool.mcp.server.FitMcpServerTransportProvider;
import modelengine.fit.http.annotation.DeleteMapping;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PostMapping;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * The default implementation of {@link McpStreamableServerTransportProvider}.
 * The FIT transport provider for MCP Streamable Server, according to
 * {@code HttpServletStreamableServerTransportProvider} in MCP
 * SDK.
 *
 * @author 黄可欣
 * @since 2025-09-30
 */
public class FitMcpStreamableServerTransportProvider extends FitMcpServerTransportProvider<McpStreamableServerSession>
        implements McpStreamableServerTransportProvider {
    private static final Logger logger = Logger.get(FitMcpStreamableServerTransportProvider.class);
    private static final String MESSAGE_ENDPOINT = "/mcp/streamable";
    private McpStreamableServerSession.Factory sessionFactory;
    private final boolean disallowDelete;

    /**
     * Constructs a new FitMcpStreamableServerTransportProvider instance,
     * for {@link FitMcpStreamableServerTransportProvider.Builder}.
     *
     * @param jsonMapper The jsonMapper to use for JSON serialization/deserialization
     * of messages.
     * @param disallowDelete Whether to disallow DELETE requests on the endpoint.
     * @param contextExtractor The context extractor to fill in a {@link McpTransportContext}.
     * @param keepAliveInterval The interval for sending keep-alive messages to clients.
     * @throws IllegalArgumentException if any parameter is null.
     */
    private FitMcpStreamableServerTransportProvider(McpJsonMapper jsonMapper, boolean disallowDelete,
            McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor, Duration keepAliveInterval) {
        super(jsonMapper, contextExtractor, keepAliveInterval);
        this.disallowDelete = disallowDelete;
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
    protected String getSessionId(McpStreamableServerSession session) {
        return session.getId();
    }

    @Override
    protected Mono<Void> closeSession(McpStreamableServerSession session) {
        return session.closeGracefully();
    }

    @Override
    protected Mono<Void> sendNotificationToSession(McpStreamableServerSession session, String method, Object params) {
        return session.sendNotification(method, params);
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
     * Set up the listening SSE connections and message replay.
     *
     * @param request The incoming server request.
     * @param response The HTTP response.
     * @return Return the HTTP response body {@link Entity} or a {@link Choir}{@code <}{@link TextEvent}{@code >}
     * object.
     */
    @GetMapping(path = MESSAGE_ENDPOINT)
    public Object handleGet(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            return this.createShuttingDownResponse(response);
        }

        Object headerError = this.validateGetAcceptHeaders(request, response);
        if (headerError != null) {
            return headerError;
        }

        // Get session ID and session
        Object sessionError = this.validateRequestSessionId(request, response);
        if (sessionError != null) {
            return sessionError;
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);
        logger.info("[GET] Receiving GET request. [sessionId={}]", sessionId);

        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            return Choir.<TextEvent>create(emitter -> {
                FitStreamableMcpSessionTransport sessionTransport =
                        new FitStreamableMcpSessionTransport(sessionId, emitter, response);

                // Handle building SSE, and check if this is a replay request
                if (request.headers().contains(HttpHeaders.LAST_EVENT_ID)) {
                    FitMcpStreamableServerTransportProvider.this.handleReplaySseRequest(request,
                            transportContext,
                            sessionId,
                            session,
                            sessionTransport,
                            emitter);
                } else {
                    FitMcpStreamableServerTransportProvider.this.handleEstablishSseRequest(sessionId,
                            session,
                            sessionTransport,
                            emitter);
                }
            });
        } catch (Exception e) {
            logger.error("[GET] Failed to handle GET request. [sessionId={}, error={}]", sessionId, e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return null;
        }
    }

    /**
     * Handles POST requests for incoming JSON-RPC messages from clients.
     *
     * @param request The incoming server request containing the JSON-RPC message.
     * @param response The HTTP response.
     * @return Return the HTTP response body {@link Entity} or a {@link Choir}{@code <}{@link TextEvent}{@code >}
     * object.
     */
    @PostMapping(path = MESSAGE_ENDPOINT)
    public Object handlePost(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            return this.createShuttingDownResponse(response);
        }
        Object headerError = this.validatePostAcceptHeaders(request, response);
        if (headerError != null) {
            return headerError;
        }

        String requestBody = new String(request.entityBytes(), StandardCharsets.UTF_8);
        McpSchema.JSONRPCMessage message = this.deserializeMessage(requestBody, response);
        if (message == null) {
            logger.error("[POST] Invalid message format.  [requestBody={}]", requestBody);
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.PARSE_ERROR).message("Invalid message format.").build());
        }
        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            // Handle JSONRPCMessage
            if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest && jsonrpcRequest.method()
                    .equals(McpSchema.METHOD_INITIALIZE)) {
                logger.info("[POST] Handling initialize method. [requestBody={}]", requestBody);
                return this.handleInitializeRequest(request, response, jsonrpcRequest);
            } else {
                return this.handleJsonRpcMessage(message, request, requestBody, transportContext, response);
            }
        } catch (Exception e) {
            logger.error("[POST] Error handling message. [error={}]", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Handles DELETE requests for session deletion.
     *
     * @param request The incoming server request.
     * @param response The HTTP response.
     * @return Return HTTP response body {@link Entity}.
     */
    @DeleteMapping(path = MESSAGE_ENDPOINT)
    public Object handleDelete(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            return this.createShuttingDownResponse(response);
        }
        if (this.disallowDelete) {
            response.statusCode(HttpResponseStatus.METHOD_NOT_ALLOWED.statusCode());
            return null;
        }

        // Get session ID and session
        Object sessionError = this.validateRequestSessionId(request, response);
        if (sessionError != null) {
            return sessionError;
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);
        logger.info("[DELETE] Receiving delete request. [sessionId={}]", sessionId);

        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            session.delete().contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
            this.sessions.remove(sessionId);
            response.statusCode(HttpResponseStatus.OK.statusCode());
            return null;
        } catch (Exception e) {
            logger.error("[DELETE] Failed to delete session. [sessionId={}, error={}]", sessionId, e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Validates the Accept header for SSE (Server-Sent Events) connections in GET requests.
     * Checks if the request contains the required {@code text/event-stream} content type.
     *
     * @param request The incoming {@link HttpClassicServerRequest}.
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails.
     * @return An error {@link Entity} if validation fails, {@code null} if validation succeeds.
     */
    private Object validateGetAcceptHeaders(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Invalid Accept header. Expected TEXT_EVENT_STREAM.");
        }
        return null;
    }

    /**
     * Validates the Accept headers for POST requests.
     * Checks if the request contains both {@code text/event-stream} and {@code application/json} content types,
     * as POST requests may return either SSE streams or JSON responses.
     *
     * @param request The incoming {@link HttpClassicServerRequest}.
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails.
     * @return An error {@link Entity} with {@link McpError} if validation fails, {@code null} if validation succeeds.
     */
    private Object validatePostAcceptHeaders(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())
                || !acceptHeaders.contains(MimeType.APPLICATION_JSON.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST)
                            .message("Invalid Accept headers. Expected TEXT_EVENT_STREAM and APPLICATION_JSON.")
                            .build());
        }
        return null;
    }

    /**
     * Validates the MCP session ID in the request headers and verifies the session exists.
     * This method checks both the presence of the {@code mcp-session-id} header and
     * the existence of the corresponding session in the active sessions map.
     *
     * @param request The incoming {@link HttpClassicServerRequest} containing the session ID header.
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails.
     * @return An error {@link Entity} if validation fails (either missing session ID or session not found),
     * {@code null} if validation succeeds.
     */
    private Object validateRequestSessionId(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID required in mcp-session-id header.");
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        return this.validateSessionExists(sessionId, response);
    }

    /**
     * Handles message replay requests for SSE connections.
     * Replays previously sent messages starting from the last received event ID,
     * allowing clients to recover missed messages after reconnection.
     *
     * @param request The incoming {@link HttpClassicServerRequest} containing the {@code Last-Event-ID} header.
     * @param transportContext The {@link McpTransportContext} for request context propagation.
     * @param sessionId The MCP session identifier.
     * @param session The {@link McpStreamableServerSession} to replay messages from.
     * @param sessionTransport The {@link FitStreamableMcpSessionTransport} for sending replayed messages.
     * @param emitter The SSE {@link Emitter} to send {@link TextEvent} to the client.
     */
    private void handleReplaySseRequest(HttpClassicServerRequest request, McpTransportContext transportContext,
            String sessionId, McpStreamableServerSession session, FitStreamableMcpSessionTransport sessionTransport,
            Emitter<TextEvent> emitter) {
        String lastId = request.headers().first(HttpHeaders.LAST_EVENT_ID).orElse("0");
        logger.info("[GET] Handling replay request. [sessionId={}]", sessionId);

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
                            logger.error("[GET] Failed to replay message. [error={}]", e.getMessage(), e);
                            emitter.fail(e);
                        }
                    });
        } catch (Exception e) {
            logger.error("[GET] Failed to replay messages. [error={}]", e.getMessage(), e);
            emitter.fail(e);
        }
    }

    /**
     * Establishes a new SSE listening stream for real-time message delivery.
     * Creates a persistent connection that allows the server to push messages to the client
     * as they become available. The stream remains open until explicitly closed or an error occurs.
     *
     * @param sessionId The MCP session identifier.
     * @param session The {@link McpStreamableServerSession} to establish the listening stream for.
     * @param sessionTransport The {@link FitStreamableMcpSessionTransport} for bidirectional communication.
     * @param emitter The SSE {@link Emitter} to send {@link TextEvent} to the client.
     */
    private void handleEstablishSseRequest(String sessionId, McpStreamableServerSession session,
            FitStreamableMcpSessionTransport sessionTransport, Emitter<TextEvent> emitter) {
        logger.info("[GET] Handling GET request to establish new SSE. [sessionId={}]", sessionId);
        McpStreamableServerSession.McpStreamableServerSessionStream listeningStream =
                session.listeningStream(sessionTransport);

        emitter.observe(new Emitter.Observer<TextEvent>() {
            @Override
            public void onEmittedData(TextEvent data) {
                // No action needed
            }

            @Override
            public void onCompleted() {
                FitMcpStreamableServerTransportProvider.logger.info("[SSE] Completed SSE emitting. [sessionId={}]",
                        sessionId);
                try {
                    listeningStream.close();
                } catch (Exception e) {
                    FitMcpStreamableServerTransportProvider.logger.warn(
                            "[SSE] Error closing listeningStream on complete. [sessionId={}, error={}]",
                            sessionId,
                            e.getMessage());
                }
            }

            @Override
            public void onFailed(Exception cause) {
                FitMcpStreamableServerTransportProvider.logger.warn("[SSE] SSE failed. [sessionId={}, cause={}]",
                        sessionId,
                        cause.getMessage());
                try {
                    listeningStream.close();
                } catch (Exception e) {
                    FitMcpStreamableServerTransportProvider.logger.warn(
                            "[SSE] Error closing listeningStream on failure. [sessionId={}, error={}]",
                            sessionId,
                            e.getMessage());
                }
            }
        });
    }

    /**
     * Handles MCP session initialization requests.
     * Creates a new {@link McpStreamableServerSession} and returns the initialization result
     * with the assigned session ID in the response headers.
     *
     * @param request The incoming {@link HttpClassicServerRequest}.
     * @param response The {@link HttpClassicServerResponse} to set session ID and initialization result.
     * @param jsonrpcRequest The {@link McpSchema.JSONRPCRequest} containing {@link McpSchema.InitializeRequest}
     * parameters.
     * @return An {@link Entity} containing the {@link McpSchema.JSONRPCResponse} with
     * {@link McpSchema.InitializeResult}
     * on success, or an error {@link Entity} with {@link McpError} on failure.
     */
    private Object handleInitializeRequest(HttpClassicServerRequest request, HttpClassicServerResponse response,
            McpSchema.JSONRPCRequest jsonrpcRequest) {
        McpSchema.InitializeRequest initializeRequest =
                this.jsonMapper.convertValue(jsonrpcRequest.params(), new TypeRef<McpSchema.InitializeRequest>() {});
        McpStreamableServerSession.McpStreamableServerSessionInit init =
                this.sessionFactory.startSession(initializeRequest);
        this.sessions.put(init.session().getId(), init.session());

        try {
            McpSchema.InitializeResult initResult = init.initResult().block();
            response.statusCode(HttpResponseStatus.OK.statusCode());
            response.headers().set("Content-Type", MimeType.APPLICATION_JSON.value());
            response.headers().set(HttpHeaders.MCP_SESSION_ID, init.session().getId());
            logger.info("[POST] Sending initialize message via HTTP response. [sessionId={}]", init.session().getId());
            return Entity.createObject(response,
                    new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, jsonrpcRequest.id(), initResult, null));
        } catch (Exception e) {
            logger.error("[POST] Failed to initialize session. [error={}]", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Handles different types of JSON-RPC messages (Response, Notification, Request).
     * Routes the message to the appropriate handler method based on its type.
     *
     * @param message The {@link McpSchema.JSONRPCMessage} to handle.
     * @param request The incoming {@link HttpClassicServerRequest}.
     * @param requestBody The {@link String} of request body..
     * @param transportContext The {@link McpTransportContext} for request context propagation.
     * @param response The {@link HttpClassicServerResponse} to set status code and return data.
     * @return An {@link Entity} or {@link Choir} containing the response data, or {@code null} for accepted messages.
     */
    private Object handleJsonRpcMessage(McpSchema.JSONRPCMessage message, HttpClassicServerRequest request,
            String requestBody, McpTransportContext transportContext, HttpClassicServerResponse response) {
        // Get session ID and session
        Object sessionError = this.validateRequestSessionId(request, response);
        if (sessionError != null) {
            return sessionError;
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);
        logger.info("[POST] Receiving message from session. [sessionId={}, requestBody={}]", sessionId, requestBody);

        if (message instanceof McpSchema.JSONRPCResponse jsonrpcResponse) {
            this.handleJsonRpcResponse(jsonrpcResponse, session, transportContext, response);
            return null;
        } else if (message instanceof McpSchema.JSONRPCNotification jsonrpcNotification) {
            this.handleJsonRpcNotification(jsonrpcNotification, session, transportContext, response);
            return null;
        } else if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest) {
            return this.handleJsonRpcRequest(jsonrpcRequest, session, sessionId, transportContext, response);
        } else {
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message("Unknown message type.").build());
        }
    }

    /**
     * Handles incoming JSON-RPC response messages from clients.
     * Accepts the response and delivers it to the corresponding pending request within the session.
     * Sets the HTTP response status to {@code 202 Accepted} to acknowledge receipt.
     *
     * @param jsonrpcResponse The {@link McpSchema.JSONRPCResponse} from the client.
     * @param session The {@link McpStreamableServerSession} to accept the response.
     * @param transportContext The {@link McpTransportContext} for request context propagation.
     * @param response The {@link HttpClassicServerResponse} to set the status code.
     */
    private void handleJsonRpcResponse(McpSchema.JSONRPCResponse jsonrpcResponse, McpStreamableServerSession session,
            McpTransportContext transportContext, HttpClassicServerResponse response) {
        session.accept(jsonrpcResponse).contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
        response.statusCode(HttpResponseStatus.ACCEPTED.statusCode());
    }

    /**
     * Handles incoming JSON-RPC notification messages from clients.
     * Notifications are one-way messages that do not require a response.
     * Sets the HTTP response status to {@code 202 Accepted} to acknowledge receipt.
     *
     * @param jsonrpcNotification The {@link McpSchema.JSONRPCNotification} from the client.
     * @param session The {@link McpStreamableServerSession} to accept the notification.
     * @param transportContext The {@link McpTransportContext} for request context propagation.
     * @param response The {@link HttpClassicServerResponse} to set the status code.
     */
    private void handleJsonRpcNotification(McpSchema.JSONRPCNotification jsonrpcNotification,
            McpStreamableServerSession session, McpTransportContext transportContext,
            HttpClassicServerResponse response) {
        session.accept(jsonrpcNotification)
                .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                .block();
        response.statusCode(HttpResponseStatus.ACCEPTED.statusCode());
    }

    /**
     * Handles incoming JSON-RPC request messages from clients with streaming response support.
     * Creates an SSE stream to send the response and any subsequent messages back to the client.
     * This allows for real-time, bidirectional communication during request processing.
     *
     * @param jsonrpcRequest The {@link McpSchema.JSONRPCRequest} from the client.
     * @param session The {@link McpStreamableServerSession} to process the request.
     * @param sessionId The MCP session identifier for logging and tracking.
     * @param transportContext The {@link McpTransportContext} for request context propagation.
     * @param response The {@link HttpClassicServerResponse} for the SSE stream.
     * @return A {@link Choir} containing {@link TextEvent} for SSE streaming of the response.
     */
    private Object handleJsonRpcRequest(McpSchema.JSONRPCRequest jsonrpcRequest, McpStreamableServerSession session,
            String sessionId, McpTransportContext transportContext, HttpClassicServerResponse response) {
        return Choir.<TextEvent>create(emitter -> {
            emitter.observe(new Emitter.Observer<TextEvent>() {
                @Override
                public void onEmittedData(TextEvent data) {
                    // No action needed
                }

                @Override
                public void onCompleted() {
                    FitMcpStreamableServerTransportProvider.logger.info("[SSE] Completed SSE emitting. [sessionId={}]",
                            sessionId);
                }

                @Override
                public void onFailed(Exception e) {
                    FitMcpStreamableServerTransportProvider.logger.warn("[SSE] SSE failed. [sessionId={}, cause={}]",
                            sessionId,
                            e.getMessage());
                }
            });

            FitStreamableMcpSessionTransport sessionTransport =
                    new FitStreamableMcpSessionTransport(sessionId, emitter, response);

            try {
                session.responseStream(jsonrpcRequest, sessionTransport)
                        .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                        .block();
            } catch (Exception e) {
                logger.error("[POST] Failed to handle request stream. [error={}]", e.getMessage(), e);
                emitter.fail(e);
            }
        });
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
    private class FitStreamableMcpSessionTransport extends AbstractFitMcpSessionTransport
            implements McpStreamableServerTransport {
        /**
         * Creates a new session transport with the specified ID and SSE builder.
         *
         * @param sessionId The unique identifier for this session.
         * @param emitter The emitter for sending events.
         * @param response The HTTP response for checking connection status.
         */
        FitStreamableMcpSessionTransport(String sessionId, Emitter<TextEvent> emitter,
                HttpClassicServerResponse response) {
            super(sessionId, emitter, response);
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return this.doSendMessage(message, null);
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return this.doSendMessage(message, messageId);
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
         * @return This builder instance.
         * @throws IllegalArgumentException if jsonMapper is null.
         */
        public Builder jsonMapper(McpJsonMapper jsonMapper) {
            Validation.notNull(jsonMapper, "Json mapper must not be null.");
            this.jsonMapper = jsonMapper;
            return this;
        }

        /**
         * Sets whether to disallow DELETE requests on the endpoint.
         *
         * @param disallowDelete true to disallow DELETE requests, false otherwise.
         * @return This builder instance.
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
         * @return This builder instance.
         * @throws IllegalArgumentException if contextExtractor is null.
         */
        public Builder contextExtractor(McpTransportContextExtractor<HttpClassicServerRequest> contextExtractor) {
            Validation.notNull(contextExtractor, "Context extractor must not be null.");
            this.contextExtractor = contextExtractor;
            return this;
        }

        /**
         * Sets the keep-alive interval for the transport. If set, a keep-alive scheduler
         * will be created to periodically check and send keep-alive messages to clients.
         *
         * @param keepAliveInterval The interval duration for keep-alive messages, or null
         * to disable keep-alive.
         * @return This builder instance.
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Builds a new instance of {@link FitMcpStreamableServerTransportProvider} with
         * the configured settings.
         *
         * @return A new FitMcpStreamableServerTransportProvider instance.
         * @throws IllegalStateException if required parameters are not set.
         */
        public FitMcpStreamableServerTransportProvider build() {
            Validation.notNull(this.jsonMapper, "Json mapper must be set.");

            return new FitMcpStreamableServerTransportProvider(this.jsonMapper,
                    this.disallowDelete,
                    this.contextExtractor,
                    this.keepAliveInterval);
        }
    }
}
