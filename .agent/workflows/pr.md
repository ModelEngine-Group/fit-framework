---
description: 按照项目标准创建 Pull Request
---

1. 解析输入中的目标分支。
   - 如果提供了分支名称（例如 "to main", "for 3.5.x"），则使用该分支。
   - 如果未指定分支，默认使用 `3.6.x`。

2. 读取 PR 模板以了解所需格式。
   - `Read(".github/PULL_REQUEST_TEMPLATE.md")`

3. 查看最近合并的 PR 以了解项目风格。
   - `run_command("gh pr list --limit 3 --state merged --json number,title,body")`

4. 分析当前分支的状态和变更。
   - `run_command("git status")`
   - `run_command("git log <target-branch>..HEAD --oneline")`
   - `run_command("git diff <target-branch>...HEAD --stat")`

5. 检查当前分支是否需要推送。
   - `run_command("git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>&1 || echo 'No upstream branch'")`
   - 如果没有上游分支或领先于远程分支，则推送：`run_command("git push -u origin <current-branch>")`

6. 创建 Pull Request。
   - 严格按照 `.github/PULL_REQUEST_TEMPLATE.md` 构建 PR 内容。
   - 确保所有复选框都已正确填写。
   - 添加签名：`🤖 Generated with Antigravity`
   - 运行命令：
     ```bash
     gh pr create --base <target-branch> --title "<title>" --body "$(cat <<'EOF'
     <FULL_PR_BODY>
     EOF
     )"
     ```
