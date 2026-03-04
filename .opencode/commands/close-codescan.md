---
description: 关闭 Code Scanning 告警(需提供合理理由)
agent: general
subtask: false
---

关闭 Code Scanning 告警 #$1。在关闭前会要求用户确认并提供合理的理由。

执行以下步骤:

1. 获取告警信息:
   !`gh api "repos/{owner}/{repo}/code-scanning/alerts/$1"`

   验证告警状态:
   - 如果已经是 dismissed 或 fixed 状态,提示用户并退出
   - 如果是 open 状态,继续执行

2. 展示告警详情:
   ```
   🔍 Code Scanning 告警 #$1

   严重程度: <severity>
   规则: <rule-id> - <rule-description>
   工具: <tool-name>
   位置: <file-path>:<line-number>
   消息: <message>
   ```

3. 询问关闭理由:

   **问题**: "为什么要关闭这个 Code Scanning 告警?"

   **选项**:
   1. 误报 (False Positive) - CodeQL 规则误判,代码实际上不存在此安全问题
   2. 不会修复 (Won't Fix) - 已知问题但基于架构或业务原因不予修复
   3. 测试代码 (Used in Tests) - 仅在测试代码中出现,不影响生产环境安全
   4. 取消 - 不关闭告警

4. 要求详细说明:
   如果用户选择关闭,要求提供详细的文字说明(最少 20 个字符):
   - 清晰说明为什么此告警可以安全关闭
   - 如果是误报,说明为什么代码不存在此安全问题
   - 如果是不修复,说明技术或业务原因

5. 最终确认:
   ```
   ⚠️ 即将关闭 Code Scanning 告警 #$1

   规则: <rule-id>
   位置: <file-path>:<line-number>
   关闭理由类别: <选择的理由>
   详细说明: <用户输入的说明>

   是否确认关闭?(y/N)
   ```

6. 执行关闭操作:
   !`gh api --method PATCH "repos/{owner}/{repo}/code-scanning/alerts/$1" -f state=dismissed -f dismissed_reason="<API参数>" -f dismissed_comment="<用户的详细说明>" && echo "✅ 告警已关闭" || echo "❌ ERROR: 关闭失败"`

   **选项到 API 参数的映射**:
   - 误报 → false positive
   - 不会修复 → won't fix
   - 测试代码 → used in tests

7. 记录到任务(如果存在):
   !`grep -r "codescan_alert_number: $1" .ai-workspace/active/ .ai-workspace/blocked/ 2>/dev/null || echo "⚠️  无关联任务"`

   如果找到相关任务,添加关闭记录并归档任务

8. 告知用户:
   ```
   ✅ Code Scanning 告警 #$1 已关闭

   **告警信息**:
   - 规则: <rule-id>
   - 位置: <file-path>:<line-number>
   - 工具: <tool-name>

   **关闭信息**:
   - 关闭理由: <关闭理由类别>
   - 详细说明: <用户的详细说明>
   - 关闭时间: <当前时间>

   **下一步**：
   如果还有其他待处理的安全告警，可以使用以下命令分析：
   - Claude Code / OpenCode: `/analyze-codescan {alert-number}`
   - Gemini CLI: `/fit:analyze-codescan {alert-number}`
   - Codex CLI: `/prompts:fit-analyze-codescan {alert-number}`

   ⚠️ 注意: 如果将来发现此告警应该修复,可以在 GitHub 网页上重新打开。
   ```

**注意事项**:
- 谨慎关闭高危告警(Critical/High)
- 理由必须真实准确
- 关闭应该是最后选择,优先考虑修复源代码
- 定期复查被关闭的告警
- 重要的关闭决定应与团队讨论
- 建议先使用 /analyze-codescan $1 进行详细分析
