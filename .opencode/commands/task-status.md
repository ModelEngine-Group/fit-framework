---
description: 查看任务的当前状态和进度
agent: general
subtask: false
---

查看任务 $1 的当前状态和进度。

执行以下步骤:

1. 查找任务文件:
   - 优先在 .ai-workspace/active/$1/ 查找
   - 如果没找到,检查 completed/ 目录
   - 如果还没找到,检查 blocked/ 目录
   - 读取 task.md

2. 检查上下文文件:
   - analysis.md - 需求分析
   - plan.md - 技术方案
   - implementation.md - 实现报告
   - review.md - 审查报告
   - refinement-report.md - 修复报告

3. 分析当前状态:
   - 从 task.md 提取: 任务ID、标题、类型、Issue编号、创建时间、更新时间
   - 从 current_step 确定当前执行到哪个步骤
   - 从工作流进度确定哪些步骤已完成、待执行
   - 从 assigned_to 确定当前负责的 AI
   - 检查是否等待人工审查

4. 输出状态报告:

```
📋 任务状态报告

**基本信息**:
- 任务ID: $1
- 任务标题: <title>
- 任务类型: <type>
- 相关Issue: #<issue-number>
- 创建时间: <created_at>
- 更新时间: <updated_at>

**当前状态**:
- 工作流: <workflow>
- 当前步骤: <current_step>
- 执行者: <assigned_to>
- 状态: <status>

**工作流进度**:
✅ 需求分析 (完成于 <date>)
✅ 技术方案设计 (完成于 <date>)
⏳ 代码实现 (进行中)
⏸️  代码审查 (待开始)
⏸️  问题修复 (待开始)

**上下文文件**:
✅ analysis.md (2.5 KB)
✅ plan.md (8.3 KB)
⏳ implementation.md (进行中)
❌ review.md (未创建)

**文件路径**:
- 任务文件: .ai-workspace/<status>/$1/task.md
- 上下文目录: <task_dir>/

**下一步建议**:
<根据当前状态给出建议>
```

5. 根据当前状态给出智能建议:
   - 如果在需求分析阶段: 继续完成需求分析,完成后使用 /plan $1
   - 如果需求分析完成: 等待人工审查,审查通过后使用 /plan $1
   - 如果在技术方案设计阶段: 继续完成技术方案设计,完成后等待人工审查
   - 如果方案设计完成: 等待人工审查,审查通过后使用 /implement $1
   - 如果在代码实现阶段: 继续完成代码实现,完成后使用 /review $1
   - 如果实现完成: 使用 /review $1 进行代码审查
   - 如果审查完成: 使用 /commit 提交代码或 /refine $1 修复问题
   - 如果任务被阻塞: 显示阻塞原因和解决建议

**注意事项**:
- 使用 emoji 增强可读性
- 突出关键信息
- 提供具体的命令示例
