---
description: 使用标准消息格式提交变更到 git
---

1. 检查仓库当前状态。
   - `run_command("git status")`
   - `run_command("git diff --stat")`

2. 识别未追踪的文件。
   - 如果有未追踪的文件（排除 `.agent/` 或 `.claude/` 如果它们被忽略），询问用户是否应该添加它们。

3. 将文件添加到暂存区。
   - `run_command("git add <files>")`

4. 提交变更。
   - 根据变更生成清晰、简洁的提交消息。
   - 确保消息遵循 conventional commits 规范（例如：`feat:`, `fix:`, `docs:`）。
   - 添加 Co-Authored-By 签名。
   - 运行命令：
     ```bash
     git commit -m "$(cat <<'EOF'
     <COMMIT_MESSAGE>

     🤖 Generated with Antigravity

     Co-Authored-By: Antigravity <noreply@google.com>
     EOF
     )"
     ```
