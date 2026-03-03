---
name: create-pr
description: 创建 Pull Request 到指定或自动推断的目标分支。当用户要求创建 PR、提交 PR、发起合并请求时触发。可选参数为目标分支。
---

# 创建 Pull Request

## 执行步骤

1. 确定目标分支:
   如果用户指定了目标分支则使用该分支，否则自动推断:
   ```bash
   git branch --show-current
   git log --oneline --decorate --first-parent -20
   ```
   推断规则:
   - 当前在核心分支上(main 或版本号分支如 3.6.x) -> 目标分支即为当前分支
   - 当前在特性分支上 -> 从 log 中找到最近的父核心分支

2. 读取 PR 模板:
   ```bash
   cat .github/PULL_REQUEST_TEMPLATE.md
   ```

3. 查看最近的 merged PR 作为格式参考:
   ```bash
   gh pr list --limit 3 --state merged --json number,title,body
   ```

4. 分析当前分支的完整变更。

5. 检查远程分支状态，如果未推送则先推送。

6. 创建 PR，结尾添加: 🤖 Generated with [Codex](https://openai.com/codex)

**注意**: 严格遵循 PR 模板格式，确保 PR 标题遵循 Conventional Commits 格式。
