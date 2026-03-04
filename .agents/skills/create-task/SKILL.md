---
name: create-task
description: 根据用户的自然语言描述创建任务并执行需求分析。当用户描述一个新功能、Bug 或改进需求，要求创建任务时触发。参数为任务的自然语言描述。
---

# 创建任务

根据用户的自然语言描述创建任务并执行需求分析。

**行为边界**:
- 本命令的唯一产出是 task.md 和 analysis.md 两个文件
- 禁止编写、修改、创建任何业务代码或配置文件
- 禁止直接实现用户描述的功能

## 执行步骤

1. 获取当前时间:
   ```bash
   date '+%Y-%m-%d %H:%M:%S'
   date +%Y%m%d-%H%M%S
   ```

2. 解析用户描述:
   - 任务标题: 精简的中文标题（20字以内）
   - 任务类型: feature|bugfix|refactor|docs|chore
   - 工作流: feature-development/bug-fix/refactoring

3. 创建任务目录:
   ```bash
   mkdir -p .ai-workspace/active/TASK-<timestamp>/
   ```
   基于 .agents/templates/task.md 模板创建 task.md。

4. 执行需求分析（仅分析，不编写业务代码）。

5. 输出分析文档到 analysis.md。

6. 更新任务状态，标记 requirement-analysis 为完成。

7. 提示下一步使用 plan-task skill。

**STOP**: 完成上述步骤后立即停止。不要继续执行 plan、implement 或任何后续步骤。
