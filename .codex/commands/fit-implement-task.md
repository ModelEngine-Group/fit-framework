---
description: 根据技术方案实施任务并输出实现报告
argument-hint: <task-id>
---

根据技术方案实施任务 $1,编写代码和测试,输出实现报告。

执行以下步骤:

1. 验证前置条件:
   检查必需文件是否存在:
   - .ai-workspace/active/$1/task.md
   - .ai-workspace/active/$1/plan.md
   如果任一文件不存在,提示用户先完成前置步骤。

2. 读取技术方案:
   仔细阅读 .ai-workspace/active/$1/plan.md,理解:
   - 技术方案和实现策略
   - 详细的实施步骤
   - 需要创建/修改的文件清单
   - 测试策略

3. 执行代码实现:
   按照 plan.md 中的步骤顺序执行:
   - 按照方案实现功能代码
   - 编写完整的单元测试
   - 遵循项目编码规范(参考 AGENTS.md)
   - 修改带版权头的文件时,先运行 `date +%Y` 获取当前年份并更新版权头

4. 运行测试验证:
   ```bash
   mvn test -pl :<module-name>
   ```
   确保所有测试通过。

5. 输出实现报告:
   创建 .ai-workspace/active/$1/implementation.md,包含:
   - 已修改文件列表(新增文件和修改文件)
   - 关键代码说明(重要逻辑的解释和代码片段)
   - 测试结果(测试用例数、通过率、测试输出)
   - 与方案的差异(如果有)
   - 待审查事项(需要 reviewer 关注的点)

6. 更新任务状态:
   使用 Edit 工具更新 .ai-workspace/active/$1/task.md:
   - current_step: implementation
   - assigned_to: codex
   - updated_at: 当前时间
   - 标记 implementation.md 为已完成
   - 在"工作流进度"中标记 implementation 为完成 ✅

7. 告知用户:
   - 输出修改文件数、新增文件数、测试通过数
   - 提示下一步使用 /review-task $1 进行代码审查

**注意事项**:
- 严格遵循 plan.md,不要偏离技术方案或添加计划外功能
- 每完成一个步骤就运行测试
- **不要**自动执行 git commit,等待代码审查通过后由用户提交
