---
name: "task-status"
description: "查看任务的当前状态和进度"
usage: "/task-status <task-id>"
---

# Task Status Command

## 功能说明

查看指定任务的当前状态、进度和上下文文件。

## 执行流程

### 1. 读取任务文件

读取 `.ai-workspace/active/{task-id}/task.md`（如果任务已完成，也检查 `completed/` 目录）

### 2. 检查上下文文件

检查以下文件是否存在：
- `analysis.md` - 需求分析
- `plan.md` - 技术方案
- `implementation.md` - 实现报告
- `review.md` - 审查报告

### 3. 分析当前状态

根据任务文件和上下文文件，确定：
- 当前执行到哪个步骤
- 哪些步骤已完成
- 哪些步骤待执行
- 当前负责的 AI
- 是否等待人工审查

### 4. 输出状态报告

输出格式化的状态信息：

```
📋 任务状态报告

**基本信息**：
- 任务ID: {task-id}
- 任务标题: {title}
- 任务类型: {type}
- 相关Issue: #{issue-number}
- 创建时间: {created_at}
- 更新时间: {updated_at}

**当前状态**：
- 工作流: {workflow}
- 当前步骤: {current_step}
- 执行者: {assigned_to}
- 状态: {status}

**工作流进度**：
✅ 需求分析 (完成于 {date})
✅ 技术方案设计 (完成于 {date})
⏳ 代码实现 (进行中)
⏸️  代码审查 (待开始)
⏸️  问题修复 (待开始)

**上下文文件**：
✅ analysis.md (2.5 KB)
✅ plan.md (8.3 KB)
⏳ implementation.md (进行中)
❌ review.md (未创建)

**文件路径**：
- 任务文件: .ai-workspace/active/{task-id}/task.md
- 上下文目录: .ai-workspace/active/{task-id}/

**下一步建议**：
{根据当前状态给出建议}
```

## 参数说明

- `<task-id>`: 任务ID，格式为 TASK-{yyyyMMdd-HHmmss}（必需）

## 使用示例

```bash
# 查看任务状态
/task-status TASK-20251227-104654

# 也可以简写
/task-status TASK-20251227-104654
```

## 状态说明

### 任务状态

- `active` - 进行中
- `blocked` - 被阻塞
- `completed` - 已完成

### 步骤状态

- ✅ 已完成
- ⏳ 进行中
- ⏸️ 待开始
- ❌ 失败/阻塞

### 下一步建议

根据当前状态自动生成建议：

**如果在需求分析阶段**：
```
继续完成需求分析，完成后使用：
/plan-task {task-id}
```

**如果需求分析完成，等待人工审查**：
```
⚠️  等待人工审查需求分析
审查通过后使用：
/plan-task {task-id}
```

**如果在技术方案设计阶段**：
```
继续完成技术方案设计，完成后等待人工审查
```

**如果方案设计完成，等待人工审查**：
```
⚠️  等待人工审查技术方案
审查通过后使用：
/implement-task {task-id}
```

**如果在代码实现阶段**：
```
继续完成代码实现，完成后使用：
/review-task {task-id}
```

**如果实现完成，待审查**：
```
使用以下命令进行代码审查：
/review-task {task-id}
```

**如果审查完成，待提交**：
```
使用以下命令提交代码：
/commit
```

**如果任务被阻塞**：
```
⚠️  任务被阻塞
阻塞原因: {原因}
请解决阻塞问题后继续
```

## 注意事项

1. **多目录查找**：
   - 优先在 `active/` 目录查找
   - 如果没找到，检查 `completed/` 目录
   - 如果还没找到，检查 `blocked/` 目录

2. **简洁输出**：
   - 输出信息清晰、结构化
   - 使用 emoji 增强可读性
   - 突出关键信息

3. **智能建议**：
   - 根据当前状态给出下一步建议
   - 提示人工检查点
   - 提供具体的命令示例

## 相关命令

- `/analyze-issue <number>` - 分析 Issue
- `/plan-task <task-id>` - 设计技术方案
- `/implement-task <task-id>` - 实施任务

## 错误处理

- 任务不存在：提示 "任务 {task-id} 不存在，请检查任务ID"
- 任务文件损坏：提示 "任务文件格式错误"
