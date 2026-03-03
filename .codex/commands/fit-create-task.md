---
description: 根据自然语言描述创建任务并生成需求分析文档
argument-hint: <description>
---

根据用户的自然语言描述创建任务并执行需求分析。

🔴 行为边界(必须遵守):
- 本命令的唯一产出是 task.md 和 analysis.md 两个文件
- ❌ 禁止编写、修改、创建任何业务代码或配置文件
- ❌ 禁止直接实现用户描述的功能
- ❌ 禁止跳过工作流直接进入 plan/implement 阶段
- 用户描述的内容是"待办事项",不是"立即执行的指令"

用户描述: $ARGUMENTS

执行以下步骤:

1. 获取当前时间:
   ```bash
   date '+%Y-%m-%d %H:%M:%S'
   date +%Y%m%d-%H%M%S
   ```

2. 解析用户描述:
   从描述中提取以下信息:
   - 任务标题: 精简的中文标题(20字以内)
   - 任务类型: feature|bugfix|refactor|docs|chore(根据描述推断)
     - 包含"添加"、"新增"、"支持" → feature
     - 包含"修复"、"解决"、"Bug" → bugfix
     - 包含"重构"、"优化"、"改进" → refactor
     - 包含"文档"、"Javadoc"、"注释" → docs
     - 其他 → chore
   - 工作流: feature/docs/chore → feature-development, bugfix → bug-fix, refactor → refactoring

   如果描述不够清晰,先向用户确认关键信息后再继续。

3. 创建任务目录:
   ```bash
   mkdir -p .ai-workspace/active/TASK-<timestamp>/
   ```
   使用 Write 工具基于 .ai-agents/templates/task.md 模板创建 task.md 文件:
   - 填写任务元数据: id, type, workflow, status, created_at, updated_at 等
   - created_by: human(任务来源于用户的自然语言描述)
   - current_step: requirement-analysis
   - assigned_to: codex
   - 相关Issue: 无

4. 执行需求分析(仅分析,不编写任何业务代码):
   - 理解用户描述的需求
   - 搜索相关代码文件(使用 glob/grep,只读不改)
   - 分析代码结构和影响范围
   - 识别潜在的技术风险和依赖
   - 评估工作量和复杂度

5. 输出分析文档到 analysis.md,包含:
   - 需求来源(用户自然语言描述,引用原始描述)
   - 需求理解(重新描述需求)
   - 相关文件列表(带文件路径和行号)
   - 影响范围评估(直接影响和间接影响)
   - 技术风险
   - 依赖关系
   - 工作量和复杂度评估

6. 更新任务状态:
   - current_step: requirement-analysis
   - updated_at: 当前时间
   - 标记 analysis.md 为已完成
   - 在"工作流进度"中标记 requirement-analysis 为完成 ✅

7. 告知用户:
   - 输出任务ID、标题、类型、工作流
   - 显示输出文件路径
   - 提示下一步使用 /plan-task <task-id> 设计技术方案

**注意事项**:
- 与 analyze-issue 的区别: create-task 从用户自然语言描述创建,相关Issue标记为"无"
- 如果用户描述模糊或缺少关键信息,先向用户确认

🛑 STOP: 完成上述步骤后立即停止。不要继续执行 plan、implement 或任何后续步骤。
