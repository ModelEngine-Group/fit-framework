# HTTP 客户端认证原理手册

本文档深入解析 HTTP 客户端认证功能的设计原理和实现细节。

## 目录

1. [架构概览](#架构概览)
2. [核心组件](#核心组件)
3. [工作流程](#工作流程)
4. [关键设计决策](#关键设计决策)
5. [与 FEL Tool 系统的一致性](#与-fel-tool-系统的一致性)
6. [扩展指南](#扩展指南)

---

## 架构概览

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                       @RequestAuth 注解                      │
│              (接口/方法/参数级别)                              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│                   AnnotationParser                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ parseInterface() / parseMethod() / parseParam()       │  │
│  └──────────────┬───────────────────────┬─────────────────┘  │
│                 │                       │                    │
│          静态认证解析             参数级别认证解析              │
│                 │                       │                    │
│                 ↓                       ↓                    │
│      ┌────────────────────┐   ┌──────────────────────┐     │
│      │StaticAuthApplier   │   │RequestAuthResolver   │     │
│      │+ BeanContainer     │   │+ AuthFieldMapper     │     │
│      └────────┬───────────┘   └──────────┬───────────┘     │
│               │                           │                 │
└───────────────┼───────────────────────────┼─────────────────┘
                │                           │
                ↓                           ↓
┌───────────────────────────────────────────────────────────┐
│                HttpInvocationHandler                       │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ 1. 应用静态认证 (staticAppliers)                      │ │
│  │ 2. 应用参数级别认证 (paramAppliers)                   │ │
│  └─────────────────────────────────────────────────────┘ │
└────────────────────┬──────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│                    RequestBuilder                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ authorization(Authorization)  // 设置 Authorization  │   │
│  │ authorizationInfo(key, value) // 更新字段            │   │
│  └─────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│                    Authorization                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ - BearerAuthorization                                │   │
│  │ - BasicAuthorization                                 │   │
│  │ - ApiKeyAuthorization                                │   │
│  │ - CustomAuthorization                                │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 核心组件

### 1. `@RequestAuth` 注解

**位置**: `modelengine.fit.http.annotation.RequestAuth`

**核心属性**:

```java
public @interface RequestAuth {
    AuthType type();              // 认证类型
    String value();               // 通用值（Bearer token / API key value）
    String name();                // 名称（语义重载）
    Source location();            // API Key 位置（HEADER/QUERY/COOKIE）
    String username();            // BASIC 认证用户名
    String password();            // BASIC 认证密码
    Class<? extends AuthProvider> provider();  // 动态 Provider
}
```

**`name` 属性的语义重载**:

| 认证类型 | `name` 含义 | 示例 |
|---------|-----------|------|
| BASIC | Authorization 字段名 | `"username"` 或 `"password"` |
| API_KEY | HTTP Header/Query 名称 | `"X-API-Key"` |
| BEARER | 无效（被忽略） | - |

**设计原因**:
- 复用现有属性，避免新增注解字段
- 语义在不同场景下自然不同，符合直觉

---

### 2. `AnnotationParser`

**位置**: `modelengine.fit.http.client.proxy.scanner.AnnotationParser`

**职责**: 解析接口、方法和参数上的注解，生成 `PropertyValueApplier`

**关键字段**:

```java
private final ValueFetcher valueFetcher;
private final BeanContainer beanContainer;  // 用于获取 AuthProvider
```

**核心方法**:

#### `parseInterface(Class<?> clazz)`

解析接口级别的注解，生成 `HttpInfo` 列表（每个方法一个）。

**流程**:
1. 解析类级别的 `@RequestAuth` → `getClassLevelAuthAppliers()`
2. 遍历方法：
   - 解析方法级别的 `@RequestAuth` → `getMethodLevelAuthAppliers()`
   - 合并类级别和方法级别的 appliers
   - 解析参数级别的注解 → `parseParam()`

#### `getClassLevelAuthAppliers(Class<?> clazz)`

```java
private List<PropertyValueApplier> getClassLevelAuthAppliers(Class<?> clazz) {
    List<PropertyValueApplier> appliers = new ArrayList<>();
    RequestAuth[] authAnnotations = clazz.getAnnotationsByType(RequestAuth.class);
    for (RequestAuth auth : authAnnotations) {
        // 关键：传递 beanContainer 给 StaticAuthApplier
        appliers.add(new StaticAuthApplier(auth, this.beanContainer));
    }
    return appliers;
}
```

**关键点**:
- `getAnnotationsByType()` 支持 `@Repeatable`，可以有多个 `@RequestAuth`
- 直接在构造时传递 `beanContainer`，避免后续运行时注入

#### `getMethodLevelAuthAppliers(Method method)`

与类级别类似，解析方法上的 `@RequestAuth`。

#### `parseParam(Parameter parameter)`

解析参数上的注解，生成 `PropertyValueApplier`。

**针对 `@RequestAuth` 的处理**:

```java
if (parameter.isAnnotationPresent(RequestAuth.class)) {
    RequestAuth auth = parameter.getAnnotation(RequestAuth.class);
    // 使用 RequestAuthResolver 处理
    DestinationSetterInfo setterInfo =
        new RequestAuthResolver().resolve(auth, jsonPath);
    // 创建 MultiDestinationsPropertyValueApplier
    return new MultiDestinationsPropertyValueApplier(...);
}
```

---

### 3. `StaticAuthApplier`

**位置**: `modelengine.fit.http.client.proxy.support.applier.StaticAuthApplier`

**职责**: 处理静态认证（类级别和方法级别的 `@RequestAuth`）

**核心实现**:

```java
public class StaticAuthApplier implements PropertyValueApplier {
    private final Authorization authorization;

    public StaticAuthApplier(RequestAuth authAnnotation, BeanContainer beanContainer) {
        notNull(beanContainer, "The bean container cannot be null.");
        // 构造时立即创建 Authorization
        this.authorization = this.createAuthorizationFromAnnotation(
            authAnnotation, beanContainer);
    }

    @Override
    public void apply(RequestBuilder requestBuilder, Object value) {
        // 静态认证不需要参数值
        requestBuilder.authorization(this.authorization);
    }
}
```

**关键设计**:
1. **构造函数注入**: 接受 `BeanContainer` 参数，由 `AnnotationParser` 传递
2. **立即创建**: Authorization 在构造时创建，不是延迟初始化
3. **不可变性**: `authorization` 字段为 `final`，线程安全
4. **Fail-fast**: 使用 `notNull()` 在入口处验证参数

#### `createAuthorizationFromAnnotation()` 方法

```java
private Authorization createAuthorizationFromAnnotation(
        RequestAuth annotation, BeanContainer beanContainer) {

    // 如果使用 Provider
    if (annotation.provider() != AuthProvider.class) {
        AuthProvider provider = beanContainer.beans().get(annotation.provider());
        if (provider == null) {
            throw new IllegalStateException(
                "AuthProvider not found: " + annotation.provider().getName());
        }
        return provider.provide();
    }

    // 根据类型创建静态 Authorization
    AuthType type = annotation.type();
    switch (type) {
        case BEARER:
            return Authorization.createBearer(annotation.value());
        case BASIC:
            return Authorization.createBasic(
                annotation.username(), annotation.password());
        case API_KEY:
            return Authorization.createApiKey(
                annotation.name(), annotation.value(), annotation.location());
        case CUSTOM:
            throw new IllegalArgumentException("CUSTOM requires provider");
    }
}
```

**Provider 处理**:
- 通过 `beanContainer.beans().get()` 获取 Provider 实例
- Provider 必须是 Spring Bean（通过 `@Component` 等注册）
- Provider 的 `provide()` 方法在构造时调用一次

---

### 4. `RequestAuthResolver`

**位置**: `modelengine.fit.http.client.proxy.scanner.resolver.RequestAuthResolver`

**职责**: 解析参数级别的 `@RequestAuth`，生成 `DestinationSetterInfo`

**核心实现**:

```java
public class RequestAuthResolver implements ParamResolver<RequestAuth> {
    @Override
    public DestinationSetterInfo resolve(RequestAuth annotation, String jsonPath) {
        // 使用 AuthFieldMapper 获取字段名
        String authField = AuthFieldMapper.getParameterAuthField(
            annotation.type(), annotation.name());

        // 创建 AuthorizationDestinationSetter
        return new DestinationSetterInfo(
            new AuthorizationDestinationSetter(authField), jsonPath);
    }
}
```

**关键点**:
- 复用 `AuthorizationDestinationSetter`，与 FEL Tool 系统一致
- 通过 `AuthFieldMapper` 确定要更新的字段

---

### 5. `AuthFieldMapper`

**位置**: `modelengine.fit.http.client.proxy.scanner.resolver.AuthFieldMapper`

**职责**: 确定参数级别认证应该更新 `Authorization` 对象的哪个字段

**核心方法**:

```java
public static String getParameterAuthField(AuthType type, String nameAttribute) {
    return switch (type) {
        case BEARER -> "token";  // BearerAuthorization.token

        case BASIC -> {
            // 使用 name 属性指定字段
            if (StringUtils.isNotBlank(nameAttribute)) {
                if (StringUtils.equals("username", nameAttribute)) {
                    yield "username";
                } else if (StringUtils.equals("password", nameAttribute)) {
                    yield "password";
                } else {
                    throw new IllegalArgumentException(
                        "For BASIC auth, name must be 'username' or 'password'");
                }
            }
            // 默认返回 username（向后兼容）
            yield "username";
        }

        case API_KEY -> "value";  // ApiKeyAuthorization.value

        case CUSTOM -> throw new IllegalArgumentException(
            "CUSTOM auth requires AuthProvider");
    };
}
```

**字段映射表**:

| AuthType | Authorization 类 | 可更新字段 | 字段含义 |
|----------|-----------------|-----------|---------|
| BEARER | BearerAuthorization | `token` | Bearer Token 值 |
| BASIC | BasicAuthorization | `username`<br>`password` | 用户名<br>密码 |
| API_KEY | ApiKeyAuthorization | `value` | API Key 值 |

**重要说明**:

**API_KEY 的两个概念**:
1. **key 字段**: HTTP Header/Query 的名称（如 "X-API-Key"）
   - 来自注解的 `name` 属性
   - 在静态认证时设置
2. **value 字段**: 实际的 API Key 值
   - 来自参数传入
   - 参数级别更新此字段

**示例**:
```java
@RequestAuth(type = API_KEY, name = "X-API-Key", value = "static-key")
void api1();

void api2(@RequestAuth(type = API_KEY, name = "X-API-Key") String key);
```

- `api1`: `ApiKeyAuthorization.key = "X-API-Key"`, `value = "static-key"`
- `api2("dynamic-key")`: `ApiKeyAuthorization.key = "X-API-Key"`, `value = "dynamic-key"`

---

### 6. `AuthorizationDestinationSetter`

**位置**: `modelengine.fit.http.client.proxy.support.setter.AuthorizationDestinationSetter`

**职责**: 更新 `Authorization` 对象的指定字段

**核心实现**:

```java
public class AuthorizationDestinationSetter extends AbstractDestinationSetter {
    public AuthorizationDestinationSetter(String key) {
        super(key);  // key 是字段名，如 "token", "username", "password", "value"
    }

    @Override
    public void set(RequestBuilder requestBuilder, Object value) {
        // 调用 requestBuilder.authorizationInfo() 更新字段
        requestBuilder.authorizationInfo(this.key(), value);
    }
}
```

**RequestBuilder 的处理**:

```java
public interface RequestBuilder {
    // 设置完整的 Authorization 对象（静态认证）
    RequestBuilder authorization(Authorization authorization);

    // 更新 Authorization 对象的字段（参数级别认证）
    RequestBuilder authorizationInfo(String key, Object value);
}
```

**DefaultRequestBuilder 实现**:

```java
@Override
public RequestBuilder authorizationInfo(String key, Object value) {
    if (this.authorization == null) {
        throw new IllegalStateException("Authorization not set");
    }
    // 调用 Authorization.setValue() 更新字段
    this.authorization.setValue(key, value);
    return this;
}
```

---

### 7. `Authorization` 对象

**位置**: `modelengine.fit.http.client.proxy.Authorization`

**职责**: 封装认证信息，提供统一的更新接口

**核心方法**:

```java
public abstract class Authorization {
    // 更新字段（供参数级别认证使用）
    public abstract void setValue(String key, Object value);

    // 应用到 HTTP 请求（生成 HTTP Header）
    public abstract void apply(HttpClassicClientRequest request);
}
```

#### `BearerAuthorization`

```java
public class BearerAuthorization extends Authorization {
    private String token;

    @Override
    public void setValue(String key, Object value) {
        if ("token".equals(key)) {
            this.token = (String) value;
        }
    }

    @Override
    public void apply(HttpClassicClientRequest request) {
        request.setHeader("Authorization", "Bearer " + this.token);
    }
}
```

#### `BasicAuthorization`

```java
public class BasicAuthorization extends Authorization {
    private String username;
    private String password;

    @Override
    public void setValue(String key, Object value) {
        if ("username".equals(key)) {
            this.username = (String) value;
        } else if ("password".equals(key)) {
            this.password = (String) value;
        }
    }

    @Override
    public void apply(HttpClassicClientRequest request) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(
            credentials.getBytes(StandardCharsets.UTF_8));
        request.setHeader("Authorization", "Basic " + encoded);
    }
}
```

#### `ApiKeyAuthorization`

```java
public class ApiKeyAuthorization extends Authorization {
    private String key;    // HTTP Header/Query 名称
    private String value;  // API Key 值
    private Source location;

    @Override
    public void setValue(String fieldName, Object fieldValue) {
        if ("key".equals(fieldName)) {
            this.key = (String) fieldValue;
        } else if ("value".equals(fieldName)) {
            this.value = (String) fieldValue;
        }
    }

    @Override
    public void apply(HttpClassicClientRequest request) {
        switch (location) {
            case HEADER:
                request.setHeader(key, value);
                break;
            case QUERY:
                request.addQueryParameter(key, value);
                break;
            case COOKIE:
                request.setCookie(key, value);
                break;
        }
    }
}
```

---

### 8. `HttpInvocationHandler`

**位置**: `modelengine.fit.http.client.proxy.scanner.HttpInvocationHandler`

**职责**: 拦截接口方法调用，构建 HTTP 请求并执行

**认证处理流程**:

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) {
    // 1. 获取 HttpInfo
    HttpInfo httpInfo = this.httpInfoMap.get(method);
    List<PropertyValueApplier> staticAppliers = httpInfo.getStaticAppliers();
    List<PropertyValueApplier> paramAppliers = httpInfo.getParamAppliers();

    // 2. 创建 RequestBuilder
    RequestBuilder requestBuilder = new DefaultRequestBuilder()
        .client(this.client)
        .method(httpInfo.getMethod());

    // 3. 应用静态认证（包括类级别和方法级别）
    for (PropertyValueApplier staticApplier : staticAppliers) {
        staticApplier.apply(requestBuilder, null);
    }

    // 4. 应用参数级别认证
    for (int i = 0; i < paramAppliers.size(); i++) {
        paramAppliers.get(i).apply(requestBuilder, args[i]);
    }

    // 5. 构建并执行请求
    HttpClassicClientRequest request = requestBuilder.build();
    return request.execute();
}
```

**关键点**:
1. **静态优先**: 先应用静态认证（创建 Authorization 对象）
2. **参数更新**: 再应用参数级别认证（更新 Authorization 字段）
3. **简洁设计**: 不再需要 `instanceof` 检查或运行时注入

---

## 工作流程

### 流程 1: 静态认证

**场景**: 方法级别使用静态 Bearer Token

```java
@GetMapping("/api/users")
@RequestAuth(type = AuthType.BEARER, value = "my-token")
List<User> getUsers();
```

**执行流程**:

```
1. AnnotationParser.parseMethod()
   └─> 发现 @RequestAuth 注解
   └─> 调用 getMethodLevelAuthAppliers()
       └─> 创建 StaticAuthApplier(authAnnotation, beanContainer)
           └─> createAuthorizationFromAnnotation()
               └─> Authorization.createBearer("my-token")
                   └─> 返回 BearerAuthorization(token="my-token")

2. 存储到 HttpInfo.staticAppliers

3. HttpInvocationHandler.invoke()
   └─> 应用 staticAppliers
       └─> StaticAuthApplier.apply(requestBuilder, null)
           └─> requestBuilder.authorization(bearerAuth)
               └─> DefaultRequestBuilder 存储 authorization 对象

4. requestBuilder.build()
   └─> authorization.apply(request)
       └─> BearerAuthorization.apply()
           └─> request.setHeader("Authorization", "Bearer my-token")

5. 发送 HTTP 请求
   GET /api/users HTTP/1.1
   Authorization: Bearer my-token
```

---

### 流程 2: 参数级别认证

**场景**: 参数驱动的 Bearer Token

```java
@GetMapping("/api/users")
List<User> getUsers(@RequestAuth(type = AuthType.BEARER) String token);
```

**执行流程**:

```
1. AnnotationParser.parseParam(parameter)
   └─> 发现 @RequestAuth 注解
   └─> 调用 RequestAuthResolver.resolve()
       └─> AuthFieldMapper.getParameterAuthField(BEARER, null)
           └─> 返回 "token"
       └─> 创建 AuthorizationDestinationSetter("token")
       └─> 返回 DestinationSetterInfo

2. 创建 MultiDestinationsPropertyValueApplier
   └─> 存储到 HttpInfo.paramAppliers

3. HttpInvocationHandler.invoke(proxy, method, ["user-token-123"])
   └─> 应用 paramAppliers
       └─> MultiDestinationsPropertyValueApplier.apply(requestBuilder, "user-token-123")
           └─> AuthorizationDestinationSetter.set(requestBuilder, "user-token-123")
               └─> requestBuilder.authorizationInfo("token", "user-token-123")
                   └─> authorization.setValue("token", "user-token-123")
                       └─> BearerAuthorization.token = "user-token-123"

4. requestBuilder.build()
   └─> authorization.apply(request)
       └─> request.setHeader("Authorization", "Bearer user-token-123")

5. 发送 HTTP 请求
   GET /api/users HTTP/1.1
   Authorization: Bearer user-token-123
```

---

### 流程 3: BASIC 认证双参数更新

**场景**: 参数分别更新 username 和 password

```java
@RequestAuth(type = BASIC, username = "base-user", password = "base-pass")
void login(
    @RequestAuth(type = BASIC, name = "username") String user,
    @RequestAuth(type = BASIC, name = "password") String pass
);
```

**执行流程**:

```
1. AnnotationParser 阶段

   方法级别:
   └─> getMethodLevelAuthAppliers()
       └─> StaticAuthApplier(auth, beanContainer)
           └─> Authorization.createBasic("base-user", "base-pass")
               └─> BasicAuthorization(username="base-user", password="base-pass")

   参数 0:
   └─> parseParam(parameter[0])  // @RequestAuth(type=BASIC, name="username")
       └─> RequestAuthResolver.resolve()
           └─> AuthFieldMapper.getParameterAuthField(BASIC, "username")
               └─> 返回 "username"
           └─> AuthorizationDestinationSetter("username")

   参数 1:
   └─> parseParam(parameter[1])  // @RequestAuth(type=BASIC, name="password")
       └─> RequestAuthResolver.resolve()
           └─> AuthFieldMapper.getParameterAuthField(BASIC, "password")
               └─> 返回 "password"
           └─> AuthorizationDestinationSetter("password")

2. HttpInvocationHandler.invoke(proxy, method, ["john", "secret"])

   应用静态认证:
   └─> StaticAuthApplier.apply(requestBuilder, null)
       └─> requestBuilder.authorization(basicAuth)
           └─> BasicAuthorization(username="base-user", password="base-pass")

   应用参数 0:
   └─> paramAppliers[0].apply(requestBuilder, "john")
       └─> requestBuilder.authorizationInfo("username", "john")
           └─> basicAuth.setValue("username", "john")
               └─> BasicAuthorization.username = "john"

   应用参数 1:
   └─> paramAppliers[1].apply(requestBuilder, "secret")
       └─> requestBuilder.authorizationInfo("password", "secret")
           └─> basicAuth.setValue("password", "secret")
               └─> BasicAuthorization.password = "secret"

3. 构建请求
   └─> authorization.apply(request)
       └─> credentials = "john:secret"
       └─> encoded = Base64.encode("john:secret") = "am9objpzZWNyZXQ="
       └─> request.setHeader("Authorization", "Basic am9objpzZWNyZXQ=")

4. 发送 HTTP 请求
   POST /login HTTP/1.1
   Authorization: Basic am9objpzZWNyZXQ=
```

---

### 流程 4: Provider 动态认证

**场景**: 使用 Provider 动态获取 Token

```java
@GetMapping("/api/users")
@RequestAuth(type = BEARER, provider = DynamicTokenProvider.class)
List<User> getUsers();
```

**执行流程**:

```
1. 应用启动时
   └─> Spring 容器扫描
       └─> 发现 @Component DynamicTokenProvider
           └─> 创建实例并注册到 BeanContainer

2. AnnotationParser.parseMethod()
   └─> getMethodLevelAuthAppliers()
       └─> StaticAuthApplier(authAnnotation, beanContainer)
           └─> createAuthorizationFromAnnotation()
               └─> 检测到 provider != AuthProvider.class
               └─> beanContainer.beans().get(DynamicTokenProvider.class)
                   └─> 获取 Provider 实例
               └─> provider.provide()
                   └─> DynamicTokenProvider.provide()
                       └─> token = loadTokenFromCache()
                       └─> return Authorization.createBearer(token)

3. 后续每次请求
   └─> StaticAuthApplier.apply() 使用已创建的 Authorization
       └─> 注意：Token 在启动时获取一次，不会每次请求都刷新

4. 如果需要每次请求刷新 Token
   └─> 需要在 Provider 中实现 ThreadLocal 或请求作用域机制
   └─> 或者使用参数级别认证动态传入
```

**重要说明**:
- Provider 的 `provide()` 方法在 `StaticAuthApplier` 构造时调用**一次**
- 如果需要每次请求动态获取，Provider 内部需要实现缓存刷新机制
- 或者考虑使用参数级别认证

---

## 关键设计决策

### 决策 1: 构造函数注入 vs Setter 注入

**原方案（已废弃）**: Setter 注入

```java
// 原来的设计
public class StaticAuthApplier {
    private Authorization cachedAuthorization;

    public StaticAuthApplier(RequestAuth authAnnotation) {
        // 如果不使用 Provider，提前创建
        if (authAnnotation.provider() == AuthProvider.class) {
            this.cachedAuthorization = createAuth(authAnnotation, null);
        }
    }

    public void setBeanContainer(BeanContainer beanContainer) {
        // 延迟创建（如果使用了 Provider）
        if (this.cachedAuthorization == null) {
            this.cachedAuthorization = createAuth(authAnnotation, beanContainer);
        }
    }
}
```

**问题**:
1. 需要保存 `authAnnotation` 字段用于延迟创建
2. `cachedAuthorization` 可能为 null，需要运行时检查
3. `HttpInvocationHandler` 需要 `instanceof` 检查并调用 `setBeanContainer()`
4. 延迟初始化增加复杂度

**新方案**: 构造函数注入

```java
public class StaticAuthApplier {
    private final Authorization authorization;

    public StaticAuthApplier(RequestAuth authAnnotation, BeanContainer beanContainer) {
        notNull(beanContainer, "The bean container cannot be null.");
        this.authorization = createAuth(authAnnotation, beanContainer);
    }
}
```

**优势**:
1. ✅ 立即创建，不需要保存 `authAnnotation`
2. ✅ `authorization` 为 `final`，不可变，线程安全
3. ✅ 不需要 null 检查
4. ✅ `HttpInvocationHandler` 代码简化
5. ✅ Fail-fast：错误在构造时暴露

**权衡**:
- `AnnotationParser` 已经有 `beanContainer`，直接传递更自然

---

### 决策 2: name 属性语义重载

**问题**: BASIC 认证需要指定更新 username 还是 password，但不想新增注解字段

**方案比较**:

| 方案 | 示例 | 优点 | 缺点 |
|------|------|------|------|
| 1. 新增 `authField` 属性 | `@RequestAuth(type=BASIC, authField="username")` | 语义明确 | 增加注解复杂度 |
| 2. 从注解属性推断 | `@RequestAuth(type=BASIC, username="xxx")` 有值则更新 username | 无需新字段 | 逻辑复杂，不够灵活 |
| 3. 复用 `name` 属性 | `@RequestAuth(type=BASIC, name="username")` | 复用现有字段 | 语义重载 |

**选择方案 3**: 复用 `name` 属性

**理由**:
1. `name` 在不同场景下含义本来就不同（API_KEY 的 Header 名 vs BASIC 的字段名）
2. 符合直觉：`name="username"` → 更新 username
3. 不增加注解复杂度
4. 通过文档清晰说明语义

---

### 决策 3: 参数级别认证是覆盖还是更新？

**问题**: 参数级别的认证应该完全替换静态认证，还是只更新部分字段？

**选择**: **更新字段**

**理由**:
1. **复用 Authorization 对象**: 静态认证已创建对象，参数只需更新字段
2. **与 FEL Tool 一致**: Tool 系统也是通过 `authorizationInfo(key, value)` 更新字段
3. **支持部分更新**: 如 BASIC 认证可以只更新 username，保留 password
4. **性能更好**: 不需要重新创建对象

**示例**:
```java
@RequestAuth(type = BASIC, username = "admin", password = "pass123")
void login(@RequestAuth(type = BASIC) String username);

// 调用 login("john")
// 结果: BasicAuthorization(username="john", password="pass123")
```

**限制**:
- 方法级别必须提供完整的认证信息（如 BASIC 必须有 username + password）
- 参数只能更新已存在的字段

---

### 决策 4: AuthFieldMapper 为什么是静态工具类？

**问题**: 是否需要将 `AuthFieldMapper` 设计为可扩展的接口？

**选择**: **静态工具类**

**理由**:
1. **固定映射**: 字段映射是由 `Authorization` 实现类决定的，不应该可变
2. **简单性**: 不需要实例化，直接调用静态方法
3. **性能**: 避免创建额外对象
4. **一致性**: 与 FEL Tool 的设计保持一致

**扩展性**:
- 如果需要自定义 Authorization 实现，应该通过 `CUSTOM` 类型和 `AuthProvider` 实现
- 不应该通过修改 `AuthFieldMapper` 的映射逻辑

---

## 与 FEL Tool 系统的一致性

### 统一的底层机制

**FEL Tool JSON 配置**:

```json
{
  "mappings": {
    "people": {
      "name": {
        "key": "token",
        "httpSource": "AUTHORIZATION"
      }
    }
  }
}
```

**注解方式**:

```java
String api(@RequestAuth(type = BEARER) String token);
```

**底层执行**:

两种方式最终都调用：
```java
requestBuilder.authorizationInfo("token", value);
↓
authorization.setValue("token", value);
```

### 复用 AuthorizationDestinationSetter

**原来的设计（已废弃）**: 使用单独的 `AuthDestinationSetter`

**问题**:
- 与 FEL Tool 系统的 `AuthorizationDestinationSetter` 功能重复
- 维护两套相同逻辑的代码

**重构后**: 复用 `AuthorizationDestinationSetter`

**优势**:
- ✅ 代码复用，减少维护成本
- ✅ 架构一致性，降低理解成本
- ✅ 确保行为一致

---

## 扩展指南

### 扩展 1: 添加新的认证类型

**步骤**:

1. **定义新的 AuthType**

```java
public enum AuthType {
    BEARER,
    BASIC,
    API_KEY,
    CUSTOM,
    OAUTH2  // 新增
}
```

2. **创建 Authorization 实现**

```java
public class OAuth2Authorization extends Authorization {
    private String accessToken;
    private String refreshToken;

    @Override
    public void setValue(String key, Object value) {
        switch (key) {
            case "accessToken":
                this.accessToken = (String) value;
                break;
            case "refreshToken":
                this.refreshToken = (String) value;
                break;
        }
    }

    @Override
    public void apply(HttpClassicClientRequest request) {
        request.setHeader("Authorization", "Bearer " + accessToken);
        // 其他 OAuth2 特定逻辑
    }
}
```

3. **更新 StaticAuthApplier**

```java
private Authorization createAuthorizationFromAnnotation(...) {
    switch (type) {
        // 现有类型...
        case OAUTH2:
            return Authorization.createOAuth2(
                annotation.value(),      // accessToken
                annotation.password()    // refreshToken (复用字段)
            );
    }
}
```

4. **更新 AuthFieldMapper**

```java
public static String getParameterAuthField(AuthType type, String nameAttribute) {
    return switch (type) {
        // 现有类型...
        case OAUTH2 -> StringUtils.isNotBlank(nameAttribute)
            ? nameAttribute  // 支持指定字段
            : "accessToken";  // 默认
    };
}
```

5. **添加 Authorization 工厂方法**

```java
public abstract class Authorization {
    public static Authorization createOAuth2(String accessToken, String refreshToken) {
        return new OAuth2Authorization(accessToken, refreshToken);
    }
}
```

---

### 扩展 2: 自定义 Provider

**场景**: 实现带刷新机制的 Token Provider

```java
@Component
public class RefreshableTokenProvider implements AuthProvider {
    private String cachedToken;
    private long expirationTime;

    @Autowired
    private TokenService tokenService;

    @Override
    public Authorization provide() {
        // 检查 Token 是否过期
        if (cachedToken == null || System.currentTimeMillis() > expirationTime) {
            refreshToken();
        }
        return Authorization.createBearer(cachedToken);
    }

    private void refreshToken() {
        TokenResponse response = tokenService.getToken();
        this.cachedToken = response.getToken();
        this.expirationTime = System.currentTimeMillis() + response.getExpiresIn() * 1000;
    }
}
```

**注意**: Provider 的 `provide()` 方法在 `StaticAuthApplier` 构造时调用一次，因此：
- 缓存和刷新逻辑需要在 Provider 内部实现
- 或者使用参数级别认证每次动态传入

---

### 扩展 3: 请求级别的动态 Token

**场景**: 每次 HTTP 请求时动态获取最新的 Token

**方案**: 使用参数级别认证

```java
@Service
public class UserService {
    @Autowired
    private UserClient userClient;

    @Autowired
    private SecurityContext securityContext;

    public UserProfile getProfile() {
        // 每次请求时获取当前用户的 Token
        String token = securityContext.getCurrentUserToken();
        return userClient.getProfile(token);
    }
}
```

**客户端接口**:

```java
@HttpProxy
public interface UserClient {
    @GetMapping("/api/profile")
    UserProfile getProfile(@RequestAuth(type = BEARER) String token);
}
```

---

### 扩展 4: 多认证策略

**场景**: 根据环境（开发/生产）使用不同的认证方式

```java
@Component
public class EnvironmentAwareAuthProvider implements AuthProvider {
    @Value("${app.env}")
    private String environment;

    @Value("${api.dev.token}")
    private String devToken;

    @Value("${api.prod.username}")
    private String prodUsername;

    @Value("${api.prod.password}")
    private String prodPassword;

    @Override
    public Authorization provide() {
        if ("development".equals(environment)) {
            return Authorization.createBearer(devToken);
        } else {
            return Authorization.createBasic(prodUsername, prodPassword);
        }
    }
}
```

---

## 总结

### 核心特性

1. **三级配置**: 接口级别、方法级别、参数级别
2. **多种认证类型**: Bearer、Basic、API Key、Custom
3. **动态 Provider**: 支持运行时动态获取认证信息
4. **字段更新机制**: 参数级别更新 Authorization 字段
5. **架构一致性**: 与 FEL Tool 系统复用底层机制

### 设计原则

1. **构造函数注入**: 依赖在构造时注入，立即创建，Fail-fast
2. **不可变性**: Authorization 对象在创建后为 final，线程安全
3. **复用机制**: 复用 AuthorizationDestinationSetter，避免重复代码
4. **语义重载**: name 属性在不同类型下有不同含义，减少注解复杂度
5. **静态优先**: 先应用静态认证，再应用参数级别认证

### 关键组件职责

| 组件 | 职责 |
|------|------|
| `@RequestAuth` | 声明认证信息 |
| `AnnotationParser` | 解析注解，创建 Applier |
| `StaticAuthApplier` | 应用静态认证（创建 Authorization） |
| `RequestAuthResolver` | 解析参数级别认证 |
| `AuthFieldMapper` | 确定要更新的字段 |
| `AuthorizationDestinationSetter` | 更新 Authorization 字段 |
| `Authorization` | 封装认证信息，应用到 HTTP 请求 |
| `HttpInvocationHandler` | 协调所有 Applier，构建请求 |

---

## 相关文档

- [HTTP 客户端认证使用手册](./HTTP_CLIENT_AUTH_USAGE.md)
- [示例代码 - Example 07](../../../../../../../examples/fit-example/07-http-client-proxy)
