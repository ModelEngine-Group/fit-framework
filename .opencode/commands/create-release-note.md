---
description: 从 PR/commit 中自动生成结构化的 Release Notes，并可选创建 GitHub Draft Release
agent: general
subtask: false
---

为版本 $ARGUMENTS 生成 Release Notes。

自动从 PR、commit 和 Issue 中收集变更信息，按模块和类型分类，生成符合项目格式的 Release Notes。支持创建 GitHub Draft Release。对于 x.y.0 版本，支持合并前一 minor 系列的已发布 release notes。

步骤 1-3 为公共步骤，步骤 3 根据版本类型分流：
- **合并路径**（PATCH == 0，如 `3.7.0`）：步骤 4-7 → 跳到步骤 14
- **常规路径**（PATCH > 0，如 `3.6.3`）：步骤 8-13 → 继续步骤 14
步骤 14-15 为公共步骤。

**步骤 1: 解析参数**

从 $ARGUMENTS 中提取参数。支持两种形式：
- 单参数：`<version>` — 当前版本号
- 双参数：`<version> <prev-version>` — 当前版本号和上一版本号

版本号必须匹配 `X.Y.Z` 格式（X、Y、Z 均为非负整数）。如果格式不正确，报错退出：`❌ 错误：版本号格式不正确，期望格式为 X.Y.Z（例如 3.6.3）`

**步骤 2: 确定版本范围**

当前版本 tag: `v<version>`

上一版本 tag 推断逻辑（仅当未指定 `<prev-version>` 时）：
!`git tag --sort=-v:refname | head -20`

- 如果 PATCH > 0: 查找同一 minor 系列中的前一个 tag
- 如果 PATCH == 0: 查找前一个 minor 系列的最后一个 tag

验证 tag 存在：
!`git rev-parse v<version> 2>/dev/null && echo "✅ Tag exists" || echo "❌ Tag not found"`

（将 `<version>` 替换为实际版本号执行）

如果任一 tag 不存在，报错退出：`❌ 错误：Tag v<version> 不存在，请确认 tag 已创建`

**步骤 3: 判断版本类型并选择路径**

根据版本号的 PATCH 部分选择不同的生成路径：
- 如果 PATCH == 0 → 走**合并路径**，从步骤 4 开始
- 如果 PATCH > 0 → 走**常规路径**，从步骤 8 开始

---

**合并路径（PATCH == 0，x.y.0 版本）**

**步骤 4: 查找前一 minor 系列的所有已发布 release**

!`gh release list --limit 50 --json tagName,isDraft,isPrerelease`

从结果中筛选满足以下条件的条目：
- `tagName` 以 `vX.(Y-1).` 开头
- `isDraft == false`
- `isPrerelease == false`

如果未找到任何已发布的 release，提示用户并回退到常规路径（步骤 8）。

**步骤 5: 按版本号升序获取各 release body**

对每个 release tag 执行：
!`gh release view v<tag> --json body --jq .body`

（将 `<tag>` 替换为实际 tag 执行）

**步骤 6: 合并所有 release body**

将各版本的 release notes 合并为一份完整文档：
1. 按版本顺序拼接各版本的完整 release notes
2. 同平台同类型条目合并（如 FIT Function Platform 的 Enhancement 条目合并到一起）
3. 去重 Contributors（合并所有 ❤️ Contributors 段落，去重后按贡献量降序排列）

**步骤 7: 生成 Overview**

x.y.0 始终视为 Major release：
1. 在顶部添加 🌟 Overview 总结段落（2-3 句话概括整个 minor 系列的核心主题和亮点）
2. 每个平台添加 🚀 Features Overview 要点列表（3-5 个关键特性要点）

**完成后跳到步骤 14。**

---

**常规路径（PATCH > 0）**

**步骤 8: 收集合并的 PR**

获取两个 tag 之间的日期范围：
!`git log v<prev-version> --format=%aI -1`
!`git log v<version> --format=%aI -1`

获取目标分支（分支名格式: X.Y.x），获取合并到目标分支的 PR：
!`gh pr list --state merged --base <branch> --json number,title,body,author,labels,mergedAt,url --limit 200 --search "merged:YYYY-MM-DD..YYYY-MM-DD"`

（将占位符替换为实际值执行）

补充数据源 — 获取没有关联 PR 的直接 commit：
!`git log v<prev-version>..v<version> --format="%H %s" --no-merges`

对比 PR 列表和 commit 列表，找出没有关联 PR 的 commit。

**步骤 9: 收集关联的 Issue**

从每个 PR 的 body 中提取关联的 Issue：
- 匹配模式：`Closes #N`、`Fixes #N`、`Resolves #N`（不区分大小写）
- 也匹配：`close #N`、`fix #N`、`resolve #N` 及其复数形式

对每个提取到的 Issue 编号：
!`gh issue view <N> --json number,title,labels,url`

**步骤 10: 分类 — 按模块分组**

按优先级判断模块:
1. PR title 中的模块标签 `[fit]`, `[FEL]`, `[waterflow]`
2. Conventional commit scope `feat(fit):`, `fix(waterflow):`
3. PR 变更文件路径（`gh pr view <N> --json files`）
4. 默认归入 FIT Function Platform

模块到平台的映射:
- fit/python/docker/fit-launcher/framework/fit/** → **FIT Function Platform**
- fel/FEL/framework/fel/** → **FIT Expression for LLM**
- waterflow/framework/waterflow/** → **Waterflow Flow Scheduling Platform**
- claude/.claude/.agents/AI 相关 → **🤖 AI Development Configuration**

**步骤 11: 分类 — 按类型分组**

- feat/perf/refactor/chore(deps)/依赖升级 → ✨ Enhancement
- fix → ✅ Bugfix
- docs → 📚 Documentation（条目少于 3 个则并入 Enhancement）

**步骤 12: 判断发布级别**

- **Major release**（合并 PR > 15 个）：生成 🌟 Overview + 每个平台 🚀 Features Overview
- **Regular release**：直接列出条目，不加 Overview

**步骤 13: 生成 Release Notes**

按项目格式输出 markdown：

```markdown
## FIT Function Platform

### ✨ Enhancement

- [fit] 描述内容 by @author1 and @author2 in [#123](url)
- Upgrade xxx from v1 to v2 by @author in [#456](url)

### ✅ Bugfix

- [fit] 修复xxx问题 by @author in [#100](issue-url) and [#789](pr-url)

## FIT Expression for LLM

### ✨ Enhancement

- [FEL] 描述内容 by @author in [#234](url)

## Waterflow Flow Scheduling Platform

### ✨ Enhancement

- [waterflow] 描述内容 by @author in [#345](url)

## 🤖 AI Development Configuration

### ✨ Enhancement

- 描述内容 by @author in [#567](url)

## ❤️ Contributors

@contributor1, @contributor2, @contributor3
```

格式规则:
1. 条目格式: `- [module] 描述 by @author1 and @author2 in [#N](url)`
2. 关联 Issue 和 PR: `in [#Issue](issue-url) and [#PR](pr-url)`
3. 没有关联 PR 的 commit: 省略 `in [#N]` 部分
4. 描述优先使用 PR title，去掉 type(scope): 前缀，首字母大写
5. 贡献者去重，按贡献量降序排列
6. 空平台不输出
7. 多作者用 `and` 连接

---

**公共步骤**

**步骤 14: 展示并确认**

将 release notes 完整输出给用户查看，询问：
1. 是否需要调整内容（修改描述、调整分类、增删条目等）
2. 是否创建 GitHub Draft Release

如果用户要求调整，根据反馈修改后重新输出。

**步骤 15: 创建 Draft Release**

用户确认后，将 release notes 写入临时文件，然后创建 Draft Release：
!`gh release create v<version> --title "v<version>" --notes-file /tmp/release-notes-v<version>.md --target <release-branch-or-tag> --draft`

输出结果：
```
✅ Draft Release 已创建

- Release URL: <draft-release-url>
- 版本: v<version>
- 状态: Draft（草稿）

⚠️ 请在 GitHub 上最终审核并发布：
1. 打开上述 URL
2. 检查 Release Notes 内容
3. 确认无误后点击 "Publish release"
```

**错误处理**:
- 版本号格式错误 → 提示正确格式并退出
- Tag 不存在 → 提示确认 tag 已创建（可能需要先执行 `/release`）
- gh CLI 未安装或未认证 → 提示安装/认证方法
- 无合并 PR → 提示检查 tag 和分支
- x.y.0 无已发布 release → 回退到常规路径

**相关命令**:
- `/release` - 执行版本发布流程（创建 tag 和发布分支）
- `/commit` - 提交代码
- `/create-pr` - 创建 Pull Request
