# FIT Framework - AI 智能体协作配置

本目录包含 FIT Framework 项目的 AI 智能体协作配置，支持多个 AI 工具（Claude、GPT、Cursor 等）在同一项目中高效协作。

## 📋 目录说明

```
.ai-agents/                  # AI配置目录（版本控制）
├── README.md               # 本文件
├── workflows/              # 工作流定义（推荐流程）
│   ├── feature-development.yaml
│   ├── bug-fix.yaml
│   ├── code-review.yaml
│   └── refactoring.yaml
├── templates/              # 任务模板
│   ├── task.md            # 任务跟踪模板
│   ├── handoff.md         # 交接文档模板
│   └── review-report.md   # 审查报告模板
├── claude/                 # Claude 专用配置
│   └── → 指向 .claude/ 目录
└── gpt/                    # ChatGPT/Gemini/Cursor 专用配置
    ├── preferences.yaml
    └── README.md

.ai-workspace/              # 工作目录（临时文件，被 git ignore）
├── active/                 # 进行中的任务
│   └── {task-id}/
│       ├── task.md        # 任务主文件
│       ├── analysis.md    # 需求分析
│       ├── plan.md        # 技术方案
│       ├── implementation.md  # 实现报告
│       └── review.md      # 审查报告
├── completed/             # 已完成的任务
│   └── {task-id}/
│       └── task.md + context files
├── blocked/               # 被阻塞的任务
│   └── {task-id}/
│       └── task.md + context files
└── logs/                  # 执行日志
```

## 🤝 协作模式

### 标准协作流程

1. **需求分析**（推荐：Claude）
   - 理解需求，分析现有代码
   - 输出：`.ai-workspace/active/{task-id}/analysis.md`

2. **方案设计**（推荐：Claude）
   - 设计技术方案，制定计划
   - 输出：`.ai-workspace/active/{task-id}/plan.md`

3. **代码实现**（推荐：ChatGPT/Gemini/Cursor）
   - 根据方案编写代码和测试
   - 输出：`.ai-workspace/active/{task-id}/implementation.md`

4. **代码审查**（推荐：Claude）
   - 审查代码质量、安全性、性能
   - 输出：`.ai-workspace/active/{task-id}/review.md`

5. **问题修复**（任意 AI）
   - 根据审查意见修复问题
   - 循环到代码审查

6. **最终提交**（人工确认）
   - 人工审核后执行提交

**重要**：以上流程是**推荐**而非**强制**，人类决定使用哪个 AI 执行哪个步骤。

### AI 工具能力评分对比

基于三个独立评估的综合结果（满分 5 分），帮助你选择最合适的 AI 工具：

| 阶段       | ClaudeCode  |    Codex    |  GeminiCli  | 🏆 推荐顺序                            |
|:---------|:-----------:|:-----------:|:-----------:|:-----------------------------------|
| **需求分析** | ⭐⭐⭐⭐⭐ (5.0) |  ⭐⭐⭐ (3.0)  | ⭐⭐⭐⭐ (4.3)  | **ClaudeCode** > GeminiCli > Codex |
| **方案设计** | ⭐⭐⭐⭐⭐ (5.0) |  ⭐⭐⭐ (3.0)  | ⭐⭐⭐⭐ (3.7)  | **ClaudeCode** > GeminiCli > Codex |
| **代码实现** | ⭐⭐⭐⭐ (4.0)  | ⭐⭐⭐⭐⭐ (5.0) | ⭐⭐⭐⭐⭐ (4.7) | **Codex = GeminiCli** > ClaudeCode |
| **代码审查** | ⭐⭐⭐⭐⭐ (5.0) |  ⭐⭐⭐ (3.0)  | ⭐⭐⭐⭐ (3.7)  | **ClaudeCode** > GeminiCli > Codex |
| **问题修复** | ⭐⭐⭐⭐ (4.0)  | ⭐⭐⭐⭐⭐ (5.0) | ⭐⭐⭐⭐⭐ (4.7) | **Codex = GeminiCli** > ClaudeCode |
| **最终提交** | ⭐⭐⭐⭐⭐ (4.5) |  ⭐⭐⭐ (3.0)  |  ⭐⭐⭐ (2.5)  | **ClaudeCode** > Codex > GeminiCli |

#### 核心优势总结

**ClaudeCode**
- 🧠 **最擅长**: 需求分析、方案设计、代码审查、最终提交
- 💡 **核心优势**: 深度推理、系统性分析、架构设计、安全审查 (OWASP Top 10)
- 🎯 **定位**: 架构师 + 审查员 + 质量把关者
- 📝 **特色**: 内置 `/commit` 命令，深度理解项目规范

**Codex**
- 🧠 **最擅长**: 代码实现、问题修复
- 💡 **核心优势**: 代码生成速度快、补全准确、快速迭代
- 🎯 **定位**: 超级程序员
- 📝 **特色**: 代码生成效率最高，实现阶段首选

**GeminiCli**
- 🧠 **最擅长**: 需求分析、代码实现、问题修复
- 💡 **核心优势**: 超大上下文 (2M tokens)、全局分析、快速编码
- 🎯 **定位**: 全能助手 + 分析师
- 📝 **特色**: 可一次性加载海量代码进行全局分析

#### 实战选择建议

```
思考型任务 (分析/设计/审查) → 优先选择 ClaudeCode
执行型任务 (编码/修复/测试) → 优先选择 Codex 或 GeminiCli
大型代码库分析           → 优先选择 GeminiCli (超大上下文优势)
安全审查和架构评估       → 必须使用 ClaudeCode
```

### 灵活切换 AI

任何 AI 都可以通过读取以下内容接手任务：
- 任务文件：`.ai-workspace/active/{task-id}/task.md`
- 上下文：`.ai-workspace/active/{task-id}/`

## 🚀 快速开始

### 1. 创建新任务

```bash
# 复制任务模板
cp .ai-agents/templates/task.md .ai-workspace/active/TASK-{task-id}/task.md

# 编辑任务描述
vim .ai-workspace/active/TASK-{task-id}/task.md
```

### 2. 使用 AI 分析

**使用 Claude Code**：
```
请分析并设计 TASK-{task-id} 的实现方案
```

或使用其他 AI 工具，参考各工具的使用指南。

Claude 会：
- 读取任务文件
- 分析现有代码
- 制定技术方案
- 输出到 context 目录

### 3. 切换到其他 AI 实现

在 Cursor/ChatGPT/Gemini 中：
```
根据 .ai-workspace/active/TASK-{task-id}/task.md 实现代码
```

AI 会：
- 读取计划文档
- 实现功能代码
- 编写单元测试
- 更新任务状态

### 4. 切换回 Claude 审查

在 Claude Code 中：
```
审查 TASK-{task-id} 的实现
```

### 5. 人工确认后提交

根据使用的 AI 工具提交代码（如 Claude Code 的 `/commit` 命令）

## 📐 工作流定义

工作流文件位于 `workflows/` 目录，使用 YAML 格式定义。

每个步骤包含：
- **required_capabilities**: 能力要求
- **recommended_agents**: 推荐的 AI（仅供参考）
- **tasks**: 任务清单
- **inputs/outputs**: 输入输出文件

详见各工作流文件。

## 🔧 配置说明

### Claude 配置

Claude Code 的配置保持在 `.claude/` 目录，包括：
- Slash Commands
- Project Rules
- Permissions

参考：`.claude/README.md`

### ChatGPT/Gemini/Cursor 配置

GPT 和 Cursor 的配置在 `.ai-agents/gpt/` 目录。

### 通用配置

所有 AI 都应该：
1. 读取 `AGENTS.md`（项目根目录）
2. 遵循 `.ai-agents/workflows/` 中的工作流
3. 使用 `.ai-workspace/` 进行任务跟踪和上下文共享

### 版权年份管理规范

**重要规则**：修改任意带版权头的文件时，**必须同步更新版权年份到当前年份**。

**执行步骤**：

1. **动态获取当前年份**（绝对不要硬编码年份）：
   ```bash
   # 使用系统命令获取当前年份
   CURRENT_YEAR=$(date +%Y)
   echo $CURRENT_YEAR
   ```

2. **检查并更新版权头**：
   - 检查文件是否包含版权声明（如 `Copyright (C) 2024-2025`）
   - 如果包含，将年份更新为当前年份（如 `2024-2025` → `2024-2026`）
   - 如果是单一年份（如 `2024`），更新为年份范围（如 `2024-2026`）

3. **常见格式示例**（假设当前年份为 2026）：
   - `Copyright (C) 2024-2025` → `Copyright (C) 2024-2026`
   - `Copyright (C) 2024` → `Copyright (C) 2024-2026`
   - `© 2024-2025` → `© 2024-2026`

**为什么重要**：
- 确保版权声明的准确性和法律有效性
- 遵循项目规范，保持一致性
- 避免年份过时导致的合规问题

## ⚠️ 注意事项

### 人工检查点

以下步骤需要人工确认：
1. **方案设计完成后**：审查技术方案是否合理
2. **代码审查完成后**：决定是否需要修改
3. **最终提交前**：确认所有变更无误

### 上下文管理

- `.ai-workspace/{status}/{task-id}/` 包含任务的完整上下文
- 切换 AI 时，新的 AI 会读取这些文件了解进度
- 保持文档完整和准确，便于协作

### 任务状态

- `tasks/active/`：正在进行的任务
- `tasks/blocked/`：遇到问题被阻塞的任务
- `tasks/completed/`：已完成的任务（可定期清理）

## 📚 相关文档

- **主配置文件**：`AGENTS.md`（中文）、`AGENTS.en.md`（英文）
- **Claude 配置**：`.claude/README.md`
- **贡献指南**：`CONTRIBUTING.md`
- **PR 模板**：`.github/PULL_REQUEST_TEMPLATE.md`

## 🔄 迁移说明

如果你之前使用单一 AI 工具，现在想启用多 AI 协作：

1. ✅ 保持现有配置（`.claude/`、`.cursor/` 等）
2. ✅ 创建 `.ai-workspace/` 目录（已被 git ignore）
3. ✅ 开始使用任务模板进行协作
4. ✅ 不需要修改现有工作流程

## 🆘 常见问题

### Q: 必须按照工作流执行吗？
A: 不是。工作流是推荐的最佳实践，你可以灵活调整。

### Q: 可以只用一个 AI 吗？
A: 可以。这个配置也支持单 AI 工作，只是提供了多 AI 协作的能力。

### Q: 如何知道任务当前在哪一步？
A: 查看任务文件的 `current_step` 字段和 `context/` 目录下的文件。Claude Code 用户可使用 `/task-status` 命令。

### Q: .ai-workspace 需要提交到 git 吗？
A: 不需要，这是临时工作目录，已在 `.gitignore` 中忽略。

### Q: 如何清理已完成的任务？
A: 定期删除 `tasks/completed/` 和 `context/` 下的旧任务文件。

**配置版本**: 1.0.0
**基于标准**: [AGENTS.md](https://www.infoq.com/news/2025/08/agents-md/) (Linux Foundation AAIF)
