---
description: 执行完整的测试流程
agent: build
subtask: false
---

执行完整的测试流程,包括单元测试、构建验证和集成测试。

使用自动化测试脚本执行完整的测试流程:

!`./.ai-agents/scripts/run-test.sh`

**测试流程包括**:

1. 清理构建产物 - 删除之前的 build 目录
2. 执行单元测试和构建 - 运行 mvn clean install 执行全量单元测试
3. 创建动态插件目录 - 创建 dynamic-plugins 目录
4. 启动 FIT 服务 - 使用 build/bin/fit start 启动服务
5. 验证健康检查接口 - 访问 /actuator/plugins 接口
6. 验证 Swagger 文档 - 访问 /openapi.html 页面
7. 清理测试环境 - 停止服务并删除构建产物

**测试报告**:

脚本会自动生成测试报告,包含:
- ✅/✗ 单元测试结果
- ✅/✗ 构建状态
- ✅/✗ FIT 服务启动状态
- ✅/✗ 健康检查接口响应
- ✅/✗ Swagger 文档页面可访问性

**注意事项**:
- 确保 8080 端口未被占用
- 测试脚本已在配置中自动授权
- 整个测试流程无需手动确认,自动执行所有步骤
- 超时时间: 15分钟(900000ms)
