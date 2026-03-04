---
description: 标记任务完成并归档到 completed 目录
argument-hint: <task-id>
---

标记任务 $1 为已完成状态,更新任务元数据,并归档到 completed 目录。

**前置条件检查**:
在执行前,确认以下条件全部满足:
- 所有工作流步骤已完成
- 代码已审查通过(review.md 显示批准)
- 代码已提交到 Git
- 所有测试通过
**如果以上条件未全部满足,请勿执行此命令。**

执行以下步骤:

1. 验证任务存在:
   ```bash
   test -f .ai-workspace/active/$1/task.md && echo "✅ 任务存在" || echo "❌ ERROR: 任务不存在"
   ```

2. 读取并验证任务状态:
   - 检查所有步骤是否标记为完成 ✅
   - 检查文件完整性: analysis.md, plan.md, implementation.md, review.md 都存在

3. 获取当前时间:
   ```bash
   date '+%Y-%m-%d %H:%M:%S'
   ```

4. 更新任务状态:
   使用 Edit 工具更新 task.md:
   - status: completed
   - current_step: finalize
   - updated_at: 当前时间
   - completed_at: 当前时间

5. 在 task.md 末尾添加完成总结(交付成果、完成标准等)

6. 归档任务:
   ```bash
   mkdir -p .ai-workspace/completed && mv .ai-workspace/active/$1 .ai-workspace/completed/
   ```

7. 验证移动成功:
   ```bash
   test ! -d .ai-workspace/active/$1 && echo "✅ 已移除 active" || echo "❌ active 仍存在"
   test -d .ai-workspace/completed/$1 && echo "✅ 已归档到 completed" || echo "❌ 归档失败"
   ```

8. 告知用户:
   ```
   🎉 任务 $1 已完成并归档
   归档位置: .ai-workspace/completed/$1/
   ```
   - 提示下一步:
     - 如果还有其他待处理任务:
       - Claude Code / OpenCode: /check-task {task-id}
       - Gemini CLI: /fit:check-task {task-id}
       - Codex CLI: /prompts:fit-check-task {task-id}

**注意事项**:
- 只有在真正完成所有工作后才归档
- 归档前确认所有文档都已创建
- 不要过早归档未完成的任务
