---
name: refine-task
description: 处理代码审查反馈并修复审查中发现的问题。当用户要求修复审查问题、处理 review 反馈时触发。参数为 task-id。
---

# 修复审查问题

## 执行步骤

1. 验证前置条件:
   - .ai-workspace/active/<task-id>/task.md 存在
   - .ai-workspace/active/<task-id>/review.md 存在

2. 读取审查报告，整理问题:
   - 🔴 Blocker 问题（必须修复）
   - 🟡 Major 问题（建议修复）
   - 🟢 Minor 问题（可选修复）

3. 按优先级逐项修复，修复后运行相关测试验证。

4. 运行测试验证:
   ```bash
   mvn test -pl :<module-name>
   ```

5. 更新 implementation.md，追加"修复记录"章节。

6. 更新任务状态。

7. 提示重新审查（review-task）或直接提交（commit）。

**注意**: 严格按照审查报告修复，不要添加额外变更。不要自动 git commit。
