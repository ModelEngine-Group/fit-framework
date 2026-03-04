---
name: test-integration
description: 执行 FIT Framework 集成测试（服务启动 + 接口验证），需要先完成 Maven 构建。当用户要求运行集成测试、验证服务接口时触发。
---

# 执行集成测试

## 前置条件

需要先完成 Maven 构建，确保 `build/` 目录存在。如果还没有构建，请先使用 test skill 执行完整构建。

## 执行方式

按以下步骤顺序执行：

**步骤 1：验证构建产物**
```bash
if [ ! -d "build" ]; then
    echo "错误: 构建产物不存在，请先执行完整测试流程"
    exit 1
fi
```

**步骤 2：加载公共脚本并初始化环境**
```bash
source .agents/scripts/fit-service.sh
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
test_result=$?
```

**步骤 5：清理测试环境**
```bash
cleanup false
exit $test_result
```

## 测试内容

1. 检查构建产物是否存在
2. 启动 FIT 服务（自动等待健康检查，最多 60 秒）
3. 验证健康检查接口 `/actuator/health`
4. 验证插件列表接口 `/actuator/plugins`
5. 验证 Swagger 文档页面 `/openapi.html`
6. 停止服务并清理临时文件

## 注意事项

1. 必须先完成构建，确保 `build/` 目录存在
2. 确保 8080 端口未被占用
3. 预计时间：约 1-2 分钟（不包含构建时间）
