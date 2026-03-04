---
name: analyze-codescan
description: 分析 Code Scanning (CodeQL) 告警，评估安全风险并创建修复任务。当用户要求分析 Code Scanning 告警、CodeQL 告警时触发。参数为告警编号。
---

# 分析 Code Scanning 告警

分析指定的 Code Scanning（CodeQL）告警，评估安全风险并创建修复任务。

## 执行步骤

1. 获取告警信息:
   ```bash
   gh api repos/{owner}/{repo}/code-scanning/alerts/<alert-number>
   ```
   提取: rule (id/severity/description), tool (name), most_recent_instance (location/message)

2. 创建任务目录和文件，基于 .agents/templates/task.md 模板。

3. 定位和分析源码:
   - 根据 most_recent_instance.location 定位源码文件和行号
   - 读取告警所在的源码上下文
   - 理解 CodeQL 规则的含义和检测逻辑
   - 检查是否有其他位置也存在相同问题

4. 评估安全风险（代码路径可达性、可利用性、影响程度）。

5. 输出分析文档到 analysis.md。

6. 更新任务状态。

7. 提示下一步: plan-task 设计修复方案，或 close-codescan 关闭告警。

**注意**: Critical/High 级别立即处理，Medium 计划处理，Low 可延后。
