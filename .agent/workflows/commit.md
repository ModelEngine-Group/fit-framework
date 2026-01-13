---
description: 使用标准消息格式提交变更到 git
---

1. 检查仓库当前状态。
   - `run_command("git status")`
   - `run_command("git diff --stat")`

2. **⚠️ CRITICAL: 检查并更新版权头年份**

   **步骤 2.1：获取当前年份**
   - `run_command("date +%Y")` - 动态获取当前年份，**绝对不要硬编码**

   **步骤 2.2：检查修改的文件**
   - `run_command("git status --short")` - 查看修改的文件
   - `run_command("git diff --cached --name-only")` - 查看已暂存的文件

   **步骤 2.3：检查版权头**
   - 对于每个修改的文件，检查是否包含版权头：
     ```bash
     grep -l "Copyright" <modified_file>
     ```
   - 如果包含版权头，检查年份：
     ```bash
     grep "Copyright.*[0-9]\{4\}" <modified_file>
     ```

   **步骤 2.4：更新版权头年份**
   - 如果版权头年份不是当前年份，使用 `Edit` 工具更新
   - 常见格式：
     - `Copyright (C) 2024-2025` → `Copyright (C) 2024-<CURRENT_YEAR>`
     - `Copyright (C) 2024` → `Copyright (C) 2024-<CURRENT_YEAR>`
   - **重要**：只更新当前修改的文件，不批量更新所有文件

   **示例**：
   ```python
   # 假设当前年份为 2026
   Edit(
     file_path="src/example.java",
     old_string="Copyright (C) 2024-2025 FIT Framework",
     new_string="Copyright (C) 2024-2026 FIT Framework"
   )
   ```

   **检查清单**：
   - [ ] 已动态获取当前年份
   - [ ] 已检查所有修改文件的版权头
   - [ ] 已更新过时的版权年份
   - [ ] 只更新了修改的文件，未批量更新
   - [ ] 未硬编码年份

3. 识别未追踪的文件。
   - 如果有未追踪的文件（排除 `.agent/` 或 `.claude/` 如果它们被忽略），询问用户是否应该添加它们。

4. 将文件添加到暂存区。
   - `run_command("git add <files>")`

5. 提交变更。
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
