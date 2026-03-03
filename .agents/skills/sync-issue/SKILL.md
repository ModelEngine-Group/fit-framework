---
name: sync-issue
description: 将任务处理进度同步到对应的 GitHub Issue 评论。当用户要求同步进度到 issue、更新 issue 状态时触发。参数为 task-id。
---

# 同步进度到 Issue

## 执行步骤

1. 查找任务文件（搜索 active/blocked/completed 目录）。

2. 从 task.md 中获取 issue_number、current_step、status 等。

3. 读取上下文文件（analysis.md, plan.md, implementation.md, review.md），提取关键进展。

4. 生成进度摘要:
   ```markdown
   ## 🤖 任务进度更新

   **任务ID**: <task-id>
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

**注意**: 摘要要简洁，面向人类阅读，建议在完成一个完整阶段后同步。
