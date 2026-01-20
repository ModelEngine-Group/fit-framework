---
description: 创建 Pull Request
agent: general
subtask: false
---

创建 Pull Request 到指定分支(默认: 3.6.x)。

使用方式:
- /create-pr → 创建PR到默认分支 3.6.x
- /create-pr main → 创建PR到 main 分支
- /create-pr <branch-name> → 创建PR到指定分支

执行以下步骤:

1. 解析目标分支:
   - 如果提供了参数 $1,使用 $1 作为目标分支
   - 如果没有参数,默认使用 3.6.x 作为目标分支

2. 读取 PR 模板:
   !`cat .github/PULL_REQUEST_TEMPLATE.md`

3. 查看最近 3 个 merged PR 作为参考:
   !`gh pr list --limit 3 --state merged --json number,title,body`

4. 分析当前分支的完整变更:
   !`git status`
   !`git log <target-branch>..HEAD --oneline`
   !`git diff <target-branch>...HEAD --stat`

5. 检查远程分支状态:
   !`git rev-parse --abbrev-ref --symbolic-full-name @{u}`
   - 如果分支未推送,先推送: !`git push -u origin <current-branch>`

6. 根据模板创建 PR:
   - 按照 .github/PULL_REQUEST_TEMPLATE.md 格式填写所有部分
   - 参考最近的 PR 格式和风格
   - PR 标题格式: [模块名] 简短描述
   - PR 结尾添加: 🤖 Generated with [OpenCode](https://opencode.ai)
   - 使用 HEREDOC 格式传递 body

示例:
```bash
gh pr create --base <target-branch> --title "<标题>" --body "$(cat <<'EOF'
<完整的PR描述>
EOF
)"
```

**注意事项**:
- 必须严格遵循 PR 模板格式
- 所有必填项都要填写完整
- 参考最近的 merged PR 的格式和风格
- 确保 PR 标题格式正确
