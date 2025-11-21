# MCP Server 插件维护文档

## 文档概述

本文档用于记录 MCP Server 插件的设计、实现细节以及维护更新指南。该插件基于 MCP SDK 改造而来，用于在 FIT 框架中提供 MCP（Model Context Protocol）服务端的传输层实现。

**创建时间**: 2025-11-04

---

## 架构概览

### 核心组件关系

本插件提供了两种独立的 MCP 服务器实例，分别支持不同的传输协议：

1. **McpSseServer** - 基于 SSE 传输的服务器实例
2. **McpStreamableServer** - 基于 Streamable 传输的服务器实例

每个服务器实例由以下三个核心组件构成：

```
配置类 (McpSseServerConfig / McpStreamableServerConfig)
  │
  ├─> TransportProvider (传输层实现)
  │     ├─> FitMcpSseServerTransportProvider
  │     └─> FitMcpStreamableServerTransportProvider
  │
  ├─> McpSyncServer (MCP SDK 提供的同步服务器)
  │
  └─> FitMcpServer (服务器的Fit接口包装，实现工具注册和执行)
```

### FitMcpServer - Fit接口的MCP服务器

`FitMcpServer` 是连接 FIT 工具系统与 MCP SDK服务器的核心类，主要职责包括：

- **工具观察**: 实现 `ToolChangedObserver` 接口，监听工具的添加和移除
- **工具注册**: 将 FIT 工具转换为 MCP 工具规范并注册到 MCP 服务器
- **工具执行**: 处理来自 MCP 客户端的工具调用请求
- **生命周期管理**: 在服务销毁时自动注销观察者

每个 `FitMcpServer` 实例持有一个 `McpSyncServer`，通过配置类注入。两个独立的实例（SSE 和 Streamable）分别管理各自的工具列表和执行逻辑。

### FitMcpServerTransportProvider - 传输层基类

`FitMcpServerTransportProvider<S>` 是一个抽象基类，为 SSE 和 Streamable 两种传输方式提供通用功能：

**通用职责**:
- **会话管理**: 维护客户端会话的生命周期（创建、存储、销毁）
- **消息序列化**: 使用 `McpJsonMapper` 处理 JSON-RPC 消息的序列化和反序列化
- **上下文提取**: 从 HTTP 请求中提取传输上下文信息
- **Keep-Alive**: 支持可选的连接保活机制
- **优雅关闭**: 提供服务优雅关闭和资源清理

**成员变量**:
- `jsonMapper` - JSON 序列化器
- `contextExtractor` - 上下文提取器
- `keepAliveScheduler` - Keep-Alive 调度器
- `sessions` - 会话映射表 (ConcurrentHashMap)
- `isClosing` - 关闭标志

两种传输方式的具体实现继承此基类，泛型参数 `<S>` 指定会话类型。

---

## 传输层实现

### SSE 传输方式

`FitMcpSseServerTransportProvider` 基于 MCP SDK 的 `HttpServletSseServerTransportProvider` 改造，提供基本的 SSE 传输实现。

#### 端点配置

- **GET `/mcp/sse`**: 建立 SSE 连接，用于服务端向客户端推送消息
- **POST `/mcp/message`**: 接收客户端发送的 JSON-RPC 消息

#### 特点

- **会话类型**: 使用 `McpServerSession` 管理客户端会话
- **会话创建**: 在 GET 请求时创建会话，生成唯一的 session ID
- **协议版本**: 仅支持 `MCP_2024_11_05`
- **简洁设计**: 适合简单的服务端到客户端推送场景

#### 请求处理流程

**GET 请求**:
1. 检查服务器是否正在关闭
2. 验证 Accept 头是否包含 `text/event-stream`
3. 提取传输上下文
4. 生成会话 ID 并创建新会话
5. 建立 SSE 监听流，持续推送消息

**POST 请求**:
1. 检查服务器是否正在关闭
2. 验证 Accept 头包含 `application/json`
3. 验证 `mcp-session-id` 头及会话存在性
4. 提取传输上下文
5. 反序列化 JSON-RPC 消息并转发给会话处理

#### 内部实现

- **Transport 类**: `FitSseMcpSessionTransport`
- **职责**: 封装 SSE 消息发送逻辑，通过 `Emitter<TextEvent>` 发送消息

---

### Streamable 传输方式

`FitMcpStreamableServerTransportProvider` 基于 MCP SDK 的 `HttpServletStreamableServerTransportProvider` 改造，提供功能更丰富的传输实现。

#### 端点配置

- **GET `/mcp/streamable`**: 建立 SSE 连接或重放消息
- **POST `/mcp/streamable`**: 处理初始化请求和其他 JSON-RPC 消息
- **DELETE `/mcp/streamable`**: 删除指定会话

#### 特点

- **会话类型**: 使用 `McpStreamableServerSession` 管理客户端会话
- **会话创建**: 在 POST 初始化请求时创建会话
- **协议版本**: 支持 `MCP_2024_11_05`、`MCP_2025_03_26`、`MCP_2025_06_18`
- **消息重放**: 支持断线重连后恢复错过的消息（通过 `Last-Event-ID`）
- **会话管理**: 提供显式的会话删除机制
- **功能完整**: 适合需要完整会话管理的复杂场景

#### 请求处理流程

**GET 请求**:
1. 检查服务器是否正在关闭
2. 验证 Accept 头是否包含 `text/event-stream`
3. 验证 `mcp-session-id` 头及会话存在性
4. 提取传输上下文
5. 检查是否为重放请求（`Last-Event-ID` 头）：
   - **重放模式**: 重放错过的消息
   - **监听模式**: 建立新的 SSE 监听流

**POST 请求**:
1. 检查服务器是否正在关闭
2. 验证 Accept 头包含 `text/event-stream` 和 `application/json`
3. 提取传输上下文
4. 反序列化 JSON-RPC 消息
5. 判断消息类型：
   - **初始化请求**: 创建新会话并返回初始化结果
   - **其他消息**: 验证会话后分发处理（响应/通知/请求）

**DELETE 请求**:
1. 检查服务器是否正在关闭
2. 检查是否禁用 DELETE 操作
3. 验证 `mcp-session-id` 头及会话存在性
4. 提取传输上下文
5. 删除会话并清理资源

#### 辅助方法

为提高代码可读性，从请求处理方法中抽取了以下辅助方法：

**验证类**:
- `validateGetAcceptHeaders()` - 验证 GET 请求的 Accept 头
- `validatePostAcceptHeaders()` - 验证 POST 请求的 Accept 头
- `validateRequestSessionId()` - 验证会话 ID

**处理类**:
- `handleReplaySseRequest()` - 处理消息重放请求
- `handleEstablishSseRequest()` - 处理 SSE 连接建立
- `handleInitializeRequest()` - 处理初始化请求
- `handleJsonRpcMessage()` - 分流非初始化消息
- `handleJsonRpcResponse()` - 处理 JSON-RPC 响应
- `handleJsonRpcNotification()` - 处理 JSON-RPC 通知
- `handleJsonRpcRequest()` - 处理 JSON-RPC 请求

#### 内部实现

- **Transport 类**: `FitStreamableMcpSessionTransport`
- **职责**: 封装 SSE 消息发送逻辑，支持消息重放和连接状态检查

---

## 传输方式对比

### 功能对比表

| 特性 | SSE | Streamable |
|------|-----|------------|
| **端点路径** | GET `/mcp/sse`<br>POST `/mcp/message` | GET/POST/DELETE `/mcp/streamable` |
| **支持的协议版本** | `MCP_2024_11_05` | `MCP_2024_11_05`<br>`MCP_2025_03_26`<br>`MCP_2025_06_18` |
| **会话类型** | `McpServerSession` | `McpStreamableServerSession` |
| **会话创建时机** | GET 请求时 | POST 初始化请求时 |
| **消息重放** | ❌ 不支持 | ✅ 支持 (通过 `Last-Event-ID`) |
| **显式会话删除** | ❌ 无 DELETE 端点 | ✅ 支持 DELETE 请求 |
| **Keep-Alive** | ✅ 支持 | ✅ 支持 |
| **代码复杂度** | 较低 | 较高 |
| **适用场景** | 简单的单向推送 | 复杂的双向通信和会话管理 |

### 选择建议

**使用 SSE 方式**，当你需要：
- 简单的服务端到客户端消息推送
- 最小化的会话管理开销
- 单一协议版本支持

**使用 Streamable 方式**，当你需要：
- 完整的会话生命周期管理
- 断线重连后的消息重放功能
- 支持多个 MCP 协议版本
- 显式的会话清理机制

---

## SDK 改造说明

以下是将 MCP SDK 适配到 FIT 框架的通用改造点，两种传输方式均涉及这些改造（详细实现可参考各自的 TransportProvider 类）。

### 1. HTTP 请求/响应对象

**SDK 原始**:
```java
HttpServletRequest request
HttpServletResponse response
```

**FIT 改造**:
```java
HttpClassicServerRequest request
HttpClassicServerResponse response
```

**HTTP 头操作**:
```java
// 获取 Header
String accept = request.headers().first(MessageHeaderNames.ACCEPT).orElse("");
String sessionId = request.headers().first(HttpHeaders.MCP_SESSION_ID).orElse("");
boolean hasSessionId = request.headers().contains(HttpHeaders.MCP_SESSION_ID);

// 设置 Header
response.headers().set("Content-Type", MimeType.APPLICATION_JSON.value());
response.headers().set(HttpHeaders.MCP_SESSION_ID, sessionId);

// 设置状态码
response.statusCode(HttpResponseStatus.OK.statusCode());
```

### 2. SSE 事件流实现

**SDK 原始**:
```java
SseEmitter sseEmitter = new SseEmitter();
sseEmitter.send(SseEmitter.event()
    .id(messageId)
    .name("message")
    .data(jsonText));
sseEmitter.complete();
```

**FIT 改造**:
```java
return Choir.<TextEvent>create(emitter -> {
    // 创建 Transport 封装 emitter
    FitStreamableMcpSessionTransport sessionTransport =
            new FitStreamableMcpSessionTransport(sessionId, emitter, response);

    // 调用 SDK 的 session 逻辑发送消息
    session.responseStream(jsonrpcRequest, sessionTransport)
            .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
            .block();

    // 监听生命周期
    emitter.observe(new Emitter.Observer<TextEvent>() {
        @Override
        public void onEmittedData(TextEvent data) { }

        @Override
        public void onCompleted() {
            listeningStream.close();
        }

        @Override
        public void onFailed(Exception cause) {
            listeningStream.close();
        }
    });
});
```

**关键变化**:
- 使用 `Choir<TextEvent>` 替代 `SseEmitter`
- 使用 `Emitter<TextEvent>` 发送事件
- 使用 `Emitter.Observer` 监听生命周期

### 3. HTTP 响应创建

**返回纯文本**:
```java
response.statusCode(HttpResponseStatus.BAD_REQUEST.statusCode());
return Entity.createText(response, "Session ID required");
```

**返回 JSON 对象**:
```java
response.statusCode(HttpResponseStatus.NOT_FOUND.statusCode());
return Entity.createObject(response, McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
        .message("Session not found: " + sessionId)
        .build());
```

**返回 SSE 流**:
```java
return Choir.<TextEvent>create(emitter -> {
    emitter.emit(textEvent);
});
```

### 4. Transport 实现类

两种传输方式都实现了内部 Transport 类，封装 SSE 消息发送逻辑：

**核心职责**:
- 通过 `Emitter<TextEvent>` 发送 SSE 消息
- 在 `close()` 时关闭 Emitter
- 发送前检查连接是否活跃

**连接检查**:
```java
@Override
public void sendMessage(JSONRPCMessage message) {
    // 检查连接是否仍然活跃
    if (!this.response.isActive()) {
        logger.warn("[SSE] Connection inactive, session: {}", this.sessionId);
        this.close();
        return;
    }

    // 发送消息
    String messageJson = jsonMapper.writeValueAsString(message);
    Event event = new Event(messageId, "message", messageJson);
    this.emitter.emit(new TextEvent(event.toString()));
}
```

---

## SDK 保留逻辑

以下是从 MCP SDK 保留的核心逻辑，两种传输方式共享。

### 1. 会话存储

```java
private final Map<String, S> sessions = new ConcurrentHashMap<>();
```

- 使用线程安全的 `ConcurrentHashMap` 存储会话
- 键为 `mcp-session-id`，值为会话对象

### 2. 会话工厂

```java
public void setSessionFactory(S.Factory sessionFactory) {
    this.sessionFactory = sessionFactory;
}
```

- 由外部（MCP SDK）设置会话工厂
- 用于创建新会话实例

### 3. 客户端通知

```java
public Mono<Void> notifyClients(String method, Object params) {
    // 并行向所有活跃会话发送通知
    sessions.values().parallelStream()
            .forEach(session -> session.sendNotification(method, params));
}
```

- 向所有活跃会话并行发送通知
- 单个会话失败不影响其他会话

### 4. 优雅关闭

```java
public Mono<Void> closeGracefully() {
    this.isClosing = true;
    // 关闭所有会话
    // 关闭 keep-alive 调度器
    // 清理资源
}
```

- 设置关闭标志，拒绝新请求
- 关闭所有活跃会话
- 清理调度器和其他资源

---

## 配置说明

### SSE 配置类 (McpSseServerConfig)

创建三个组件：
1. `FitMcpSseServerTransportProvider` - 传输层
2. `McpSyncSseServer` - MCP 同步服务器
3. `McpSseServer` - FIT 工具服务器

### Streamable 配置类 (McpStreamableServerConfig)

创建三个组件：
1. `FitMcpStreamableServerTransportProvider` - 传输层
2. `McpSyncStreamableServer` - MCP 同步服务器
3. `McpStreamableServer` - FIT 工具服务器

### 配置参数

- `mcp.server.ping.interval-seconds` - Keep-Alive 间隔（秒）
- `mcp.server.request.timeout-seconds` - 请求超时时间（秒）
- `mcp.server.streamable.disallow-delete` - 是否禁用 DELETE 请求（仅 Streamable）

---

## 参考资源

### MCP 协议文档

- MCP 协议规范：[https://spec.modelcontextprotocol.io/](https://spec.modelcontextprotocol.io/)
- MCP SDK GitHub: [https://github.com/modelcontextprotocol/](https://github.com/modelcontextprotocol/)

### 更新记录

| 日期       | 更新内容                          | 负责人 |
|----------|---------------------------------|-----|
| 2025-11-04 | 初始版本，从 SDK 改造为 FIT 框架实现        | 黄可欣 |
| 2025-11-05 | 代码重构，提取辅助方法提高可读性和可维护性      | 黄可欣 |
| 2025-11-21 | 文档重构，调整结构使其与代码保持一致，简化技术术语 | 黄可欣 |
