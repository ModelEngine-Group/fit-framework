# 仓库指南

## 项目结构与模块划分

本仓库包含 FIT 框架及相关引擎。
- `framework/` 为核心实现目录（`framework/fit/java`、`framework/fit/python`、`framework/waterflow`、`framework/fel`）。
- `docs/` 存放用户指南、快速入门与白皮书。
- `examples/` 包含 FIT/FEL 可运行示例。
- `docker/` 提供容器构建资源。
- `build/` 为 Maven 构建产物输出目录（自动生成）。

## 构建、测试与开发命令

- `mvn clean install`（仓库根目录）：构建全部模块并运行测试，产物输出到 `build/`。
- `cd framework/fit/java && mvn clean install`：仅构建 Java FIT 框架。
- `./build/bin/fit start`：启动 Java 运行时（依赖 Node.js）；默认端口 `8080`。
- `./.ai-agents/scripts/run-test.sh`：完整验证流程（构建、启动、健康检查）。

## 编码风格与命名规范

- Java 格式化使用 IntelliJ 配置 `CodeFormatterFromIdea.xml`。
- 公共/受保护的 Java API 需要 Javadoc，并包含 `@param`/`@return`。
- 类头需包含 `@author` 与 `@since yyyy-MM-dd`。
- 修改任意带版权头的文件时，需同步更新版权年份到当前年份。**必须先通过系统命令 `date +%Y` 获取当前年份**，然后根据版权头格式更新（例如若当前年份为 2026：`2024-2025` → `2024-2026`，`2024` → `2024-2026`）。
- 分支命名使用模块前缀与意图，例如 `fit-feature-xxx`、`waterflow-bugfix-yyy`。

## 测试规范

- Java 测试通过 Maven（Surefire）执行，`mvn clean install` 为基线命令。
- 迭代时可在模块目录运行定向测试。
- 测试与源码同模块放置，命名优先使用 `*Test`。

## 提交与 PR 规范

- 提交信息遵循 Conventional Commits：`<type>(<scope>): <subject>`，subject 使用中文且约 20 字以内。
- scope 为模块名（如 `fit`、`waterflow`、`fel`），可省略。
- PR 基于正确的模块/版本分支，通常仅包含一次提交。

## 安全与配置提示

- 安全问题请勿在公共 Issue 报告，按 `SECURITY.md` 指引私下提交。
- 请启用 git hooks 进行编码检查（`git-hooks/check-utf8-encoding.sh`）。

## 多 AI 智能体协作

本项目支持多个 AI 工具（Claude、GPT、Cursor 等）协同工作。

### 协作配置目录

- **`.ai-agents/`** - AI 智能体配置（版本控制）
  - `workflows/` - 工作流定义（推荐流程）
  - `templates/` - 任务和文档模板
  - `claude/` - Claude 专用配置（指向 `.claude/`）
  - `chatgpt/` - ChatGPT 专用配置
  - `gemini/` - Gemini 专用配置
  - `cursor/` - Cursor 专用配置

- **`.ai-workspace/`** - 协作工作区（临时文件，已 ignore）
  - `active/` - 进行中的任务（包含任务文件和上下文）
  - `blocked/` - 被阻塞的任务
  - `completed/` - 已完成的任务
  - `logs/` - 协作日志和记录

### 标准协作流程

推荐（但非强制）的工作流程：

1. **需求分析**（推荐：Claude）
   - 使用 `/analyze-issue <issue-number>` 命令
   - 理解需求，分析代码，评估影响
   - 输出：`active/{task-id}/analysis.md`

2. **方案设计**（推荐：Claude）
   - 使用 `/plan-task <task-id>` 命令
   - 设计技术方案，制定实施计划
   - 输出：`active/{task-id}/plan.md`
   - ⚠️ **人工检查点**：审查方案

3. **代码实现**（推荐：ChatGPT/Gemini/Cursor）
   - 使用 `/implement-task <task-id>` 命令
   - 编写代码和单元测试
   - 输出：`active/{task-id}/implementation.md`

4. **代码审查**（推荐：Claude）
   - 使用 `/review-task <task-id>` 命令
   - 审查质量、安全、性能
   - 输出：`active/{task-id}/review.md`

5. **问题修复**（任意 AI）
   - 使用 `/refinement-task <task-id>` 命令
   - 根据审查意见改进代码
   - 输出：`active/{task-id}/refinement-report.md`

6. **任务归档**（推荐：Claude）
   - 使用 `/complete-task <task-id>` 命令
   - ⚠️ **人工检查点**：确认后归档
   - 任务移动到 `completed/` 目录

7. **阻塞处理**（特殊情况）
   - 使用 `/block-task <task-id> --reason <原因>` 命令
   - 任务移动到 `blocked/` 目录
   - 记录阻塞原因和需要的帮助

### 任务跟踪

**任务跟踪**：
- 任务存储在 `.ai-workspace/` 目录（active/、blocked/、completed/）
- 每个任务包含：task.md（元数据）、analysis.md、plan.md、implementation.md、review.md
- AI 接手：读取 `active/{task-id}/task.md` 和上下文文件
- 任务切换：任何 AI 都可以通过读取任务目录接手任务

### 交流语言规范

**所有 AI 智能体必须遵循以下语言规范**：

- **回复语言与问题保持一致**：AI 应根据用户输入的语言自动调整回复语言（自适应策略）
  - 用户使用中文提问 → AI 使用中文回复
  - 用户使用英文提问 → AI 使用英文回复
- **项目文档默认语言**：中文
  - 代码注释：中文
  - 文档生成：中文
  - 提交信息：中文（遵循 Conventional Commits 格式）
- **配置说明**：各 AI 的 `preferences.yaml` 中通过 `communication_language: "Adaptive (Match user's language)"` 字段定义

### 详细文档

- 协作总指南：`.ai-agents/README.md`（包含工作流定义、快速开始指南）
- 工具特定配置：`.ai-agents/{claude,codex,gemini,cursor}/`

### 基于标准

本协作方案基于 [AGENTS.md 标准](https://www.infoq.com/news/2025/08/agents-md/)（Linux Foundation AAIF）。
