---
name: close-security
description: 关闭 Dependabot 安全告警，需提供合理理由。当用户要求关闭安全告警、dismiss 安全告警时触发。参数为告警编号。
---

# 关闭安全告警

## 执行步骤

1. 获取安全告警信息:
   ```bash
   gh api "repos/{owner}/{repo}/dependabot/alerts/<alert-number>"
   ```

2. 展示告警详情（严重程度、漏洞、受影响包、修复版本）。

3. 询问关闭理由:
   1. 误报（False Positive）
   2. 无法利用（Not Exploitable）
   3. 已有缓解措施（Mitigated）
   4. 无修复版本且风险可接受
   5. 测试或开发依赖（Dev Only）

4. 要求详细说明（最少 20 个字符）。

5. 最终确认后执行关闭:
   ```bash
   gh api --method PATCH "repos/{owner}/{repo}/dependabot/alerts/<alert-number>" -f state=dismissed -f dismissed_reason="<reason>" -f dismissed_comment="<comment>"
   ```

**注意**: 谨慎关闭高危告警，优先考虑修复。建议先使用 analyze-security 进行详细分析。
