---
description: 分析 Code Scanning 告警并创建安全分析文档
argument-hint: <alert-number>
---

分析 Code Scanning (CodeQL) 告警 #$1,评估安全风险并创建修复任务。

执行以下步骤:

1. 获取告警信息:
   ```bash
   gh api repos/{owner}/{repo}/code-scanning/alerts/$1
   ```
   提取: rule (id/severity/description), tool (name/version), most_recent_instance (location/message)

2. 创建任务目录和文件:
   ```bash
   date +%Y%m%d-%H%M%S
   mkdir -p .ai-workspace/active/TASK-<timestamp>/
   ```
   使用 Write 工具基于 .agents/templates/task.md 模板创建 task.md:
   - codescan_alert_number: $1
   - severity, rule_id, tool
   - current_step: security-analysis
   - assigned_to: codex

3. 定位和分析源码:
   - 根据 most_recent_instance.location 定位源码文件和行号
   - 读取告警所在的源码上下文
   - 理解 CodeQL 规则的含义
   - 检查是否有其他位置也存在相同问题

4. 评估安全风险:
   - 代码路径是否可达(外部输入能否到达漏洞点)
   - 漏洞的实际影响(是否可被利用)
   - 修复的紧急程度和复杂度

5. 输出分析文档到 analysis.md,包含:
   - 告警基本信息(编号、严重程度、规则 ID、工具)
   - 源码位置和代码上下文
   - 影响范围评估(受影响代码和相同模式)
   - 安全风险评估(可利用性、攻击向量、影响程度)
   - 修复建议
   - 参考链接

6. 更新任务状态:
   - current_step: security-analysis
   - updated_at: 当前时间
   - 标记 analysis.md 为已完成

7. 告知用户:
   - 输出告警严重程度、任务ID、风险等级
   - 提示下一步设计修复方案:
     - Claude Code / OpenCode: /plan-task <task-id>
     - Gemini CLI: /fit:plan-task <task-id>
     - Codex CLI: /prompts:fit-plan-task <task-id>
   - 如果是误报，关闭告警:
     - Claude Code / OpenCode: /close-codescan $1
     - Gemini CLI: /fit:close-codescan $1
     - Codex CLI: /prompts:fit-close-codescan $1

**注意事项**:
- Critical/High 级别立即处理,Medium 计划处理,Low 可延后
- 专注于信息收集和风险评估,不在此阶段制定修复方案
