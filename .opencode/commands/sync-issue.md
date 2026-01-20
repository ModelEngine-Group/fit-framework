---
description: 将任务处理进度同步到 GitHub Issue 评论
agent: general
subtask: false
---

将任务 $1 的处理进度摘要同步到对应的 GitHub Issue 评论中。

执行以下步骤:

1. 查找任务文件:
   - 在 .ai-workspace/active/$1/ 查找
   - 如果不存在,检查 blocked/ 和 completed/ 目录
   - 读取 task.md,获取 issue_number 字段

2. 读取上下文文件:
   - analysis.md - 需求分析
   - plan.md - 技术方案
   - implementation.md - 实现报告
   - review.md - 审查报告

3. 生成进度摘要:

基本格式:
```markdown
## 🤖 任务进度更新

**任务ID**: $1
**更新时间**: !`date '+%Y-%m-%d %H:%M:%S'`
**当前状态**: <状态描述>

### ✅ 已完成

- [x] 需求分析 - <完成时间>
  - <核心要点摘要 1-2 条>
- [x] 技术方案设计 - <完成时间>
  - <方案选择和关键决策 1-2 条>
- [ ] 代码实现(进行中)
- [ ] 代码审查
- [ ] 最终提交

### 📋 当前进展

<当前步骤的详细说明>

### 🎯 下一步

<下一步计划>

### 📂 相关文件

- 任务文件: .ai-workspace/<status>/$1/task.md
- 需求分析: .ai-workspace/<status>/$1/analysis.md
- 技术方案: .ai-workspace/<status>/$1/plan.md

---
*由 OpenCode 自动生成 - [任务管理系统](../.ai-agents/README.md)*
```

**摘要原则**:
- **简洁**: 每个阶段只提取核心要点
- **逻辑清晰**: 按时间顺序展示进展
- **突出关键决策**: 技术方案选择、重要发现等
- **面向人类阅读**: 避免技术细节,使用易懂的语言

4. 同步到 Issue:
   ```bash
   gh issue comment <issue-number> --body "$(cat <<'EOF'
   <生成的进度摘要>
   EOF
   )"
   ```

5. 更新任务状态:
   - 在 task.md 中添加或更新 last_synced_at 字段

6. 告知用户:
   ```
   ✅ 任务进度已同步到 Issue #<issue-number>
   
   **同步内容**:
   - 已完成步骤: <数量>
   - 当前状态: <状态>
   - 下一步: <下一步说明>
   
   **查看链接**:
   https://github.com/<owner>/<repo>/issues/<issue-number>
   ```

**注意事项**:
- 任务文件中必须有 issue_number 字段
- 避免频繁同步,建议在完成重要阶段后同步
- 使用 Markdown 格式和 emoji 增强可读性
