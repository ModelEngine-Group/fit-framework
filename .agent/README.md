# FIT Framework - Agent 配置

本目录包含 AI Agent 的配置文件和工作流，旨在帮助 Agent 更高效地参与 FIT Framework 项目的开发。

## 📁 目录结构

```
.agent/
├── README.md              # 本文件
└── workflows/             # Agent 工作流 (对应 Slash Commands)
    ├── pr.md              # 创建 Pull Request
    ├── pr-update.md       # 更新 Pull Request 描述
    ├── commit.md          # 提交变更
    ├── upgrade-dep.md     # 升级依赖
    ├── review.md          # 审查 Pull Request
    └── fix-permissions.md # 修复文件权限
```

## 🤖 工作流 (Workflows)

当您要求 Agent 执行特定任务时，它会触发这些工作流。这些工作流确保了操作的一致性，并严格遵守项目规则。

### 创建 Pull Request (`pr.md`)
按照项目模板和规范创建一个新的 Pull Request。
- **触发方式**: "创建 PR", "提交这个变更"
- **执行动作**: 检查状态, 推送分支, 读取模板, 创建 PR。

### 更新 PR (`pr-update.md`)
更新现有 Pull Request 的描述信息。
- **触发方式**: "更新 PR 描述", "修复 PR 格式"
- **执行动作**: 读取模板, 更新 PR 内容。

### 提交变更 (`commit.md`)
将变更提交到本地仓库。
- **触发方式**: "提交这些变更"
- **执行动作**: 检查状态, 添加文件, 使用签名提交。

### 升级依赖 (`upgrade-dep.md`)
自动化依赖升级流程。
- **触发方式**: "升级 [包名] 到 [版本]"
- **执行动作**: 创建分支, 更新文件, 验证构建, 提交, 创建 PR。

### 审查 PR (`review.md`)
审查 Pull Request 的质量和合规性。
- **触发方式**: "审查 PR #123"
- **执行动作**: 分析 diff, 检查模板合规性, 生成报告。

### 修复权限 (`fix-permissions.md`)
修复文件所有权问题。
- **触发方式**: "修复权限", "修复文件所有者"
- **执行动作**: 识别 root 拥有的文件, 将所有者修改为与 README.md 一致。

## 📋 项目规则

Agent 会隐式遵守 `.claude/project-rules.md` 中定义的规则，特别是：
1. **文件权限**: 创建或修改文件后，必须修复权限。
2. **PR 模板**: 必须严格遵循 `.github/PULL_REQUEST_TEMPLATE.md`。
