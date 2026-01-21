---
description: 标记任务完成并归档到 completed 目录
usage: /complete-task <task-id>
argument-hint: <task-id>
---

# Complete Task Command

## 使用前：自动识别仓库

命令会默认使用当前工作目录所在的 Git 仓库作为目标，无需传入仓库参数。若当前目录不在 Git 仓库内，请先 `cd` 到目标仓库根目录后再执行。

文中所有路径示例默认以仓库根目录为基准。

## 功能说明

标记任务为已完成状态，更新任务元数据，并将任务从 `active` 目录移动到 `completed` 目录进行归档。

## ⚠️ CRITICAL: 状态更新要求

执行此命令后，你**必须**立即更新任务状态并移动任务目录。参见规则 7。

## 前置条件

在执行此命令前，请确认以下条件全部满足：

- [ ] 所有工作流步骤已完成
- [ ] 代码已审查通过（review.md 显示批准）
- [ ] 代码已提交到 Git
- [ ] 代码已合并到目标分支（如果需要）
- [ ] 所有测试通过
- [ ] Issue 已同步更新（如果有关联 Issue）
- [ ] PR 已合并（如果有 PR）

**如果以上条件未全部满足，请勿执行此命令。**

## 执行流程

### 1. 验证任务存在

检查任务文件是否存在：
```bash
ls -la .ai-workspace/active/{task-id}/task.md
```

如果任务不在 `active` 目录，检查是否已经在 `completed` 或 `blocked` 目录。

### 2. 读取并验证任务状态

读取 `.ai-workspace/active/{task-id}/task.md`，检查：

**工作流进度**：
- 所有步骤是否标记为完成 ✅
- `current_step` 是否为最后一步（如 `finalize` 或 `code-review`）

**任务状态**：
- `status` 是否为 `active`（即将改为 `completed`）
- 是否有未解决的阻塞问题

**文件完整性**：
- [ ] `analysis.md` 存在（如果是 feature-development 或 security-fix）
- [ ] `plan.md` 存在（如果是 feature-development）
- [ ] `implementation.md` 存在
- [ ] `review.md` 存在且显示批准

如果发现任何问题，提示用户并**停止执行**。

### 3. 更新任务状态 (CRITICAL)

**必须更新** `.ai-workspace/active/{task-id}/task.md`：

```yaml
status: completed
current_step: finalize
updated_at: {当前时间，格式: yyyy-MM-dd HH:mm:ss}
completed_at: {当前时间，格式: yyyy-MM-dd HH:mm:ss}
```

**在工作流进度中标记所有步骤完成**：
```markdown
## 工作流进度

- [x] requirement-analysis (codex, {日期})
- [x] technical-design (codex, {日期})
- [x] implementation (codex, {日期})
- [x] code-review (codex, {日期})
- [x] finalize (codex, {当前日期})  ← 标记为完成
```

**添加完成总结**（在 task.md 末尾添加）：
```markdown
---

## 任务完成总结

### 完成信息

- **完成时间**: {当前时间}
- **完成者**: {当前AI}
- **关联 PR**: #{pr-number}（如果有）
- **关联 Issue**: #{issue-number}（如果有）
- **目标分支**: {分支名}

### 交付成果

- [x] 需求分析文档: `analysis.md`
- [x] 技术方案文档: `plan.md`
- [x] 实现报告: `implementation.md`
- [x] 代码审查报告: `review.md`
- [x] 代码提交: {commit-hash}
- [x] PR 合并: #{pr-number}

### 任务完成标准

- [x] 功能完整实现
- [x] 代码审查通过
- [x] 所有测试通过
- [x] 文档完整
- [x] 代码已合并

### 备注

{如有需要，添加备注}
```

### 4. 归档任务 (CRITICAL)

将任务从 `active` 目录移动到 `completed` 目录：

```bash
# 确保 completed 目录存在
mkdir -p .ai-workspace/completed

# 移动任务目录
mv .ai-workspace/active/{task-id} .ai-workspace/completed/
```

**验证移动成功**：
```bash
# 验证源目录不存在
test ! -d .ai-workspace/active/{task-id} && echo "已移除 active 目录" || echo "ERROR: active 目录仍存在"

# 验证目标目录存在
test -d .ai-workspace/completed/{task-id} && echo "已归档到 completed" || echo "ERROR: 归档失败"
```

### 5. 可选：同步到 Issue

如果任务关联了 GitHub Issue，使用 `/sync-issue` 更新 Issue 状态：

```bash
/sync-issue {issue-number}
```

在 Issue 中添加完成总结：
```markdown
✅ 任务已完成

**完成时间**: {当前时间}
**关联 PR**: #{pr-number}
**任务 ID**: {task-id}

所有工作已完成，任务已归档到 `.ai-workspace/completed/{task-id}`。
```

### 6. 可选：更新里程碑

如果任务是里程碑的一部分，更新里程碑进度：
- 在项目管理工具中标记任务完成
- 更新里程碑的完成百分比

### 7. 告知用户

输出格式：
```
🎉 任务 {task-id} 已完成并归档

**任务信息**：
- 任务ID: {task-id}
- 任务类型: {type}
- 工作流: {workflow}
- 完成时间: {当前时间}

**归档位置**：
- `.ai-workspace/completed/{task-id}/`

**关联资源**：
- PR: #{pr-number}（已合并）
- Issue: #{issue-number}（已同步）
- Commit: {commit-hash}

**交付成果**：
- ✅ 需求分析文档
- ✅ 技术方案文档
- ✅ 实现报告
- ✅ 代码审查报告
- ✅ 代码提交并合并

**统计信息**：
- 修改文件: {文件数量}
- 代码行数: +{新增行数} -{删除行数}
- 工作时长: {从创建到完成的时间}

任务已成功归档！🎊
```

## ✅ 完成检查清单

执行此命令后，确认：

- [ ] 已验证所有前置条件满足
- [ ] 已更新 task.md 中的 `status` 为 completed
- [ ] 已更新 task.md 中的 `current_step` 为 finalize
- [ ] 已更新 task.md 中的 `updated_at` 为当前时间
- [ ] 已添加 task.md 中的 `completed_at` 字段
- [ ] 已在"工作流进度"中标记所有步骤为完成 ✅
- [ ] 已添加"任务完成总结"章节
- [ ] 已将任务从 active 移动到 completed 目录
- [ ] 已验证移动成功
- [ ] 如果有关联 Issue，已同步更新
- [ ] 已告知用户任务完成

## 常见问题

### Q: 如果任务还有未完成的步骤怎么办？

A: **不要执行此命令**。请完成所有步骤后再归档任务。如果某个步骤无法完成，应该使用 `/block-task` 标记为阻塞。

### Q: 如果代码已提交但 PR 未合并怎么办？

A: 等待 PR 合并后再执行此命令。任务完成意味着代码已经合并到主分支。

### Q: 如果审查发现问题但选择不修复怎么办？

A: 必须在 `review.md` 中说明原因并获得批准。如果有阻塞问题未修复，不能标记任务完成。

### Q: 已归档的任务还能修改吗？

A: 可以读取但不建议修改。如果发现问题，应该创建新任务进行修复。

### Q: 如果任务是多人协作完成的怎么办？

A: 在"任务完成总结"中列出所有贡献者。`completed_by` 字段记录最后归档操作的执行者。

### Q: 需要同时关闭 Issue 吗？

A: 使用 `/sync-issue` 会更新 Issue 状态，但是否关闭由用户决定。通常任务完成后应该关闭 Issue。

## 注意事项

### 不要过早归档

只有在**真正完成**所有工作后才归档任务。常见的"未完成"情况：
- ❌ 代码已提交但测试失败
- ❌ PR 已创建但未合并
- ❌ 审查未通过
- ❌ 还有已知问题待修复
- ❌ 文档不完整

### 确保数据完整

归档前确认所有文档都已创建：
- `analysis.md`
- `plan.md`
- `implementation.md`
- `review.md`

### 验证移动成功

移动目录后必须验证：
- 源目录 (`active/{task-id}`) 不存在
- 目标目录 (`completed/{task-id}`) 存在且内容完整

## 相关命令

- `/task-status` - 查看任务状态，确认是否满足完成条件
- `/sync-issue` - 同步任务状态到 GitHub Issue
- `/block-task` - 如果发现任务无法完成，使用此命令标记阻塞

## 工作流位置

此命令对应 `.ai-agents/workflows/feature-development.yaml` 中的 **finalize** 步骤。

## 示例

```bash
# 标记 TASK-20260103-135501 为完成并归档
/complete-task TASK-20260103-135501
```

## 回滚操作

如果误操作归档了任务，可以手动回滚：

```bash
# 将任务移回 active 目录
mv .ai-workspace/completed/{task-id} .ai-workspace/active/

# 更新任务状态
# 编辑 task.md，将 status 改回 active，移除 completed_at
```

但请谨慎操作，误归档通常说明流程有问题。
