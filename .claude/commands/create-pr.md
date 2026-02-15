---
name: "create-pr"
description: "创建 Pull Request"
usage: "/create-pr [branch-name]"
---

# Create PR Command

## 功能说明

创建 Pull Request 到指定分支（默认: 当前分支）。

## 用法

- `/create-pr` - 创建PR到当前分支
- `/create-pr main` - 创建PR到 main 分支
- `/create-pr <branch-name>` - 创建PR到指定分支

## 执行步骤

### 1. 确定目标分支

- 如果用户提供了参数（如 `main`, `3.5.x`, `develop` 等），使用该参数作为目标分支
- 如果没有参数，自动推断目标分支：
  ```bash
  git branch --show-current
  git log --oneline --decorate --first-parent -20
  ```
  推断规则：
  - 当前在核心分支上（main 或版本号分支如 大版本号.小版本号.x（如 3.6.x））→ 目标分支即为当前分支
  - 当前在特性分支上 → 从 log 中的分支标记找到最近的父核心分支作为目标
  - 无法确定时 → 询问用户

### 2. 读取 PR 模板

必须执行：
```bash
Read(".github/PULL_REQUEST_TEMPLATE.md")
```

### 3. 查看最近 3 个 merged PR 作为参考

必须执行：
```bash
gh pr list --limit 3 --state merged --json number,title,body
```

### 4. 分析当前分支的完整变更

- 运行 `git status` 查看当前状态
- 运行 `git log <target-branch>..HEAD --oneline` 查看所有提交
- 运行 `git diff <target-branch>...HEAD --stat` 查看变更统计
- 运行 `git diff <target-branch>...HEAD` 查看详细变更（如果需要）

### 5. 检查远程分支状态

```bash
git rev-parse --abbrev-ref --symbolic-full-name @{u}
```

### 6. 如果分支未推送，先推送

```bash
git push -u origin <current-branch>
```

### 7. 根据模板创建 PR

- 按照 `.github/PULL_REQUEST_TEMPLATE.md` 格式填写所有部分
- 参考最近的 PR 格式和风格
- 使用 HEREDOC 格式传递 body
- PR 结尾必须添加：`🤖 Generated with [Claude Code](https://claude.com/claude-code)`

```bash
gh pr create --base <target-branch> --title "<标题>" --body "$(cat <<'EOF'
<完整的PR描述>
EOF
)"
```

## 注意事项

- 必须严格遵循 PR 模板格式
- 所有必填项都要填写完整
- 参考最近的 merged PR 的格式和风格
- 确保 PR 标题格式正确（如：`[模块名] 简短描述`）

## 相关命令

- `/sync-pr <task-id>` - 同步进度到 PR
- `/commit` - 提交代码
- `/review-task` - 代码审查
