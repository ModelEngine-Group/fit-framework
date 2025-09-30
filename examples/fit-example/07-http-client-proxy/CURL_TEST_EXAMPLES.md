# CURL 测试用例

本文档提供了用于测试 HTTP 客户端认证功能的 curl 命令示例。

## 前置条件

1. 编译整个项目：在项目根目录执行 `mvn clean install`
2. 启动服务器端：按照 [README](../../../README.md) 中的说明启动服务器
3. 确保服务器运行在 `http://localhost:8080`（FIT 框架默认端口）

## 测试用例

### 1. Bearer Token 静态认证

```bash
# 测试静态 Bearer Token
curl -X GET "http://localhost:8080/http-server/auth/bearer-static" \
  -H "Authorization: Bearer static-bearer-token-12345" \
  -H "X-Service-Key: service-default-key"

# 期望响应：Bearer Static Auth: Bearer static-bearer-token-12345
```

### 2. Bearer Token 动态认证

```bash
# 测试动态 Bearer Token
curl -X GET "http://localhost:8080/http-server/auth/bearer-dynamic" \
  -H "Authorization: Bearer dynamic-bearer-token-67890"

# 期望响应：Bearer Dynamic Auth: Bearer dynamic-bearer-token-67890
```

### 3. Basic 认证

```bash
# 测试 Basic 认证（admin:secret123 的 base64 编码）
curl -X GET "http://localhost:8080/http-server/auth/basic-static" \
  -H "Authorization: Basic YWRtaW46c2VjcmV0MTIz" \
  -H "X-Service-Key: service-default-key"

# 期望响应：Basic Static Auth: Basic YWRtaW46c2VjcmV0MTIz
```

### 4. API Key Header 静态认证

```bash
# 测试 Header 中的 API Key
curl -X GET "http://localhost:8080/http-server/auth/apikey-header-static" \
  -H "X-API-Key: static-api-key-67890" \
  -H "X-Service-Key: service-default-key"

# 期望响应：API Key Header Static: static-api-key-67890, Service Key: service-default-key
```

### 5. API Key Query 静态认证

```bash
# 测试 Query 参数中的 API Key
curl -X GET "http://localhost:8080/http-server/auth/apikey-query-static?api_key=query-api-key-111" \
  -H "X-Service-Key: service-default-key"

# 期望响应：API Key Query Static: query-api-key-111, Service Key: service-default-key
```

### 6. API Key 动态认证

```bash
# 测试动态 API Key
curl -X GET "http://localhost:8080/http-server/auth/apikey-dynamic" \
  -H "X-Dynamic-Key: dynamic-api-key-999" \
  -H "X-Service-Key: service-default-key"

# 期望响应：API Key Dynamic: dynamic-api-key-999, Service Key: service-default-key
```

### 7. 动态 Provider 认证

```bash
# 测试动态 Token Provider
curl -X GET "http://localhost:8080/http-server/auth/dynamic-provider" \
  -H "Authorization: Bearer provider-generated-token-123" \
  -H "X-Service-Key: service-default-key"

# 期望响应：Dynamic Provider Auth: Bearer provider-generated-token-123, Service Key: service-default-key
```

### 8. 自定义 Provider 认证

```bash
# 测试自定义签名 Provider
curl -X GET "http://localhost:8080/http-server/auth/custom-provider" \
  -H "X-Timestamp: 1640995200000" \
  -H "X-Signature: custom-signature-abc123" \
  -H "X-App-Id: test-app-001" \
  -H "X-Service-Key: service-default-key"

# 期望响应：Custom Provider Auth - Timestamp: 1640995200000, Signature: custom-signature-abc123, AppId: test-app-001, Service Key: service-default-key
```

### 9. 方法级别覆盖

```bash
# 测试方法级别的认证覆盖
curl -X GET "http://localhost:8080/http-server/auth/method-override" \
  -H "X-API-Key: method-override-key-456"

# 期望响应：Method Override Auth: method-override-key-456
```

### 10. 组合认证

```bash
# 测试组合认证（多种认证方式同时使用）
curl -X GET "http://localhost:8080/http-server/auth/combined-auth" \
  -H "Authorization: Bearer combined-auth-token-789" \
  -H "X-User-Context: user-context-key-abc" \
  -H "X-Service-Key: service-default-key"

# 期望响应：Combined Auth - Authorization: Bearer combined-auth-token-789, UserContext: user-context-key-abc, Service Key: service-default-key
```

## 错误场景测试

### 1. 缺少必需的认证头

```bash
# 测试缺少 Authorization 头
curl -X GET "http://localhost:8080/http-server/auth/bearer-static"

# 期望：400 Bad Request 或相应的错误响应
```

### 2. 错误的认证格式

```bash
# 测试错误的 Bearer Token 格式
curl -X GET "http://localhost:8080/http-server/auth/bearer-static" \
  -H "Authorization: InvalidFormat token-123"

# 期望：401 Unauthorized 或相应的错误响应
```

### 3. 缺少 API Key

```bash
# 测试缺少 API Key
curl -X GET "http://localhost:8080/http-server/auth/apikey-header-static" \
  -H "X-Service-Key: service-default-key"

# 期望：400 Bad Request 或相应的错误响应
```

## 批量测试

使用提供的脚本进行批量测试：

```bash
# 确保脚本有执行权限
chmod +x ./run_tests.sh

# 运行所有测试用例
./run_tests.sh

# 运行特定类型的测试
./run_tests.sh bearer    # Bearer Token 相关测试
./run_tests.sh apikey    # API Key 相关测试
./run_tests.sh basic     # Basic 认证测试
./run_tests.sh provider  # Provider 模式测试
./run_tests.sh error     # 错误场景测试

# 详细模式运行
./run_tests.sh -v bearer

# 自定义超时时间
./run_tests.sh -t 30 all
```

## 验证清单

- [ ] 所有静态认证配置正常工作
- [ ] 动态认证（参数驱动）正常工作
- [ ] Provider 模式正常工作
- [ ] 认证优先级正确（参数 > 方法 > 接口）
- [ ] 组合认证场景正常工作
- [ ] 错误场景返回正确的状态码
- [ ] 服务级别的默认认证始终包含在请求中

## 注意事项

1. **Base64 编码**: Basic 认证需要对 `username:password` 进行 base64 编码
2. **Headers 大小写**: HTTP 头的大小写在某些服务器上可能敏感
3. **Query 参数编码**: 确保特殊字符正确进行 URL 编码
4. **Provider 依赖**: 使用 Provider 的测试需要确保相应的 Bean 已注册

## 故障排除

如果测试失败，请检查：

1. 服务器是否正常启动（查看启动日志中是否有 "FIT application started" 信息）
2. 端口 8080 是否被占用
3. 认证头的格式是否正确
4. Provider Bean 是否正确注册（使用 `@Component` 注解）
5. 项目是否已正确编译（`mvn clean install`）
6. 查看服务器日志获取详细错误信息