---
description: 分析 GitHub Issue 并创建需求分析文档
agent: general
subtask: false
---

分析 GitHub Issue #$1 并创建任务文件。

执行以下步骤:

1. 获取 Issue 信息:
   !`gh issue view $1 --json number,title,body,labels`
   
   如果 Issue 不存在,提示用户检查 Issue 编号

2. 生成任务ID:
   !`date +%Y%m%d-%H%M%S`

3. 创建任务目录:
   !`mkdir -p .ai-workspace/active/TASK-$(date +%Y%m%d-%H%M%S)/`
   
   使用 Write 工具基于 .ai-agents/templates/task.md 模板创建 task.md 文件:
   - 填写任务元数据: issue_number, title, created_at, workflow 等
   - created_at 和 updated_at 使用步骤2获取的时间戳

4. 执行需求分析:
   - 阅读并理解 Issue 描述
   - 搜索相关代码文件(使用 glob/grep)
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

7. 告知用户:
   - 输出任务ID、标题、工作流
   - 显示输出文件路径
   - 提示下一步使用 /plan $TASK_ID 设计技术方案

**注意事项**:
- 严格遵循 .ai-agents/workflows/feature-development.yaml 工作流定义
- 分析完成后建议人工审查
- 如果已存在相关任务,询问是否重新分析
