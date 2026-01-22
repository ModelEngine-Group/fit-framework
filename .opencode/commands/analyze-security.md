---
description: 分析 Dependabot 安全告警并创建安全分析文档
agent: general
subtask: false
---

分析 Dependabot 安全告警 #$1,评估安全风险并创建任务文件。

执行以下步骤:

1. 获取安全告警信息:
   !`gh api repos/<owner>/<repo>/dependabot/alerts/$1`
   
   提取关键信息:
   - number, state, severity
   - security_advisory: ghsa_id, cve_id, summary, description
   - dependency: package.name, package.ecosystem, manifest_path
   - security_vulnerability: first_patched_version, vulnerable_version_range

2. 创建任务文件:
   - 检查是否已存在该安全告警的任务
   - 如果没有,创建新任务目录: .ai-workspace/active/TASK-!`date +%Y%m%d-%H%M%S`/
   - 使用 .ai-agents/templates/task.md 模板创建 task.md
   - 填写任务元数据:
     ```yaml
     security_alert_number: $1
     severity: <critical/high/medium/low>
     cve_id: <CVE-ID>
     ghsa_id: <GHSA-ID>
     ```

3. 分析受影响范围:
   - 识别受影响的依赖包和版本
   - 搜索项目中使用该依赖的所有位置(使用 glob/grep)
   - 检查依赖文件(pom.xml, requirements.txt, package.json 等)
   - 分析是否直接使用了漏洞代码路径
   - 识别依赖关系(直接依赖 vs 传递依赖)
   - 定位受影响的代码模块和文件

4. 评估安全风险:
   - 评估漏洞的实际影响(是否可被利用)
   - 分析漏洞触发条件和场景
   - 评估对系统安全性的影响程度
   - 识别潜在的安全威胁
   - 确定修复的紧急程度
   - 查找是否有已知的攻击案例

5. 输出分析文档到 analysis.md,包含:
   - 告警基本信息(编号、严重程度、GHSA/CVE ID、描述)
   - 漏洞详情(受影响的依赖、版本范围、修复版本)
   - 依赖使用情况(依赖文件位置、依赖类型、使用模块列表)
   - 影响范围评估(直接影响的代码、间接影响的功能)
   - 安全风险评估(漏洞可利用性、触发条件、影响程度、紧急程度)
   - 技术依赖和约束
   - 参考链接(CVE/GHSA链接、厂商公告、安全建议)

6. 更新任务状态:
    - current_step: security-analysis
    - updated_at: 当前时间
    - 标记 analysis.md 为已完成

7. 告知用户:
   - 输出任务ID、漏洞严重程度、受影响包
   - 显示输出文件路径
   - 提示下一步使用 /plan <task-id> 设计修复方案
   - 如果是误报，提示使用 /close-security $1 关闭告警

**注意事项**:
- 严格遵循 .ai-agents/workflows/bug-fix.yaml 工作流定义（安全修复视为特殊类型的 bug 修复）
- 专注于信息收集和风险评估，不制定具体修复方案（修复方案在 /plan 阶段设计）
- 分析完成后建议人工审查
- 如果已存在相关任务,询问是否重新分析
- 区分直接依赖和传递依赖
- Critical/High 级别的漏洞需要标注紧急程度
