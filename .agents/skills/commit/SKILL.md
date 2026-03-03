---
name: commit
description: 提交当前变更到 Git，包含版权头年份检查和任务状态更新。当用户要求提交代码、git commit、保存变更时触发。
---

# 提交代码

## 步骤 0：检查用户本地修改（CRITICAL）

```bash
git status --short
git diff
```

仔细阅读 `git diff` 输出，在用户修改的基础上进行增量编辑，不要覆盖用户的实现。

## 步骤 1：版权头年份检查（CRITICAL）

```bash
current_year=$(date +%Y)
echo "当前年份: $current_year"
```

对每个修改的文件检查版权头：
- `Copyright (C) 2024-2025` -> `Copyright (C) 2024-<current_year>`
- `Copyright (C) 2024` -> `Copyright (C) 2024-<current_year>`

## 步骤 2：分析变更并生成提交信息

提交消息格式（Conventional Commits）：
- `<type>(<scope>): <subject>`，subject 使用中文且约 20 字以内
- scope 参考：`.codex/` -> `codex`，`framework/fit/java` -> `fit`，`framework/waterflow` -> `waterflow`，`framework/fel` -> `fel`

Codex 署名：
```
🤖 Generated with Codex

Co-Authored-By: Codex <noreply@openai.com>
```

## 步骤 3：创建提交

```bash
git commit -m "$(cat <<'EOF'
<type>(<scope>): <subject>

<body>

🤖 Generated with Codex

Co-Authored-By: Codex <noreply@openai.com>
EOF
)"
```

## 提交后：任务状态更新

根据情况更新任务状态：
- 最终提交 -> 执行 complete-task 归档任务
- 需要继续 -> 更新 task.md 中的 updated_at
- 需要审查 -> 更新 current_step 为 code-review
