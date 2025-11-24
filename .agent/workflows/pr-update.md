---
description: 更新现有的 Pull Request 描述以符合项目标准
---

1. 从输入中解析 PR 编号。
   - 示例输入："Update PR #123", "Fix description for 123"

2. 读取 PR 模板。
   - `Read(".github/PULL_REQUEST_TEMPLATE.md")`

3. 查看当前 PR 信息。
   - `run_command("gh pr view <pr-number>")`

4. 更新 PR 描述。
   - 重新编写描述以完全符合 `.github/PULL_REQUEST_TEMPLATE.md`。
   - 保留原始描述中有价值的信息。
   - 添加签名：`🤖 Generated with Antigravity`
   - 运行命令：
     ```bash
     gh pr edit <pr-number> --body "$(cat <<'EOF'
     <FULL_PR_BODY>
     EOF
     )"
     ```
