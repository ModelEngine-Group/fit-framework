---
name: complete-task
description: 标记任务完成并归档到 completed 目录。当用户要求完成任务、归档任务、标记任务为已完成时触发。参数为 task-id。
---

# 完成任务

## 前置条件

- 所有工作流步骤已完成
- 代码已审查通过
- 代码已提交到 Git
- 所有测试通过

## 执行步骤

1. 验证任务存在:
   ```bash
   test -f .ai-workspace/active/<task-id>/task.md
   ```

2. 验证所有步骤标记为完成，文件完整性检查。

3. 更新 task.md: status -> completed, completed_at。

4. 添加完成总结。

5. 归档任务:
   ```bash
   mkdir -p .ai-workspace/completed && mv .ai-workspace/active/<task-id> .ai-workspace/completed/
   ```

6. 验证归档成功。

**注意**: 只有在真正完成所有工作后才归档，不要过早归档未完成的任务。
