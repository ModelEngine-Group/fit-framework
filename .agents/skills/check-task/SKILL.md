---
name: check-task
description: 查看任务的当前状态、进度和上下文文件。当用户要求查看任务状态、检查任务进度时触发。参数为 task-id。
---

# 查看任务状态

## 执行步骤

1. 查找任务文件（按优先级搜索 active/blocked/completed 目录）。

2. 读取 task.md 中的元数据。

3. 检查上下文文件: analysis.md, plan.md, implementation.md, review.md。

4. 输出状态报告:
   - 基本信息（ID、标题、类型、创建时间）
   - 当前状态（工作流、当前步骤、执行者）
   - 工作流进度（各步骤状态）
   - 上下文文件存在状态
   - 下一步建议

## 下一步建议规则

- 需求分析完成 -> plan-task
- 技术方案完成 -> 等待人工审查后 implement-task
- 实现完成 -> review-task
- 审查通过 -> commit
- 审查需修改 -> refine-task
- 任务阻塞 -> 显示阻塞原因
