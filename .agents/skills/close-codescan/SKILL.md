---
name: close-codescan
description: 关闭 Code Scanning (CodeQL) 告警，需提供合理理由。当用户要求关闭 Code Scanning 告警、dismiss CodeQL 告警时触发。参数为告警编号。
---

# 关闭 Code Scanning 告警

## 执行步骤

1. 获取告警信息:
   ```bash
   gh api "repos/{owner}/{repo}/code-scanning/alerts/<alert-number>"
   ```

2. 展示告警详情（规则 ID、严重程度、源码位置、告警消息）。

3. 询问关闭理由:
   1. 误报（False Positive）
   2. 不会修复（Won't Fix）
   3. 测试代码（Used in Tests）

4. 要求详细说明（最少 20 个字符）。

5. 最终确认后执行关闭:
   ```bash
   gh api --method PATCH "repos/{owner}/{repo}/code-scanning/alerts/<alert-number>" -f state=dismissed -f dismissed_reason="<reason>" -f dismissed_comment="<comment>"
   ```
   dismissed_reason 有效值: "false positive", "won't fix", "used in tests"

**注意**: 谨慎关闭高危告警，优先考虑修复源代码。建议先使用 analyze-codescan 进行详细分析。
