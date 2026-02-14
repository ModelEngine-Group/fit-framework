# OpenCode 配置说明

本目录包含 FIT Framework 项目的 OpenCode 配置和自定义命令。

## 📁 目录结构

```
.opencode/
├── README.md              # 本文件
└── commands/              # 自定义 Slash Commands
    ├── analyze-issue.md
    ├── analyze-security.md
    ├── block.md
    ├── close-security.md
    ├── commit.md
    ├── complete.md
    ├── create-pr.md
    ├── create-task.md
    ├── implement.md
    ├── plan.md
    ├── refine-title.md
    ├── refine.md
    ├── review.md
    ├── sync-issue.md
    ├── sync-pr.md
    ├── task-status.md
    ├── test.md
    └── upgrade-dependency.md
```

## 📋 可用命令

### 任务管理工作流

完整的任务管理工作流命令：

1. **`/create-task <description>`** - 根据自然语言描述创建任务并生成需求分析文档
2. **`/analyze-issue <issue-number>`** - 分析 GitHub Issue 并创建需求分析文档
3. **`/analyze-security <alert-number>`** - 分析 Dependabot 安全告警并创建安全分析文档
4. **`/plan-task <task-id>`** - 为任务设计技术方案并输出实施计划
5. **`/implement-task <task-id>`** - 根据技术方案实施任务并输出实现报告
6. **`/review-task <task-id>`** - 审查任务实现并输出代码审查报告
7. **`/refinement-task <task-id>`** - 处理代码审查反馈并修复问题
8. **`/task-status <task-id>`** - 查看任务的当前状态和进度
9. **`/complete-task <task-id>`** - 标记任务完成并归档到 completed 目录
10. **`/block-task <task-id>`** - 标记任务阻塞并记录阻塞原因

### Git 和 PR 管理

代码提交和 PR 相关命令：

- **`/commit`** - 提交当前变更到 Git（自动检查版权头年份）
- **`/create-pr [branch-name]`** - 创建 Pull Request（默认到 3.6.x 分支）
- **`/sync-issue <task-id>`** - 将任务进度同步到 GitHub Issue 评论
- **`/sync-pr <task-id>`** - 将任务进度同步到 Pull Request 评论
- **`/refine-title <id>`** - 深度分析 Issue/PR 内容并重构标题为 Conventional Commits 格式

### 依赖和安全管理

依赖升级和安全告警处理命令：

- **`/upgrade-dependency <package> <from> <to>`** - 升级项目依赖
- **`/close-security <alert-number>`** - 关闭 Dependabot 安全告警（需提供合理理由）

### 测试

- **`/test`** - 执行完整的测试流程

## 🚀 使用示例

### 完整的功能开发流程

```bash
# 0. 从自然语言描述创建任务（无 Issue 时使用）
/create-task 给 fit-runtime 添加优雅停机功能

# 1. 或者从 Issue 创建任务
/analyze-issue 207

# 2. 设计技术方案
/plan-task TASK-20260120-104654

# 3. 实施功能
/implement-task TASK-20260120-104654

# 4. 代码审查
/review-task TASK-20260120-104654

# 5. 如果有问题需要修复
/refinement-task TASK-20260120-104654

# 6. 提交代码
/commit

# 7. 创建 PR
/create-pr

# 8. 标记任务完成
/complete-task TASK-20260120-104654
```

### 依赖升级流程

```bash
# 1. 升级依赖
/upgrade-dependency swagger-ui 5.30.0 5.30.2

# 2. 提交变更
/commit
```

### 安全告警处理流程

```bash
# 1. 分析安全告警
/analyze-security 23

# 2. 设计修复方案
/plan-task TASK-20260120-110000

# 3. 实施修复
/implement-task TASK-20260120-110000

# 4. 代码审查
/review-task TASK-20260120-110000

# 5. 提交修复
/commit

# 6. 创建 PR
/create-pr

# 7. 标记任务完成
/complete-task TASK-20260120-110000

# 或者如果是误报
/close-security 23
```

## 🔧 命令语法说明

### Shell 输出注入

OpenCode 支持使用 `!` 符号注入 bash 命令输出：

```markdown
!`date +%Y`  # 注入当前年份
!`git status`  # 注入 git 状态
!`gh issue view $1 --json title`  # 注入 Issue 信息
```

### 文件引用

使用 `@` 符号引用文件内容：

```markdown
@src/components/Button.tsx  # 引用文件内容
```

### 参数占位符

- `$ARGUMENTS` - 所有参数
- `$1` - 第一个参数
- `$2` - 第二个参数
- `$3` - 第三个参数
- ...

## 📝 自定义命令格式

所有命令文件使用 Markdown 格式，带有 YAML frontmatter：

```markdown
---
description: 命令描述
agent: general  # 使用的 agent (general/explore/build)
subtask: false  # 是否作为子任务运行
model: anthropic/claude-3-5-sonnet-20241022  # 可选：指定模型
---

命令的 prompt template 内容...

可以使用:
- !`shell command` 注入命令输出
- @file-path 引用文件
- $1, $2 访问参数
```

## 🎯 最佳实践

1. **使用完整工作流**：按照 analyze → plan → implement → review → commit 的顺序执行
2. **人工检查点**：在 plan 和 review 步骤后进行人工审查
3. **任务状态管理**：使用 `/task-status` 随时查看任务进度
4. **及时同步**：使用 `/sync-issue` 和 `/sync-pr` 保持沟通
5. **阻塞处理**：遇到无法解决的问题及时使用 `/block` 标记

## 📚 相关文档

- [OpenCode 官方文档](https://opencode.ai/docs/commands)
- [FIT Framework AI 协作指南](../.ai-agents/README.md)
- [AI 协作快速开始](../.ai-agents/QUICKSTART.md)

## 🤝 贡献

如需添加新命令或修改现有命令：

1. 在 `commands/` 目录创建 `.md` 文件
2. 按照上述格式编写命令
3. 测试命令是否正常工作
4. 更新本 README 文档
