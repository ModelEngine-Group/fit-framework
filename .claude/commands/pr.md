创建 Pull Request。

**用法：**
- `/pr` - 创建PR到默认分支 3.6.x
- `/pr main` - 创建PR到 main 分支
- `/pr <branch-name>` - 创建PR到指定分支

**执行步骤：**

1. **解析目标分支**
   - 如果用户提供了参数（如 `main`, `3.5.x`, `develop` 等），使用该参数作为目标分支
   - 如果没有参数，默认使用 `3.6.x` 作为目标分支
   - 从命令参数中提取分支名：命令格式为 `/pr <branch>`，参数部分即为分支名

2. **读取 PR 模板**（必须执行）
   ```
   Read(".github/PULL_REQUEST_TEMPLATE.md")
   ```

3. **查看最近 3 个 merged PR 作为参考**（必须执行）
   ```
   gh pr list --limit 3 --state merged --json number,title,body
   ```

4. **分析当前分支的完整变更**
   - 运行 `git status` 查看当前状态
   - 运行 `git log <target-branch>..HEAD --oneline` 查看所有提交
   - 运行 `git diff <target-branch>...HEAD --stat` 查看变更统计
   - 运行 `git diff <target-branch>...HEAD` 查看详细变更（如果需要）

5. **检查远程分支状态**
   ```
   git rev-parse --abbrev-ref --symbolic-full-name @{u}
   ```

6. **如果分支未推送，先推送**
   ```
   git push -u origin <current-branch>
   ```

7. **根据模板创建 PR**
   - 按照 `.github/PULL_REQUEST_TEMPLATE.md` 格式填写所有部分
   - 参考最近的 PR 格式和风格
   - 使用 HEREDOC 格式传递 body
   - PR 结尾必须添加：`🤖 Generated with [Claude Code](https://claude.com/claude-code)`

   ```
   gh pr create --base <target-branch> --title "<标题>" --body "$(cat <<'EOF'
   <完整的PR描述>
   EOF
   )"
   ```

**注意事项：**
- 必须严格遵循 PR 模板格式
- 所有必填项都要填写完整
- 参考最近的 merged PR 的格式和风格
- 确保 PR 标题格式正确（如：`[模块名] 简短描述`）
