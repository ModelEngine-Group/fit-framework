---
description: 处理代码审查反馈并修复问题
argument-hint: <task-id>
---

处理任务 $1 的代码审查反馈,修复审查中发现的问题。

执行以下步骤:

1. 验证前置条件:
   检查必需文件是否存在:
   - .ai-workspace/active/$1/task.md
   - .ai-workspace/active/$1/review.md
   如果任一文件不存在,提示用户先完成前置步骤。

2. 读取审查报告:
   仔细阅读 .ai-workspace/active/$1/review.md,整理:
   - 🔴 Blocker 问题(必须修复)
   - 🟡 Major 问题(建议修复)
   - 🟢 Minor 问题(可选修复)

3. 逐项修复:
   按优先级(Blocker → Major → Minor)逐项修复:
   - 对每个问题,理解根因并实施修复
   - 修复后运行相关测试验证
   - 记录每个问题的修复方式

4. 运行测试验证:
   ```bash
   mvn test -pl :<module-name>
   ```
   确保所有测试通过,包括新增的回归测试。

5. 更新实现报告:
   在 .ai-workspace/active/$1/implementation.md 中追加"修复记录"章节:
   - 列出每个修复的问题及修复方式
   - 新增或修改的文件
   - 测试验证结果

6. 更新任务状态:
   使用 Edit 工具更新 .ai-workspace/active/$1/task.md:
   - current_step: refinement
   - assigned_to: codex
   - updated_at: 当前时间

7. 告知用户:
   - 输出修复的问题数量(按级别)
   - 提示重新审查: /review-task $1
   - 或直接提交: /commit

**注意事项**:
- 严格按照审查报告修复,不要添加额外变更
- 每修复一个问题就运行测试
- **不要**自动执行 git commit
