# HTTP 客户端认证使用手册

本文档详细介绍如何使用 `@RequestAuth` 注解为 HTTP 客户端添加认证功能。

## 目录

1. [快速开始](#快速开始)
2. [认证类型](#认证类型)
3. [使用场景](#使用场景)
4. [最佳实践](#最佳实践)
5. [常见问题](#常见问题)

---

## 快速开始

### 基本用法

在 HTTP 客户端接口的类、方法或参数上使用 `@RequestAuth` 注解：

```java
@HttpProxy
@RequestAuth(type = AuthType.BEARER, value = "my-token")
public interface MyClient {
    @GetMapping("/api/users")
    List<User> getUsers();
}
```

### 注解位置

`@RequestAuth` 可以应用在三个层级：

1. **接口级别**：所有方法继承此认证
2. **方法级别**：覆盖接口级别的认证
3. **参数级别**：动态更新认证信息

---

## 认证类型

### 1. Bearer Token 认证

#### 静态配置

```java
@HttpProxy
public interface UserClient {
    @GetMapping("/api/users")
    @RequestAuth(type = AuthType.BEARER, value = "static-token-12345")
    List<User> getUsers();
}
```

**生成的 HTTP 请求：**
```
GET /api/users HTTP/1.1
Authorization: Bearer static-token-12345
```

#### 参数驱动

```java
@HttpProxy
public interface UserClient {
    @GetMapping("/api/users")
    List<User> getUsers(@RequestAuth(type = AuthType.BEARER) String token);
}
```

**调用示例：**
```java
userClient.getUsers("dynamic-token-67890");
```

**生成的 HTTP 请求：**
```
GET /api/users HTTP/1.1
Authorization: Bearer dynamic-token-67890
```

---

### 2. Basic 认证

#### 静态配置

```java
@HttpProxy
public interface AdminClient {
    @GetMapping("/admin/settings")
    @RequestAuth(type = AuthType.BASIC, username = "admin", password = "secret123")
    Settings getSettings();
}
```

**生成的 HTTP 请求：**
```
GET /admin/settings HTTP/1.1
Authorization: Basic YWRtaW46c2VjcmV0MTIz
```
（YWRtaW46c2VjcmV0MTIz 是 "admin:secret123" 的 Base64 编码）

#### 参数级别 - 单字段更新

```java
@HttpProxy
public interface AdminClient {
    @GetMapping("/admin/users")
    @RequestAuth(type = AuthType.BASIC, username = "default-user", password = "default-pass")
    List<User> getUsers(@RequestAuth(type = AuthType.BASIC) String username);
}
```

**说明：**
- 方法级别提供完整的 username + password
- 参数级别覆盖 username 字段（默认行为）

**调用示例：**
```java
adminClient.getUsers("john");  // 最终: john:default-pass
```

#### 参数级别 - 双字段更新

```java
@HttpProxy
public interface AdminClient {
    @GetMapping("/admin/login")
    @RequestAuth(type = AuthType.BASIC, username = "base-user", password = "base-pass")
    LoginResult login(
        @RequestAuth(type = AuthType.BASIC, name = "username") String user,
        @RequestAuth(type = AuthType.BASIC, name = "password") String pass
    );
}
```

**说明：**
- 使用 `name` 属性明确指定要更新的字段
- `name = "username"` 更新 username 字段
- `name = "password"` 更新 password 字段

**调用示例：**
```java
adminClient.login("john", "secret");  // 最终: john:secret
```

**重要提示：**
- 方法级别的 BASIC 认证必须提供完整的 username 和 password
- 参数级别只能更新已存在的认证对象的字段
- 如果不指定 `name` 属性，默认更新 `username` 字段

---

### 3. API Key 认证

#### Header 中的 API Key

```java
@HttpProxy
public interface SearchClient {
    @GetMapping("/search")
    @RequestAuth(type = AuthType.API_KEY, name = "X-API-Key", value = "my-api-key-123")
    SearchResult search(@RequestQuery("q") String query);
}
```

**生成的 HTTP 请求：**
```
GET /search?q=hello HTTP/1.1
X-API-Key: my-api-key-123
```

#### Query 参数中的 API Key

```java
@HttpProxy
public interface SearchClient {
    @GetMapping("/search")
    @RequestAuth(
        type = AuthType.API_KEY,
        name = "api_key",
        value = "my-key-456",
        location = Source.QUERY
    )
    SearchResult search(@RequestQuery("q") String query);
}
```

**生成的 HTTP 请求：**
```
GET /search?q=hello&api_key=my-key-456 HTTP/1.1
```

#### 参数驱动的 API Key

```java
@HttpProxy
public interface SearchClient {
    @GetMapping("/search")
    SearchResult search(
        @RequestQuery("q") String query,
        @RequestAuth(type = AuthType.API_KEY, name = "X-API-Key") String apiKey
    );
}
```

**调用示例：**
```java
searchClient.search("hello", "user-specific-key");
```

**生成的 HTTP 请求：**
```
GET /search?q=hello HTTP/1.1
X-API-Key: user-specific-key
```

---

### 4. 自定义认证（Provider）

#### 定义 Provider

```java
@Component
public class DynamicTokenProvider implements AuthProvider {
    @Override
    public Authorization provide() {
        // 动态获取 token（如从缓存、数据库等）
        String token = loadTokenFromCache();
        return Authorization.createBearer(token);
    }
}
```

#### 使用 Provider

```java
@HttpProxy
public interface UserClient {
    @GetMapping("/api/users")
    @RequestAuth(type = AuthType.BEARER, provider = DynamicTokenProvider.class)
    List<User> getUsers();
}
```

**优势：**
- Token 在每次请求时动态获取
- 支持 Token 刷新、轮换等复杂场景
- Provider 可以依赖注入其他 Bean

#### 自定义签名 Provider

```java
@Component
public class CustomSignatureProvider implements AuthProvider {
    @Override
    public Authorization provide() {
        long timestamp = System.currentTimeMillis();
        String signature = calculateSignature(timestamp);

        return Authorization.createCustom(headers -> {
            headers.put("X-Timestamp", String.valueOf(timestamp));
            headers.put("X-Signature", signature);
            headers.put("X-App-Id", "my-app");
        });
    }
}
```

---

## 使用场景

### 场景 1：接口级别全局认证

**需求：** 所有 API 都使用相同的 API Key

```java
@HttpProxy
@RequestAddress(host = "api.example.com")
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-key-123")
public interface ServiceClient {
    @GetMapping("/api/users")
    List<User> getUsers();

    @GetMapping("/api/orders")
    List<Order> getOrders();

    // 所有方法自动携带 X-Service-Key: service-key-123
}
```

---

### 场景 2：方法级别覆盖

**需求：** 大部分 API 用 API Key，个别 API 需要 Bearer Token

```java
@HttpProxy
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-key")
public interface ServiceClient {
    @GetMapping("/api/users")
    List<User> getUsers();  // 使用接口级别的 API Key

    @GetMapping("/api/admin")
    @RequestAuth(type = AuthType.BEARER, value = "admin-token")
    AdminData getAdminData();  // 覆盖为 Bearer Token
}
```

---

### 场景 3：组合认证

**需求：** 服务级 API Key + 用户级 Bearer Token

```java
@HttpProxy
@RequestAuth(type = AuthType.API_KEY, name = "X-Service-Key", value = "service-key")
public interface UserClient {
    @GetMapping("/api/user/profile")
    @RequestAuth(type = AuthType.BEARER, provider = UserTokenProvider.class)
    UserProfile getProfile();
}
```

**生成的 HTTP 请求：**
```
GET /api/user/profile HTTP/1.1
X-Service-Key: service-key
Authorization: Bearer user-token-from-provider
```

**说明：**
- 接口级别的认证不会被覆盖，而是叠加
- 最终请求同时包含两种认证信息

---

### 场景 4：多租户场景

**需求：** 不同租户使用不同的 API Key

```java
@HttpProxy
public interface TenantClient {
    @GetMapping("/api/data")
    Data getData(@RequestAuth(type = AuthType.API_KEY, name = "X-Tenant-Key") String tenantKey);
}
```

**调用示例：**
```java
// 租户 A
Data dataA = tenantClient.getData("tenant-a-key");

// 租户 B
Data dataB = tenantClient.getData("tenant-b-key");
```

---

### 场景 5：动态用户认证

**需求：** 根据当前登录用户动态设置认证

```java
@HttpProxy
public interface UserClient {
    @GetMapping("/api/profile")
    UserProfile getProfile(@RequestAuth(type = AuthType.BEARER) String userToken);
}
```

**业务代码：**
```java
@Service
public class UserService {
    @Autowired
    private UserClient userClient;

    public UserProfile getCurrentUserProfile() {
        String token = SecurityContext.getCurrentToken();
        return userClient.getProfile(token);
    }
}
```

---

## 最佳实践

### 1. 敏感信息管理

**❌ 不推荐：硬编码**
```java
@RequestAuth(type = AuthType.BEARER, value = "hardcoded-token-123")
```

**✅ 推荐：使用配置**
```java
@Component
public class ConfigurableTokenProvider implements AuthProvider {
    @Value("${api.token}")
    private String token;

    @Override
    public Authorization provide() {
        return Authorization.createBearer(token);
    }
}

@RequestAuth(type = AuthType.BEARER, provider = ConfigurableTokenProvider.class)
```

---

### 2. 认证层级选择

| 场景 | 推荐层级 | 原因 |
|------|---------|------|
| 所有方法使用相同认证 | 接口级别 | 避免重复配置 |
| 少数方法需要特殊认证 | 方法级别覆盖 | 灵活性 |
| 每次请求认证信息不同 | 参数级别 | 动态性 |
| 认证信息需要刷新/计算 | Provider | 复用和维护性 |

---

### 3. Provider 最佳实践

**单例 Provider（推荐）**
```java
@Component
public class TokenProvider implements AuthProvider {
    // 注入依赖
    @Autowired
    private TokenService tokenService;

    @Override
    public Authorization provide() {
        return Authorization.createBearer(tokenService.getToken());
    }
}
```

**优点：**
- 可以依赖注入其他 Bean
- 生命周期由 Spring 管理
- 支持缓存和复用

---

### 4. 参数级别认证注意事项

**BASIC 认证必须提供基础值：**

```java
// ✅ 正确：方法级别提供完整认证
@RequestAuth(type = AuthType.BASIC, username = "base", password = "pass")
UserData login(@RequestAuth(type = AuthType.BASIC) String username);

// ❌ 错误：方法级别缺少 password
@RequestAuth(type = AuthType.BASIC, username = "base")  // 缺少 password
UserData login(@RequestAuth(type = AuthType.BASIC) String username);
```

**API_KEY 的 name 属性语义：**

```java
// BASIC: name 指定要更新的字段
@RequestAuth(type = BASIC, name = "username")  // 更新 username 字段

// API_KEY: name 指定 HTTP Header/Query 名称
@RequestAuth(type = API_KEY, name = "X-API-Key")  // HTTP Header 名称
```

---

### 5. 错误处理

**Provider 中的错误处理：**

```java
@Component
public class ResilientTokenProvider implements AuthProvider {
    @Override
    public Authorization provide() {
        try {
            String token = fetchTokenFromRemote();
            return Authorization.createBearer(token);
        } catch (Exception e) {
            // 降级到本地缓存的 token
            String cachedToken = loadFromCache();
            if (cachedToken != null) {
                return Authorization.createBearer(cachedToken);
            }
            throw new AuthenticationException("Failed to obtain token", e);
        }
    }
}
```

---

## 常见问题

### Q1: 如何在运行时切换认证方式？

**A:** 使用 Provider 动态决定认证类型

```java
@Component
public class DynamicAuthProvider implements AuthProvider {
    @Value("${auth.type}")
    private String authType;

    @Override
    public Authorization provide() {
        if ("bearer".equals(authType)) {
            return Authorization.createBearer(getToken());
        } else if ("basic".equals(authType)) {
            return Authorization.createBasic(getUsername(), getPassword());
        }
        throw new IllegalStateException("Unknown auth type: " + authType);
    }
}
```

---

### Q2: 参数级别的认证会覆盖方法级别的认证吗？

**A:** 不会完全覆盖，而是**更新字段**

```java
@RequestAuth(type = BASIC, username = "admin", password = "pass123")
void login(@RequestAuth(type = BASIC) String username);

// 调用: login("john")
// 结果: username=john, password=pass123 (password 保持不变)
```

---

### Q3: 可以同时使用多个 @RequestAuth 注解吗？

**A:** 可以，使用 `@Repeatable` 支持

```java
@HttpProxy
@RequestAuth(type = API_KEY, name = "X-Service-Key", value = "service-key")
@RequestAuth(type = BEARER, provider = UserTokenProvider.class)
public interface MultiAuthClient {
    // 同时发送两种认证信息
}
```

---

### Q4: Provider 的生命周期是什么？

**A:** Provider 通过 `BeanContainer` 获取，遵循 Spring Bean 的生命周期

- **单例模式**（推荐）：Provider 只创建一次，可以维护状态（如缓存）
- **原型模式**：每次请求创建新实例（需要配置 `@Scope("prototype")`）

---

### Q5: 如何测试使用了认证的客户端？

**方法 1：Mock Provider**

```java
@SpringBootTest
class UserClientTest {
    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private UserClient userClient;

    @Test
    void testGetUsers() {
        when(tokenProvider.provide())
            .thenReturn(Authorization.createBearer("test-token"));

        List<User> users = userClient.getUsers();
        assertNotNull(users);
    }
}
```

**方法 2：使用测试配置**

```java
@TestConfiguration
public class TestAuthConfig {
    @Bean
    @Primary
    public TokenProvider testTokenProvider() {
        return () -> Authorization.createBearer("test-token");
    }
}
```

---

### Q6: BASIC 认证的 name 属性为什么和 API_KEY 不同？

**A:** `name` 属性语义重载，根据认证类型有不同含义：

| 认证类型 | name 属性含义 | 示例 |
|---------|-------------|------|
| BASIC | Authorization 对象字段名 | `name="username"` 或 `name="password"` |
| API_KEY | HTTP Header/Query 名称 | `name="X-API-Key"` |
| BEARER | 无效（被忽略） | - |

**设计原因：**
- BASIC：参数级别需要指定更新 username 还是 password
- API_KEY：需要指定 HTTP 中的 Key 名称

---

## 示例代码

完整的示例代码请参考：
- **Example 07**: `examples/fit-example/07-http-client-proxy`
- **测试用例**: `TestAuthClient.java`
- **Provider 示例**: `DynamicTokenProvider.java`, `CustomSignatureProvider.java`

---

## 相关文档

- [HTTP 客户端认证原理手册](./HTTP_CLIENT_AUTH_PRINCIPLES.md)
- [Authorization 对象详解](./HTTP_CLIENT_AUTH_AUTHORIZATION.md)
- [FIT HTTP 客户端代理文档](./README.md)
