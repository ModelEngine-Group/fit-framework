---
name: review-task
description: 审查任务实现代码，输出代码审查报告。当用户要求审查代码、review 任务、检查实现质量时触发。参数为 task-id。
---

# 代码审查

## 执行步骤

1. 验证前置条件:
   - .ai-workspace/active/<task-id>/task.md 存在
   - .ai-workspace/active/<task-id>/implementation.md 存在

2. 读取上下文: task.md, plan.md, implementation.md, git diff。

3. 执行代码审查:
   - 功能正确性: 实现是否符合技术方案
   - 代码质量: 编码规范、命名、注释、复杂度
   - 测试覆盖: 是否有充分的测试用例
   - 安全性: SQL注入、XSS、权限控制
   - 性能: 算法复杂度、资源使用
   - 边界情况: 空值处理、异常处理

4. 输出审查报告到 review.md:
   - 审查发现（分级: 🔴 Blocker / 🟡 Major / 🟢 Minor）
   - 总结与建议（✅批准 / ⚠️修改后批准 / ❌需要重大修改）

5. 更新任务状态，标记 code-review 为完成。

6. 如果需要修改，提示使用 refine-task skill；如果批准，提示使用 commit skill。
