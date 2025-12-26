# AI 协作工作区

这是一个临时工作目录，用于 AI 智能体之间的任务跟踪和上下文共享。

## 目录说明

- `tasks/` - 任务状态跟踪
  - `active/` - 进行中的任务
  - `blocked/` - 被阻塞的任务
  - `completed/` - 已完成的任务（可定期清理）

- `context/` - 任务上下文文件
  - `{task-id}/` - 每个任务的上下文目录
    - `analysis.md` - 需求分析
    - `plan.md` - 技术方案
    - `implementation.md` - 实现报告
    - `review.md` - 审查报告

- `logs/` - 执行日志
  - `{date}-{agent}-{task}.log` - 日志文件

## 使用说明

### 创建新任务

```bash
# 复制模板
cp .ai-agents/templates/task.md .ai-workspace/tasks/active/TASK-$(date +%Y%m%d-%H%M%S).md

# 编辑任务
vim .ai-workspace/tasks/active/TASK-*.md
```

### 任务状态流转

```bash
# 任务被阻塞
mv .ai-workspace/tasks/active/TASK-001.md .ai-workspace/tasks/blocked/

# 任务完成
mv .ai-workspace/tasks/active/TASK-001.md .ai-workspace/tasks/completed/
```

### 创建任务上下文

```bash
# 创建任务上下文目录
mkdir -p .ai-workspace/context/TASK-001

# AI 在这里创建各阶段的文档
# - analysis.md
# - plan.md
# - implementation.md
# - review.md
```

## 注意事项

⚠️ **重要**:
- 这个目录下的所有文件（除了.gitkeep和README）都会被 git ignore
- 不要在这里保存重要文件
- 已完成的任务可以定期清理
- 建议保留最近30天的任务记录

## 清理建议

```bash
# 清理30天前完成的任务
find .ai-workspace/tasks/completed -name "*.md" -mtime +30 -delete
find .ai-workspace/context -type d -mtime +30 -exec rm -rf {} +

# 清理所有日志
rm -f .ai-workspace/logs/*.log
```

## 示例任务

查看 `.ai-agents/templates/task.md` 了解任务文件的格式。
