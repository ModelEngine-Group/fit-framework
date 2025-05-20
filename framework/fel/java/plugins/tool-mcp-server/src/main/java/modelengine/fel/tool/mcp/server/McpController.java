/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.mcp.server.entity.JsonRpcEntity;
import modelengine.fel.tool.mcp.server.handler.InitializeHandler;
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
import modelengine.fitframework.annotation.Value;
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
public class McpController implements McpServer.ToolsChangedObserver {
    private static final Logger log = Logger.get(McpController.class);
    private static final String MESSAGE_PATH = "/mcp/message";
    private static final String EVENT_ENDPOINT = "endpoint";
    private static final String EVENT_MESSAGE = "message";
    private static final String METHOD_INITIALIZE = "initialize";
    private static final String METHOD_TOOLS_LIST = "tools/list";
    private static final String METHOD_TOOLS_CALL = "tools/call";
    private static final String METHOD_NOTIFICATION_TOOLS_CHANGED = "notifications/tools/list_changed";
    private static final String RESPONSE_OK = StringUtils.EMPTY;

    private final Map<String, Emitter<TextEvent>> emitters = new ConcurrentHashMap<>();
    private final Map<String, HttpClassicServerResponse> responses = new ConcurrentHashMap<>();
    private final Map<String, MessageHandler> methodHandlers = new HashMap<>();
    private final MessageHandler unsupportedMethodHandler = new UnsupportedMethodHandler();
    private final String baseUrl;
    private final ObjectSerializer serializer;

    /**
     * Constructs a new instance of the McpController class.
     *
     * @param baseUrl The base URL for the MCP server as a {@link String}, used to construct message endpoints.
     * @param serializer The JSON serializer used to serialize and deserialize RPC messages, as an
     * {@link ObjectSerializer}.
     * @param mcpServer The MCP server instance used to handle tool operations such as initialization,
     * listing tools, and calling tools, as a {@link McpServer}.
     */
    public McpController(@Value("${base-url}") String baseUrl, @Fit(alias = "json") ObjectSerializer serializer,
            McpServer mcpServer) {
        this.baseUrl = notBlank(baseUrl, "The base URL for MCP server cannot be blank.");
        this.serializer = notNull(serializer, "The json serializer cannot be null.");
        notNull(mcpServer, "The MCP server cannot be null.");
        mcpServer.registerToolsChangedObserver(this);

        this.methodHandlers.put(METHOD_INITIALIZE, new InitializeHandler(mcpServer));
        this.methodHandlers.put(METHOD_TOOLS_LIST, new ToolListHandler(mcpServer));
        this.methodHandlers.put(METHOD_TOOLS_CALL, new ToolCallHandler(mcpServer, this.serializer));

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
            TextEvent textEvent = TextEvent.custom()
                    .id(sessionId)
                    .event(EVENT_ENDPOINT)
                    .data(this.baseUrl + MESSAGE_PATH + "?sessionId=" + sessionId)
                    .build();
            emitter.emit(textEvent);
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
    public Object receiveMcpMessage(@RequestQuery(name = "sessionId") String sessionId,
            @RequestBody JsonRpcEntity request) {
        log.info("Receive MCP message. [sessionId={}, request={}]", sessionId, request);
        Object id = request.getId();
        if (id == null) {
            // Request without an ID indicates a notification message, ignore.
            return RESPONSE_OK;
        }
        MessageHandler handler = this.methodHandlers.getOrDefault(request.getMethod(), this.unsupportedMethodHandler);
        JsonRpcEntity response = new JsonRpcEntity();
        response.setId(id);
        try {
            Object result = handler.handle(request.getParams());
            response.setResult(result);
        } catch (Exception e) {
            log.error("Failed to handle MCP message.", e);
            response.setError(e.getMessage());
        }
        String serialized = this.serializer.serialize(response);
        TextEvent textEvent = TextEvent.custom().id(sessionId).event(EVENT_MESSAGE).data(serialized).build();
        Emitter<TextEvent> emitter = this.emitters.get(sessionId);
        emitter.emit(textEvent);
        log.info("Send MCP message. [response={}]", serialized);
        return RESPONSE_OK;
    }

    @Override
    public void onToolsChanged() {
        JsonRpcEntity notification = new JsonRpcEntity();
        notification.setMethod(METHOD_NOTIFICATION_TOOLS_CHANGED);
        String serialized = this.serializer.serialize(notification);
        this.emitters.forEach((sessionId, emitter) -> {
            TextEvent textEvent = TextEvent.custom().id(sessionId).event(EVENT_MESSAGE).data(serialized).build();
            emitter.emit(textEvent);
            log.info("Send MCP notification: tools changed. [sessionId={}]", sessionId);
        });
    }
}
