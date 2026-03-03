---
name: implement-task
description: 根据技术方案实施任务，编写代码和测试，输出实现报告。当用户要求实施任务、开始编码、执行技术方案时触发。参数为 task-id。
---

# 实施任务

根据技术方案实施任务，编写代码和测试。

## 执行步骤

1. 验证前置条件:
   - .ai-workspace/active/<task-id>/task.md 存在
   - .ai-workspace/active/<task-id>/plan.md 存在

2. 读取技术方案（plan.md），理解实现策略和步骤。

3. 执行代码实现:
   - 按照 plan.md 中的步骤顺序执行
   - 编写完整的单元测试
   - 修改带版权头的文件时，先运行 `date +%Y` 获取当前年份并更新版权头

4. 运行测试验证:
   ```bash
   mvn test -pl :<module-name>
   ```

5. 输出实现报告到 implementation.md。

6. 更新任务状态，标记 implementation 为完成。

7. 提示下一步使用 review-task skill。

**注意**: 严格遵循 plan.md，不要添加计划外功能。不要自动 git commit。
