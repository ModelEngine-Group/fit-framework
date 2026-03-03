---
name: refine-title
description: 深度分析 Issue 或 PR 内容并重构标题为 Conventional Commits 格式。当用户要求优化标题、重命名 issue/PR 标题、规范化标题格式时触发。参数为 issue 或 PR 编号。
---

# 重构标题

针对 GitHub Issue 或 PR，读取其详细描述、标签和代码变更，深度理解其意图，生成符合 `type(scope): subject` 规范的新标题。

## 执行步骤

1. 识别对象与获取信息:
   先尝试 Issue，失败则尝试 PR:
   ```bash
   gh issue view <id> --json number,title,body,labels,state
   gh pr view <id> --json number,title,body,labels,state,files
   ```

2. 智能分析:
   - 确定 Type: 阅读 body、检查 labels、分析 files
   - 确定 Scope: 分析涉及的模块（fit/waterflow/fel）
   - 生成 Subject: 从 body 中提炼核心意图，20字以内中文

3. 生成建议与交互，询问用户确认。

4. 执行修改:
   ```bash
   gh issue edit <id> --title "<new-title>"
   # 或
   gh pr edit <id> --title "<new-title>"
   ```

**注意**: 必须先分析内容，不要直接使用原标题。确保用户确认后再执行修改。
