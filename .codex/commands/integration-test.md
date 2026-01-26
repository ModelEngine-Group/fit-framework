---
description: 执行集成测试（需要先完成构建）
usage: /prompts:integration-test
---

# Integration Test Command

## 使用前：自动识别仓库

命令会默认使用当前工作目录所在的 Git 仓库作为目标，无需传入仓库参数。若当前目录不在 Git 仓库内，请先 `cd` 到目标仓库根目录后再执行。

文中所有路径示例默认以仓库根目录为基准。

## 功能说明

执行 FIT Framework 的集成测试，包括服务启动和接口验证。

**前置条件**：需要先完成 Maven 构建，确保 `build/` 目录存在。如果还没有构建，请先运行 `/prompts:test` 命令。

**用法：**
- `/prompts:integration-test` - 执行集成测试流程

**执行方式：**

按照以下步骤顺序执行（每个步骤需等待前一步完成）：

**步骤 1：验证构建产物**
```bash
if [ ! -d "build" ]; then
    echo "错误: 构建产物不存在，请先运行 /prompts:test 命令"
    exit 1
fi
```

**步骤 2：加载公共脚本并初始化环境**
```bash
source .ai-agents/scripts/fit-service.sh
init_log_dir
init_plugin_dir
```

**步骤 3：启动 FIT 服务**
```bash timeout=90000
start_fit_service
```

**步骤 4：执行所有验证**
```bash timeout=30000
verify_all
TEST_RESULT=$?
```

**步骤 5：清理测试环境**
```bash
cleanup false
exit $TEST_RESULT
```

**测试内容包括：**

1. ✅ 检查构建产物是否存在
2. ✅ 启动 FIT 服务（自动等待健康检查，最多 60 秒）
3. ✅ 验证健康检查接口 `/actuator/health`
4. ✅ 验证插件列表接口 `/actuator/plugins`
5. ✅ 验证 Swagger 文档页面 `/openapi.html`
6. ✅ 停止服务并清理临时文件

**日志输出：**

所有测试日志保存在 `.ai-workspace/logs/` 目录（已被 git 忽略）：
- `fit-server-{timestamp}.log` - FIT 服务启动日志

**注意事项：**

1. **前置条件**：必须先运行 `/prompts:test` 完成构建，或确保 `build/` 目录存在
2. **端口冲突**：确保 8080 端口未被占用（脚本会自动检测并清理）
3. **超时设置**：服务启动步骤设置 90 秒超时，验证步骤设置 30 秒超时
4. **并行执行**：各步骤需按顺序执行，不可并行
5. **构建产物保留**：测试结束后不会删除 `build/` 目录，便于重复测试
6. **权限配置**：如需自动授权，请在本地 Codex 配置中放行相关命令
7. **完全自动化**：整个测试流程无需手动确认，自动执行所有步骤
8. **预计时间**：整个流程约 1-2 分钟（不包含构建时间）

---

## 相关命令

- `/prompts:test` - 执行完整测试流程（包含构建）
- `/prompts:commit` - 提交代码变更
- `/prompts:create-pr` - 创建 Pull Request
