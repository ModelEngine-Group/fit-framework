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
    exit 1
fi
```

**说明：** 检查 `build/` 目录是否存在，不存在则退出（需要先运行 `/test` 命令）

### 步骤 2：创建动态插件目录

```bash
mkdir -p dynamic-plugins
```

### 步骤 3：启动 FIT 服务

```bash
mkdir -p .ai-workspace/logs
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
FIT_LOG=".ai-workspace/logs/fit-server-${TIMESTAMP}.log"
nohup build/bin/fit start --plugin-dir=dynamic-plugins > "$FIT_LOG" 2>&1 &
FIT_PID=$!

# 等待服务启动（最多 60 秒）
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        exit 0
    fi
    if [ $i -eq 60 ]; then
        cat "$FIT_LOG"
        kill $FIT_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done
```

**说明：** FIT 服务日志保存到 `.ai-workspace/logs/fit-server-{timestamp}.log`

### 步骤 4：验证健康检查接口

```bash
PLUGINS_RESPONSE=$(curl -s http://localhost:8080/actuator/plugins)
if [ $? -eq 0 ]; then
    echo "$PLUGINS_RESPONSE"
else
    pkill -f fit-discrete-launcher || true
    exit 1
fi
```

**说明：** 返回插件列表 JSON

### 步骤 5：验证 Swagger 文档页面

```bash
curl -s http://localhost:8080/openapi.html | grep -qi "swagger\|openapi"
```

**说明：** 检查 Swagger 文档页面是否可访问

### 步骤 6：清理测试环境

```bash
pkill -f fit-discrete-launcher || true
sleep 2
rm -rf dynamic-plugins
ls -lh .ai-workspace/logs/
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
