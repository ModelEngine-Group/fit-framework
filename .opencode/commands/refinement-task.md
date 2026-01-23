---
description: 处理代码审查反馈并修复问题
agent: general
subtask: false
---

处理任务 $1 的代码审查反馈并修复问题。

执行以下步骤:

1. 验证前置条件:
   !`test -f .ai-workspace/active/$1/task.md && echo "✅ 任务文件存在" || echo "❌ ERROR: 任务文件不存在"`
   !`test -f .ai-workspace/active/$1/review.md && echo "✅ 审查报告存在" || echo "❌ ERROR: 审查报告不存在,请先执行 /review $1"`
   !`test -f .ai-workspace/active/$1/implementation.md && echo "✅ 实现报告存在" || echo "❌ ERROR: 实现报告不存在"`

2. 读取审查报告:
   - 提取需要修复的问题,按优先级分类:
     - 🔴 必须修复(Blocker)
     - 🟡 建议修改(Major)
     - 🟢 优化建议(Minor)
   - 提取问题标题、文件路径、行号、问题描述、修复建议

3. 规划修复任务:
   - 使用 TodoWrite 创建修复任务清单
   - 按优先级排序: 先修复🔴,再修复🟡,最后考虑🟢

4. 执行代码修复:
   - 逐个修复问题
   - 读取相关文件,理解问题上下文
   - 按照审查建议修复代码
   - 确保修复不引入新问题
   - 在 TodoWrite 中标记已完成的问题

5. 运行测试(如果有测试失败):
   - mvn test / npm test / pytest
   - 确保所有测试通过

6. 创建修复报告 refinement-report.md,包含:
   - 修复概要(修复者、时间、修复范围)
   - 修复内容(已修复的阻塞问题、建议问题、优化建议)
   - 未修复的问题(如果有,说明原因和计划)
   - 测试结果
   - 下一步(重新审查或提交)

7. 更新任务状态:
   - current_step: refinement
   - updated_at: 当前时间
   - 在工作流进度中标记 refinement 为进行中

8. 告知用户:
   - 输出修复内容(必须修复、建议修改、优化建议的数量)
   - 显示输出文件路径
   - 提示下一步:
     - 使用 /review $1 重新审查代码
     - 如果修改较小且有信心,可以直接 /commit 提交
     - 如果修复涉及大量更改,建议重新审查

**修复原则**:
- 严格按照审查建议修复
- 如果建议不明确,询问用户
- 如果发现新问题,一并修复
- 保持代码风格一致

**注意事项**:
- 阻塞问题必须全部修复
- 建议修改可以跳过,但必须在报告中说明原因
- 修复后不需要更新 implementation.md,创建 refinement-report.md 即可
