---
description: 提交当前变更到 Git（提供最佳实践指南）
usage: /prompts:commit
---

# Commit Command

提交当前变更到 Git。

**此命令提供 Git 提交的最佳实践指南。**

**用法：**
- `/prompts:commit` - 查看提交指南

**功能说明：**

本命令会引导你完成以下步骤：
1. 检查并更新版权头年份（CRITICAL）
2. 分析变更内容并生成提交信息
3. 执行 Git 提交操作
4. 更新任务状态（如适用）

**注意事项：**
- 不要提交包含敏感信息的文件（.env, credentials 等）
- 确保提交消息清晰描述了变更内容
- 遵循项目的 commit message 规范
- 提交前必须检查版权头年份（见下文）

---

## ⚠️ 提交前确认用户本地修改（CRITICAL）

**强制要求**：在执行任何编辑操作之前，**必须**先检查用户的本地修改，避免覆盖用户的工作。

### 检查流程

**步骤 0：检查用户本地修改**

```bash
# 查看所有已修改的文件
git status --short

# 查看每个文件的具体修改内容
git diff
```

**处理规则**：

1. **仔细阅读 `git diff` 的输出**，理解用户已经做了哪些修改
2. **在用户修改的基础上**进行增量编辑，不要覆盖用户的实现
3. **如果你计划的修改与用户的修改有冲突**，必须先询问用户：
   ```
   我发现该文件已有本地修改：
   - 你的修改：[描述用户的修改]
   - 我计划的修改：[描述你计划的修改]
   请确认如何处理。
   ```
4. **禁止**按自己的想法重写用户已经实现的代码
5. **禁止**添加用户没有要求的"改进"

---

## ⚠️ 提交前的版权头年份检查（CRITICAL）

**强制要求**：在执行提交之前，**必须**检查并更新所有修改文件的版权头年份。参见项目规则第 5 条。

### 检查流程

**步骤 1：自动获取当前年份**
```bash
# 动态获取当前年份（绝对不要硬编码）

## 使用前：自动识别仓库

命令会默认使用当前工作目录所在的 Git 仓库作为目标，无需传入仓库参数。若当前目录不在 Git 仓库内，请先 `cd` 到目标仓库根目录后再执行。

文中所有路径示例默认以仓库根目录为基准。

current_year=$(date +%Y)
echo "当前年份: $current_year"
```

**步骤 2：检查修改的文件**
```bash
# 查看即将提交的文件
git status --short

# 或查看已暂存的文件
git diff --cached --name-only
```

**步骤 3：检查版权头**

对于每个修改的文件：
```bash
# 检查文件是否包含版权头
grep -l "Copyright" <modified_file>

# 查看版权年份
grep "Copyright.*[0-9]\{4\}" <modified_file>
```

**步骤 4：更新版权年份**

如果文件包含版权头且年份不是当前年份，使用 `Edit` 工具更新：

**常见格式：**
- `Copyright (C) 2024-2025` → `Copyright (C) 2024-<current_year>`
- `Copyright (C) 2024` → `Copyright (C) 2024-<current_year>`
- `Copyright (C) 2025` → `Copyright (C) <current_year>`（如果已是当前年）

**示例：**
```bash
# 示例输出：命令返回 2026

# 格式 1：年份范围
Edit(
  file_path="src/example.java",
  old_string="Copyright (C) 2024-2025 FIT Framework",
  new_string="Copyright (C) 2024-2026 FIT Framework"
)

# 格式 2：单一年份
Edit(
  file_path="src/another.java",
  old_string="Copyright (C) 2024 FIT Framework",
  new_string="Copyright (C) 2024-2026 FIT Framework"
)
```

### 检查清单

在执行 `git commit` 之前，必须确认：

- [ ] 已使用 `date +%Y` 动态获取当前年份
- [ ] 不需要也不允许手动输入年份
- [ ] 已检查所有即将提交的修改文件
- [ ] 对于包含版权头的文件，已检查年份是否为当前年份
- [ ] 如果年份不是当前年份，已使用 `Edit` 工具更新
- [ ] **绝对不要**硬编码年份（如 2026）
- [ ] **只更新修改的文件**，不批量更新项目中所有文件

### 为什么必须这样做

- **法律合规**：确保版权声明的准确性和法律有效性
- **项目规范**：遵循 FIT Framework 的项目规范
- **自动化**：动态获取年份确保在任何时间点执行都是正确的
- **避免遗漏**：提交前检查确保不会遗漏任何文件

### 完整示例

```bash
# 1. 获取当前年份
current_year=$(date +%Y)
# 输出：2026

# 2. 查看修改的文件
git status --short
# M src/main/java/com/fit/Example.java
# M src/test/java/com/fit/ExampleTest.java

# 3. 检查第一个文件的版权头
grep "Copyright" src/main/java/com/fit/Example.java
# Copyright (C) 2024-2025 FIT Framework

# 4. 更新版权头（使用 Edit 工具）
Edit(
  file_path="src/main/java/com/fit/Example.java",
  old_string="Copyright (C) 2024-2025 FIT Framework",
  new_string="Copyright (C) 2024-2026 FIT Framework"
)

# 5. 检查第二个文件的版权头
grep "Copyright" src/test/java/com/fit/ExampleTest.java
# Copyright (C) 2024-2025 FIT Framework

# 6. 更新版权头（使用 Edit 工具）
Edit(
  file_path="src/test/java/com/fit/ExampleTest.java",
  old_string="Copyright (C) 2024-2025 FIT Framework",
  new_string="Copyright (C) 2024-2026 FIT Framework"
)

# 7. 验证更新
grep "Copyright" src/main/java/com/fit/Example.java
# Copyright (C) 2024-2026 FIT Framework

# 8. 现在可以安全提交
/commit
```

### 违反此规则的后果

如果不更新版权头年份：
- 版权声明过时，可能影响法律效力
- 违反项目规范，PR 审查不通过
- 需要额外的修复提交，增加工作量

**这是一个 CRITICAL 规则，必须在每次提交前执行。**

---

## ⚠️ 提交后的任务状态更新（CRITICAL）

提交代码后，你**必须**根据情况更新任务状态。参见规则 7。

### 情况 1：这是最终提交（任务完成）

如果这是任务的最后一次提交，所有工作已完成：

```bash
# 执行 /complete-task 归档任务
/complete-task <task-id>
```

**检查清单**：
- [ ] 所有代码已提交
- [ ] 所有测试通过
- [ ] 代码审查已通过
- [ ] 任务的所有工作流步骤已完成
- [ ] 已执行 `/complete-task` 归档任务

### 情况 2：还需要继续工作（任务未完成）

如果提交后还有后续工作（如等待审查、需要修复等）：

**必须更新**：
- 更新 `task.md` 中的 `updated_at` 为当前时间
- 在任务中记录本次提交的内容和下一步计划

**示例**：
```markdown
## 交接信息

### 最近的提交

- **提交时间**: {当前时间}
- **提交内容**: {简要描述}
- **提交哈希**: {commit-hash}
- **下一步**: {说明接下来要做什么}
```

### 情况 3：提交后需要审查

如果提交后需要代码审查：

**必须更新**：
- 更新 `task.md` 中的 `current_step` 为 `code-review`
- 更新 `task.md` 中的 `updated_at` 为当前时间
- 在"工作流进度"中标记实现步骤为完成 ✅
- 通知用户进行代码审查

**下一步命令**：
```bash
/review-task <task-id>
```

### 情况 4：提交后需要创建 PR

如果提交后需要创建 Pull Request：

**建议流程**：
1. 手动推送分支：`git push origin <branch-name>`
2. 使用 `gh pr create` 创建 PR
3. PR 创建后，更新任务状态

**必须更新**：
- 更新 `task.md` 中的 `updated_at`
- 在任务中记录 PR 编号
- 如果 PR 合并后任务完成，执行 `/complete-task`

### 违反此规则的后果

如果提交后不更新任务状态：
- 任务状态与实际进度不一致
- 无法追踪任务是否完成
- 已完成的任务可能被遗忘在 `active` 目录

**这是一个 CRITICAL 要求，必须遵守。**

---

## 执行 Git 提交

在完成版权头检查后，执行以下步骤进行提交。

### 步骤 1：查看变更

```bash
# 查看工作区状态
git status

# 查看已暂存的变更
git diff --cached

# 如果还没暂存，先暂存文件
git add <file-path>
```

### 步骤 2：分析变更并生成提交信息

参考最近的提交风格：
```bash
# 查看最近 10 个提交
git log -10 --oneline

# 查看最近的提交消息格式
git log -3 --format="%s"
```

**提交消息格式建议**：
- 使用项目规范的格式（Conventional Commits）
- 采用 `<type>(<scope>): <subject>`，subject 使用中文且约 20 字以内
- scope 为模块名（可省略）
- 第一行简明扼要（50字符以内）
- 如需详细说明，空一行后添加正文
- 说明改动的原因，而非改动的内容

### 步骤 3：创建提交

使用 HEREDOC 格式保证提交消息格式正确：

```bash
git commit -m "$(cat <<'EOF'
<type>(<scope>): <subject>

<body>
EOF
)"
```

**示例**：
```bash
git commit -m "$(cat <<'EOF'
docs: 更新 Codex 命令配置说明

- 移除对 Claude Code 插件的引用
- 添加基于 Git 命令的实际操作步骤
- 保留版权头检查和任务状态更新规则

Co-Authored-By: Codex CLI <noreply@openai.com>
EOF
)"
```

### 步骤 4：验证提交

```bash
# 查看最后一次提交
git log -1

# 查看提交的内容
git show HEAD
```

### 步骤 5：推送到远程（如需要）

```bash
# 推送到远程分支
git push origin <branch-name>

# 如果是新分支，使用 -u 设置上游
git push -u origin <branch-name>
```

---

## 完整示例

以下是完整的提交流程示例：

```bash
# 1. 获取当前年份
current_year=$(date +%Y)
echo "当前年份: $current_year"

# 2. 查看修改的文件
git status --short

# 3. 检查版权头（假设修改了 Example.java）
grep "Copyright" src/main/java/com/fit/Example.java

# 4. 如需要，更新版权头（使用 Edit 工具）

# 5. 暂存文件
git add src/main/java/com/fit/Example.java

# 6. 查看暂存的变更
git diff --cached

# 7. 参考最近的提交格式
git log -3 --oneline

# 8. 创建提交
git commit -m "$(cat <<'EOF'
feat(core): 添加用户认证功能

- 实现 JWT 令牌生成和验证
- 添加登录和登出接口
- 更新相关测试用例

Co-Authored-By: Codex CLI <noreply@openai.com>
EOF
)"

# 9. 验证提交
git log -1

# 10. 推送到远程
git push origin feature/user-auth
```

---

## 相关命令

- `/prompts:review-task <task-id>` - 代码审查（提交前）
- `/prompts:create-pr <target-branch>` - 创建 Pull Request（提交后）
- `/prompts:complete-task <task-id>` - 归档任务（工作完成后）

---

## 常见问题

### Q: 如何撤销上一次提交？

```bash
# 撤销提交但保留更改
git reset --soft HEAD~1

# 撤销提交并删除更改
git reset --hard HEAD~1
```

### Q: 如何修改上一次提交消息？

```bash
# 如果还没推送到远程
git commit --amend

# 修改提交消息后强制推送（谨慎使用）
git push --force origin <branch-name>
```

### Q: 提交后发现遗漏了文件怎么办？

```bash
# 暂存遗漏的文件
git add <forgotten-file>

# 追加到上一次提交（如果还没推送）
git commit --amend --no-edit
```
