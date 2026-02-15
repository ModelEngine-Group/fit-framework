---
description: 创建 Pull Request
argument-hint: <target-branch>
---

创建 Pull Request 到指定分支（默认: 自动推断父分支）。

使用方式:
- /create-pr → 自动推断目标分支并创建PR
- /create-pr main → 创建PR到 main 分支

执行以下步骤:

1. 确定目标分支:
   用户指定的目标分支: $1
   如果上述值为空,自动推断目标分支:
   ```bash
   git branch --show-current
   git log --oneline --decorate --first-parent -20
   ```
   推断规则:
   - 当前在核心分支上(main 或版本号分支如 大版本号.小版本号.x（如 3.6.x）) → 目标分支即为当前分支
   - 当前在特性分支上 → 从 log 中的分支标记找到最近的父核心分支作为目标
   - 无法确定时 → 询问用户

2. 读取 PR 模板:
   ```bash
   cat .github/PULL_REQUEST_TEMPLATE.md
   ```

3. 查看最近的 merged PR 作为格式参考:
   ```bash
   gh pr list --limit 3 --state merged --json number,title,body
   ```

4. 分析当前分支的完整变更:
   ```bash
   git status
   git log <target-branch>..HEAD --oneline
   git diff <target-branch>...HEAD --stat
   ```

5. 检查远程分支状态:
   ```bash
   git rev-parse --abbrev-ref --symbolic-full-name @{u}
   ```
   如果分支未推送,先推送:
   ```bash
   git push -u origin <current-branch>
   ```

6. 根据模板创建 PR:
   - 按照 PR 模板格式填写所有部分
   - PR 结尾添加: 🤖 Generated with [Codex](https://openai.com/codex)
   - 使用 HEREDOC 格式传递 body:
   ```bash
   gh pr create --base <target-branch> --title "<标题>" --body "$(cat <<'EOF'
   <完整PR描述>
   EOF
   )"
   ```

**注意事项**:
- 必须严格遵循 PR 模板格式
- 所有必填项都要填写完整
- 确保 PR 标题遵循 Conventional Commits 格式
