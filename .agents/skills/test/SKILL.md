---
name: test
description: 执行 FIT Framework 完整测试流程（Maven 构建 + FIT 服务启动 + 健康检查 + Swagger 验证）。当用户要求运行测试、验证构建、检查服务是否正常启动时触发。
---

# 执行测试

## 使用前

命令默认使用当前工作目录所在的 Git 仓库，无需传入仓库参数。

## 执行方式

使用自动化测试脚本执行完整的测试流程：
```bash timeout=900000
./.agents/scripts/run-test.sh
```

## 测试流程

1. **清理构建产物** - 删除之前的 build 目录
2. **执行单元测试和构建** - 运行 `mvn clean install` 执行全量单元测试
3. **创建动态插件目录** - 创建 `dynamic-plugins` 目录
4. **启动 FIT 服务** - 使用 `build/bin/fit start` 启动服务
5. **验证健康检查接口** - 访问 `/actuator/plugins` 接口
6. **验证 Swagger 文档** - 访问 `/openapi.html` 页面
7. **清理测试环境** - 停止服务并删除构建产物

## 测试报告

脚本自动生成测试报告，包含各步骤的通过/失败状态。

## 注意事项

1. 确保 8080 端口未被占用
2. 整个测试流程无需手动确认，自动执行所有步骤
