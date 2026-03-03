# Codex 项目级 Prompts（fit-framework）

本目录提供 fit-framework 仓库的 Codex prompts（用于生成/管理 slash 命令的文档版本）。
由于 Codex CLI 的自定义 prompts 只会从用户目录读取（默认 `~/.codex/prompts/`），
这里的内容需要手动安装到本地目录后才会生效。

## 命名空间

所有命令文件以 `fit-` 为前缀，避免与其他项目的同名命令冲突。
在 Codex CLI 中使用 `/prompts:fit-<name>` 调用（例如：`/prompts:fit-test`）。

> **为什么需要前缀？** Codex 的自定义 prompts 是全局的（`~/.codex/prompts/`），
> 当你同时使用多个项目（如 fit-framework 和 agora）时，
> 相同名称的命令会互相覆盖。添加项目前缀后，各项目的命令可以共存。

## 安装到本地

运行安装脚本，将本目录的 prompts 复制到 `~/.codex/prompts/`：

```bash
bash .codex/scripts/install-prompts.sh
```

安装完成后，使用 `/prompts:fit-<name>` 调用（例如：`/prompts:fit-analyze-issue`）。

## 使用提示

- 这些 prompts 以"仓库根目录"为默认上下文。
- 作为全局 prompts 使用时，请先切换到目标仓库或显式指定路径（详见各命令文件中的"使用前：选择目标仓库"）。

## Prompt 文件格式规范

本目录下的 prompts 采用混合格式，既符合 Codex 官方规范，又保留完整的使用示例：

### 混合格式（兼容官方标准）

```yaml
---
description: 命令的功能描述
usage: /prompts:fit-command-name <参数>
argument-hint: <参数>
---
```

### 字段说明

- **description** (必需): 描述 prompt 的功能，使用中文
- **usage** (推荐): 完整的使用示例，包含命令名和参数
  - 格式：`/prompts:fit-command-name <参数>`
  - 提供完整的调用示例供参考
- **argument-hint** (可选): 仅参数部分的描述（Codex 官方格式）
  - 使用 `<param>` 表示必需参数
  - 使用 `[param]` 表示可选参数
  - 使用 `[--flag=<value>]` 表示可选标志参数
  - 仅包含参数部分，不含命令名称

### 调用方式

- 在 Codex CLI 中使用 `/prompts:fit-<filename>` 调用（不含 .md 扩展名和 `fit-` 前缀中的项目名）
- 例如：`/prompts:fit-analyze-issue <issue-number>`

### 示例

**有参数的命令**：
```yaml
---
description: 分析 GitHub Issue 并创建需求分析文档
usage: /prompts:fit-analyze-issue <issue-number>
argument-hint: <issue-number>
---
```

**有多个参数的命令**：
```yaml
---
description: 升级项目依赖
usage: /prompts:fit-upgrade-dependency <package-name> <from-version> <to-version>
argument-hint: <package-name> <from-version> <to-version>
---
```

**无参数的命令**：
```yaml
---
description: 执行完整的测试流程
usage: /prompts:fit-test
---
```

### 格式说明

- ✅ 兼容 Codex 官方格式（使用 `argument-hint`）
- ✅ 保留完整使用示例（使用 `usage`）
- ✅ 文件名即为 prompt 名称，无需 `name` 字段
- ✅ 所有字段值不使用引号

## 可用命令列表

所有命令都支持 Codex CLI（通过 `/prompts:fit-<name>` 调用）：

**任务管理**：
- `fit-analyze-issue` - 分析 GitHub Issue 并创建需求分析文档
- `fit-plan-task` - 设计技术方案并输出实施计划
- `fit-implement-task` - 根据技术方案实施任务
- `fit-review-task` - 审查任务实现并输出代码审查报告
- `fit-refine-task` - 处理代码审查反馈并修复问题
- `fit-complete-task` - 标记任务完成并归档到 completed 目录
- `fit-check-task` - 查看任务的当前状态和进度
- `fit-block-task` - 标记任务阻塞并记录阻塞原因

**Git 操作**：
- `fit-commit` - 提交当前变更到 Git（提供最佳实践指南）
- `fit-create-pr` - 创建 Pull Request
- `fit-sync-pr` - 将任务处理进度同步到 PR 评论
- `fit-sync-issue` - 将任务处理进度同步到 Issue 评论
- `fit-refine-title` - 重构 Issue/PR 标题为 Conventional Commits 格式

**发布**：
- `fit-release` - 执行版本发布流程
- `fit-create-release-note` - 从 PR/commit 中自动生成 Release Notes

**依赖和安全**：
- `fit-upgrade-dependency` - 升级项目依赖
- `fit-analyze-security` - 分析 Dependabot 安全告警并创建修复任务
- `fit-close-security` - 关闭 Dependabot 安全告警（需提供合理理由）

**测试**：
- `fit-test` - 执行完整的测试流程
- `fit-test-integration` - 执行集成测试（需要先完成构建）
- `fit-create-task` - 根据自然语言描述创建任务

## 参数传递

本项目的 prompt 文件在正文中直接使用占位符引用用户传入的参数。Codex CLI 在展开 prompt 时，会将占位符替换为实际的参数值。

### 占位符类型

| 占位符                       | 含义             | 适用场景                     |
|---------------------------|----------------|--------------------------|
| `$1`, `$2`, `$3` ... `$9` | 位置参数（按空格分隔）    | 结构化参数，如 task-id、issue 编号 |
| `$ARGUMENTS`              | 所有参数拼接为一个完整字符串 | 自由文本输入，如任务描述             |

### `$1` vs `$ARGUMENTS` 的区别

当用户输入 `/prompts:fit-create-task 给 fit-runtime 添加优雅停机功能` 时：

| 占位符          | 展开结果                            |
|--------------|---------------------------------|
| `$1`         | `给`（仅第一个空格分隔的词）                 |
| `$2`         | `fit-runtime`（第二个词）             |
| `$ARGUMENTS` | `给 fit-runtime 添加优雅停机功能`（完整字符串） |

因此：
- **结构化参数**（task-id、issue 编号等）使用 `$1`、`$2`，每个参数含义明确
- **自由文本**（任务描述等）必须使用 `$ARGUMENTS`，否则只能拿到第一个词

### 可选参数处理

当用户未提供某个参数时，对应的占位符会被替换为**空字符串**（不会保留字面 `$1`）。
对于可选参数，应使用「声明 + 判空」的写法，确保语句通顺：

```markdown
1. 确定目标分支:
   用户指定的目标分支: $1
   如果上述值为空,自动推断目标分支:
   ```bash
   git branch --show-current
   git log --oneline --decorate --first-parent -20
   ```
   根据当前分支类型和 log 中的分支标记推断目标
```

### 使用示例

```bash
# 单参数命令 — 使用 $1
/prompts:fit-implement-task TASK-20260205-202013
/prompts:fit-analyze-issue 207

# 多参数命令 — 使用 $1 $2 $3
/prompts:fit-upgrade-dependency jackson 2.15.0 2.16.0

# 自由文本参数 — 使用 $ARGUMENTS
/prompts:fit-create-task 给 fit-runtime 添加优雅停机功能
```

### 编写新 prompt 的注意事项

- `argument-hint` 字段仅用于 UI 展示，不影响参数注入
- 在正文中需要使用参数的位置直接写 `$1`、`$ARGUMENTS` 等占位符
- 如果需要使用字面 `$` 符号，使用 `$$` 转义（避免被误识别为占位符）
- 正文中的 `$lowercase` 变量和 `$(...)` shell 语法不受影响（Codex 仅匹配 `$UPPERCASE` 格式）
- 新命令文件必须以 `fit-` 前缀命名

## 常见问题

### Q: 为什么命令都有 `fit-` 前缀？

A: Codex 的自定义 prompts 是全局的（`~/.codex/prompts/`），不同项目的同名命令会互相覆盖。
添加项目前缀后可以区分不同项目的命令，多个项目的命令可以同时共存。

### Q: 为什么我无法使用 `/commit-commands:commit`？

A: 这是 Claude Code 的官方插件命令，在 Codex CLI 中不可用。请使用 `/prompts:fit-commit` 查看提交指南，然后手动执行 Git 命令。

### Q: 如何进行代码审查？

A: 使用 `/prompts:fit-review-task <task-id>` 查看详细的审查清单，然后按照清单手动审查代码。Claude Code 用户可以使用 `/code-review:code-review` 进行自动审查。

### Q: 命令文件中提到的插件功能我能用吗？

A: 如果命令文件中标记为"Claude Code 插件"或"Codex CLI 不支持"，则这些功能仅适用于 Claude Code。请使用文档中提供的替代方法（通常是手动操作步骤）。
