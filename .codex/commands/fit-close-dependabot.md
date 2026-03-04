---
description: 关闭 Dependabot 安全告警(需提供合理理由)
argument-hint: <alert-number>
---

关闭 Dependabot 安全告警 #$1。在关闭前会要求用户确认并提供合理的理由。

执行以下步骤:

1. 获取安全告警信息:
   ```bash
   gh api "repos/{owner}/{repo}/dependabot/alerts/$1"
   ```
   验证告警状态: 如果已是 dismissed 或 fixed,提示用户并退出。

2. 展示告警详情:
   ```
   🔒 安全告警 #$1
   严重程度: <severity>
   漏洞: <summary>
   受影响包: <package-name>
   修复版本: <first-patched-version>
   ```

3. 询问关闭理由:
   选项:
   1. 误报(False Positive) - 漏洞代码路径未被使用
   2. 无法利用(Not Exploitable) - 当前场景下无法被利用
   3. 已有缓解措施(Mitigated)
   4. 无修复版本且风险可接受
   5. 测试或开发依赖(Dev Only)
   6. 取消 - 不关闭

4. 要求详细说明(最少 20 个字符)

5. 最终确认后执行关闭:
   ```bash
   gh api --method PATCH "repos/{owner}/{repo}/dependabot/alerts/$1" -f state=dismissed -f dismissed_reason="<reason>" -f dismissed_comment="<comment>"
   ```

6. 告知用户:
   ```
   ✅ 安全告警 #$1 已关闭
   关闭理由: <理由>
   查看链接: https://github.com/<owner>/<repo>/security/dependabot/$1
   ```

**注意事项**:
- 谨慎关闭高危告警(Critical/High)
- 关闭应该是最后选择,优先考虑修复
- 建议先使用 /analyze-dependabot $1 进行详细分析
- 定期复查被关闭的告警
