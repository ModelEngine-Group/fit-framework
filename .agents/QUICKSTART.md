# 多 AI 协作快速开始指南

本指南帮助你快速上手 FIT Framework 的多 AI 协作功能（支持 Claude Code、Codex、Gemini CLI、Cursor 等）。

> **📌 重要提示**：开始协作前，请先阅读根目录的 `AGENTS.md` 文件，了解项目的基本开发规范和命令。

## 🚀 5分钟快速开始

### 1. 创建你的第一个协作任务

```bash
# 复制任务模板
cp .agents/templates/task.md .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/task.md
```

编辑任务文件，填写：
- 任务标题
- 任务描述
- 选择工作流（feature-development/bug-fix/code-review/refactoring）

### 2. 使用 ClaudeCode 分析需求

在 Claude Code 中：
```
请分析 .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/task.md 中的任务
```

ClaudeCode 会：
1. 读取任务描述
2. 分析相关代码
3. 创建需求分析报告：`.ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/analysis.md`

### 3. ClaudeCode 设计技术方案

```
请为 TASK-{yyyyMMdd}-{task-id} 设计技术方案
```

ClaudeCode 会创建：
- `.ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/plan.md`

**人工检查点**：审查方案是否合理

### 4. 切换到 Codex/GeminiCli 实现代码

**使用 Codex (OpenAI/ChatGPT)**：
```
请根据 .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/task.md 实现代码。
参考方案：.ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/plan.md
```

**或使用 GeminiCli**：
```
请根据 .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/task.md 实现代码。
参考方案：.ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/plan.md
```

AI 会：
1. 读取任务和方案
2. 编写代码实现
3. 编写单元测试
4. 创建实现报告：`.ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/implementation.md`

### 5. 切换回 ClaudeCode 审查代码

在 Claude Code 中：
```
请审查 TASK-{yyyyMMdd}-{task-id} 的实现
```

ClaudeCode 会：
1. 读取实现报告
2. 审查代码变更
3. 创建审查报告：`.ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/review.md`

### 6. 根据审查结果决定

**如果审查通过**：
```
/commit
```

**如果需要修改**：
切换到 Codex 或 GeminiCli 修复问题，然后重新审查。

---

## 📋 常见场景

### 场景 1：修复 Bug

```bash
# 1. 创建 Bug 任务
cp .agents/templates/task.md .ai-workspace/active/BUG-{yyyyMMdd}-{task-id}/task.md
# 编辑：type: bugfix, workflow: bug-fix

# 2. ClaudeCode 分析 Bug
"请分析 BUG-{yyyyMMdd}-{task-id} 中的问题"

# 3. Codex/GeminiCli 修复 Bug
"请修复 BUG-{yyyyMMdd}-{task-id}"

# 4. ClaudeCode 审查修复
"请审查 BUG-{yyyyMMdd}-{task-id} 的修复"

# 5. 提交
/commit
```

### 场景 2：代码审查

```bash
# 1. 创建审查任务
cp .agents/templates/task.md .ai-workspace/active/REVIEW-PR-{task-id}/task.md
# 编辑：type: review, workflow: code-review

# 2. ClaudeCode 执行审查
/review {task-id}
# 或
"请审查 PR #{task-id}"

# 3. 根据审查报告采取行动
```

### 场景 3：代码重构

```bash
# 1. 创建重构任务
cp .agents/templates/task.md .ai-workspace/active/REFACTOR-{yyyyMMdd}-{task-id}/task.md
# 编辑：type: refactor, workflow: refactoring

# 2. ClaudeCode 分析和规划
"请分析 REFACTOR-{yyyyMMdd}-{task-id} 并制定重构计划"

# 3. 审查重构计划（重要！）

# 4. ClaudeCode 或 Codex/GeminiCli 执行重构
"请执行 REFACTOR-{yyyyMMdd}-{task-id} 的重构"

# 5. 审查 + 提交
```

---

## 🎯 最佳实践

### 1. 任务命名规范

```
TASK-{yyyyMMdd-序号}.md   # 一般任务
BUG-{yyyyMMdd-序号}.md    # Bug修复
FEAT-{yyyyMMdd-序号}.md   # 新功能
REFACTOR-{yyyyMMdd-序号}.md # 重构
REVIEW-PR-{pr-number}.md  # PR审查
```

### 2. 关键检查点

每个任务至少有2个人工检查点：
1. **方案设计后**：确保技术方案合理
2. **最终提交前**：确保所有变更正确

### 3. 上下文完整性

确保每个步骤都创建完整的输出文件：
- `analysis.md` - 需求分析
- `plan.md` - 技术方案
- `implementation.md` - 实现报告
- `review.md` - 审查报告

这样任何 AI 都能接手。

### 4. AI 切换时机

**切换到 ClaudeCode**：
- 需要系统性分析
- 代码审查
- 架构设计
- 安全审计

**切换到 Codex (OpenAI/ChatGPT)**：
- 快速实现功能
- 编写单元测试
- 修复简单问题
- 快速迭代开发

**切换到 GeminiCli (Google Gemini)**：
- 大规模代码分析
- 全局重构
- 复杂问题修复
- 需要超大上下文的任务

### 5. 灵活应对

工作流是推荐，不是强制：
- 简单任务可以跳过某些步骤
- 可以根据实际情况调整顺序
- 人类始终拥有最终决策权

---

## 🔍 故障排查

### 问题 1：AI 不知道任务在哪里

**解决方案**：明确指定任务文件
```
请处理 .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/task.md
```

### 问题 2：上下文丢失

**解决方案**：检查 context 目录
```bash
ls -la .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/
```

确保每个步骤都创建了输出文件。

### 问题 3：任务状态不清楚

**解决方案**：查看任务文件
```bash
cat .ai-workspace/active/TASK-{yyyyMMdd}-{task-id}/task.md
```

检查 `current_step` 和 `assigned_to` 字段。

### 问题 4：工作流程不清楚

**解决方案**：查看工作流定义
```bash
cat .agents/workflows/feature-development.yaml
```

---

## 📚 进一步学习

- **协作总指南**：`.agents/README.md`
- **工作流定义**：`.agents/workflows/`
- **任务模板**：`.agents/templates/`
- **ClaudeCode 配置**：`.claude/README.md`
- **Codex 配置**：`.agents/codex/README.md`
- **GeminiCli 配置**：`.agents/gemini/README.md`

---

## 💡 提示

1. **先小后大**：从简单任务开始熟悉流程
2. **保持沟通**：在任务文件和输出文件中写清楚
3. **人工确认**：关键步骤一定要人工审查
4. **定期清理**：完成的任务可以移到 completed 目录
5. **灵活调整**：根据实际情况调整工作流
6. **发挥优势**：根据各 AI 的优势选择合适的工具

---

## 🤖 三个 AI 的特点

**ClaudeCode** - 思考型专家
- 擅长：需求分析、方案设计、代码审查、安全审计
- 特点：深度推理、系统性思考、架构能力强
- 配置：`.claude/` 目录

**Codex** (OpenAI/ChatGPT) - 执行型专家
- 擅长：代码实现、快速迭代、单元测试
- 特点：代码生成速度快、补全准确
- 配置：`.agents/codex/` 目录

**GeminiCli** (Google Gemini) - 全能型专家
- 擅长：大规模代码分析、复杂问题修复
- 特点：超大上下文窗口（2M tokens）
- 配置：`.agents/gemini/` 目录

---

**祝协作愉快！** 🎉

有问题查看 `.agents/README.md` 或询问项目维护者。
