提交当前变更到 Git。

**用法：**
- `/commit` - 交互式提交（会询问提交信息）
- `/commit <message>` - 直接提交并使用指定消息

**执行步骤：**

1. **查看当前变更**
   ```
   git status
   git diff --stat
   ```

2. **如果有未追踪的文件，询问用户是否添加**
   - 列出所有 untracked 文件
   - 排除 `.claude/` 目录
   - 让用户确认要添加哪些文件

3. **添加文件到暂存区**
   ```
   git add <files>
   ```

4. **创建提交**
   - 如果用户提供了消息，使用该消息
   - 如果没有消息，根据变更内容生成合适的提交消息
   - 提交消息格式遵循项目规范
   - 添加 Co-Authored-By 签名

   ```
   git commit -m "$(cat <<'EOF'
   <提交消息>

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude <noreply@anthropic.com>
   EOF
   )"
   ```

5. **显示提交结果**
   ```
   git log -1 --stat
   git status
   ```

**注意事项：**
- 不要提交包含敏感信息的文件（.env, credentials 等）
- 确保提交消息清晰描述了变更内容
- 遵循项目的 commit message 规范
