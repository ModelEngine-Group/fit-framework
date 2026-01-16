---
description: 创建 Pull Request
usage: /create-pr <target-branch>
argument-hint: <target-branch>
---

# Create PR Command

## 使用前：自动识别仓库

命令会默认使用当前工作目录所在的 Git 仓库作为目标，无需传入仓库参数。若当前目录不在 Git 仓库内，请先 `cd` 到目标仓库根目录后再执行。

文中所有路径示例默认以仓库根目录为基准。

## 功能说明

创建 Pull Request 到指定分支。目标分支必须由用户明确提供。

## 用法

- `/create-pr main` - 创建PR到 main 分支
- `/create-pr 3.6.x` - 创建PR到 3.6.x 分支
- `/create-pr <target-branch>` - 创建PR到指定目标分支

## 执行步骤

### 1. 确认目标分支

- 目标分支必须由用户明确提供（如 `main`, `3.5.x`, `develop`）
- 如果未提供目标分支，提示用户补充并停止执行
- 命令格式为 `/create-pr <target-branch>`，参数部分即为合入的目标分支

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
- PR 结尾必须添加：`🤖 Generated with [Codex CLI](https://developers.openai.com/codex/cli)`

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
