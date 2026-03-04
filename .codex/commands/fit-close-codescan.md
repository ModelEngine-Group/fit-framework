---
description: 关闭 Code Scanning 告警(需提供合理理由)
argument-hint: <alert-number>
---

关闭 Code Scanning 告警 #$1。在关闭前会要求用户确认并提供合理的理由。

执行以下步骤:

1. 获取告警信息:
   ```bash
   gh api "repos/{owner}/{repo}/code-scanning/alerts/$1"
   ```
   验证告警状态: 如果已是 dismissed 或 fixed,提示用户并退出。

2. 展示告警详情:
   ```
   🔍 Code Scanning 告警 #$1
   严重程度: <severity>
   规则: <rule-id> - <rule-description>
   工具: <tool-name>
   位置: <file-path>:<line-number>
   ```

3. 询问关闭理由:
   选项:
   1. 误报(False Positive) - CodeQL 规则误判
   2. 不会修复(Won't Fix) - 基于架构或业务原因不予修复
   3. 测试代码(Used in Tests) - 仅在测试代码中出现
   4. 取消 - 不关闭

4. 要求详细说明(最少 20 个字符)

5. 最终确认后执行关闭:
   ```bash
   gh api --method PATCH "repos/{owner}/{repo}/code-scanning/alerts/$1" -f state=dismissed -f dismissed_reason="<reason>" -f dismissed_comment="<comment>"
   ```

6. 告知用户:
   ```
   ✅ Code Scanning 告警 #$1 已关闭
   关闭理由: <理由>
   ```
   - 提示下一步:
     - 如果还有其他待处理的安全告警:
       - Claude Code / OpenCode: /analyze-codescan {alert-number}
       - Gemini CLI: /fit:analyze-codescan {alert-number}
       - Codex CLI: /prompts:fit-analyze-codescan {alert-number}

**注意事项**:
- 谨慎关闭高危告警(Critical/High)
- 关闭应该是最后选择,优先考虑修复源代码
- 建议先使用 /analyze-codescan $1 进行详细分析
- 定期复查被关闭的告警
