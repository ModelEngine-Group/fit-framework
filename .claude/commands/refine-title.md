---
name: "refine-title"
description: "深度分析 Issue 或 PR 内容，并将其标题重构为 Conventional Commits 格式"
usage: "/refine-title <id>"
---

# Refine Title Command

## 功能说明

针对指定的 GitHub Issue 或 PR，读取其详细描述（Body）、标签（Labels）以及代码变更（如果是 PR），深度理解其意图，然后生成符合 `type(scope): subject` 规范的新标题并执行修改。

## 执行流程

### 1. 识别对象与获取信息

尝试判断 ID 是 Issue 还是 PR，并获取详细信息。

```bash
# 尝试获取 Issue 信息
gh issue view <id> --json number,title,body,labels,state

# 如果提示是 PR，或者查不到 Issue 但存在同号 PR，则获取 PR 信息
gh pr view <id> --json number,title,body,labels,state,files
```

### 2. 智能分析

根据获取到的 JSON 数据进行分析：

1.  **确定 Type (类型)**：
    - 阅读 `body` 中的 "变更类型" 或描述。
    - 检查 `labels` (如 `type: bug` -> `fix`, `type: feature` -> `feat`)。
    - 如果是 PR，分析 `files` (仅文档变动 -> `docs`，仅测试变动 -> `test`)。

2.  **确定 Scope (范围)**：
    - 阅读 `body` 提及的模块。
    - 检查 `labels` (如 `in: fit` -> `fit`)。
    - 如果是 PR，分析 `files` 路径 (如 `framework/fit/java/...` -> `fit`)。

3.  **生成 Subject (摘要)**：
    - **忽略原标题**（避免受干扰），直接从 `body` 中提炼核心意图。
    - 确保简练（20字以内）、中文描述、无句号。

### 3. 生成建议与交互

输出分析结果供用户确认：

```text
🔍 分析对象: Issue #<id> / PR #<id>

当前标题: [原标题]
--------------------------------------------------
🧠 分析依据:
- 原始意图: (从 Body 提取的一句话摘要)
- 推断类型: Fix (依据: 标签 type:bug, Body 关键词 "修复")
- 推断范围: fit (依据: 涉及文件路径 framework/fit/...)
--------------------------------------------------
✨ 建议标题: fix(fit): 修复并发场景下的空指针异常
```

询问用户：*"是否确认修改？(y/n)"*

### 4. 执行修改

用户确认（y）后，根据对象类型执行命令：

```bash
# 如果是 Issue
gh issue edit <id> --title "<new-title>"

# 如果是 PR
gh pr edit <id> --title "<new-title>"
```

## 参数说明

- `<id>`: Issue 或 PR 的编号（必需）。

## 使用示例

```bash
# 智能重命名 Issue #1024
/refine-title 1024
```

## 优势

相比于批量修改 (`/normalize-titles`)，本命令：
1. **修正错误**：如果原标题是 "Help me"，此命令能读懂内容并改为 "fix(core): 修复启动报错"。
2. **更精准的 Scope**：通过分析 PR 的文件变动，能自动判断是 `fit` 还是 `waterflow`，无需人工指定。
