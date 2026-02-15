---
description: 分析 Dependabot 安全告警并创建安全分析文档
argument-hint: <alert-number>
---

分析 Dependabot 安全告警 #$1,评估安全风险并创建修复任务。

执行以下步骤:

1. 获取安全告警信息:
   ```bash
   gh api repos/{owner}/{repo}/dependabot/alerts/$1
   ```
   提取: severity, summary, package name, vulnerable version range, first patched version, GHSA/CVE ID

2. 创建任务目录和文件:
   ```bash
   date +%Y%m%d-%H%M%S
   mkdir -p .ai-workspace/active/TASK-<timestamp>/
   ```
   使用 Write 工具基于 .ai-agents/templates/task.md 模板创建 task.md:
   - security_alert_number: $1
   - severity, cve_id, ghsa_id
   - current_step: security-analysis
   - assigned_to: codex

3. 分析受影响范围:
   - 搜索项目中使用该依赖的所有位置(grep pom.xml/package.json 等)
   - 分析是否直接使用了漏洞代码路径
   - 识别依赖关系(直接依赖 vs 传递依赖)

4. 评估安全风险:
   - 漏洞的实际影响(是否可被利用)
   - 触发条件和场景
   - 修复的紧急程度

5. 输出分析文档到 analysis.md,包含:
   - 告警基本信息(编号、严重程度、GHSA/CVE)
   - 漏洞详情(受影响包、版本范围、修复版本)
   - 影响范围评估(受影响代码和功能)
   - 安全风险评估(可利用性、触发条件、影响程度)
   - 技术依赖和约束
   - 参考链接

6. 更新任务状态:
   - current_step: security-analysis
   - updated_at: 当前时间
   - 标记 analysis.md 为已完成

7. 告知用户:
   - 输出漏洞严重程度、任务ID、风险等级
   - 提示下一步: /plan-task <task-id> 设计修复方案
   - 如果是误报: /close-security $1 关闭告警

**注意事项**:
- Critical/High 级别立即处理,Medium 计划处理,Low 可延后
- 专注于信息收集和风险评估,不在此阶段制定修复方案
