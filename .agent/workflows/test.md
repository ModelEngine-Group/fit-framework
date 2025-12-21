---
description: 执行完整的测试流程，包括单元测试、构建验证和集成测试
---

## 测试流程

执行 FIT 框架的完整测试流程，包括单元测试、构建、服务启动和功能验证。

### 1. 清理构建产物

删除之前的构建目录：
- `run_command("rm -rf build")`

### 2. 执行单元测试和构建

运行 Maven 构建，执行全量单元测试并生成构建产物（预计耗时 9-12 分钟）：
- `run_command("mvn clean install", timeout=900000, run_in_background=true)`

**预期结果：**
- 所有单元测试通过
- 在根目录生成 `build/` 目录
- 构建产物包含 `build/bin/fit` 启动脚本

### 3. 启动 FIT 服务进行集成测试

**启动步骤：**

1. 创建动态插件目录：
   - `run_command("mkdir -p dynamic-plugins")`

2. 启动 FIT 服务：
   - `run_command("build/bin/fit start --plugin-dir=dynamic-plugins", timeout=120000, run_in_background=true)`

使用后台运行模式，超时时间设置为 120 秒（2分钟），给服务足够的启动时间。

**说明：**
- 使用 `--plugin-dir=dynamic-plugins` 参数指定插件目录

**启动成功的判断标准：**
- 输出日志中包含启动成功的关键信息
- 没有出现 ERROR 或 FATAL 级别的日志
- 服务端口（默认 8080）成功监听

### 4. 验证服务功能

#### 4.1 健康检查接口验证（推荐）

访问 Actuator 健康检查接口，验证插件加载情况：
- `run_command("curl -s http://localhost:8080/actuator/plugins")`

**预期结果：**
- HTTP 状态码 200
- 返回 JSON 格式的插件列表
- 包含核心插件信息

#### 4.2 Swagger 文档页面验证

访问 OpenAPI 文档页面，验证 HTTP 服务和文档生成功能：
- `run_command("curl -s http://localhost:8080/openapi.html")`

**预期结果：**
- HTTP 状态码 200
- 返回 HTML 内容
- 包含 Swagger UI 页面

### 5. 清理测试环境

测试完成后，停止 FIT 服务并清理动态创建的目录：
- `run_command("pkill -f fit-discrete-launcher")`
- `run_command("rm -rf build")`
- `run_command("rm -rf dynamic-plugins")`

### 6. 生成测试报告

生成测试报告，包含：
1. ✅/❌ 单元测试结果
2. ✅/❌ 构建状态
3. ✅/❌ FIT 服务启动状态
4. ✅/❌ 健康检查接口响应
5. ✅/❌ Swagger 文档页面可访问性

### 注意事项

1. **启动目录限制**：必须在动态插件目录下执行 `fit start`
2. **端口冲突**：确保 8080 端口未被占用
3. **进程管理**：测试完成后记得停止 FIT 服务
4. **构建时间**：Maven 构建预计需要 9-12 分钟，请耐心等待
