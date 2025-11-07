# FitMcpStreamableServerTransportProvider类维护文档

## 文档概述

本文档用于记录 `FitMcpStreamableServerTransportProvider` 类的设计、实现细节以及维护更新指南。该类是基于 MCP SDK 中的
`HttpServletStreamableServerTransportProvider` 类改造而来，用于在 FIT 框架中提供 MCP（Model Context Protocol）服务端的传输层实现。

**原始参考类**: MCP SDK 中的 `HttpServletStreamableServerTransportProvider`

**创建时间**: 2025-11-04

---

## 类的作用和职责

`FitMcpStreamableServerTransportProvider` 是 MCP 服务端传输层的核心实现类，负责：

1. **HTTP 端点处理**: 处理 GET、POST、DELETE 请求，实现 MCP 协议的 HTTP 传输层
2. **会话管理**: 管理客户端会话的生命周期（创建、维护、销毁）
3. **SSE 通信**: 通过 Server-Sent Events (SSE) 实现服务端到客户端的实时消息推送
4. **消息序列化**: 处理 JSON-RPC 消息的序列化和反序列化
5. **连接保活**: 支持可选的 Keep-Alive 机制
6. **优雅关闭**: 支持服务的优雅关闭和资源清理

---

## 类结构概览

### 主要成员变量

| 变量名                  | 类型                                                       | 来源         | 说明                              |
|----------------------|----------------------------------------------------------|------------|---------------------------------|
| `MESSAGE_ENDPOINT`   | `String`                                                 | SDK 原始     | 消息端点路径 `/mcp/streamable`        |
| `disallowDelete`     | `boolean`                                                | SDK 原始     | 是否禁用 DELETE 请求                  |
| `jsonMapper`         | `McpJsonMapper`                                          | SDK 原始     | JSON 序列化器                       |
| `contextExtractor`   | `McpTransportContextExtractor<HttpClassicServerRequest>` | **FIT 改造** | 上下文提取器（泛型参数改为 FIT 的 Request 类型） |
| `keepAliveScheduler` | `KeepAliveScheduler`                                     | SDK 原始     | Keep-Alive 调度器                  |
| `sessionFactory`     | `McpStreamableServerSession.Factory`                     | SDK 原始     | 会话工厂                            |
| `sessions`           | `Map<String, McpStreamableServerSession>`                | SDK 原始     | 活跃会话映射表                         |
| `isClosing`          | `volatile boolean`                                       | SDK 原始     | 关闭标志                            |

### 主要方法

| 方法名                | 来源         | 说明                            |
| --------------------- | ------------ | ------------------------------- |
| `protocolVersions()`  | SDK 原始     | 返回支持的 MCP 协议版本         |
| `setSessionFactory()` | SDK 原始     | 设置会话工厂                    |
| `notifyClients()`     | SDK 原始     | 广播通知到所有客户端            |
| `closeGracefully()`   | SDK 原始     | 优雅关闭传输层                  |
| `handleGet()`         | **FIT 改造** | 处理 GET 请求（SSE 连接）       |
| `handlePost()`        | **FIT 改造** | 处理 POST 请求（JSON-RPC 消息） |
| `handleDelete()`      | **FIT 改造** | 处理 DELETE 请求（会话删除）    |

### 重构后的辅助方法

为提高代码可读性和可维护性，从原本的 `handleGet()`、`handlePost()`、`handleDelete()` 方法中抽取了以下辅助方法：

#### 验证请求合法性的方法

| 方法名                           | 说明                                                      |
|-------------------------------|----------------------------------------------------------|
| `validateGetAcceptHeaders()`  | 验证 GET 请求的 Accept 头，确保包含 `text/event-stream`         |
| `validatePostAcceptHeaders()` | 验证 POST 请求的 Accept 头，确保包含 `text/event-stream` 和 `application/json` |
| `validateRequestSessionId()`  | 验证请求的 `mcp-session-id` 头是否存在，以及对应的会话是否存在            |

#### 根据请求类型调用处理逻辑的方法

| 方法名                             | 处理的请求类型 | 说明                                       |
|---------------------------------|---------|------------------------------------------|
| `handleReplaySseRequest()`      | GET     | 处理 SSE 消息重放请求，用于断线重连后恢复错过的消息             |
| `handleEstablishSseRequest()`   | GET     | 处理 SSE 连接建立请求，创建新的持久化 SSE 监听流            |
| `handleInitializeRequest()`     | POST    | 处理客户端初始化连接请求，创建新的 MCP 会话                 |
| `handleJsonRpcMessage()`        | POST    | 把非Initialize的客户端消息分流给下面三个方法，包含Session验证。 |
| `handleJsonRpcResponse()`       | POST    | 处理 JSON-RPC 响应消息（如 Elicitation 中的客户端响应）  |
| `handleJsonRpcNotification()`   | POST    | 处理 JSON-RPC 通知消息（客户端单向通知）                |
| `handleJsonRpcRequest()`        | POST    | 处理 JSON-RPC 请求消息，返回 SSE 流式响应             |

### 内部类

| 类名                                 | 来源         | 说明                          |
|------------------------------------|------------|-----------------------------|
| `FitStreamableMcpSessionTransport` | **FIT 改造** | 用于SSE 会话`sendMessage()`传输实现 |
| `Builder`                          | SDK 原始     | 构建器模式                       |

---

## SDK 原始逻辑

以下是从 MCP SDK 的 `HttpServletStreamableServerTransportProvider` 类保留的原始逻辑：

### 1. 会话管理核心逻辑

```java
private final Map<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();
```

- 使用 `ConcurrentHashMap` 存储活跃会话
- 会话以 `mcp-session-id` 作为键

### 2. 会话工厂设置

```java
public void setSessionFactory(McpStreamableServerSession.Factory sessionFactory) {
    this.sessionFactory = sessionFactory;
}
```

- 由外部设置会话工厂，用于创建新会话

### 3. 客户端通知

```java
public Mono<Void> notifyClients(String method, Object params) {
    // ... 广播逻辑
}
```

- 向所有活跃会话并行发送通知
- 使用 `parallelStream()` 提高效率
- 单个会话失败不影响其他会话

### 4. 关闭逻辑

```java
public Mono<Void> closeGracefully() {
    this.isClosing = true;
    // ... 关闭所有会话
    // ... 关闭 keep-alive 调度器
}
```

- 设置关闭标志
- 关闭所有活跃会话
- 清理资源

## FIT 框架改造核心逻辑

以下是为适配 FIT 框架而新增或改造的部分：

### 1. HTTP 端点处理核心流程（核心改造）

- 请求/响应对象类型变更：
  - `HttpServletRequest` → `HttpClassicServerRequest`
  - `HttpServletResponse` → `HttpClassicServerResponse`
- 返回类型改为通用的 `Object`，支持多种返回形式

#### a. GET 请求处理流程

1. 检查服务器是否正在关闭
2. **调用 `validateGetAcceptHeaders()`** - 验证 Accept 头是否包含 `text/event-stream`
3. **调用 `validateRequestSessionId()`** - 验证 `mcp-session-id` 头是否存在及对应会话是否存在
4. 提取 `transportContext` 上下文
5. 获取会话 ID 和会话对象
6. 检查是否是重放请求（`Last-Event-ID` 头）：
   - 如果是，**调用 `handleReplaySseRequest()`** - 重放错过的消息
   - 如果否，**调用 `handleEstablishSseRequest()`** - 建立新的 SSE 监听流

#### b. POST 请求处理流程

1. 检查服务器是否正在关闭
2. **调用 `validatePostAcceptHeaders()`** - 验证 Accept 头包含 `text/event-stream` 和 `application/json`
3. 提取 `transportContext` 上下文
4. 反序列化 JSON-RPC 消息
5. 判断是否为初始化请求（`initialize` 方法）：
   - 如果是，**调用 `handleInitializeRequest()`** - 创建新会话并返回初始化结果
6. **调用 `validateRequestSessionId()`** - 验证会话（仅非初始化请求）
7. 获取会话 ID 和会话对象
8. 根据消息类型分发处理：
   - `JSONRPCResponse` → **调用 `handleJsonRpcResponse()`**
   - `JSONRPCNotification` → **调用 `handleJsonRpcNotification()`**
   - `JSONRPCRequest` → **调用 `handleJsonRpcRequest()`**

#### c. DELETE 请求处理流程

1. 检查服务器是否正在关闭
2. 检查是否禁用 DELETE 操作
3. **调用 `validateRequestSessionId()`** - 验证 `mcp-session-id` 头及会话存在性
4. 提取 `transportContext` 上下文
5. 获取会话 ID 和会话对象
6. 删除会话并从会话映射表中移除

### 2. SSE 实现改造（核心改造）

**原始 SDK**:

```java
SseEmitter sseEmitter = new SseEmitter();
sseEmitter.send(SseEmitter.event()
    .id(messageId)
    .name("message")
    .data(jsonText));
sseEmitter.complete();
```

**FIT 框架改造**:

```java
// 使用 Choir 和 Emitter 实现 SSE
Choir.<TextEvent>create(emitter -> {
    // 创建sessionTransport类，用于调用emitter发送消息
    FitStreamableMcpSessionTransport sessionTransport =
            new FitStreamableMcpSessionTransport(sessionId, emitter, response);

    // session的逻辑是SDK原有的，里面会调用sessionTransport发送事件流
    session.responseStream(jsonrpcRequest, sessionTransport)
            .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
            .block();

    // 监听 Emitter 的生命周期
    emitter.observe(new Emitter.Observer<TextEvent>() {
            @Override
            public void onEmittedData(TextEvent data) {
                // 数据发送完成
            }
        
            @Override
            public void onCompleted() {
                // SSE 流正常结束
                listeningStream.close();
            }
        
            @Override
            public void onFailed(Exception cause) {
                // SSE 流异常结束
                listeningStream.close();
            }
    });
});
```

**关键变化**:

- 使用 `Choir<TextEvent>` 返回事件流
- 使用 `Emitter<TextEvent>` 替代 `SseEmitter` 的发送方法
- 使用 `Emitter.Observer` 监听 SSE 生命周期事件

### 3. HTTP 响应处理改造

**FIT 特有的响应方式**:

#### 返回纯文本

```java
response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
return Entity.createText(response, "Session ID required in mcp-session-id header");
```

#### 返回 JSON 对象

```java
response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
return Entity.createObject(response, McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
        .message("Session not found: "+sessionId)
        .build());
```

#### 返回 SSE 流（重要改造）

```java
return Choir.<TextEvent> create(emitter ->{
    // emitter封装在sessionTransport中，被session调用
    emitter.emit(textEvent);
});
```

### 4. HTTP 头处理改造

**FIT 框架的 Headers API**:

```java
// 获取 Header
String acceptHeaders = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
boolean hasSessionId = request.headers().contains(HttpHeaders.MCP_SESSION_ID);
String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");

// 设置 Header
response.headers().set("Content-Type",MimeType.APPLICATION_JSON.value());
response.headers().set(HttpHeaders.MCP_SESSION_ID, sessionId);

// 设置状态码
response.statusCode(HttpResponseStatus.OK.statusCode());
```

**变化**:

- 使用 `request.headers().first(name).orElse(default)` 获取单个 Header
- 使用 `request.headers().contains(name)` 检查 Header 是否存在
- 使用 FIT 的 `MessageHeaderNames` 和 `MimeType` 常量
- 使用 `HttpResponseStatus` 枚举设置状态码

### 5. 内部类 Transport 实现

`FitStreamableMcpSessionTransport` 类的核心职责是发送SSE事件：

- `sendmessage()`方法通过`Emitter<TextEvent>` 发送SSE消息到客户端
- 保存了当前会话的事件的`Emitter<TextEvent>`，负责close时关闭`Emitter<TextEvent>`

- SSE的`Emitter<TextEvent>`感知不到GET连接是否断开，因此在`sendmessage()`发送前检查GET连接是否活跃

```java
// 在发送消息前检查连接是否仍然活跃
if(!this.response.isActive()){
    logger.warn("[SSE] Connection inactive detected while sending message for session: {}",
        this.sessionId);
    this.close();
    return;
}
```

## 参考资源

### MCP 协议文档

- MCP 协议规范：[https://spec.modelcontextprotocol.io/](https://spec.modelcontextprotocol.io/)
- MCP SDK GitHub: [https://github.com/modelcontextprotocol/](https://github.com/modelcontextprotocol/)

### 更新记录

| 日期       | 更新内容                          | 负责人 |
|----------|---------------------------------|-----|
| 2025-11-04 | 初始版本，从 SDK 改造为 FIT 框架实现        | 黄可欣 |
| 2025-11-05 | 代码重构，提取9个辅助方法提高可读性和可维护性      | 黄可欣 |