---
description: 分析 Dependabot 安全告警并创建修复任务
agent: general
subtask: false
---

分析 Dependabot 安全告警 #$1,评估安全风险并创建修复任务。

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
   - 搜索项目中使用该依赖的所有位置(使用 grep)
   - 检查依赖文件(pom.xml, requirements.txt, package.json 等)
   - 分析是否直接使用了漏洞代码路径
   - 评估漏洞的实际影响(是否可被利用)
   - 识别依赖关系(直接依赖 vs 传递依赖)

4. 制定修复方案:
   
   **优先方案**:
   1. 升级到安全版本
      - 检查是否有修复版本
      - 评估升级的兼容性风险
      - 检查是否有破坏性变更
   
   2. 替换依赖(如果无法升级)
      - 寻找替代库
      - 评估迁移成本
   
   3. 缓解措施(临时方案)
      - 配置调整
      - 代码层面的防护
      - 网络隔离等

5. 输出分析文档到 security-analysis.md,包含:
   - 告警基本信息(编号、严重程度、GHSA/CVE ID)
   - 漏洞详情(描述、受影响的依赖、依赖文件位置)
   - 影响范围评估(直接影响、漏洞可利用性分析)
   - 修复方案(推荐方案、备选方案、临时缓解措施)
   - 测试策略
   - 参考链接

6. 更新任务状态:
   - current_step: security-analysis
   - updated_at: 当前时间
   - 标记 security-analysis.md 为已完成

7. 告知用户:
   ```
   🔒 安全告警 #$1 分析完成
   
   **漏洞信息**:
   - 严重程度: <severity>
   - CVE/GHSA: <cve-id> / <ghsa-id>
   - 受影响包: <package-name>
   
   **任务信息**:
   - 任务ID: <task-id>
   - 任务标题: <title>
   - 风险等级: <高/中/低>
   
   **输出文件**:
   - 任务文件: .ai-workspace/active/<task-id>/task.md
   - 分析文档: .ai-workspace/active/<task-id>/security-analysis.md
   
   **修复建议**: <简短的修复建议摘要>
   
   **下一步**:
   审查安全分析后,使用以下命令设计修复方案: /plan <task-id>
   
   如果是误报,可以使用以下命令关闭告警: /close-security $1
   ```

**注意事项**:
- Critical/High: 立即处理
- Medium: 计划处理
- Low: 可延后处理
- 区分直接依赖和传递依赖
- 特别注意跨大版本升级的兼容性风险
