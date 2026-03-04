---
name: "analyze-codescan"
description: "分析 Code Scanning 告警并创建安全分析文档"
usage: "/analyze-codescan <alert-number>"
---

# Analyze Code Scanning Command

## 功能说明

分析指定的 Code Scanning（CodeQL）告警，评估安全风险并创建修复任务，输出安全分析文档。

## 执行流程

### 1. 获取告警信息

```bash
gh api repos/{owner}/{repo}/code-scanning/alerts/<alert-number>
```

提取关键信息：
- `number`: 告警编号
- `state`: 状态（open/dismissed/fixed）
- `rule`: 规则信息
  - `id`: 规则 ID（如 `java/sql-injection`）
  - `severity`: 严重程度（error/warning/note）
  - `description`: 规则描述
  - `security_severity_level`: 安全严重级别（critical/high/medium/low）
- `tool`: 扫描工具信息
  - `name`: 工具名（如 CodeQL）
  - `version`: 版本
- `most_recent_instance`: 最近发现的实例
  - `location`: 文件位置（path/start_line/end_line）
  - `message`: 告警消息
  - `state`: 实例状态
- `html_url`: GitHub 上的告警链接

### 2. 创建任务目录和文件

检查是否已存在该告警的任务：
- 在 `.ai-workspace/active/` 中搜索相关任务
- 如果找到，询问是否重新分析
- 如果没有，创建新任务

**任务目录结构**：
```
.ai-workspace/active/TASK-{yyyyMMdd-HHmmss}/
├── task.md          ← 使用 .agents/templates/task.md 模板创建
└── analysis.md      ← 本命令将创建此文件
```

⚠️ **重要**：
- 任务目录命名：`TASK-{yyyyMMdd-HHmmss}`（**必须**包含 `TASK-` 前缀）
- 示例：`TASK-20260205-202013`
- 任务ID（`{task-id}`）即为目录名：`TASK-{yyyyMMdd-HHmmss}`

任务元数据（在 task.md 的 YAML front matter 中）需包含：
```yaml
id: TASK-{yyyyMMdd-HHmmss}
codescan_alert_number: <alert-number>
severity: <critical/high/medium/low>
rule_id: <rule-id>
tool: <tool-name>
```

### 3. 定位和分析源码

**必须完成的分析**：
- [ ] 根据 `most_recent_instance.location` 定位源码文件和行号
- [ ] 读取告警所在的源码上下文（前后 20 行）
- [ ] 理解 CodeQL 规则的含义和检测逻辑
- [ ] 分析代码为什么触发了该规则
- [ ] 检查是否有其他位置也存在相同问题（使用 Grep 工具）
- [ ] 评估是否为误报（代码逻辑是否确实存在安全隐患）

### 4. 评估安全风险

**必须完成的风险评估**：
- [ ] 评估漏洞的实际影响（是否可被利用）
- [ ] 分析代码路径是否可达（外部输入能否到达漏洞点）
- [ ] 评估对系统安全性的影响程度
- [ ] 识别潜在的攻击向量
- [ ] 确定修复的紧急程度
- [ ] 评估修复的复杂度和风险

### 5. 输出分析文档

创建 `.ai-workspace/active/{task-id}/analysis.md`，必须包含以下章节：

注意：`{task-id}` 格式为 `TASK-{yyyyMMdd-HHmmss}`，例如 `TASK-20260205-202013`

```markdown
# Code Scanning 告警分析报告

## 告警基本信息

- **告警编号**: #{alert-number}
- **严重程度**: {critical/high/medium/low} 🔴/🟠/🟡/🟢
- **规则 ID**: {rule-id}
- **扫描工具**: {tool-name} {tool-version}
- **告警状态**: {open/dismissed/fixed}
- **规则描述**: {rule-description}

## 告警详情

### 源码位置
- **文件路径**: `{file-path}`
- **行号范围**: L{start-line} - L{end-line}
- **告警消息**: {message}

### 代码上下文
```{language}
// 告警所在的代码片段（包含前后上下文）
{code-snippet}
```

### 规则说明
{详细解释 CodeQL 规则检测的安全问题类型}

## 影响范围评估

### 直接影响的代码
- `{file-path}:{line-number}` - {说明}

### 相同模式的其他位置
- {搜索项目中是否有类似的代码模式}

## 安全风险评估

### 漏洞可利用性
- [ ] 外部输入能否到达该代码路径？
- [ ] 是否有输入验证或过滤？
- [ ] 当前配置是否暴露了漏洞？

**结论**: {高/中/低风险 - 说明理由}

### 攻击向量
{描述可能的攻击方式}

### 影响程度
{评估对系统安全性、数据完整性、可用性的影响}

### 紧急程度
{根据严重程度和可利用性确定修复的紧急程度}

## 修复建议

### 推荐修复方式
{具体的代码修改建议}

### 修复复杂度
{评估修复的难度和工作量}

## 技术依赖和约束

{列出修复时需要考虑的技术依赖和约束条件}

## 参考链接

- GitHub Alert: {html_url}
- CodeQL Rule: https://codeql.github.com/codeql-query-help/{language}/{rule-id}/
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
🔍 Code Scanning 告警 #{alert-number} 分析完成

**告警信息**:
- 严重程度: {severity}
- 规则: {rule-id}
- 工具: {tool-name}
- 位置: {file-path}:{line-number}

**任务信息**:
- 任务ID: {task-id}
- 任务标题: {title}
- 风险等级: {高/中/低}

**输出文件**:
- 任务文件: .ai-workspace/active/{task-id}/task.md
- 分析文档: .ai-workspace/active/{task-id}/analysis.md

**下一步**:
审查安全分析后，使用以下命令设计修复方案：
- Claude Code / OpenCode: `/plan-task {task-id}`
- Gemini CLI: `/fit:plan-task {task-id}`
- Codex CLI: `/prompts:fit-plan-task {task-id}`

如果是误报，可以使用以下命令关闭告警：
- Claude Code / OpenCode: `/close-codescan {alert-number}`
- Gemini CLI: `/fit:close-codescan {alert-number}`
- Codex CLI: `/prompts:fit-close-codescan {alert-number}`
```

## 参数说明

- `<alert-number>`: Code Scanning 告警编号（必需）

## 使用示例

```bash
# 分析 Code Scanning 告警 #5
/analyze-codescan 5
```

## 注意事项

1. **告警验证**：
   - 执行前检查告警是否存在
   - 如果告警已关闭，询问用户是否继续分析

2. **严重程度优先级**：
   - Critical/High: 立即处理
   - Medium: 计划处理
   - Low: 可延后处理

3. **职责范围**：
   - 专注于信息收集和风险评估
   - 不制定具体修复方案（修复方案在 `/plan-task` 阶段设计）
   - 分析完成后建议人工审查

4. **误报识别**：
   - 检查代码路径是否可达
   - 评估输入是否可控
   - 如确认是误报，建议使用 `/close-codescan` 关闭

5. **紧急程度标注**：
   - Critical/High 级别的告警需要明确标注紧急程度

## 相关命令

- `/close-codescan <alert-number>` - 关闭 Code Scanning 告警（需提供理由）
- `/analyze-dependabot <alert-number>` - 分析 Dependabot 依赖漏洞告警
- `/plan-task <task-id>` - 设计修复方案
- `/check-task <task-id>` - 查看任务状态

## 错误处理

- 告警不存在：提示 "Code Scanning 告警 #{number} 不存在，请检查告警编号"
- 网络错误：提示 "无法连接到 GitHub，请检查网络连接"
- 权限错误：提示 "没有访问该仓库的权限，请检查 GitHub CLI 认证状态"
- API 限制：提示 "GitHub API 请求限制，请稍后重试"
