# HTTP Client Authentication Usage Guide

本文档演示了 fit-framework HTTP 客户端代理系统中各种身份认证方式的使用方法。

## 1. 概述

`@RequestAuth` 注解提供了统一的身份认证解决方案，支持多种认证类型和应用级别：

### 认证类型 (AuthType)
- **BEARER**: Bearer Token 认证
- **BASIC**: HTTP Basic 认证
- **API_KEY**: API Key 认证（支持 Header、Query、Cookie）
- **CUSTOM**: 自定义认证（通过 Provider）

### 应用级别
- **接口级别**: 应用于整个接口的所有方法
- **方法级别**: 应用于特定方法（会覆盖接口级别）
- **参数级别**: 通过方法参数动态设置（最高优先级）

## 2. 静态认证配置

### 2.1 Bearer Token 认证

```java
// 接口级别静态配置
@RequestAuth(type = AuthType.BEARER, value = "your-static-token")
public interface YourClient {

    // 方法级别覆盖
    @RequestAuth(type = AuthType.BEARER, value = "method-specific-token")
    String someMethod();
}
```

### 2.2 Basic 认证

```java
@RequestAuth(type = AuthType.BASIC, username = "admin", password = "secret")
String basicAuthMethod();
```

### 2.3 API Key 认证

```java
// Header 中的 API Key
@RequestAuth(type = AuthType.API_KEY, name = "X-API-Key", value = "your-api-key")
String headerApiKeyMethod();

// Query 参数中的 API Key
@RequestAuth(type = AuthType.API_KEY, name = "api_key", value = "your-key", location = Source.QUERY)
String queryApiKeyMethod();
```

## 3. 动态认证配置

### 3.1 参数驱动的认证

```java
// 动态 Bearer Token
String dynamicBearer(@RequestAuth(type = AuthType.BEARER) String token);

// 动态 API Key
String dynamicApiKey(@RequestAuth(type = AuthType.API_KEY, name = "X-Dynamic-Key") String apiKey);
```

### 3.2 Provider 模式

#### 创建 Provider

```java
@Component
public class DynamicTokenProvider implements AuthProvider {
    @Override
    public Authorization provide() {
        // 从 TokenManager、缓存或其他来源获取 token
        String token = TokenManager.getCurrentToken();
        return Authorization.createBearer(token);
    }
}
```

#### 使用 Provider

```java
@RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
String providerBasedMethod();
```

## 4. 组合认证

可以在不同级别同时应用多种认证：

```java
@HttpProxy
@RequestAddress(protocol = "http", host = "localhost", port = "8080")
// 接口级别：默认 API Key
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-key")
public interface CombinedAuthClient {

    // 方法级别：添加 Bearer Token（会与接口级别的 API Key 共存）
    @RequestAuth(type = AuthType.BEARER, provider = TokenProvider.class)
    String combinedAuth(
        // 参数级别：用户上下文 API Key
        @RequestAuth(type = AuthType.API_KEY, name = "X-User-Context") String userToken
    );
}
```

## 5. 完整示例

### TestAuthClient 接口

```java
@HttpProxy
@RequestAddress(protocol = "http", host = "localhost", port = "8080")
@RequestMapping(path = "/http-server/auth")
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-default-key")
public interface TestAuthClient {

    // 1. 静态 Bearer Token
    @GetMapping(path = "/bearer-static")
    @RequestAuth(type = AuthType.BEARER, value = "static-bearer-token-12345")
    String testBearerStatic();

    // 2. 动态 Bearer Token
    @GetMapping(path = "/bearer-dynamic")
    String testBearerDynamic(@RequestAuth(type = AuthType.BEARER) String token);

    // 3. Basic 认证
    @GetMapping(path = "/basic-static")
    @RequestAuth(type = AuthType.BASIC, username = "admin", password = "secret123")
    String testBasicStatic();

    // 4. Header API Key
    @GetMapping(path = "/apikey-header-static")
    @RequestAuth(type = AuthType.API_KEY, name = "X-API-Key", value = "static-api-key-67890")
    String testApiKeyHeaderStatic();

    // 5. Query API Key
    @GetMapping(path = "/apikey-query-static")
    @RequestAuth(type = AuthType.API_KEY, name = "api_key", value = "query-api-key-111", location = Source.QUERY)
    String testApiKeyQueryStatic();

    // 6. 动态 API Key
    @GetMapping(path = "/apikey-dynamic")
    String testApiKeyDynamic(@RequestAuth(type = AuthType.API_KEY, name = "X-Dynamic-Key") String apiKey);

    // 7. Provider 模式
    @GetMapping(path = "/dynamic-provider")
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    String testDynamicProvider();

    // 8. 自定义认证
    @GetMapping(path = "/custom-provider")
    @RequestAuth(type = AuthType.CUSTOM, provider = CustomSignatureProvider.class)
    String testCustomProvider();

    // 9. 组合认证
    @GetMapping(path = "/combined-auth")
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    String testCombinedAuth(@RequestAuth(type = AuthType.API_KEY, name = "X-User-Context") String userToken);
}
```

## 6. 注意事项

1. **优先级**: 参数级别 > 方法级别 > 接口级别
2. **Provider**: 需要标记为 `@Component` 并在容器中可用
3. **组合认证**: 不同级别的认证会叠加，相同级别的认证会覆盖
4. **安全性**: 避免在代码中硬编码敏感信息，优先使用 Provider 模式

## 7. 快速启动和测试

### 启动应用

本示例基于 FIT 框架，启动方式如下：

```bash
# 1. 编译整个项目（在 fit-framework 根目录）
mvn clean install

# 2. 启动服务器端
# 方式一：在 IDEA 中运行 plugin-http-server 模块的 main 方法
# 方式二：命令行运行 JAR 文件（编译后在 target 目录）
java -jar plugin-http-server/target/plugin-http-server-*.jar
```

### 验证启动成功

查看日志中是否包含以下信息：

```
[INFO] [main] [modelengine.fitframework.runtime.aggregated.AggregatedFitRuntime] FIT application started.
[INFO] [netty-http-server-thread-0] [modelengine.fit.http.server.netty.NettyHttpClassicServer] Start netty http server successfully. [httpPort=8080]
```

### 快速测试

```bash
# 测试基本连接
curl http://localhost:8080/http-server/auth/bearer-static \
  -H "Authorization: Bearer static-bearer-token-12345" \
  -H "X-Service-Key: service-default-key"

# 期望响应：Bearer Static Auth: Bearer static-bearer-token-12345
```

## 8. 下一步

- 查看 [CURL_TEST_EXAMPLES.md](./CURL_TEST_EXAMPLES.md) 了解如何测试这些认证场景
- 查看 [run_tests.sh](./run_tests.sh) 了解如何批量执行测试