执行完整的测试流程，包括单元测试、构建验证和集成测试。

**用法：**
- `/test` - 执行完整测试流程

**执行步骤：**

## 1. 清理构建产物

删除之前的构建目录：
```bash
rm -rf build
```

## 2. 执行单元测试和构建

运行 Maven 构建，这会执行全量单元测试并生成构建产物：
```bash run_in_background timeout=900000
mvn clean install
```

**说明：** 使用后台运行模式以避免超时限制

**预期结果：**
- 所有单元测试通过
- 在根目录生成 `build/` 目录
- 构建产物包含 `build/bin/fit` 启动脚本

## 3. 启动 FIT 服务进行集成测试

**关键要求：**
- `fit` 命令无法在包含 FIT 框架的目录中执行
- 必须在一个新建的动态插件目录下执行

**启动步骤：**

**创建动态插件目录并启动**
```bash run_in_background timeout=120000
mkdir -p dynamic-plugins
cd dynamic-plugins
../build/bin/fit start
```

使用后台运行模式，超时时间设置为 120 秒（2分钟），给服务足够的启动时间。

**启动成功的判断标准：**
- 输出日志中包含启动成功的关键信息
- 没有出现 ERROR 或 FATAL 级别的日志
- 服务端口（默认 8080）成功监听

## 4. 验证服务功能

### 4.1 健康检查接口验证（推荐）

访问 Actuator 健康检查接口，验证插件加载情况：
```bash
curl -s http://localhost:8080/actuator/plugins
```

**预期结果：**
- HTTP 状态码 200
- 返回 JSON 格式的插件列表
- 包含核心插件信息

### 4.2 Swagger 文档页面验证

访问 OpenAPI 文档页面，验证 HTTP 服务和文档生成功能：
```bash
curl -s http://localhost:8080/openapi.html
```

**预期结果：**
- HTTP 状态码 200
- 返回 HTML 内容
- 包含 Swagger UI 页面

## 5. 清理测试环境

测试完成后，停止 FIT 服务并清理动态创建的目录：

```bash
# 停止 FIT 服务（找到进程并终止）
pkill -f fit-discrete-launcher
```

```bash
# 删除动态创建的目录
rm -rf dynamic-plugins build
```

**测试报告：**

生成测试报告，包含：
1. ✅/❌ 单元测试结果
2. ✅/❌ 构建状态
3. ✅/❌ FIT 服务启动状态
4. ✅/❌ 健康检查接口响应
5. ✅/❌ Swagger 文档页面可访问性

**注意事项：**

1. **启动目录限制**：必须在动态插件目录下执行 `fit start`
2. **端口冲突**：确保 8080 端口未被占用
3. **权限问题**：确保 `build/bin/fit` 有执行权限
4. **进程管理**：测试完成后记得停止 FIT 服务
