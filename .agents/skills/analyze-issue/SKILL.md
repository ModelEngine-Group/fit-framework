---
name: analyze-issue
description: 分析 GitHub Issue 并创建任务文件和需求分析文档。当用户要求分析某个 issue、创建任务跟踪 issue 时触发。参数为 issue 编号。
---

# 分析 Issue

分析指定的 GitHub Issue 并创建任务文件。

## 执行步骤

1. 获取 Issue 信息:
   ```bash
   gh issue view <issue-number> --json number,title,body,labels
   ```

2. 生成任务ID:
   ```bash
   date +%Y%m%d-%H%M%S
   ```

3. 创建任务目录:
   ```bash
   mkdir -p .ai-workspace/active/TASK-<timestamp>/
   ```
   基于 .agents/templates/task.md 模板创建 task.md，填写任务元数据。

4. 执行需求分析（仅分析，不编写业务代码）:
   - 阅读并理解 Issue 描述
   - 搜索相关代码文件（只读不改）
   - 分析代码结构和影响范围
   - 识别技术风险和依赖

5. 输出分析文档到 analysis.md。

6. 更新任务状态，标记 requirement-analysis 为完成。

7. 提示下一步使用 plan-task skill。

**注意**: 禁止编写或修改任何业务代码，只做分析。
