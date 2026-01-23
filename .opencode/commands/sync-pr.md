---
description: 将任务处理进度同步到 Pull Request 评论
agent: general
subtask: false
---

将任务 $1 的处理进度摘要同步到对应的 Pull Request 评论区中。

执行以下步骤:

1. 查找任务文件:
   - 在 .ai-workspace/active/$1/ 查找
   - 如果不存在,检查 completed/ 和 blocked/ 目录
   - 读取 task.md,获取 pr_number 字段

2. 读取上下文文件:
   - analysis.md - 需求分析
   - plan.md - 技术方案
   - implementation.md - 实现报告
   - review.md - 审查报告

3. 获取当前时间:
   !`date '+%Y-%m-%d %H:%M:%S'`

4. 生成进度摘要:

基本格式（使用步骤3获取的时间）:
```markdown
## 🤖 开发进度更新

**任务ID**: $1
**更新时间**: <当前时间>
**当前状态**: <状态描述>

### ✅ 已完成

- [x] 需求分析 - <完成时间>
  - <核心要点摘要 1-2 条>
- [x] 技术方案设计 - <完成时间>
  - <方案选择和关键决策 1-2 条>
- [x] 代码实现 - <完成时间>
  - 修改文件: <数量>
  - 新增测试: <数量>
- [ ] 代码审查(进行中)
- [ ] 最终合并

### 📋 当前进展

<当前步骤的详细说明>

### 🎯 下一步

<下一步计划>

### 📊 技术要点

<关键的技术决策和实现细节,方便审查者理解>

### 📂 相关文档

- 任务文件: .ai-workspace/<status>/$1/task.md
- 需求分析: .ai-workspace/<status>/$1/analysis.md
- 技术方案: .ai-workspace/<status>/$1/plan.md
- 实现报告: .ai-workspace/<status>/$1/implementation.md

---
*由 OpenCode 自动生成 - [任务管理系统](../.ai-agents/README.md)*
```

**摘要原则**:
- **面向审查者**: 突出技术决策和实现要点
- **简洁清晰**: 每个阶段只提取核心要点
- **逻辑连贯**: 按开发流程展示进展
- **便于审查**: 说明关键变更的原因和影响

5. 同步到 PR:
   使用 gh 命令添加评论，将生成的进度摘要作为 body。
   
   执行命令:
   !`gh pr comment <pr-number> --body "<生成的进度摘要>"`
   
   或使用 HEREDOC 格式（推荐用于多行内容）:
   !`gh pr comment <pr-number> --body "$(cat <<'EOF'
<生成的进度摘要>
EOF
)"`

6. 更新任务状态:
   - 在 task.md 中添加或更新 last_synced_to_pr_at 字段

7. 告知用户:
   ```
   ✅ 任务进度已同步到 PR #<pr-number>
   
   **同步内容**:
   - 已完成步骤: <数量>
   - 当前状态: <状态>
   - 下一步: <下一步说明>
   
   **查看链接**:
   https://github.com/<owner>/<repo>/pull/<pr-number>
   ```

**同步时机**:
- 代码实现完成,准备审查时
- 处理完审查反馈后
- 重大进展或决策变更时
- PR 长时间等待审查时的进度提醒

**注意事项**:
- 任务文件中必须有 pr_number 字段
- 避免频繁同步,建议在完成重要阶段后同步
- 与 /sync-issue 的区别: /sync-pr 面向代码审查者,/sync-issue 面向项目管理者
