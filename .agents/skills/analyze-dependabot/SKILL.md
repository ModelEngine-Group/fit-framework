---
name: analyze-dependabot
description: 分析 Dependabot 安全告警，评估安全风险并创建修复任务。当用户要求分析 Dependabot 告警、评估依赖漏洞风险时触发。参数为告警编号。
---

# 分析安全告警

分析指定的 Dependabot 安全告警，评估安全风险并创建修复任务。

## 执行步骤

1. 获取安全告警信息:
   ```bash
   gh api repos/{owner}/{repo}/dependabot/alerts/<alert-number>
   ```
   提取: severity, summary, package name, vulnerable version range, first patched version

2. 创建任务目录和文件，基于 .agents/templates/task.md 模板。

3. 分析受影响范围:
   - 搜索项目中使用该依赖的所有位置
   - 分析是否直接使用了漏洞代码路径
   - 识别依赖关系（直接依赖 vs 传递依赖）

4. 评估安全风险（可利用性、触发条件、影响程度）。

5. 输出分析文档到 analysis.md。

6. 更新任务状态。

7. 提示下一步: plan-task 设计修复方案，或 close-dependabot 关闭告警。

**注意**: Critical/High 级别立即处理，Medium 计划处理，Low 可延后。
