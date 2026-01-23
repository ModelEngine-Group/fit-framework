# FIT Framework 公共脚本

本目录包含 FIT Framework 的公共工具脚本，供测试命令和其他自动化任务调用。

## 脚本列表

### 1. fit-service.sh - FIT 服务管理脚本

提供 FIT 服务的启动、验证、停止和清理功能。

#### 功能模块

| 函数 | 说明 | 返回值 |
|------|------|--------|
| `init_log_dir()` | 初始化日志目录 `.ai-workspace/logs` | - |
| `init_plugin_dir()` | 创建动态插件目录 `dynamic-plugins` | - |
| `start_fit_service([log_file])` | 启动 FIT 服务，等待健康检查（60秒超时） | 0=成功, 1=失败 |
| `verify_health()` | 验证健康检查接口 `/actuator/health` | 0=成功, 1=失败 |
| `verify_plugins()` | 验证插件列表接口 `/actuator/plugins` | 0=成功, 1=失败 |
| `verify_swagger()` | 验证 Swagger 文档页面 `/openapi.html` | 0=成功, 1=失败 |
| `verify_all()` | 执行所有验证（health + plugins + swagger） | 0=成功, 1=失败 |
| `stop_fit_service()` | 停止 FIT 服务进程 | - |
| `cleanup([clean_build])` | 清理测试环境，可选清理构建产物 | - |

#### 全局变量

- `FIT_PID` - FIT 服务进程 ID（由 `start_fit_service` 设置）
- `FIT_LOG` - FIT 服务日志文件路径（由 `start_fit_service` 设置）

#### 使用示例

##### 在脚本中调用

```bash
#!/bin/bash

# 加载公共脚本
source .ai-agents/scripts/fit-service.sh

# 初始化环境
init_log_dir
init_plugin_dir

# 启动服务
start_fit_service
if [ $? -ne 0 ]; then
    echo "服务启动失败"
    exit 1
fi

# 执行验证
verify_all
TEST_RESULT=$?

# 清理环境
cleanup false  # 保留构建产物
exit $TEST_RESULT
```

##### 直接运行

```bash
# 启动服务
.ai-agents/scripts/fit-service.sh start

# 验证接口
.ai-agents/scripts/fit-service.sh verify

# 停止服务
.ai-agents/scripts/fit-service.sh stop

# 清理环境（保留构建产物）
.ai-agents/scripts/fit-service.sh cleanup

# 清理环境（包含构建产物）
.ai-agents/scripts/fit-service.sh cleanup true
```

#### 特性

- ✅ 自动端口占用检测（8080）
- ✅ 60秒服务启动超时
- ✅ 进程存活检查
- ✅ 彩色日志输出
- ✅ 详细错误信息
- ✅ 支持独立运行或作为库加载

### 2. run-test.sh - 完整测试流程

执行完整的 FIT Framework 测试流程（构建 + 集成测试）。

#### 使用方法

```bash
.ai-agents/scripts/run-test.sh
```

## 调用关系

```
.opencode/commands/
├── test.md              → 调用 fit-service.sh（完整测试）
└── integration-test.md  → 调用 fit-service.sh（仅集成测试）

.ai-agents/scripts/
├── fit-service.sh       ← 被测试命令调用
└── run-test.sh          ← 独立测试脚本
```

## 维护指南

### 添加新功能

1. 在 `fit-service.sh` 中添加新函数
2. 更新本 README 的功能列表
3. 在测试命令中调用新函数

### 修改现有功能

1. 修改 `fit-service.sh` 中的函数实现
2. 确保向后兼容，或同步更新调用方
3. 更新本 README 文档

### 脚本规范

- ✅ 使用 `set -e` 遇错即停
- ✅ 提供清晰的日志输出（log_info/log_error/log_warn）
- ✅ 函数返回值：0=成功, 1=失败
- ✅ 支持独立运行和作为库加载
- ✅ 添加详细的函数注释

## 日志说明

所有脚本的日志输出到 `.ai-workspace/logs/` 目录：

- `maven-build-{timestamp}.log` - Maven 构建日志
- `fit-server-{timestamp}.log` - FIT 服务启动日志

日志文件在测试完成后保留，便于问题排查。
