# Codex 项目级 Prompts

本目录提供仓库内的 Codex prompts（用于生成/管理 slash 命令的文档版本）。
由于 Codex CLI 的自定义 prompts 只会从用户目录读取（默认 `~/.codex/prompts/`），
这里的内容需要手动安装到本地目录后才会生效。

## 安装到本地

运行安装脚本，将本目录的 prompts 复制到 `~/.codex/prompts/`：

```bash
bash .codex/scripts/install-prompts.sh
```

安装完成后，使用 `/prompts:<name>` 调用（例如：`/prompts:analyze-issue`）。

## 使用提示

- 这些 prompts 以"仓库根目录"为默认上下文。
- 作为全局 prompts 使用时，请先切换到目标仓库或显式指定路径（详见各命令文件中的"使用前：选择目标仓库"）。

## 与 Claude Code 的区别

本目录中的命令配置是为 **Codex CLI** 设计的，与 Claude Code 存在以下重要区别：

### 1. 插件系统

**Claude Code**：
- 支持官方插件系统（在 `.claude/settings.json` 中配置）
- 可以使用 `/plugin-name:command-name` 格式调用插件
- 提供自动化的代码审查、提交管理等功能
- 示例：`/commit-commands:commit`, `/code-review:code-review`

**Codex CLI**：
- **没有官方插件系统**，仅支持 prompts
- 使用 `/prompts:command-name` 格式调用
- 命令以文档化的最佳实践指南为主
- 需要手动执行 Git 命令和代码审查

### 2. 命令调用格式

| 系统 | 调用格式 | 示例 |
|------|---------|------|
| Claude Code | `/command` 或 `/plugin:command` | `/commit`, `/code-review:code-review` |
| Codex CLI | `/prompts:command` | `/prompts:commit`, `/prompts:review-task` |

### 3. 自动化程度

**Claude Code**：
- `/commit` 可以自动分析变更、生成提交消息、执行提交
- `/code-review` 自动运行多个并行代理进行深度审查
- 提供一键式工作流（如 `/commit-commands:commit-push-pr`）

**Codex CLI**：
- 提供详细的操作步骤和检查清单
- 需要手动执行 Git 命令（参考命令文档中的示例）
- 代码审查依赖手动检查清单

### 4. 文档中的标记

在本目录的命令文件中，你会看到以下标记：

```markdown
⚠️ **注意**：以下工具仅在 Claude Code 环境中可用。
如果你在使用 Codex CLI，请跳过此步骤...

（Claude Code 插件，Codex CLI 不支持）
```

这些标记表明某些功能仅适用于 Claude Code，Codex CLI 用户应使用文档中提供的替代方法。

### 5. 推荐使用场景

**使用 Claude Code（`.claude/` 配置）当**：
- 需要快速自动化的代码审查和提交流程
- 在团队中使用 Claude Code 作为标准工具
- 希望利用官方插件的高级功能

**使用 Codex CLI（`.codex/` 配置）当**：
- 使用 OpenAI 的 Codex CLI 工具
- 需要详细的操作指南和检查清单
- 希望更好地理解每个步骤的具体操作

## 可用命令列表

所有命令都支持 Codex CLI（通过 `/prompts:command-name` 调用）：

**任务管理**：
- `analyze-issue` - 分析 GitHub Issue 并创建需求分析文档
- `plan-task` - 设计技术方案并输出实施计划
- `implement-task` - 根据技术方案实施任务
- `review-task` - 审查任务实现并输出代码审查报告
- `refinement-task` - 处理代码审查反馈并修复问题
- `complete-task` - 标记任务完成并归档到 completed 目录
- `task-status` - 查看任务的当前状态和进度
- `block-task` - 标记任务阻塞并记录阻塞原因

**Git 操作**：
- `commit` - 提交当前变更到 Git（提供最佳实践指南）
- `create-pr` - 创建 Pull Request
- `sync-pr` - 将任务处理进度同步到 PR 评论
- `sync-issue` - 将任务处理进度同步到 Issue 评论
- `refine-title` - 重构 Issue/PR 标题为 Conventional Commits 格式

**依赖和安全**：
- `upgrade-dependency` - 升级项目依赖
- `analyze-security` - 分析 Dependabot 安全告警并创建修复任务
- `close-security` - 关闭 Dependabot 安全告警（需提供合理理由）

**其他**：
- `fix-permissions` - 检查并修复项目文件权限
- `test` - 执行完整的测试流程

## 常见问题

### Q: 为什么我无法使用 `/commit-commands:commit`？

A: 这是 Claude Code 的官方插件命令，在 Codex CLI 中不可用。请使用 `/prompts:commit` 查看提交指南，然后手动执行 Git 命令。

### Q: 如何进行代码审查？

A: 使用 `/prompts:review-task <task-id>` 查看详细的审查清单，然后按照清单手动审查代码。Claude Code 用户可以使用 `/code-review:code-review` 进行自动审查。

### Q: 命令文件中提到的插件功能我能用吗？

A: 如果命令文件中标记为"Claude Code 插件"或"Codex CLI 不支持"，则这些功能仅适用于 Claude Code。请使用文档中提供的替代方法（通常是手动操作步骤）。
