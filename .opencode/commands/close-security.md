---
description: 关闭 Dependabot 安全告警(需提供合理理由)
agent: general
subtask: false
---

关闭 Dependabot 安全告警 #$1。在关闭前会要求用户确认并提供合理的理由。

执行以下步骤:

1. 获取安全告警信息:
   !`gh api repos/<owner>/<repo>/dependabot/alerts/$1`
   
   验证告警状态:
   - 如果已经是 dismissed 或 fixed 状态,提示用户并退出
   - 如果是 open 状态,继续执行

2. 展示告警详情:
   ```
   🔒 安全告警 #$1
   
   严重程度: <severity>
   漏洞: <summary>
   受影响包: <package-name> (<ecosystem>)
   当前版本: <current-version>
   受影响范围: <vulnerable-version-range>
   修复版本: <first-patched-version>
   
   GHSA: <ghsa-id>
   CVE: <cve-id>
   ```

3. 询问关闭理由:
   
   **问题**: "为什么要关闭这个安全告警?"
   
   **选项**:
   1. 误报 (False Positive) - 漏洞代码路径在项目中未被使用
   2. 无法利用 (Not Exploitable) - 在当前项目场景下无法被利用
   3. 已有缓解措施 (Mitigated) - 已通过其他方式缓解了风险
   4. 无修复版本且风险可接受 (No Fix Available)
   5. 测试或开发依赖 (Dev Dependency Only) - 仅在测试或开发环境使用
   6. 取消 - 不关闭告警

4. 要求详细说明:
   如果用户选择关闭,要求提供详细的文字说明(最少 20 个字符):
   - 清晰说明为什么此告警可以安全关闭
   - 如果是误报,说明为什么代码路径不会被触发
   - 如果有缓解措施,说明具体措施是什么

5. 最终确认:
   ```
   ⚠️ 即将关闭安全告警 #$1
   
   告警: <summary>
   严重程度: <severity>
   关闭理由类别: <选择的理由>
   详细说明: <用户输入的说明>
   
   是否确认关闭?(y/N)
   ```

6. 执行关闭操作:
   ```bash
   gh api --method PATCH \
     repos/<owner>/<repo>/dependabot/alerts/$1 \
     -f state=dismissed \
     -f dismissed_reason="<API参数>" \
     -f dismissed_comment="<用户的详细说明>"
   ```
   
   **选项到 API 参数的映射**:
   - 误报 → not_used 或 inaccurate
   - 无法利用 → tolerable_risk
   - 已有缓解措施 → tolerable_risk
   - 无修复版本且风险可接受 → tolerable_risk
   - 测试或开发依赖 → not_used

7. 记录到任务(如果存在):
   - 搜索包含 security_alert_number: $1 的任务
   - 如果找到,添加关闭记录并归档任务

8. 告知用户:
   ```
   ✅ 安全告警 #$1 已关闭
   
   **告警信息**:
   - 漏洞: <summary>
   - 严重程度: <severity>
   - 受影响包: <package-name>
   
   **关闭信息**:
   - 关闭理由: <关闭理由类别>
   - 详细说明: <用户的详细说明>
   - 关闭时间: <当前时间>
   
   **查看链接**:
   https://github.com/<owner>/<repo>/security/dependabot/$1
   
   ⚠️ 注意: 如果将来发现此告警应该修复,可以在 GitHub 网页上重新打开。
   ```

**注意事项**:
- 谨慎关闭高危告警(Critical/High)
- 理由必须真实准确
- 关闭应该是最后选择,优先考虑修复
- 定期复查被关闭的告警
- 重要的关闭决定应与团队讨论
- 建议先使用 /analyze-security $1 进行详细分析
