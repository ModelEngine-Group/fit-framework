---
name: "analyze-security"
description: "分析 Dependabot 安全告警并创建修复任务"
usage: "/analyze-security <alert-number>"
---

# Analyze Security Command

## 功能说明

分析指定的 Dependabot 安全告警，评估安全风险并创建修复任务，输出安全分析文档。

## 执行流程

### 1. 获取安全告警信息

```bash
gh api repos/{owner}/{repo}/dependabot/alerts/<alert-number>
```

提取关键信息：
- `number`: 告警编号
- `state`: 状态（open/dismissed/fixed）
- `security_advisory`: 安全公告详情
  - `ghsa_id`: GHSA ID
  - `cve_id`: CVE ID（如果有）
  - `severity`: 严重程度（critical/high/medium/low）
  - `summary`: 漏洞摘要
  - `description`: 详细描述
  - `vulnerabilities`: 受影响的版本范围
- `dependency`: 受影响的依赖
  - `package.name`: 包名
  - `package.ecosystem`: 生态系统（maven/pip/npm等）
  - `manifest_path`: 依赖文件路径
- `security_vulnerability.first_patched_version`: 首个修复版本
- `security_vulnerability.vulnerable_version_range`: 受影响版本范围

### 2. 创建任务文件

检查是否已存在该安全告警的任务：
- 在 `.ai-workspace/active/` 中搜索相关任务
- 如果找到，询问是否重新分析
- 如果没有，使用 `.ai-agents/templates/task.md` 模板创建新任务

任务文件命名：`TASK-{yyyyMMdd-HHmmss}.md`

任务元数据需包含：
```yaml
security_alert_number: <alert-number>
severity: <critical/high/medium/low>
cve_id: <CVE-ID>  # 如果有
ghsa_id: <GHSA-ID>
```

### 3. 分析受影响范围

**必须完成的分析**：
- [ ] 识别受影响的依赖包和版本
- [ ] 搜索项目中使用该依赖的所有位置（使用 Grep 工具）
- [ ] 检查依赖文件（pom.xml, requirements.txt, package.json 等）
- [ ] 分析是否直接使用了漏洞代码路径
- [ ] 评估漏洞的实际影响（是否可被利用）
- [ ] 识别依赖关系（直接依赖 vs 传递依赖）

### 4. 制定修复方案

根据漏洞严重程度和修复难度制定方案：

**优先方案**：
1. **升级到安全版本**
   - 检查是否有修复版本
   - 评估升级的兼容性风险
   - 检查是否有破坏性变更

2. **替换依赖**（如果无法升级）
   - 寻找替代库
   - 评估迁移成本

3. **缓解措施**（临时方案）
   - 配置调整
   - 代码层面的防护
   - 网络隔离等

### 5. 输出分析文档

创建 `{task_dir}/analysis.md`，必须包含以下章节：

```markdown
# 安全告警分析报告

## 告警基本信息

- **告警编号**: #{alert-number}
- **严重程度**: {critical/high/medium/low} 🔴/🟠/🟡/🟢
- **GHSA ID**: {ghsa-id}
- **CVE ID**: {cve-id}
- **告警状态**: {open/dismissed/fixed}

## 漏洞详情

### 漏洞描述
{详细描述漏洞的性质和攻击方式}

### 受影响的依赖
- **包名**: {package-name}
- **生态系统**: {maven/pip/npm/...}
- **当前版本**: {current-version}
- **受影响版本范围**: {vulnerable-range}
- **首个修复版本**: {patched-version}

### 依赖文件位置
- `{manifest-path}` - {说明}

## 影响范围评估

### 直接影响
- {项目中使用该依赖的模块和文件}

### 漏洞可利用性分析
- [ ] 是否直接使用了漏洞代码路径？
- [ ] 是否有外部输入触发漏洞？
- [ ] 当前配置是否暴露了漏洞？

**结论**: {高/中/低风险 - 说明理由}

## 修复方案

### 推荐方案: {方案名称}

**具体步骤**:
1. {步骤1}
2. {步骤2}
...

**兼容性评估**:
- {是否有破坏性变更}
- {需要的代码调整}

**工作量预估**:
- 复杂度: {高/中/低}
- 预估时间: {时间}
- 风险等级: {高/中/低}

### 备选方案（如果有）
{其他可能的修复方案}

### 临时缓解措施（如果无法立即修复）
{临时性的防护措施}

## 测试策略

- [ ] {测试项1}
- [ ] {测试项2}
- [ ] 验证漏洞已修复
- [ ] 回归测试

## 参考链接

- GHSA Advisory: https://github.com/advisories/{ghsa-id}
- CVE Details: https://cve.mitre.org/cgi-bin/cvename.cgi?name={cve-id}
- {其他相关文档}
```

### 6. 更新任务状态

更新 `.ai-workspace/active/{task-id}/task.md`：
- `current_step`: security-analysis
- `assigned_to`: claude
- `updated_at`: {当前时间}
- 标记 analysis.md 为已完成

### 7. 告知用户

输出格式：
```
🔒 安全告警 #{alert-number} 分析完成

**漏洞信息**:
- 严重程度: {severity}
- CVE/GHSA: {cve-id} / {ghsa-id}
- 受影响包: {package-name}

**任务信息**:
- 任务ID: {task-id}
- 任务标题: {title}
- 风险等级: {高/中/低}

**输出文件**:
- 任务文件: .ai-workspace/active/{task-id}/task.md
- 分析文档: {task_dir}/analysis.md

**修复建议**: {简短的修复建议摘要}

**下一步**:
审查安全分析后，使用以下命令设计修复方案：
/plan-task {task-id}

如果是误报，可以使用以下命令关闭告警：
/close-security {alert-number}
```

## 参数说明

- `<alert-number>`: Dependabot 安全告警编号（必需）

## 使用示例

```bash
# 分析安全告警 #23
/analyze-security 23
```

## 注意事项

1. **告警验证**：
   - 执行前检查告警是否存在
   - 如果告警已关闭，询问用户是否继续分析

2. **严重程度优先级**：
   - Critical/High: 立即处理
   - Medium: 计划处理
   - Low: 可延后处理

3. **依赖类型区分**：
   - **直接依赖**: 在依赖文件中明确声明
   - **传递依赖**: 由其他依赖引入，修复可能需要升级父依赖

4. **误报识别**：
   - 检查漏洞代码路径是否被使用
   - 评估实际可利用性
   - 如确认是误报，建议使用 `/close-security` 关闭

5. **兼容性风险**：
   - 特别注意跨大版本升级
   - 查看 CHANGELOG 和 Migration Guide
   - 评估对现有代码的影响

## 相关命令

- `/close-security <alert-number>` - 关闭安全告警（需提供理由）
- `/plan-task <task-id>` - 设计修复方案
- `/task-status <task-id>` - 查看任务状态
- `/upgrade-dependency` - 升级依赖

## 错误处理

- 告警不存在：提示 "安全告警 #{number} 不存在，请检查告警编号"
- 网络错误：提示 "无法连接到 GitHub，请检查网络连接"
- 权限错误：提示 "没有访问该仓库的权限，请检查 GitHub CLI 认证状态"
- API 限制：提示 "GitHub API 请求限制，请稍后重试"
