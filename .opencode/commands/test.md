---
description: 执行完整的测试流程
agent: general
subtask: false
---

执行完整的测试流程,包括单元测试、构建验证和集成测试。

## 测试流程

### 步骤 1：清理构建产物

```bash
rm -rf build
```

### 步骤 2：执行 Maven 构建和单元测试

```bash
mkdir -p .ai-workspace/logs
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
LOG_FILE=".ai-workspace/logs/maven-build-${TIMESTAMP}.log"

# 使用 -f 参数指定 pom.xml 位置，避免 cd 命令
mvn -f framework/pom.xml clean install -pl '!ohscript' -DskipITs > "$LOG_FILE" 2>&1
BUILD_STATUS=$?

# 显示构建摘要
tail -100 "$LOG_FILE"

if [ $BUILD_STATUS -ne 0 ]; then
    grep -A 5 "BUILD FAILURE\|COMPILATION ERROR\|ERROR\|FAILED" "$LOG_FILE" | head -50
    exit $BUILD_STATUS
fi
```

**说明：** 构建日志保存到 `.ai-workspace/logs/maven-build-{timestamp}.log`

### 步骤 3：加载公共脚本并初始化环境

```bash
source .ai-agents/scripts/fit-service.sh

init_plugin_dir
```

**说明：** 加载 FIT 服务管理公共脚本，初始化插件目录（日志目录在步骤 2 已创建）

### 步骤 4：启动 FIT 服务

```bash
start_fit_service
```

**说明：** 启动服务并等待健康检查通过（最多 60 秒），日志自动保存到 `.ai-workspace/logs/fit-server-{timestamp}.log`

### 步骤 5：执行所有验证

```bash
verify_all
TEST_RESULT=$?
```

**说明：** 验证健康检查、插件列表、Swagger 文档等接口

### 步骤 6：清理测试环境

```bash
cleanup true
exit $TEST_RESULT
```

**说明：** 清理构建产物和动态插件目录，测试日志保留在 `.ai-workspace/logs/`

## 执行说明

- **超时设置：** Maven 构建步骤需要设置 900000ms (15分钟) 超时
- **并行执行：** 各步骤需按顺序执行，不可并行
- **日志保存：** 构建和服务日志自动保存到 `.ai-workspace/logs/` 目录
- **端口要求：** 确保 8080 端口未被占用
- **预计时间：** 整个流程约 15-20 分钟

## 输出文件

- `.ai-workspace/logs/maven-build-{timestamp}.log` - Maven 构建完整日志
- `.ai-workspace/logs/fit-server-{timestamp}.log` - FIT 服务启动日志
