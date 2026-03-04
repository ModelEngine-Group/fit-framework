---
description: 分析 GitHub Issue 并创建需求分析文档
argument-hint: <issue-number>
---

分析 GitHub Issue #$1 并创建任务文件。

执行以下步骤:

1. 获取 Issue 信息:
   ```bash
   gh issue view $1 --json number,title,body,labels
   ```
   如果 Issue 不存在,提示用户检查 Issue 编号。

2. 生成任务ID:
   ```bash
   date +%Y%m%d-%H%M%S
   ```

3. 创建任务目录:
   ```bash
   mkdir -p .ai-workspace/active/TASK-<timestamp>/
   ```
   使用 Write 工具基于 .agents/templates/task.md 模板创建 task.md 文件:
   - 填写任务元数据: issue_number=$1, title, created_at, workflow 等
   - created_by: human
   - current_step: requirement-analysis
   - assigned_to: codex

4. 执行需求分析(仅分析,不编写任何业务代码):
   - 阅读并理解 Issue 描述
   - 搜索相关代码文件(使用 glob/grep,只读不改)
   - 分析代码结构和影响范围
   - 识别潜在的技术风险和依赖
   - 评估工作量和复杂度

5. 输出分析文档到 analysis.md,包含:
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
   - 输出任务ID、标题、工作流
   - 显示输出文件路径
   - 提示下一步设计技术方案:
     - Claude Code / OpenCode: /plan-task <task-id>
     - Gemini CLI: /fit:plan-task <task-id>
     - Codex CLI: /prompts:fit-plan-task <task-id>

**注意事项**:
- 严格遵循 .agents/workflows/feature-development.yaml 工作流定义
- 🔴 禁止编写、修改任何业务代码,只做分析
- 分析完成后建议人工审查
- 如果已存在相关任务,询问是否重新分析

🛑 STOP: 完成上述步骤后立即停止。不要继续执行 plan、implement 或任何后续步骤。
