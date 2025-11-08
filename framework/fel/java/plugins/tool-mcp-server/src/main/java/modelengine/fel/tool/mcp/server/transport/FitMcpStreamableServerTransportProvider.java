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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default implementation of {@link McpStreamableServerTransportProvider}.
 * The FIT transport provider for MCP Server, according to {@code HttpServletStreamableServerTransportProvider} in MCP
 * SDK.
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
                    logger.error("Failed to send message to session {}: {}", session.getId(), e.getMessage(), e);
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
                    logger.error("Failed to close session {}: {}", session.getId(), e.getMessage(), e);
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

        Object headerError = validateGetAcceptHeaders(request, response);
        if (headerError != null) {
            return headerError;
        }

        // Get session ID and session
        Object sessionError = validateRequestSessionId(request, response);
        if (sessionError != null) {
            return sessionError;
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);
        logger.info("[GET] Handling GET request for session: {}", sessionId);

        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            return Choir.<TextEvent>create(emitter -> {
                FitStreamableMcpSessionTransport sessionTransport =
                        new FitStreamableMcpSessionTransport(sessionId, emitter, response);

                // Handle building SSE, and check if this is a replay request
                if (request.headers().contains(HttpHeaders.LAST_EVENT_ID)) {
                    handleReplaySseRequest(request, transportContext, sessionId, session, sessionTransport, emitter);
                } else {
                    handleEstablishSseRequest(sessionId, session, sessionTransport, emitter);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to handle GET request for session {}: {}", sessionId, e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return null;
        }
    }

    /**
     * Handles POST requests for incoming JSON-RPC messages from clients.
     *
     * @param request The incoming server request containing the JSON-RPC message
     * @param response The HTTP response
     * @return Return the HTTP response body {@link Entity} or a {@link Choir}{@code <}{@link TextEvent}{@code >} object
     */
    @PostMapping(path = MESSAGE_ENDPOINT)
    public Object handlePost(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (this.isClosing) {
            response.statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.statusCode());
            return Entity.createText(response, "Server is shutting down");
        }
        Object headerError = validatePostAcceptHeaders(request, response);
        if (headerError != null) {
            return headerError;
        }

        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            String requestBody = new String(request.entityBytes(), StandardCharsets.UTF_8);
            McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(jsonMapper, requestBody);

            // Handle JSONRPCMessage
            if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest && jsonrpcRequest.method()
                    .equals(McpSchema.METHOD_INITIALIZE)) {
                logger.info("[POST] Handling initialize method, with receiving message: {}", requestBody);
                return handleInitializeRequest(request, response, jsonrpcRequest);
            } else {
                return handleJsonRpcMessage(message, request, requestBody, transportContext, response);
            }
        } catch (IllegalArgumentException | IOException e) {
            logger.error("Failed to deserialize message: {}", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.PARSE_ERROR).message("Invalid message format").build());
        } catch (Exception e) {
            logger.error("Error handling message: {}", e.getMessage(), e);
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

        // Get session ID and session
        Object sessionError = validateRequestSessionId(request, response);
        if (sessionError != null) {
            return sessionError;
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);
        logger.info("[DELETE] Receiving delete request from session: {}", sessionId);

        McpTransportContext transportContext = this.contextExtractor.extract(request);
        try {
            session.delete().contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext)).block();
            this.sessions.remove(sessionId);
            response.statusCode(HttpResponseStatus.OK.statusCode());
            return null;
        } catch (Exception e) {
            logger.error("Failed to delete session {}: {}", sessionId, e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Validates the Accept header for SSE (Server-Sent Events) connections in GET requests.
     * Checks if the request contains the required {@code text/event-stream} content type.
     *
     * @param request The incoming {@link HttpClassicServerRequest}
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails
     * @return An error {@link Entity} if validation fails, {@code null} if validation succeeds
     */
    private Object validateGetAcceptHeaders(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Invalid Accept header. Expected TEXT_EVENT_STREAM");
        }
        return null;
    }

    /**
     * Validates the Accept headers for POST requests.
     * Checks if the request contains both {@code text/event-stream} and {@code application/json} content types,
     * as POST requests may return either SSE streams or JSON responses.
     *
     * @param request The incoming {@link HttpClassicServerRequest}
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails
     * @return An error {@link Entity} with {@link McpError} if validation fails, {@code null} if validation succeeds
     */
    private Object validatePostAcceptHeaders(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
        if (!acceptHeaders.contains(MimeType.TEXT_EVENT_STREAM.value())
                || !acceptHeaders.contains(MimeType.APPLICATION_JSON.value())) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST)
                            .message("Invalid Accept headers. Expected TEXT_EVENT_STREAM and APPLICATION_JSON")
                            .build());
        }
        return null;
    }

    /**
     * Validates the MCP session ID in the request headers and verifies the session exists.
     * This method checks both the presence of the {@code mcp-session-id} header and
     * the existence of the corresponding session in the active sessions map.
     *
     * @param request The incoming {@link HttpClassicServerRequest} containing the session ID header
     * @param response The {@link HttpClassicServerResponse} to set status code if validation fails
     * @return An error {@link Entity} if validation fails (either missing session ID or session not found),
     * {@code null} if validation succeeds
     */
    private Object validateRequestSessionId(HttpClassicServerRequest request, HttpClassicServerResponse response) {
        if (!request.headers().contains(HttpHeaders.MCP_SESSION_ID)) {
            response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
            return Entity.createText(response, "Session ID required in mcp-session-id header");
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
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
     * Handles message replay requests for SSE connections.
     * Replays previously sent messages starting from the last received event ID,
     * allowing clients to recover missed messages after reconnection.
     *
     * @param request The incoming {@link HttpClassicServerRequest} containing the {@code Last-Event-ID} header
     * @param transportContext The {@link McpTransportContext} for request context propagation
     * @param sessionId The MCP session identifier
     * @param session The {@link McpStreamableServerSession} to replay messages from
     * @param sessionTransport The {@link FitStreamableMcpSessionTransport} for sending replayed messages
     * @param emitter The SSE {@link Emitter} to send {@link TextEvent} to the client
     */
    private void handleReplaySseRequest(HttpClassicServerRequest request, McpTransportContext transportContext,
            String sessionId, McpStreamableServerSession session, FitStreamableMcpSessionTransport sessionTransport,
            Emitter<TextEvent> emitter) {
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
                            logger.error("Failed to replay message: {}", e.getMessage(), e);
                            emitter.fail(e);
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to replay messages: {}", e.getMessage(), e);
            emitter.fail(e);
        }
    }

    /**
     * Establishes a new SSE listening stream for real-time message delivery.
     * Creates a persistent connection that allows the server to push messages to the client
     * as they become available. The stream remains open until explicitly closed or an error occurs.
     *
     * @param sessionId The MCP session identifier
     * @param session The {@link McpStreamableServerSession} to establish the listening stream for
     * @param sessionTransport The {@link FitStreamableMcpSessionTransport} for bidirectional communication
     * @param emitter The SSE {@link Emitter} to send {@link TextEvent} to the client
     */
    private void handleEstablishSseRequest(String sessionId, McpStreamableServerSession session,
            FitStreamableMcpSessionTransport sessionTransport, Emitter<TextEvent> emitter) {
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

    /**
     * Handles MCP session initialization requests.
     * Creates a new {@link McpStreamableServerSession} and returns the initialization result
     * with the assigned session ID in the response headers.
     *
     * @param request The incoming {@link HttpClassicServerRequest}
     * @param response The {@link HttpClassicServerResponse} to set session ID and initialization result
     * @param jsonrpcRequest The {@link McpSchema.JSONRPCRequest} containing {@link McpSchema.InitializeRequest}
     * parameters
     * @return An {@link Entity} containing the {@link McpSchema.JSONRPCResponse} with
     * {@link McpSchema.InitializeResult}
     * on success, or an error {@link Entity} with {@link McpError} on failure
     */
    private Object handleInitializeRequest(HttpClassicServerRequest request, HttpClassicServerResponse response,
            McpSchema.JSONRPCRequest jsonrpcRequest) {
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
            logger.info("[POST] Sending initialize message via HTTP response to session {}", init.session().getId());
            return Entity.createObject(response,
                    new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, jsonrpcRequest.id(), initResult, null));
        } catch (Exception e) {
            logger.error("Failed to initialize session: {}", e.getMessage(), e);
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(e.getMessage()).build());
        }
    }

    /**
     * Handles different types of JSON-RPC messages (Response, Notification, Request).
     * Routes the message to the appropriate handler method based on its type.
     *
     * @param message The {@link McpSchema.JSONRPCMessage} to handle
     * @param request The incoming {@link HttpClassicServerRequest}
     * @param requestBody The {@link String} of request body.
     * @param transportContext The {@link McpTransportContext} for request context propagation
     * @param response The {@link HttpClassicServerResponse} to set status code and return data
     * @return An {@link Entity} or {@link Choir} containing the response data, or {@code null} for accepted messages
     */
    private Object handleJsonRpcMessage(McpSchema.JSONRPCMessage message, HttpClassicServerRequest request,
            String requestBody, McpTransportContext transportContext, HttpClassicServerResponse response) {
        // Get session ID and session
        Object sessionError = validateRequestSessionId(request, response);
        if (sessionError != null) {
            return sessionError;
        }
        String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
        McpStreamableServerSession session = this.sessions.get(sessionId);
        logger.info("[POST] Receiving message from session {}: {}", sessionId, requestBody);

        if (message instanceof McpSchema.JSONRPCResponse jsonrpcResponse) {
            handleJsonRpcResponse(jsonrpcResponse, session, transportContext, response);
            return null;
        } else if (message instanceof McpSchema.JSONRPCNotification jsonrpcNotification) {
            handleJsonRpcNotification(jsonrpcNotification, session, transportContext, response);
            return null;
        } else if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest) {
            return handleJsonRpcRequest(jsonrpcRequest, session, sessionId, transportContext, response);
        } else {
            response.statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.statusCode());
            return Entity.createObject(response,
                    McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message("Unknown message type").build());
        }
    }

    /**
     * Handles incoming JSON-RPC response messages from clients.
     * Accepts the response and delivers it to the corresponding pending request within the session.
     * Sets the HTTP response status to {@code 202 Accepted} to acknowledge receipt.
     *
     * @param jsonrpcResponse The {@link McpSchema.JSONRPCResponse} from the client
     * @param session The {@link McpStreamableServerSession} to accept the response
     * @param transportContext The {@link McpTransportContext} for request context propagation
     * @param response The {@link HttpClassicServerResponse} to set the status code
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
     * @param jsonrpcNotification The {@link McpSchema.JSONRPCNotification} from the client
     * @param session The {@link McpStreamableServerSession} to accept the notification
     * @param transportContext The {@link McpTransportContext} for request context propagation
     * @param response The {@link HttpClassicServerResponse} to set the status code
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
     * @param jsonrpcRequest The {@link McpSchema.JSONRPCRequest} from the client
     * @param session The {@link McpStreamableServerSession} to process the request
     * @param sessionId The MCP session identifier for logging and tracking
     * @param transportContext The {@link McpTransportContext} for request context propagation
     * @param response The {@link HttpClassicServerResponse} for the SSE stream
     * @return A {@link Choir} containing {@link TextEvent} for SSE streaming of the response
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
                logger.error("Failed to handle request stream: {}", e.getMessage(), e);
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
                    logger.error("Failed to send message to session {}: {}", this.sessionId, e.getMessage(), e);
                    try {
                        this.emitter.fail(e);
                    } catch (Exception errorException) {
                        logger.error("Failed to send error to SSE builder for session {}: {}",
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
