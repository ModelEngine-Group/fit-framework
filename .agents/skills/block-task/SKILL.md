---
name: block-task
description: 标记任务为阻塞状态并记录阻塞原因，移动到 blocked 目录。当用户报告任务被阻塞、遇到无法解决的问题、需要等待外部依赖时触发。参数为 task-id 和可选的阻塞原因。
---

# 标记任务阻塞

## 使用场景

- 编译失败且无法修复
- 测试失败且原因不明
- 依赖库存在 Bug
- 需求不明确，需要澄清
- 等待外部依赖

## 执行步骤

1. 验证任务存在:
   ```bash
   test -f .ai-workspace/active/<task-id>/task.md && echo "任务存在" || echo "任务不存在"
   ```

2. 分析并记录阻塞原因（类型: 技术问题/需求问题/资源问题/决策问题）。

3. 更新 task.md: status -> blocked, blocked_at, blocked_reason。

4. 添加"阻塞信息"章节。

5. 移动到阻塞目录:
   ```bash
   mkdir -p .ai-workspace/blocked && mv .ai-workspace/active/<task-id> .ai-workspace/blocked/
   ```

6. 解除阻塞时:
   ```bash
   mv .ai-workspace/blocked/<task-id> .ai-workspace/active/
   ```
