---
name: "create-release-note"
description: "从 PR/commit 中自动生成结构化的 Release Notes，并可选创建 GitHub Draft Release"
usage: "/create-release-note <version> [prev-version]"
---

# Create Release Note Command

## 功能说明

自动从 PR、commit 和 Issue 中收集变更信息，按模块和类型分类，生成符合项目格式的 Release Notes。支持创建 GitHub Draft Release。

对于 x.y.0 版本，支持合并前一 minor 系列的已发布 release notes，无需重复计算。

## 用法

```bash
/release-notes <version>                # 自动推断上一版本
/release-notes <version> <prev-version> # 手动指定版本范围
```

例如：
```bash
/create-release-note 3.6.3           # 自动推断上一版本为 3.6.2
/create-release-note 3.6.3 3.6.2     # 手动指定范围
/create-release-note 3.7.0           # x.y.0 版本：合并 3.6.x 系列的所有 release notes
```

## 参数说明

- `<version>`: 当前发布版本号，格式为 `X.Y.Z`（必需）
- `<prev-version>`: 上一版本号，格式为 `X.Y.Z`（可选，不提供则自动推断）

参数来源：`$ARGUMENTS`

## 执行步骤

步骤 1-3 为公共步骤，步骤 3 根据版本类型分流：
- **合并路径**（PATCH == 0，如 `3.7.0`）：步骤 4-7 → 跳到步骤 14
- **常规路径**（PATCH > 0，如 `3.6.3`）：步骤 8-13 → 继续步骤 14

步骤 14-15 为公共步骤。

### 步骤 1：解析参数

从 `$ARGUMENTS` 中提取参数。支持两种形式：
- 单参数：`<version>` — 当前版本号
- 双参数：`<version> <prev-version>` — 当前版本号和上一版本号

**版本号格式验证**：
- 必须匹配 `X.Y.Z` 格式（X、Y、Z 均为非负整数）
- 如果格式不正确，报错退出：`错误：版本号格式不正确，期望格式为 X.Y.Z（例如 3.6.3）`

### 步骤 2：确定版本范围

**当前版本 tag**: `v<version>`（如 `v3.6.3`）

**上一版本 tag 推断逻辑**（仅当未指定 `<prev-version>` 时）：

```bash
# 获取所有已排序的 tag
git tag --sort=-v:refname
```

- 如果 PATCH > 0（如 `3.6.3`）：查找同一 minor 系列中的前一个 tag（如 `v3.6.2`）
- 如果 PATCH == 0（如 `3.6.0`）：查找前一个 minor 系列的最后一个 tag（如 `v3.5.x` 中最大的）

**验证 tag 存在**：

```bash
git rev-parse v<version>
git rev-parse v<prev-version>
```

如果任一 tag 不存在，报错退出：`错误：Tag v<version> 不存在，请确认 tag 已创建`

### 步骤 3：判断版本类型并选择路径

根据版本号的 PATCH 部分选择不同的生成路径：

- **如果 PATCH == 0**（如 `3.7.0`）→ 走**合并路径**，从步骤 4 开始
- **如果 PATCH > 0**（如 `3.6.3`）→ 走**常规路径**，从步骤 8 开始

---

### 合并路径（PATCH == 0，x.y.0 版本）

### 步骤 4：查找前一 minor 系列的所有已发布 release

```bash
gh release list --limit 50 --json tagName,isDraft,isPrerelease
```

从结果中筛选满足以下条件的条目：
- `tagName` 以 `vX.(Y-1).` 开头（例如版本 `3.7.0` 则筛选 `v3.6.` 开头）
- `isDraft == false`
- `isPrerelease == false`

如果未找到任何已发布的 release，提示用户并回退到常规路径（步骤 8）。

### 步骤 5：按版本号升序获取各 release body

对步骤 4 筛选出的每个 release tag，按版本号升序获取其内容：

```bash
gh release view v<tag> --json body --jq .body
```

### 步骤 6：合并所有 release body

将各版本的 release notes 合并为一份完整文档：

1. **按版本顺序拼接**：按 `v3.6.1`、`v3.6.2`、`v3.6.3` ... 的顺序处理
2. **同平台同类型条目合并**：将各版本中相同平台（如 FIT Function Platform）相同类型（如 Enhancement）的条目合并到一起
3. **去重 Contributors**：合并所有 `❤️ Contributors` 段落中的贡献者，去重后按贡献量（出现次数）降序排列

### 步骤 7：生成 Overview

x.y.0 版本始终视为 Major release：

1. **在顶部添加 `🌟 Overview` 总结段落**：AI 根据合并内容生成 2-3 句话概括整个 minor 系列的核心主题和亮点
2. **每个平台添加 `🚀 Features Overview` 要点列表**：从该平台的 Enhancement 条目中提炼 3-5 个关键特性要点

**完成后跳到步骤 14。**

---

### 常规路径（PATCH > 0）

### 步骤 8：收集合并的 PR

**主要数据源** — 获取两个 tag 之间的日期范围，然后用 `gh` CLI 查询合并的 PR：

```bash
# 获取两个 tag 的日期
git log v<prev-version> --format=%aI -1
git log v<version> --format=%aI -1

# 获取目标分支（从当前版本号推断，如 3.6.x）
# 分支名格式: X.Y.x

# 获取合并到目标分支的 PR
gh pr list --state merged --base <branch> \
  --json number,title,body,author,labels,mergedAt,url \
  --limit 200 --search "merged:YYYY-MM-DD..YYYY-MM-DD"
```

**补充数据源** — 获取没有关联 PR 的直接 commit：

```bash
git log v<prev-version>..v<version> --format="%H %s" --no-merges
```

对比 PR 列表和 commit 列表，找出没有关联 PR 的 commit（这些 commit 也应纳入 release notes）。

### 步骤 9：收集关联的 Issue

从每个 PR 的 body 中提取关联的 Issue：
- 匹配模式：`Closes #N`、`Fixes #N`、`Resolves #N`（不区分大小写）
- 也匹配：`close #N`、`fix #N`、`resolve #N` 及其复数形式

对每个提取到的 Issue 编号：

```bash
gh issue view <N> --json number,title,labels,url
```

收集 Issue 详情用于丰富 release notes 的描述。

### 步骤 10：分类 — 按模块分组

根据以下**优先级**判断每个 PR/commit 所属模块：

| 优先级 | 判断依据 | 示例 |
|--------|----------|------|
| 1 | PR title 中的模块标签 `[fit]`, `[FEL]`, `[waterflow]` | `[fit] 修复空指针` → FIT |
| 2 | Conventional commit scope `feat(fit):`, `fix(waterflow):` | `feat(waterflow): xxx` → Waterflow |
| 3 | PR 变更文件路径（使用 `gh pr view <N> --json files`） | `framework/fel/**` → FEL |
| 4 | 默认归入 FIT Function Platform | |

**模块到平台的映射**：

| 关键词 / 路径 | 平台名称 |
|---------------|----------|
| `fit`, `python`, `docker`, `fit-launcher`, `framework/fit/**` | **FIT Function Platform** |
| `fel`, `FEL`, `framework/fel/**` | **FIT Expression for LLM** |
| `waterflow`, `framework/waterflow/**` | **Waterflow Flow Scheduling Platform** |
| `claude`, `.claude/`, `.agents/`, AI 相关配置 | **🤖 AI Development Configuration** |

### 步骤 11：分类 — 按类型分组

根据 PR title 的 conventional commit type 分类：

| PR Title 前缀 / 特征 | 分类 |
|----------------------|------|
| `feat`, `perf`, `refactor`, `chore(deps)`, 依赖升级 | ✨ Enhancement |
| `fix` | ✅ Bugfix |
| `docs` | 📚 Documentation（如果条目少于 3 个，并入 Enhancement） |

### 步骤 12：判断发布级别

根据变更数量和性质判断输出详细程度：

- **Major release**（合并 PR > 15 个）：
  - 生成 `🌟 Overview` 总结段落（2-3 句话概括本次发布的核心主题）
  - 每个平台生成 `🚀 Features Overview` 要点列表
- **Regular release**：
  - 直接列出 Enhancement / Bugfix 条目，不加 Overview

### 步骤 13：生成 Release Notes

按照项目已有格式输出 markdown。完整模板：

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

**格式规则**（从现有 release notes 中提炼）：

1. **条目格式**: `- [module] 描述 by @author1 and @author2 in [#N](url)`
2. **关联 Issue 和 PR**: `in [#Issue](issue-url) and [#PR](pr-url)`
3. **没有关联 PR 的 commit**: 省略 `in [#N]` 部分，直接写描述
4. **描述内容**: 优先使用 PR title，去掉 `type(scope):` 前缀，首字母大写
5. **贡献者列表**: 去重，按贡献量（PR 数量）降序排列
6. **空平台**: 如果某个平台没有任何变更，不输出该平台的章节
7. **多作者**: 如果 PR 有多个 co-author，用 `and` 连接：`by @a and @b`

---

### 公共步骤

### 步骤 14：展示并确认

将生成的 release notes **完整输出**给用户查看。

然后询问用户：
1. 是否需要调整内容（修改描述、调整分类、增删条目等）
2. 是否创建 GitHub Draft Release

如果用户要求调整，根据反馈修改后重新输出。

### 步骤 15：创建 Draft Release

用户确认后，将 release notes 写入临时文件，然后创建 Draft Release：

```bash
gh release create v<version> \
  --title "v<version>" \
  --notes-file /tmp/release-notes-v<version>.md \
  --target <release-branch-or-tag> \
  --draft
```

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

## 注意事项

1. **需要 `gh` CLI**：本命令依赖 GitHub CLI（`gh`），请确保已安装并认证
2. **Tag 必须已存在**：运行本命令前，确保 `v<version>` 和上一版本的 tag 已创建（通常由 `/release` 命令完成）
3. **Draft 模式**：创建的是草稿 Release，不会自动发布，需要人工审核后在 GitHub 上发布
4. **PR 搜索范围**：基于日期范围搜索，可能包含少量超出范围的 PR，命令会尽力过滤
5. **模块分类准确性**：自动分类基于 title/scope/文件路径推断，复杂 PR 可能需要人工调整
6. **x.y.0 合并路径**：依赖前一 minor 系列的 release 已在 GitHub 上发布（非 Draft），如果没有已发布的 release 则回退到常规路径

## 错误处理

- **版本号格式错误**：提示正确格式并退出
- **Tag 不存在**：提示确认 tag 已创建（可能需要先执行 `/release`）
- **`gh` CLI 未安装或未认证**：提示安装/认证方法
- **无合并 PR**：提示版本范围内没有找到合并的 PR，建议检查 tag 和分支
- **GitHub API 限流**：提示稍后重试
- **x.y.0 无已发布 release**：提示前一 minor 系列没有已发布的 release，回退到常规路径

## 相关命令

- `/release <version>` - 执行版本发布流程（创建 tag 和发布分支）
- `/commit` - 提交代码
- `/create-pr` - 创建 Pull Request
