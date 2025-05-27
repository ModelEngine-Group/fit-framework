/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.mcp.entity.Event;
import modelengine.fel.tool.mcp.entity.JsonRpc;
import modelengine.fel.tool.mcp.entity.Method;
import modelengine.fel.tool.mcp.server.handler.InitializeHandler;
import modelengine.fel.tool.mcp.server.handler.PingHandler;
import modelengine.fel.tool.mcp.server.handler.ToolCallHandler;
import modelengine.fel.tool.mcp.server.handler.ToolListHandler;
import modelengine.fel.tool.mcp.server.handler.UnsupportedMethodHandler;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Emitter;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.schedule.ExecutePolicy;
import modelengine.fitframework.schedule.Task;
import modelengine.fitframework.schedule.ThreadPoolScheduler;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.UuidUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIT MCP Server controller.
 *
 * @author 季聿阶
 * @since 2025-05-13
 */
@Component
public class McpServerController implements McpServer.ToolsChangedObserver {
    private static final Logger log = Logger.get(McpServerController.class);
    private static final String MESSAGE_PATH = "/mcp/message";
    private static final String RESPONSE_OK = StringUtils.EMPTY;

    private final Map<String, Emitter<TextEvent>> emitters = new ConcurrentHashMap<>();
    private final Map<String, HttpClassicServerResponse> responses = new ConcurrentHashMap<>();
    private final Map<String, MessageHandler> methodHandlers = new HashMap<>();
    private final MessageHandler unsupportedMethodHandler = new UnsupportedMethodHandler();
    private final ObjectSerializer serializer;

    /**
     * Constructs a new instance of the McpController class.
     *
     * @param serializer The JSON serializer used to serialize and deserialize RPC messages, as an
     * {@link ObjectSerializer}.
     * @param mcpServer The MCP server instance used to handle tool operations such as initialization,
     * listing tools, and calling tools, as a {@link McpServer}.
     */
    public McpServerController(@Fit(alias = "json") ObjectSerializer serializer, McpServer mcpServer) {
        this.serializer = notNull(serializer, "The json serializer cannot be null.");
        notNull(mcpServer, "The MCP server cannot be null.");
        mcpServer.registerToolsChangedObserver(this);

        this.methodHandlers.put(Method.INITIALIZE.code(), new InitializeHandler(mcpServer));
        this.methodHandlers.put(Method.PING.code(), new PingHandler());
        this.methodHandlers.put(Method.TOOLS_LIST.code(), new ToolListHandler(mcpServer));
        this.methodHandlers.put(Method.TOOLS_CALL.code(), new ToolCallHandler(mcpServer, this.serializer));

        ThreadPoolScheduler channelDetectorScheduler = ThreadPoolScheduler.custom()
                .corePoolSize(1)
                .isDaemonThread(true)
                .threadPoolName("mcp-server-channel-detector")
                .build();
        channelDetectorScheduler.schedule(Task.builder().policy(ExecutePolicy.fixedDelay(10000)).runnable(() -> {
            if (MapUtils.isEmpty(this.responses)) {
                return;
            }
            List<String> toRemoved = new ArrayList<>();
            for (Map.Entry<String, HttpClassicServerResponse> entry : this.responses.entrySet()) {
                if (entry.getValue().isActive()) {
                    continue;
                }
                toRemoved.add(entry.getKey());
            }
            if (CollectionUtils.isEmpty(toRemoved)) {
                return;
            }
            toRemoved.forEach(this.responses::remove);
            toRemoved.forEach(this.emitters::remove);
            log.info("Channels are inactive, remove emitters and responses. [sessionIds={}]", toRemoved);
        }).build());
    }

    /**
     * Creates a Server-Sent Events (SSE) channel for real-time communication with the client.
     *
     * <p>This method generates a unique session ID and registers an emitter to send events.</p>
     *
     * @param response The HTTP server response object used to manage the SSE connection as a
     * {@link HttpClassicServerResponse}.
     * @return A {@link Choir}{@code <}{@link TextEvent}{@code >} object that emits text events to the connected client.
     */
    @GetMapping(path = "/sse")
    public Choir<TextEvent> createSse(HttpClassicServerResponse response) {
        String sessionId = UuidUtils.randomUuidString();
        this.responses.put(sessionId, response);
        log.info("New SSE channel for MCP server created. [sessionId={}]", sessionId);
        return Choir.create(emitter -> {
            emitters.put(sessionId, emitter);
            String data = MESSAGE_PATH + "?session_id=" + sessionId;
            TextEvent textEvent = TextEvent.custom().id(sessionId).event(Event.ENDPOINT.code()).data(data).build();
            emitter.emit(textEvent);
            log.info("Send MCP endpoint. [endpoint={}]", data);
        });
    }

    /**
     * Receives and processes an MCP message via HTTP POST request.
     *
     * <p>This method handles incoming JSON-RPC requests, routes them to the appropriate handler,
     * and returns a response via the associated event emitter.</p>
     *
     * @param sessionId The session ID used to identify the current client session.
     * @param request The JSON-RPC request entity containing the method name and parameters.
     * @return Always returns an empty string ({@value #RESPONSE_OK}) to indicate success.
     */
    @PostMapping(path = MESSAGE_PATH)
    public Object receiveMcpMessage(@RequestQuery(name = "session_id") String sessionId,
            @RequestBody Map<String, Object> request) {
        log.info("Receive MCP message. [sessionId={}, message={}]", sessionId, request);
        Object id = request.get("id");
        if (id == null) {
            // Request without an ID indicates a notification message, ignore.
            return RESPONSE_OK;
        }
        String method = cast(request.getOrDefault("method", StringUtils.EMPTY));
        MessageHandler handler = this.methodHandlers.getOrDefault(method, this.unsupportedMethodHandler);
        JsonRpc.Response<Object> response;
        try {
            Object result = handler.handle(cast(request.get("params")));
            response = JsonRpc.createResponse(id, result);
        } catch (Exception e) {
            log.error("Failed to handle MCP message.", e);
            response = JsonRpc.createResponseWithError(id, e.getMessage());
        }
        String serialized = this.serializer.serialize(response);
        TextEvent textEvent = TextEvent.custom().id(sessionId).event(Event.MESSAGE.code()).data(serialized).build();
        Emitter<TextEvent> emitter = this.emitters.get(sessionId);
        emitter.emit(textEvent);
        log.info("Send MCP message. [message={}]", serialized);
        return RESPONSE_OK;
    }

    @Override
    public void onToolsChanged() {
        JsonRpc.Notification notification = JsonRpc.createNotification(Method.NOTIFICATION_TOOLS_CHANGED.code());
        String serialized = this.serializer.serialize(notification);
        this.emitters.forEach((sessionId, emitter) -> {
            TextEvent textEvent = TextEvent.custom().id(sessionId).event(Event.MESSAGE.code()).data(serialized).build();
            emitter.emit(textEvent);
            log.info("Send MCP notification: tools changed. [sessionId={}]", sessionId);
        });
    }
}
