---
name: sync-pr
description: 将任务处理进度同步到对应的 Pull Request 评论。当用户要求同步进度到 PR、更新 PR 状态时触发。参数为 task-id。
---

# 同步进度到 PR

## 执行步骤

1. 查找任务文件（搜索 active/blocked/completed 目录）。

2. 从 task.md 中获取 PR 号码、current_step、status 等。

3. 读取上下文文件，提取关键进展和决策。

4. 生成进度摘要（格式同 sync-issue）。

5. 同步到 PR:
   ```bash
   gh pr comment <pr-number> --body "<摘要内容>"
   ```

**注意**: 摘要要简洁，在进度摘要中 @mention 相关审查者，标注重要的技术决策。
