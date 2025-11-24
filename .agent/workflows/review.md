---
description: 审查 Pull Request 的质量和合规性
---

1. 解析 PR 编号。
   - 示例输入："Review PR #123"

2. 收集 PR 信息。
   - `run_command("gh pr view <pr-number>")`
   - `run_command("gh pr diff <pr-number>")`
   - `run_command("gh pr checks <pr-number>")`

3. 分析 PR。
   - **合规性**: PR 描述是否符合 `.github/PULL_REQUEST_TEMPLATE.md`？所有复选框是否已填写？
   - **质量**: 是否有明显的 bug、安全问题或代码风格违规？
   - **测试**: 是否包含测试？检查是否通过？

4. 生成审查报告。
   - 创建发现问题的摘要。
   - 列出具体问题或建议。
   - 给出结论性建议（批准、请求更改、评论）。
