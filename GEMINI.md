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
- `./.claude/run-test.sh`：完整验证流程（构建、启动、健康检查）。

## 编码风格与命名规范

- Java 格式化使用 IntelliJ 配置 `CodeFormatterFromIdea.xml`。
- 公共/受保护的 Java API 需要 Javadoc，并包含 `@param`/`@return`。
- 类头需包含 `@author` 与 `@since yyyy-MM-dd`。
- 分支命名使用模块前缀与意图，例如 `fit-feature-xxx`、`waterflow-bugfix-yyy`。

## 测试规范

- Java 测试通过 Maven（Surefire）执行，`mvn clean install` 为基线命令。
- 迭代时可在模块目录运行定向测试。
- 测试与源码同模块放置，命名优先使用 `*Test`。

## 提交与 PR 规范

- 提交信息以模块标签开头，中文简述，例如 `[fit] 修复某问题`。
- 标题建议 20 字左右，必要时在正文补充细节。
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
  - `tasks/active/` - 进行中的任务
  - `tasks/blocked/` - 被阻塞的任务
  - `tasks/completed/` - 已完成的任务
  - `context/{task-id}/` - 任务上下文文件

### 标准协作流程

推荐（但非强制）的工作流程：

1. **需求分析**（推荐：Claude）
   - 理解需求，分析代码，评估影响
   - 输出：`context/{task-id}/analysis.md`

2. **方案设计**（推荐：Claude）
   - 设计技术方案，制定实施计划
   - 输出：`context/{task-id}/plan.md`
   - ⚠️ **人工检查点**：审查方案

3. **代码实现**（推荐：ChatGPT/Gemini/Cursor）
   - 编写代码和单元测试
   - 输出：`context/{task-id}/implementation.md`

4. **代码审查**（推荐：Claude）
   - 审查质量、安全、性能
   - 输出：`context/{task-id}/review.md`

5. **问题修复**（任意 AI）
   - 根据审查意见改进

6. **最终提交**（人工确认）
   - ⚠️ **人工检查点**：确认后提交

### 任务跟踪

创建任务：
```bash
cp .ai-agents/templates/task.md .ai-workspace/tasks/active/TASK-$(date +%Y%m%d-%H%M%S).md
```

AI 接手任务：
- 读取任务文件：`.ai-workspace/tasks/active/TASK-*.md`
- 读取上下文：`.ai-workspace/context/{task-id}/`
- 完成任务清单
- 创建输出文件

AI 切换：任何 AI 都可以通过读取 context 目录接手任务。

### AI 能力参考

- **Claude**：擅长架构设计、代码审查、安全分析、复杂推理
- **ChatGPT**：擅长代码实现、测试编写、Bug 修复、文档生成
- **Gemini**：擅长代码实现、多模态理解、快速响应
- **Cursor**：擅长代码编辑、快速实现（基于多种模型）

**重要**：这只是推荐，人类决定使用哪个 AI 执行哪个步骤。

### 详细文档

- 协作总指南：`.ai-agents/README.md`
- 工作流定义：`.ai-agents/workflows/`
- Claude 配置：`.claude/README.md`
- ChatGPT Config: `.ai-agents/chatgpt/README.md`
- Gemini Config: `.ai-agents/gemini/README.md`

### 基于标准

本协作方案基于 [AGENTS.md 标准](https://www.infoq.com/news/2025/08/agents-md/)（Linux Foundation AAIF）。