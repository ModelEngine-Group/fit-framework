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
