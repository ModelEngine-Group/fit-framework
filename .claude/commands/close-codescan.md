---
name: "close-codescan"
description: "关闭 Code Scanning 告警（需提供合理理由）"
usage: "/close-codescan <alert-number>"
---

# Close Code Scanning Command

## 功能说明

关闭指定的 Code Scanning（CodeQL）告警。在关闭前会要求用户确认并提供合理的理由，确保不会误关闭真实的安全风险。

## 执行流程

### 1. 获取告警信息

```bash
gh api repos/{owner}/{repo}/code-scanning/alerts/<alert-number>
```

验证告警状态：
- 如果已经是 `dismissed` 或 `fixed` 状态，提示用户并退出
- 如果是 `open` 状态，继续执行

### 2. 展示告警详情

向用户展示告警的关键信息：
```
🔍 Code Scanning 告警 #{alert-number}

严重程度: {security_severity_level} 🔴/🟠/🟡/🟢
规则: {rule.id} - {rule.description}
工具: {tool.name}
位置: {location.path}:{location.start_line}
消息: {message}
```

### 3. 询问关闭理由

使用 `AskUserQuestion` 工具让用户选择关闭理由：

**问题**: "为什么要关闭这个 Code Scanning 告警？"

**选项**:
1. **误报 (False Positive)**
   - 描述: CodeQL 规则误判，代码实际上不存在此安全问题
   - 对应 API 参数: `dismissed_reason: "false positive"`

2. **不会修复 (Won't Fix)**
   - 描述: 已知问题但基于架构或业务原因不予修复
   - 对应 API 参数: `dismissed_reason: "won't fix"`

3. **测试代码 (Used in Tests)**
   - 描述: 仅在测试代码中出现，不影响生产环境安全
   - 对应 API 参数: `dismissed_reason: "used in tests"`

4. **取消**
   - 描述: 不关闭告警
   - 操作: 退出命令

### 4. 要求详细说明

如果用户选择关闭（非"取消"），要求用户提供详细的文字说明：

```
请提供详细的关闭理由（将记录到 GitHub）:
```

**说明要求**：
- 最少 20 个字符
- 清晰说明为什么此告警可以安全关闭
- 如果是误报，说明为什么代码不存在此安全问题
- 如果是不修复，说明技术或业务原因

### 5. 最终确认

显示即将提交的信息，要求最终确认：

```
⚠️ 即将关闭 Code Scanning 告警 #{alert-number}

规则: {rule.id}
位置: {location.path}:{location.start_line}
关闭理由类别: {选择的理由}
详细说明: {用户输入的说明}

是否确认关闭？(y/N)
```

- 如果用户输入 `y` 或 `yes`，继续执行
- 否则，取消操作

### 6. 执行关闭操作

使用 GitHub API 关闭告警：

```bash
gh api --method PATCH \
  repos/{owner}/{repo}/code-scanning/alerts/<alert-number> \
  -f state=dismissed \
  -f dismissed_reason="{API参数}" \
  -f dismissed_comment="{用户的详细说明}"
```

**API 参数映射**：
- `dismissed_reason` 的有效值（根据 GitHub Code Scanning API）:
  - `false positive`: 误报
  - `won't fix`: 不修复
  - `used in tests`: 测试代码

### 7. 记录到任务（如果存在）

检查是否有相关的安全分析任务：
- 搜索 `.ai-workspace/active/`、`.ai-workspace/blocked/`、`.ai-workspace/completed/` 中包含 `codescan_alert_number: <alert-number>` 的任务
- 如果找到，在任务文件中添加关闭记录：

```yaml
closed_at: {当前时间}
closed_reason: {关闭理由类别}
closed_comment: {用户的详细说明}
```

并将任务移动到 `completed/` 或 `dismissed/` 目录（根据理由）

### 8. 告知用户

输出格式：
```
✅ Code Scanning 告警 #{alert-number} 已关闭

**告警信息**:
- 规则: {rule.id}
- 位置: {location.path}:{location.start_line}
- 工具: {tool.name}

**关闭信息**:
- 关闭理由: {关闭理由类别}
- 详细说明: {用户的详细说明}
- 关闭时间: {当前时间}

**查看链接**:
{html_url}

**下一步**：
如果还有其他待处理的安全告警，可以使用以下命令分析：
- Claude Code / OpenCode: `/analyze-codescan {alert-number}`
- Gemini CLI: `/fit:analyze-codescan {alert-number}`
- Codex CLI: `/prompts:fit-analyze-codescan {alert-number}`

⚠️ 注意: 如果将来发现此告警应该修复，可以在 GitHub 网页上重新打开。
```

## 参数说明

- `<alert-number>`: Code Scanning 告警编号（必需）

## 使用示例

```bash
# 关闭 Code Scanning 告警 #5
/close-codescan 5
```

## 注意事项

1. **谨慎关闭高危告警**：
   - Critical/High 严重程度的告警需要特别谨慎
   - 必须有充分的技术分析支持
   - 建议先进行安全分析（使用 `/analyze-codescan`）

2. **理由必须真实准确**：
   - 关闭记录会保存在 GitHub 中
   - 可能被安全审计或团队审查
   - 不要为了清空告警而随意关闭

3. **定期复查**：
   - 被关闭的告警应定期复查
   - 项目代码变更可能导致之前的理由失效

4. **优先考虑修复**：
   - 关闭应该是最后选择
   - 优先考虑修改源代码修复问题
   - 只在确实为误报或风险可接受时关闭

5. **团队沟通**：
   - 重要的关闭决定应与团队讨论
   - 在 Issue 或 PR 中记录决策过程
   - 确保相关人员知晓

## 相关命令

- `/analyze-codescan <alert-number>` - 分析 Code Scanning 告警
- `/analyze-dependabot <alert-number>` - 分析 Dependabot 依赖漏洞告警
- `/plan-task <task-id>` - 设计修复方案

## 错误处理

- 告警不存在：提示 "Code Scanning 告警 #{number} 不存在，请检查告警编号"
- 告警已关闭：提示 "Code Scanning 告警 #{number} 已经是 {state} 状态"
- 权限错误：提示 "没有权限修改 Code Scanning 告警，请检查 GitHub CLI 认证状态"
- API 错误：提示 "关闭失败: {错误信息}"
- 用户取消：提示 "已取消关闭操作"

## 最佳实践

1. **先分析再关闭**：
   ```bash
   # 推荐的工作流程
   /analyze-codescan 5    # 先进行详细分析
   # 审查分析报告
   /close-codescan 5      # 确认后再关闭
   ```

2. **记录决策过程**：
   - 在关闭说明中引用分析文档
   - 说明谁做的决定、基于什么分析

3. **建立复查机制**：
   - 定期（如每季度）复查已关闭的告警
   - 在项目升级时重新评估
