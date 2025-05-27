/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.entity.ClientSchema;
import modelengine.fel.tool.mcp.entity.Event;
import modelengine.fel.tool.mcp.entity.JsonRpc;
import modelengine.fel.tool.mcp.entity.Method;
import modelengine.fel.tool.mcp.entity.ServerSchema;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.entity.Entity;
import modelengine.fit.http.entity.TextEvent;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.schedule.ExecutePolicy;
import modelengine.fitframework.schedule.Task;
import modelengine.fitframework.schedule.ThreadPoolExecutor;
import modelengine.fitframework.schedule.ThreadPoolScheduler;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.LockUtils;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.ThreadUtils;
import modelengine.fitframework.util.UuidUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a default implementation of the MCP client, responsible for interacting with the MCP server.
 * This class provides methods for initializing the client, retrieving tools, and calling tools.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
public class DefaultMcpClient implements McpClient {
    private static final Logger log = Logger.get(DefaultMcpClient.class);
    private static final long DELAY_MILLIS = 30_000L;

    private final ObjectSerializer jsonSerializer;
    private final HttpClassicClient client;
    private final String baseUri;
    private final String sseEndpoint;
    private final String name;
    private final AtomicLong id = new AtomicLong(0);

    private volatile String messageEndpoint;
    private volatile String sessionId;
    private volatile ServerSchema serverSchema;
    private volatile boolean initialized = false;
    private final List<Tool> tools = new ArrayList<>();
    private final Object initializedLock = LockUtils.newSynchronizedLock();
    private final Object toolsLock = LockUtils.newSynchronizedLock();
    private final Map<Long, Consumer<JsonRpc.Response<Long>>> responseConsumers = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> pendingRequests = new ConcurrentHashMap<>();
    private final Map<Long, Object> pendingResults = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of the DefaultMcpClient.
     *
     * @param jsonSerializer The serializer used for JSON serialization and deserialization.
     * @param client The HTTP client used for communication with the MCP server.
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The endpoint for the Server-Sent Events (SSE) connection.
     */
    public DefaultMcpClient(ObjectSerializer jsonSerializer, HttpClassicClient client, String baseUri,
            String sseEndpoint) {
        this.jsonSerializer = jsonSerializer;
        this.client = client;
        this.baseUri = baseUri;
        this.sseEndpoint = sseEndpoint;
        this.name = UuidUtils.randomUuidString();
    }

    @Override
    public void initialize() {
        HttpClassicClientRequest request =
                this.client.createRequest(HttpRequestMethod.GET, this.baseUri + this.sseEndpoint);
        Choir<TextEvent> messages = this.client.exchangeStream(request, TextEvent.class);
        ThreadPoolExecutor threadPool = ThreadPoolExecutor.custom()
                .threadPoolName("mcp-client-" + this.name)
                .awaitTermination(3, TimeUnit.SECONDS)
                .isImmediateShutdown(true)
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .workQueueCapacity(Integer.MAX_VALUE)
                .isDaemonThread(true)
                .exceptionHandler((thread, cause) -> log.warn("Exception in MCP client pool.", cause))
                .rejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy())
                .build();
        messages.subscribeOn(threadPool).subscribe(subscription -> {
                    log.info("Prepare to create SSE channel.");
                    subscription.request(Long.MAX_VALUE);
                },
                (subscription, textEvent) -> this.consumeTextEvent(textEvent),
                subscription -> log.info("SSE channel is completed."),
                (subscription, cause) -> log.error("SSE channel is failed.", cause));
        ThreadPoolScheduler pingScheduler = ThreadPoolScheduler.custom()
                .threadPoolName("mcp-client-ping-" + this.name)
                .awaitTermination(3, TimeUnit.SECONDS)
                .isImmediateShutdown(true)
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .workQueueCapacity(Integer.MAX_VALUE)
                .isDaemonThread(true)
                .build();
        pingScheduler.schedule(Task.builder()
                .runnable(this::pingServer)
                .policy(ExecutePolicy.fixedDelay(DELAY_MILLIS))
                .build(), DELAY_MILLIS);
        if (!this.waitInitialized()) {
            throw new IllegalStateException("Failed to initialize.");
        }
    }

    private void consumeTextEvent(TextEvent textEvent) {
        log.info("Receive message from MCP server. [id={}, event={}, message={}]",
                textEvent.id(),
                textEvent.event(),
                textEvent.data());
        if (StringUtils.isBlank(textEvent.event()) || StringUtils.isBlank((String) textEvent.data())) {
            return;
        }
        if (Objects.equals(textEvent.event(), Event.ENDPOINT.code())) {
            this.initializeMcpServer(textEvent);
            return;
        }
        Map<String, Object> jsonRpc = this.jsonSerializer.deserialize((String) textEvent.data(), Object.class);
        Object messageId = jsonRpc.get("id");
        if (messageId == null) {
            // Notification message, ignore.
            return;
        }
        long actualId = Long.parseLong(messageId.toString());
        Consumer<JsonRpc.Response<Long>> consumer = this.responseConsumers.remove(actualId);
        if (consumer == null) {
            log.info("No consumer registered. [id={}]", actualId);
            return;
        }
        Object error = jsonRpc.get("error");
        JsonRpc.Response<Long> response;
        if (error == null) {
            response = JsonRpc.createResponse(actualId, jsonRpc.get("result"));
        } else {
            response = JsonRpc.createResponseWithError(actualId, error);
        }
        consumer.accept(response);
    }

    private void pingServer() {
        if (this.isNotInitialized()) {
            log.info("MCP client is not initialized and {} method will be delayed.", Method.PING.code());
            return;
        }
        this.post2McpServer(Method.PING, null, null);
    }

    private void initializeMcpServer(TextEvent textEvent) {
        this.messageEndpoint = textEvent.data().toString();
        ClientSchema schema = new ClientSchema("2024-11-05",
                new ClientSchema.Capabilities(),
                new ClientSchema.Info("FIT MCP Client", "3.5.0-SNAPSHOT"));
        this.post2McpServer(Method.INITIALIZE, schema, (request, currentId) -> {
            this.sessionId = request.queries()
                    .first("session_id")
                    .orElseThrow(() -> new IllegalStateException("The session_id cannot be empty."));
            this.responseConsumers.put(currentId, this::initializedMcpServer);
        });
    }

    private void initializedMcpServer(JsonRpc.Response<Long> response) {
        if (response.error() != null) {
            log.error("Abort send {} method to MCP server. [sessionId={}, error={}]",
                    Method.NOTIFICATION_INITIALIZED.code(),
                    this.sessionId,
                    response.error());
            throw new IllegalStateException(response.error().toString());
        }
        synchronized (this.initializedLock) {
            this.initialized = true;
            this.initializedLock.notifyAll();
        }
        this.recordServerSchema(response);
        HttpClassicClientRequest request =
                this.client.createRequest(HttpRequestMethod.POST, this.baseUri + this.messageEndpoint);
        JsonRpc.Notification notification = JsonRpc.createNotification(Method.NOTIFICATION_INITIALIZED.code());
        request.entity(Entity.createObject(request, notification));
        log.info("Send {} method to MCP server. [sessionId={}, notification={}]",
                Method.NOTIFICATION_INITIALIZED.code(),
                this.sessionId,
                notification);
        try (HttpClassicClientResponse<Object> exchange = request.exchange(Object.class)) {
            if (exchange.statusCode() >= 200 && exchange.statusCode() < 300) {
                log.info("Send {} method to MCP server successfully. [sessionId={}, statusCode={}]",
                        Method.NOTIFICATION_INITIALIZED.code(),
                        this.sessionId,
                        exchange.statusCode());
            } else {
                log.error("Failed to {} MCP server. [sessionId={}, statusCode={}]",
                        Method.NOTIFICATION_INITIALIZED.code(),
                        this.sessionId,
                        exchange.statusCode());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void recordServerSchema(JsonRpc.Response<Long> response) {
        Map<String, Object> mapResult = cast(response.result());
        this.serverSchema = ServerSchema.create(mapResult);
        log.info("MCP server has initialized. [server={}]", this.serverSchema);
    }

    @Override
    public List<Tool> getTools() {
        if (this.isNotInitialized()) {
            throw new IllegalStateException("MCP client is not initialized. Please wait a moment.");
        }
        long requestId = this.post2McpServer(Method.TOOLS_LIST, null, (request, currentId) -> {
            this.responseConsumers.put(currentId, this::getTools0);
            this.pendingRequests.put(currentId, true);
        });
        while (this.pendingRequests.get(requestId)) {
            ThreadUtils.sleep(100);
        }
        synchronized (this.toolsLock) {
            return this.tools;
        }
    }

    private void getTools0(JsonRpc.Response<Long> response) {
        if (response.error() != null) {
            log.error("Failed to get tools list from MCP server. [sessionId={}, response={}]",
                    this.sessionId,
                    response);
            this.pendingRequests.put(response.id(), false);
            return;
        }
        Map<String, Object> result = cast(response.result());
        List<Map<String, Object>> rawTools = cast(result.get("tools"));
        synchronized (this.toolsLock) {
            this.tools.clear();
            this.tools.addAll(rawTools.stream()
                    .map(rawTool -> ObjectUtils.<Tool>toCustomObject(rawTool, Tool.class))
                    .toList());
        }
        this.pendingRequests.put(response.id(), false);
    }

    @Override
    public Object callTool(String name, Map<String, Object> arguments) {
        if (this.isNotInitialized()) {
            throw new IllegalStateException("MCP client is not initialized. Please wait a moment.");
        }
        long requestId = this.post2McpServer(Method.TOOLS_CALL,
                MapBuilder.<String, Object>get().put("name", name).put("arguments", arguments).build(),
                (request, currentId) -> {
                    this.responseConsumers.put(currentId, this::callTools0);
                    this.pendingRequests.put(currentId, true);
                });
        while (this.pendingRequests.get(requestId)) {
            ThreadUtils.sleep(100);
        }
        return this.pendingResults.get(requestId);
    }

    private void callTools0(JsonRpc.Response<Long> response) {
        if (response.error() != null) {
            log.error("Failed to call tool from MCP server. [sessionId={}, response={}]", this.sessionId, response);
            this.pendingRequests.put(response.id(), false);
            return;
        }
        Map<String, Object> result = cast(response.result());
        boolean isError = cast(result.get("isError"));
        if (isError) {
            log.error("Failed to call tool from MCP server. [sessionId={}, result={}]", this.sessionId, result);
            this.pendingRequests.put(response.id(), false);
            return;
        }
        List<Map<String, Object>> rawContents = cast(result.get("content"));
        if (CollectionUtils.isEmpty(rawContents)) {
            log.error("Failed to call tool from MCP server: no result returned. [sessionId={}, result={}]",
                    this.sessionId,
                    result);
            this.pendingRequests.put(response.id(), false);
            return;
        }
        Map<String, Object> rawContent = rawContents.get(0);
        this.pendingResults.put(response.id(), rawContent.get("text"));
        this.pendingRequests.put(response.id(), false);
    }

    private long post2McpServer(Method method, Object requestParams,
            BiConsumer<HttpClassicClientRequest, Long> requestConsumer) {
        HttpClassicClientRequest request =
                this.client.createRequest(HttpRequestMethod.POST, this.baseUri + this.messageEndpoint);
        long currentId = this.getNextId();
        if (requestConsumer != null) {
            requestConsumer.accept(request, currentId);
        }
        JsonRpc.Request<Long> rpcRequest = JsonRpc.createRequest(currentId, method.code(), requestParams);
        request.entity(Entity.createObject(request, rpcRequest));
        log.info("Send {} method to MCP server. [sessionId={}, request={}]", method.code(), this.sessionId, rpcRequest);
        try (HttpClassicClientResponse<Object> exchange = request.exchange(Object.class)) {
            if (exchange.statusCode() >= 200 && exchange.statusCode() < 300) {
                log.info("Send {} method to MCP server successfully. [sessionId={}, statusCode={}]",
                        method.code(),
                        this.sessionId,
                        exchange.statusCode());
            } else {
                log.error("Failed to {} MCP server. [sessionId={}, statusCode={}]",
                        method.code(),
                        this.sessionId,
                        exchange.statusCode());
            }
        } catch (IOException e) {
            throw new IllegalStateException(StringUtils.format("Failed to {0} MCP server. [sessionId={1}]",
                    method.code(),
                    this.sessionId), e);
        }
        return currentId;
    }

    private long getNextId() {
        long tmpId = this.id.getAndIncrement();
        if (tmpId < 0) {
            this.id.set(0);
            return 0;
        }
        return tmpId;
    }

    private boolean isNotInitialized() {
        return !this.initialized;
    }

    private boolean waitInitialized() {
        if (this.initialized) {
            return true;
        }
        synchronized (this.initializedLock) {
            if (this.initialized) {
                return true;
            }
            try {
                this.initializedLock.wait(60_000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Failed to initialize.", e);
            }
        }
        return this.initialized;
    }
}
