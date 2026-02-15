---
description: 查看任务的当前状态和进度
argument-hint: <task-id>
---

查看任务 $1 的当前状态、进度和上下文文件。

执行以下步骤:

1. 查找任务文件:
   按以下优先级搜索:
   - .ai-workspace/active/$1/task.md(优先)
   - .ai-workspace/blocked/$1/task.md
   - .ai-workspace/completed/$1/task.md
   如果都不存在,提示用户任务不存在。

2. 读取任务信息:
   从 task.md 中获取: id, type, workflow, status, current_step, assigned_to, created_at, updated_at

3. 检查上下文文件是否存在:
   - analysis.md - 需求分析
   - plan.md - 技术方案
   - implementation.md - 实现报告
   - review.md - 审查报告

4. 输出状态报告:
   ```
   📋 任务状态报告

   **基本信息**:
   - 任务ID: $1
   - 任务标题: <title>
   - 任务类型: <type>
   - 创建时间: <created_at>

   **当前状态**:
   - 工作流: <workflow>
   - 当前步骤: <current_step>
   - 执行者: <assigned_to>

   **工作流进度**:
   ✅ / ⏳ / ⏸️ 各步骤状态

   **上下文文件**:
   ✅ / ❌ 各文件存在状态

   **下一步建议**:
   根据当前状态给出具体命令建议
   ```

**下一步建议规则**:
- 需求分析完成 → /plan-task $1
- 技术方案完成 → ⚠️ 等待人工审查,审查通过后 /implement-task $1
- 实现完成 → /review-task $1
- 审查完成(批准) → /commit
- 审查完成(需修改) → /refinement-task $1
- 任务阻塞 → 显示阻塞原因和解除条件
