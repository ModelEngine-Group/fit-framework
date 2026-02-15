---
description: 审查任务实现并输出代码审查报告
argument-hint: <task-id> [--pr-number]
---

审查任务 $1 的实现代码,输出代码审查报告。

执行以下步骤:

1. 验证前置条件:
   检查必需文件是否存在:
   - .ai-workspace/active/$1/task.md
   - .ai-workspace/active/$1/implementation.md
   如果任一文件不存在,提示用户先完成前置步骤。

2. 读取上下文:
   - 读取 task.md 了解任务描述和要求
   - 读取 plan.md 了解技术方案
   - 读取 implementation.md 了解实现细节
   - 查看 git diff 获取实际代码变更

3. 执行代码审查:
   对照 plan.md 和实际代码变更,检查:
   - 功能正确性: 实现是否符合技术方案
   - 代码质量: 编码规范、命名、注释、复杂度
   - 测试覆盖: 是否有充分的测试用例
   - 安全性: SQL注入、XSS、权限控制等
   - 性能: 算法复杂度、资源使用
   - 边界情况: 空值处理、异常处理

4. 输出审查报告:
   创建 .ai-workspace/active/$1/review.md,包含:
   - 审查概要(审查者、时间、范围、总体评价)
   - 审查发现(分级: 🔴 Blocker / 🟡 Major / 🟢 Minor)
   - 规范检查(编码规范、测试规范合规性)
   - 安全/性能审查结果
   - 与方案的一致性
   - 总结与建议(是否批准: ✅批准 / ⚠️修改后批准 / ❌需要重大修改)

5. 更新任务状态:
   使用 Edit 工具更新 .ai-workspace/active/$1/task.md:
   - current_step: code-review
   - assigned_to: codex
   - updated_at: 当前时间
   - 标记 review.md 为已完成
   - 在"工作流进度"中标记 code-review 为完成 ✅

6. 告知用户:
   - 输出审查结论(批准/需修改)
   - 如果需要修改,提示使用 /refinement-task $1 修复
   - 如果批准,提示使用 /commit 提交代码

**注意事项**:
- 对照 plan.md 审查,确保实现符合设计意图
- 关注潜在的安全和性能问题
- 给出具体的修改建议,而不是模糊的描述
