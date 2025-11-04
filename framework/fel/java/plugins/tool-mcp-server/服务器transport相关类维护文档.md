# FitMcpStreamableServerTransportProvider类维护文档

## 文档概述

本文档用于记录 `FitMcpStreamableServerTransportProvider` 类的设计、实现细节以及维护更新指南。该类是基于 MCP SDK 中的 `WebMvcStreamableServerTransportProvider` 类改造而来，用于在 FIT 框架中提供 MCP（Model Context Protocol）服务端的传输层实现。

**原始参考类**: MCP SDK 中的 `WebMvcStreamableServerTransportProvider` (或 `HttpServletStreamableServerTransportProvider`)

**作者**: 黄可欣 
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

| 变量名 | 类型 | 来源 | 说明 |
|--------|------|------|------|
| `MESSAGE_ENDPOINT` | `String` | SDK 原始 | 消息端点路径 `/mcp/streamable` |
| `disallowDelete` | `boolean` | SDK 原始 | 是否禁用 DELETE 请求 |
| `jsonMapper` | `McpJsonMapper` | SDK 原始 | JSON 序列化器 |
| `contextExtractor` | `McpTransportContextExtractor<HttpClassicServerRequest>` | **FIT 改造** | 上下文提取器（泛型参数改为 FIT 的 Request 类型） |
| `keepAliveScheduler` | `KeepAliveScheduler` | SDK 原始 | Keep-Alive 调度器 |
| `sessionFactory` | `McpStreamableServerSession.Factory` | SDK 原始 | 会话工厂 |
| `sessions` | `Map<String, McpStreamableServerSession>` | SDK 原始 | 活跃会话映射表 |
| `isClosing` | `volatile boolean` | SDK 原始 | 关闭标志 |

### 主要方法

| 方法名 | 来源 | 说明 |
|--------|------|------|
| `protocolVersions()` | SDK 原始 | 返回支持的 MCP 协议版本 |
| `setSessionFactory()` | SDK 原始 | 设置会话工厂 |
| `notifyClients()` | SDK 原始 | 广播通知到所有客户端 |
| `closeGracefully()` | SDK 原始 | 优雅关闭传输层 |
| `handleGet()` | **FIT 改造** | 处理 GET 请求（SSE 连接） |
| `handlePost()` | **FIT 改造** | 处理 POST 请求（JSON-RPC 消息） |
| `handleDelete()` | **FIT 改造** | 处理 DELETE 请求（会话删除） |
| `deserializeJsonRpcMessage()` | **FIT 创建** | 反序列化 JSON-RPC 消息 |

### 内部类

| 类名 | 来源 | 说明 |
|------|------|------|
| `FitStreamableMcpSessionTransport` | **FIT 改造** | 用于SSE 会话`sendMessage()`传输实现 |
| `Builder` | SDK 原始 | 构建器模式 |

---

## SDK 原始逻辑

以下是从 MCP SDK 的 `WebMvcStreamableServerTransportProvider` 类保留的原始逻辑：

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

### 4. HTTP 端点处理核心流程

#### a. GET 请求处理流程（原始逻辑）

1. 检查 Accept 头是否包含 `text/event-stream`
2. 验证 `mcp-session-id` 头是否存在
3. 查找对应的会话
4. 检查是否是重放请求（`Last-Event-ID` 头）
5. 建立 SSE 连接或重放消息

#### b. POST 请求处理流程（原始逻辑）

1. 检查 Accept 头
2. 反序列化 JSON-RPC 消息
3. 特殊处理 `initialize` 请求（创建新会话）
4. 处理其他请求（需要已存在的会话）
5. 根据消息类型（Response/Notification/Request）分别处理

#### c. DELETE 请求处理流程（原始逻辑）

1. 检查是否禁用 DELETE
2. 验证 `mcp-session-id` 头
3. 查找并删除会话

### 5. 关闭逻辑
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

### 6. Keep-Alive 机制
```java
if (keepAliveInterval != null) {
    this.keepAliveScheduler = KeepAliveScheduler.builder(...)
        .initialDelay(keepAliveInterval)
        .interval(keepAliveInterval)
        .build();
    this.keepAliveScheduler.start();
}
```
- 支持可选的 Keep-Alive 调度



## FIT 框架新增/改造逻辑

以下是为适配 FIT 框架而新增或改造的部分：

### 1. HTTP 类替换（重要改造）

**原始 SDK（Spring MVC）**:

```java
@GetMapping("/mcp/streamable")
public ResponseEntity<SseEmitter> handleGet(HttpServletRequest request, HttpServletResponse response)

@PostMapping("/mcp/streamable")
public ResponseEntity<?> handlePost(HttpServletRequest request, @RequestBody Map<String, Object> body)

@DeleteMapping("/mcp/streamable")
public ResponseEntity<Void> handleDelete(HttpServletRequest request)
```

**FIT 框架改造后**:
```java
@GetMapping(path = MESSAGE_ENDPOINT)
public Object handleGet(HttpClassicServerRequest request, HttpClassicServerResponse response)

@PostMapping(path = MESSAGE_ENDPOINT)
public Object handlePost(HttpClassicServerRequest request, HttpClassicServerResponse response,
        @RequestBody Map<String, Object> requestBody)

@DeleteMapping(path = MESSAGE_ENDPOINT)
public Object handleDelete(HttpClassicServerRequest request, HttpClassicServerResponse response)
```

**关键变化**:
- 使用 FIT 的注解：`@GetMapping`, `@PostMapping`, `@DeleteMapping`
- 请求/响应对象类型变更：
  - `HttpServletRequest` → `HttpClassicServerRequest`
  - `HttpServletResponse` → `HttpClassicServerResponse`
- 返回类型改为通用的 `Object`，支持多种返回形式

### 2. SSE 实现改造（核心改造）

**原始 SDK (Spring MVC)**:
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
return Entity.createObject(response,
    McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
        .message("Session not found: " + sessionId)
        .build());
```

#### 返回 SSE 流（重要改造）

```java
return Choir.<TextEvent>create(emitter -> {
    // 使用 FIT 的 Emitter 发送 SSE 事件
    emitter.emit(textEvent);
    emitter.complete();
    emitter.fail(exception);
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
response.headers().set("Content-Type", MimeType.APPLICATION_JSON.value());
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
if (!this.response.isActive()) {
    logger.warn("[SSE] Connection inactive detected while sending message for session: {}",
            this.sessionId);
    this.close();
    return;
}
```

### 6. JSON-RPC 消息反序列化

```java
public McpSchema.JSONRPCMessage deserializeJsonRpcMessage(Map<String, Object> map) {
    // 根据字段判断消息类型
    if (map.containsKey("method") && map.containsKey("id")) {
        return jsonMapper.convertValue(map, McpSchema.JSONRPCRequest.class);
    } else if (map.containsKey("method") && !map.containsKey("id")) {
        return jsonMapper.convertValue(map, McpSchema.JSONRPCNotification.class);
    } else if (map.containsKey("result") || map.containsKey("error")) {
        return jsonMapper.convertValue(map, McpSchema.JSONRPCResponse.class);
    }
    throw new IllegalArgumentException(...);
}
```

- 智能识别 JSON-RPC 消息类型



## 代码结构对照表

| 功能模块 | 改造程度 | SDK 原始实现 | FIT 框架实现 |
|---------|---------|-------------|-------------|
| SSE 实现 | **重大改造** | `SseEmitter` | `Choir<TextEvent>` + `Emitter` |
| HTTP 请求对象 | **重大改造** | `HttpServletRequest` | `HttpClassicServerRequest` |
| HTTP 响应对象 | **重大改造** | `HttpServletResponse` | `HttpClassicServerResponse` |
| HTTP返回类型 | **重大改造** | `ResponseEntity<?>` | `Object` (`Entity`或者`Choir`) |
| Get连接检测 | 新增 | 无 | `response.isActive()` |
| 验证工具 | 新增 | 无或其他 | FIT Validation |
| 日志系统 | 轻微改造 | SLF4J | FIT Logger |
| Builder 模式 | 轻微改造 | 原始逻辑 | 类型参数调整 |
| HTTP 注解 | 无变化 | `@GetMapping` (Spring) | `@GetMapping` (FIT) |
| 接口实现 | 无变化 | `McpStreamableServerTransportProvider` | 相同 |
| 会话管理 | 无变化 | 原始逻辑 | 相同 |
| 消息序列化 | 无变化 | 原始逻辑 | 相同 |
| Keep-Alive | 无变化 | 原始逻辑 | 相同 |



## 参考资源

### MCP 协议文档
- MCP 协议规范：[https://spec.modelcontextprotocol.io/](https://spec.modelcontextprotocol.io/)
- MCP SDK GitHub: [https://github.com/modelcontextprotocol/](https://github.com/modelcontextprotocol/)

### FIT 框架文档
- FIT HTTP 模块文档：`docs/framework/fit/java/user-guide-book/04. Web MVC 能力.md`
- FIT 流式功能文档：`docs/framework/fit/java/user-guide-book/10. 流式功能.md`
- FIT 日志文档：`docs/framework/fit/java/user-guide-book/08. 日志.md`

### 相关类文档
- `Event` 枚举定义：`modelengine.fel.tool.mcp.entity.Event`
- MCP Server 工具其他实现：`framework/fel/java/plugins/tool-mcp-server/`

---

## 附录：快速定位指南

### 查找某个功能的实现位置

| 功能 | 方法/类 | 行号范围 |
|------|--------|---------|
| 协议版本声明 | `protocolVersions()` | 112-116 |
| 客户端广播 | `notifyClients()` | 133-150 |
| 优雅关闭 | `closeGracefully()` | 158-178 |
| GET 请求处理 | `handleGet()` | 188-289 |
| POST 请求处理 | `handlePost()` | 300-430 |
| DELETE 请求处理 | `handleDelete()` | 440-481 |
| 消息反序列化 | `deserializeJsonRpcMessage()` | 490-500 |
| SSE 传输实现 | `FitStreamableMcpSessionTransport` | 511-644 |
| 构建器 | `Builder` | 653-729 |

### 查找某个 FIT 改造点

| 改造内容 | 位置 |
|---------|------|
| HTTP 注解 | 187, 299, 439 行 |
| Entity 响应 | 191, 197, 212, 304, 311, 等 |
| Choir SSE | 221, 383 行 |
| Emitter 观察者 | 256-281, 385-400 行 |
| 连接状态检测 | 570-575 行 |
| FIT Logger | 55, 全文多处 |
| FIT Validation | 92-93, 668, 696, 722 行 |



