# 🔒 安全政策 / Security Policy

## 📋 支持的版本 / Supported Versions

请使用下表了解哪些版本的项目目前受到安全更新的支持。
Please use the following table to understand which versions are currently supported with security updates.

| 版本 / Version | 支持状态 / Support Status |
| -------------- | ------------------------- |
| v3.5.x         | ✅ 支持 / Supported       |
| < v3.5.0       | ❌ 不支持 / Not Supported |

## 🚨 报告漏洞 / Reporting Vulnerabilities

我们非常重视安全问题。如果您发现了安全漏洞，请按照以下步骤进行报告：
We take security issues very seriously. If you discover a security vulnerability, please follow these steps to report it:

### 如何报告 / How to Report

**⚠️ 请不要在公共 GitHub Issues 中报告安全漏洞。**
**⚠️ Please do not report security vulnerabilities in public GitHub issues.**

相反，请通过以下方式私下报告：
Instead, please report privately through the following methods:

1. **🔐 GitHub 安全建议 / GitHub Security Advisory** (推荐 / Recommended)
   - 转到项目的 "Security" 选项卡 / Go to the project's "Security" tab
   - 点击 "Report a vulnerability" / Click "Report a vulnerability"
   - 填写安全建议表单 / Fill out the security advisory form

### 📋 报告内容 / Report Content

请在您的报告中包含以下信息：
Please include the following information in your report:

- **🔍 漏洞类型 / Vulnerability Type**：简要描述漏洞的性质 / Briefly describe the nature of the vulnerability
- **📍 影响范围 / Impact Scope**：受影响的组件、版本或功能 / Affected components, versions, or features
- **🔄 重现步骤 / Reproduction Steps**：详细的步骤说明如何重现该漏洞 / Detailed steps on how to reproduce the vulnerability
- **💡 概念验证 / Proof of Concept**：如果可能，提供 PoC 代码或截图 / If possible, provide PoC code or screenshots
- **⚡ 影响评估 / Impact Assessment**：潜在的安全影响和风险等级 / Potential security impact and risk level
- **🛠️ 建议修复 / Suggested Fix**：如果您有修复建议，请提供 / If you have fix suggestions, please provide them

### ⏰ 响应时间表 / Response Timeline

我们承诺按照以下时间表响应安全报告：
We commit to responding to security reports according to the following timeline:

- **✅ 确认收到 / Acknowledgment**：24小时内 / Within 24 hours
- **🔍 初步评估 / Initial Assessment**：72小时内 / Within 72 hours
- **📊 详细分析 / Detailed Analysis**：7个工作日内 / Within 7 business days
- **🚀 修复发布 / Fix Release**：根据严重程度，1-30天内 / 1-30 days depending on severity

### 🎯 漏洞等级 / Vulnerability Severity

我们使用以下标准评估漏洞严重程度：
We use the following criteria to assess vulnerability severity:

#### 🔴 严重 / Critical
- 远程代码执行 / Remote Code Execution
- SQL注入导致数据泄露 / SQL Injection leading to data breach
- 身份验证绕过 / Authentication bypass

#### 🟠 高危 / High
- 跨站脚本攻击 (XSS) / Cross-Site Scripting (XSS)
- 跨站请求伪造 (CSRF) / Cross-Site Request Forgery (CSRF)
- 权限提升 / Privilege escalation

#### 🟡 中危 / Medium
- 信息泄露 / Information disclosure
- 拒绝服务攻击 / Denial of Service (DoS)
- 弱加密 / Weak cryptography

#### 🟢 低危 / Low
- 配置问题 / Configuration issues
- 信息收集类漏洞 / Information gathering vulnerabilities

### 🔄 处理流程 / Handling Process

1. **📥 报告接收 / Report Reception**：我们收到您的报告并确认 / We receive your report and acknowledge it
2. **✅ 漏洞验证 / Vulnerability Verification**：我们的安全团队验证漏洞的存在和影响 / Our security team verifies the vulnerability's existence and impact
3. **📊 影响评估 / Impact Assessment**：评估漏洞的严重程度和影响范围 / Assess vulnerability severity and impact scope
4. **🛠️ 修复开发 / Fix Development**：开发和测试修复方案 / Develop and test fix solutions
5. **🤝 协调发布 / Coordinated Release**：与报告者协调披露时间 / Coordinate disclosure timing with reporter
6. **📢 公开披露 / Public Disclosure**：发布安全更新和公告 / Release security updates and announcements

### 🤝 负责任的披露 / Responsible Disclosure

我们遵循负责任的披露原则：
We follow responsible disclosure principles:

- 我们会在修复漏洞后公开披露 / We will publicly disclose after fixing the vulnerability
- 在修复发布前，请不要公开讨论漏洞 / Please do not publicly discuss the vulnerability before the fix is released
- 我们将在安全公告中适当地感谢报告者（除非您希望保持匿名）/ We will appropriately thank reporters in security announcements (unless you prefer to remain anonymous)

### 📢 安全更新通知 / Security Update Notifications

要接收安全更新通知，请：
To receive security update notifications, please:

1. **👀 Watch 此仓库 / Watch this Repository**并启用安全警报 / and enable security alerts
2. **🔔 订阅发布 / Subscribe to Releases**以获取新版本通知 / to get new version notifications
3. **📰 关注我们的安全公告 / Follow our Security Announcements**

### 🛡️ 安全最佳实践 / Security Best Practices

使用此项目时，建议遵循以下安全最佳实践：
When using this project, we recommend following these security best practices:

- 始终使用最新的支持版本 / Always use the latest supported version
- 定期更新依赖项 / Regularly update dependencies
- 启用适当的日志记录和监控 / Enable appropriate logging and monitoring
- 实施最小权限原则 / Implement the principle of least privilege
- 定期进行安全审计 / Conduct regular security audits

### 📦 范围说明 / Scope

此安全政策适用于：
This security policy applies to:

- ✅ 此 GitHub 仓库中的所有代码 / All code in this GitHub repository
- ✅ 官方发布的二进制文件 / Official binary releases
- ✅ 官方 Docker 镜像 / Official Docker images
- ❌ 第三方插件或扩展 / Third-party plugins or extensions
- ❌ 用户自定义配置错误 / User configuration errors

### 🏆 致谢 / Acknowledgments

我们感谢以下研究人员对项目安全性的贡献：
We thank the following researchers for their contributions to project security:

<!-- 
感谢名单将在此处更新 / Acknowledgment list will be updated here
- [研究人员姓名 / Researcher Name] - 发现并报告了 [漏洞类型] / Discovered and reported [Vulnerability Type]
-->

---

**📌 注意 / Note**：此安全政策可能会定期更新。请定期查看最新版本。  
**📌 Note**: This security policy may be updated regularly. Please check the latest version periodically.

**最后更新 / Last Updated**：2025年6月
