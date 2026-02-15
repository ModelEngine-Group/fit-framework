---
description: 将任务处理进度同步到 GitHub Issue 评论
argument-hint: <task-id>
---

将任务 $1 的处理进度摘要同步到对应的 GitHub Issue 评论。

执行以下步骤:

1. 查找任务文件:
   按以下优先级搜索:
   - .ai-workspace/active/$1/task.md(优先)
   - .ai-workspace/blocked/$1/task.md
   - .ai-workspace/completed/$1/task.md
   如果都不存在,提示用户任务不存在。

2. 读取任务信息:
   从 task.md 中获取 issue_number(如果没有,提示用户手动指定)、标题、current_step、status 等。

3. 读取上下文文件(如果存在):
   - analysis.md、plan.md、implementation.md、review.md
   从中提取关键进展和决策。

4. 生成进度摘要:
   按以下格式生成 Markdown 摘要:
   ```markdown
   ## 🤖 任务进度更新

   **任务ID**: $1
   **更新时间**: <当前时间>
   **当前状态**: <状态描述>

   ### ✅ 已完成
   - [x] <已完成步骤及核心要点>

   ### 📋 当前进展
   <当前步骤详细说明>

   ### 🎯 下一步
   <下一步计划>

   ---
   *由 Codex 自动生成*
   ```

5. 同步到 Issue:
   ```bash
   gh issue comment <issue-number> --body "<摘要内容>"
   ```

6. 告知用户:
   - 显示同步的 Issue 链接
   - 输出同步的核心内容摘要

**注意事项**:
- 摘要要简洁,每阶段只提取核心要点
- 面向人类阅读,避免过多技术细节
- 避免频繁同步,建议在完成一个完整阶段后同步
