提交当前变更到 Git。

**此命令已迁移到官方插件，将调用 `commit-commands` 插件。**

**用法：**
- `/commit` - 创建 Git 提交
- `/commit-commands:commit` - 直接使用插件命令
- `/commit-commands:commit-push-pr` - 一键提交+推送+创建PR

**实际执行：**
调用 `/commit-commands:commit` 插件命令

**插件功能：**
- 自动分析变更内容
- 生成符合规范的提交消息
- 支持交互式和直接提交模式
- 添加 Co-Authored-By 签名
- 自动检测敏感信息

**扩展用法：**
如需一键完成提交→推送→创建PR，使用：
```
/commit-commands:commit-push-pr
```

**注意事项：**
- 不要提交包含敏感信息的文件（.env, credentials 等）
- 确保提交消息清晰描述了变更内容
- 遵循项目的 commit message 规范

---

## ⚠️ 提交前的版权头年份检查（CRITICAL）

**强制要求**：在执行提交之前，**必须**检查并更新所有修改文件的版权头年份。参见项目规则第 5 条。

### 检查流程

**步骤 1：获取当前年份**
```bash
# 动态获取当前年份（绝对不要硬编码）
date +%Y
# 输出示例：2026
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
- `Copyright (C) 2024-2025` → `Copyright (C) 2024-<CURRENT_YEAR>`
- `Copyright (C) 2024` → `Copyright (C) 2024-<CURRENT_YEAR>`
- `Copyright (C) 2025` → `Copyright (C) <CURRENT_YEAR>`（如果已是当前年）

**示例：**
```bash
# 假设当前年份为 2026

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
# 1. 获取当前年份（AI 执行时只用 date +%Y）
date +%Y
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
1. 使用 `/commit-commands:commit-push-pr` 一键完成提交+推送+创建PR
2. 或手动推送后使用 `gh pr create`
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
