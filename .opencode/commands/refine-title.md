---
description: 深度分析 Issue 或 PR 内容并重构标题为 Conventional Commits 格式
agent: general
subtask: false
---

针对 GitHub Issue 或 PR #$1,读取其详细描述、标签和代码变更,深度理解其意图,然后生成符合 type(scope): subject 规范的新标题并执行修改。

执行以下步骤:

1. 识别对象与获取信息:
   
   先尝试获取 Issue 信息:
   !`gh issue view $1 --json number,title,body,labels,state 2>&1`
   
   如果上面返回错误(不是 Issue),尝试获取 PR 信息:
   !`gh pr view $1 --json number,title,body,labels,state,files 2>&1`
   
   如果两者都失败:
   !`echo "❌ ERROR: #$1 不是有效的 Issue 或 PR 编号"`

2. 智能分析:
   
   2.1 确定 Type (类型):
   - 阅读 body 中的"变更类型"或描述
   - 检查 labels (如 type:bug → fix, type:feature → feat)
   - 如果是 PR,分析 files (仅文档变动 → docs,仅测试变动 → test)
   
   2.2 确定 Scope (范围):
   - 阅读 body 提及的模块
   - 检查 labels (如 in:fit → fit)
   - 如果是 PR,分析 files 路径 (如 framework/fit/java/... → fit)
   
   2.3 生成 Subject (摘要):
   - **忽略原标题**(避免受干扰),直接从 body 中提炼核心意图
   - 确保简练(20字以内)、中文描述、无句号

3. 生成建议与交互:
   
   输出分析结果供用户确认:
   ```
   🔍 分析对象: Issue #$1 / PR #$1
   
   当前标题: <原标题>
   --------------------------------------------------
   🧠 分析依据:
   - 原始意图: <从 Body 提取的一句话摘要>
   - 推断类型: <Fix/Feat/Docs等> (依据: <标签/Body关键词/文件变动>)
   - 推断范围: <fit/waterflow等> (依据: <涉及文件路径/标签>)
   --------------------------------------------------
   ✨ 建议标题: <type>(<scope>): <subject>
   ```
   
   询问用户: "是否确认修改?(y/n)"

4. 执行修改:
   
   用户确认后,根据对象类型执行命令:
   
   如果是 Issue:
   !`gh issue edit $1 --title "<new-title>" && echo "✅ Issue 标题已更新" || echo "❌ ERROR: 更新失败"`
   
   如果是 PR:
   !`gh pr edit $1 --title "<new-title>" && echo "✅ PR 标题已更新" || echo "❌ ERROR: 更新失败"`

5. 告知用户:
   ```
   ✅ 标题已更新
   
   **原标题**: <old-title>
   **新标题**: <new-title>
   
   **查看链接**:
   https://github.com/<owner>/<repo>/issues/$1
   或
   https://github.com/<owner>/<repo>/pull/$1
   ```

**优势**:
- 相比批量修改,此命令能深度理解内容并修正错误标题
- 通过分析 PR 的文件变动,能自动判断准确的 scope
- 不受原标题干扰,从内容中提炼真实意图

**注意事项**:
- 必须先分析内容,不要直接使用原标题
- 确保用户确认后再执行修改
- 如果分析不确定,询问用户选择
