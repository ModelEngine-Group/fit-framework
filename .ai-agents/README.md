# FIT Framework - AI 智能体协作配置

本目录包含 FIT Framework 项目的 AI 智能体协作配置，支持三个主流 AI 工具（ClaudeCode、Codex、GeminiCli）在同一项目中高效协作。

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
├── codex/                  # Codex (OpenAI/ChatGPT) 专用配置
│   ├── preferences.yaml
│   └── README.md
└── gemini/                 # Gemini 专用配置
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

1. **需求分析**（推荐：ClaudeCode）
   - 理解需求，分析现有代码
   - 输出：`.ai-workspace/active/{task-id}/analysis.md`

2. **方案设计**（推荐：ClaudeCode）
   - 设计技术方案，制定计划
   - 输出：`.ai-workspace/active/{task-id}/plan.md`

3. **代码实现**（推荐：Codex / GeminiCli）
   - 根据方案编写代码和测试
   - 输出：`.ai-workspace/active/{task-id}/implementation.md`

4. **代码审查**（推荐：ClaudeCode）
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
| **代码实现** | ⭐⭐⭐⭐ (4.0)  | ⭐⭐⭐⭐⭐ (5.0) | ⭐⭐⭐⭐⭐ (4.7) | **Codex > GeminiCli** > ClaudeCode |
| **代码审查** | ⭐⭐⭐⭐⭐ (5.0) |  ⭐⭐⭐ (3.0)  | ⭐⭐⭐⭐ (3.7)  | **ClaudeCode** > GeminiCli > Codex |
| **问题修复** | ⭐⭐⭐⭐ (4.0)  | ⭐⭐⭐⭐⭐ (5.0) | ⭐⭐⭐⭐⭐ (4.7) | **Codex > GeminiCli** > ClaudeCode |
| **最终提交** | ⭐⭐⭐⭐⭐ (4.5) |  ⭐⭐⭐ (3.0)  |  ⭐⭐⭐ (2.5)  | **ClaudeCode** > Codex > GeminiCli |

#### 核心优势总结

**ClaudeCode**
- 🧠 **最擅长**: 需求分析、方案设计、代码审查、最终提交
- 💡 **核心优势**: 深度推理、系统性分析、架构设计、安全审查 (OWASP Top 10)
- 🎯 **定位**: 架构师 + 审查员 + 质量把关者
- 📝 **特色**: 内置 `/commit` 命令，深度理解项目规范
- 🔧 **配置**: `.claude/` 目录（项目根目录）

**Codex** (OpenAI/ChatGPT)
- 🧠 **最擅长**: 代码实现、问题修复
- 💡 **核心优势**: 代码生成速度快、补全准确、快速迭代
- 🎯 **定位**: 超级程序员
- 📝 **特色**: 代码生成效率最高，实现阶段首选
- 🔧 **配置**: `.ai-agents/codex/` 目录

**GeminiCli** (Google Gemini)
- 🧠 **最擅长**: 需求分析、代码实现、问题修复
- 💡 **核心优势**: 超大上下文 (2M tokens)、全局分析、快速编码
- 🎯 **定位**: 全能助手 + 分析师
- 📝 **特色**: 可一次性加载海量代码进行全局分析
- 🔧 **配置**: `.ai-agents/gemini/` 目录

#### 实战选择建议

```
思考型任务 (分析/设计/审查) → 优先选择 ClaudeCode
执行型任务 (编码/修复/测试) → 优先选择 Codex 或 GeminiCli
大型代码库分析           → 优先选择 GeminiCli (超大上下文优势)
安全审查和架构评估       → 必须使用 ClaudeCode
快速迭代和原型开发       → 优先选择 Codex
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

### 2. 使用 ClaudeCode 分析

在 Claude Code 中：
```
请分析并设计 TASK-{task-id} 的实现方案
```

ClaudeCode 会：
- 读取任务文件
- 分析现有代码
- 制定技术方案
- 输出到 context 目录

### 3. 切换到 Codex/GeminiCli 实现

**使用 Codex (OpenAI/ChatGPT)**：
```
根据 .ai-workspace/active/TASK-{task-id}/task.md 实现代码
参考方案：.ai-workspace/active/TASK-{task-id}/plan.md
```

**或使用 GeminiCli**：
```
根据 .ai-workspace/active/TASK-{task-id}/task.md 实现代码
参考方案：.ai-workspace/active/TASK-{task-id}/plan.md
```

AI 会：
- 读取任务和方案
- 编写代码实现
- 编写单元测试
- 更新任务状态

### 4. 切换回 ClaudeCode 审查

在 Claude Code 中：
```
审查 TASK-{task-id} 的实现
```

### 5. 人工确认后提交

根据使用的 AI 工具提交代码（Claude Code 的 `/commit` 命令）

## 📐 工作流定义

工作流文件位于 `workflows/` 目录，使用 YAML 格式定义。

每个步骤包含：
- **required_capabilities**: 能力要求
- **recommended_agents**: 推荐的 AI（claude/codex/gemini）
- **tasks**: 任务清单
- **inputs/outputs**: 输入输出文件

详见各工作流文件。

## 🔧 配置说明

### 配置文件注入机制

不同 AI 工具会自动读取不同的项目指令文件：

| AI 工具       | 注入的文件               | 配置说明                                    |
|-------------|---------------------|-----------------------------------------|
| Claude Code | `.claude/CLAUDE.md` | 项目根目录                                   |
| Gemini CLI  | `AGENTS.md`         | 通过 `.gemini/settings.json` 配置           |
| OpenCode    | `AGENTS.md`         | 项目根目录（或 `~/.config/opencode/AGENTS.md`） |
| Codex CLI   | `AGENTS.md`         | 项目根目录（或 `~/.codex/AGENTS.md`）           |

#### Claude Code 加载机制（实验验证）

**测试方法**：在不同位置创建带唯一标识符的 CLAUDE.md 文件（.claude/rules/、子目录、~/.claude/），观察 Claude 接收到的系统上下文。

**核心结论**：

1. **启动时加载（永久）**
   - 加载位置：`.claude/CLAUDE.md` + `.claude/rules/*.md` + `~/.claude/CLAUDE.md`
   - 注入方式：拼接到系统提示词（持续生效）
   - 合并策略：简单拼接，无智能合并

2. **按需加载（临时）**
   - 加载位置：子目录 `CLAUDE.md`（如 `test-subdir/CLAUDE.md`）
   - 触发时机：首次 Read 该目录下文件时
   - 注入方式：通过 `<system-reminder>` 注入到函数结果（临时生效）
   - 限制：Write 工具不触发加载

3. **文件组织建议**
   ```
   .claude/
   ├── CLAUDE.md            # 核心配置（100-200 行）
   └── rules/               # 模块化规则（每个 50-100 行）
       ├── 01-xxx.md        # 用数字前缀控制顺序
       └── 02-yyy.md
   ```

**注意事项**：
- 避免重复定义规则（拼接时不去重）
- 控制总长度（建议 `.claude/` 总计 ≤500 行）
- 子目录规则按需加载（节省 token）

#### Gemini CLI 加载机制（实测验证）

**测试方法**：
1. 创建多级目录 `.gemini-test/level1/level2/`
2. 在各级目录放置包含特定指令（Tag）的 `AGENTS.md`
3. 运行时 `cd` 进入深层目录，观察回复是否包含所有 Tag
4. **结论**：无需重启，目录切换即时生效，所有层级的 `AGENTS.md` 内容被拼接注入

**核心结论**：

1. **配置驱动加载**
   - **默认行为**：Gemini CLI 默认查找 **`GEMINI.md`**。
   - **项目配置**：本项目通过 `.gemini/settings.json` 的 `context.fileName` 字段将其重写为 `["AGENTS.md"]`，以遵循 Linux Foundation AAIF 标准。
   - **加载源**：因此，实际加载的是 `AGENTS.md`。

2. **级联加载（Cascading Context）**
   - **触发机制**：**依赖 CLI 启动时的目录**。
     - Gemini CLI 的 Shell 环境通常在每轮对话后重置，因此 Agent 无法通过 `cd` 命令持久切换目录。
     - **如果需要加载子模块规则**（如 `framework/fit/AGENTS.md`），用户必须**在该目录下启动 Gemini CLI**，或者在该目录下运行 IDE。
   - **级联顺序**：
     Global (`~/.gemini/`) + Project Root + ... + Parent Dir + Current Dir
   - **合并策略**：**简单拼接（Concatenation）**。

   - **💡 使用技巧：如何临时激活子目录规则？**
     虽然 Agent 无法持久保持目录状态，但您可以在 Prompt 中明确指示上下文。
     - **方法 1（推荐）**：“请进入 `framework/fit` 目录，检查 Main.java 的代码规范”。
       - 结果：Agent 执行 `cd`，CLI 立即加载 `framework/fit/AGENTS.md`，规则在**当前回复**中生效。
     - **方法 2（手动引用）**：“请参考 `framework/fit/AGENTS.md` 的规则进行开发”。
       - 结果：Agent 读取文件内容作为普通上下文使用。

3. **文件组织建议**
   - **根目录 `AGENTS.md`**：存放**绝大多数**通用规则。这是最安全、最可靠的地方。
   - **子目录 `AGENTS.md`**：仅用于那些用户通常会**在该目录下启动终端**进行开发的独立子项目。对于普通的模块化项目，建议尽量将规则合并到根目录，避免因目录层级问题导致规则未加载。

### ClaudeCode 配置

ClaudeCode 的配置保持在 `.claude/` 目录（项目根目录），包括：
- Slash Commands
- Project Rules
- Permissions

参考：`.claude/README.md`

### Codex 配置

Codex (OpenAI/ChatGPT) 的配置在 `.ai-agents/codex/` 目录。

### GeminiCli 配置

GeminiCli 的配置在 `.ai-agents/gemini/` 目录。

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
- **ClaudeCode 配置**：`.claude/README.md`
- **Codex 配置**：`.ai-agents/codex/README.md`
- **GeminiCli 配置**：`.ai-agents/gemini/README.md`
- **贡献指南**：`CONTRIBUTING.md`
- **PR 模板**：`.github/PULL_REQUEST_TEMPLATE.md`

## 🔄 迁移说明

如果你之前使用单一 AI 工具，现在想启用多 AI 协作：

1. ✅ 保持现有配置（`.claude/`、`.ai-agents/codex/`、`.ai-agents/gemini/`）
2. ✅ 创建 `.ai-workspace/` 目录（已被 git ignore）
3. ✅ 开始使用任务模板进行协作
4. ✅ 不需要修改现有工作流程

## 🆘 常见问题

### Q: 必须按照工作流执行吗？
A: 不是。工作流是推荐的最佳实践，你可以灵活调整。

### Q: 可以只用一个 AI 吗？
A: 可以。这个配置也支持单 AI 工作，只是提供了多 AI 协作的能力。

### Q: 如何知道任务当前在哪一步？
A: 查看任务文件的 `current_step` 字段和 `context/` 目录下的文件。ClaudeCode 用户可使用 `/task-status` 命令。

### Q: .ai-workspace 需要提交到 git 吗？
A: 不需要，这是临时工作目录，已在 `.gitignore` 中忽略。

### Q: 如何清理已完成的任务？
A: 定期删除 `tasks/completed/` 和 `context/` 下的旧任务文件。

### Q: 三个 AI 都需要安装吗？
A: 不是必需的。你可以根据需要选择安装其中一个或多个。配置支持灵活组合。

**配置版本**: 2.0.0
**支持 AI**: ClaudeCode, Codex, GeminiCli
**基于标准**: [AGENTS.md](https://www.infoq.com/news/2025/08/agents-md/) (Linux Foundation AAIF)
