---
description: 执行集成测试（需要先完成构建）
agent: general
subtask: false
---

执行 FIT Framework 的集成测试，包括服务启动和接口验证。

**前置条件**：需要先完成 Maven 构建，确保 `build/` 目录存在。

## 测试流程

### 步骤 1：验证构建产物

```bash
if [ ! -d "build" ]; then
    echo "错误: 构建产物不存在，请先运行 /test 命令"
    exit 1
fi
```

**说明：** 检查 `build/` 目录是否存在，不存在则退出（需要先运行 `/test` 命令）

### 步骤 2：加载公共脚本并初始化环境

```bash
source .ai-agents/scripts/fit-service.sh

init_log_dir
init_plugin_dir
```

**说明：** 加载 FIT 服务管理公共脚本，初始化日志和插件目录

### 步骤 3：启动 FIT 服务

```bash
start_fit_service
```

**说明：** 启动服务并等待健康检查通过（最多 60 秒），日志自动保存到 `.ai-workspace/logs/fit-server-{timestamp}.log`

### 步骤 4：执行所有验证

```bash
verify_all
TEST_RESULT=$?
```

**说明：** 验证健康检查、插件列表、Swagger 文档等接口

### 步骤 5：清理测试环境

```bash
cleanup false
exit $TEST_RESULT
```

**说明：** 停止服务并清理动态插件目录，保留构建产物和测试日志

## 执行说明

- **前置条件：** 必须先完成 Maven 构建，确保 `build/` 目录存在
- **超时设置：** 服务启动步骤默认 60 秒超时
- **并行执行：** 各步骤需按顺序执行，不可并行
- **端口要求：** 确保 8080 端口未被占用
- **预计时间：** 整个流程约 1-2 分钟（不包含构建时间）
- **构建产物：** 不会删除 `build/` 目录，便于重复测试

## 输出文件

- `.ai-workspace/logs/fit-server-{timestamp}.log` - FIT 服务启动日志
