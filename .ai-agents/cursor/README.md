# Cursor 配置说明

本目录包含 Cursor 编辑器在 FIT Framework 项目中的配置。

## 关于 Cursor

Cursor 是一个 AI 驱动的代码编辑器，基于 VSCode，集成了多种 AI 模型（包括 ChatGPT、Claude 等）。

## 使用 Cursor 参与协作

### 1. 读取项目配置

在开始工作前，确保 Cursor 中的 AI 助手：

1. 读取 `AGENTS.md`（项目根目录）了解项目基本信息
2. 读取 `.ai-agents/README.md` 了解协作流程
3. 读取 `.ai-workspace/tasks/active/` 中的任务

### 2. 在 Cursor 中工作

Cursor 特别适合：

1. **交互式代码编辑**
   - 在编辑器中直接修改代码
   - 实时看到 AI 建议

2. **快速原型开发**
   - 快速实现功能
   - 即时验证结果

3. **代码重构**
   - 安全的重构操作
   - 自动更新引用

### 3. 协作模式

**典型工作流**：
1. Claude (CLI) 分析需求 → 设计方案
2. Cursor (编辑器) 实现代码
3. Claude (CLI) 审查代码

**优势**：
- Cursor 提供可视化编辑体验
- Claude CLI 提供系统性分析
- 两者互补，效率更高

### 4. 任务跟踪

在 Cursor 中工作时：

```
# 查看当前任务
cat .ai-workspace/tasks/active/TASK-{id}.md

# 查看技术方案
cat .ai-workspace/context/{task-id}/plan.md

# 完成后创建实现报告
# 编辑 .ai-workspace/context/{task-id}/implementation.md
```

### 5. 配置建议

推荐的 Cursor 设置：
- 启用 AI 代码补全
- 配置项目代码风格
- 集成项目测试框架

## 项目编码规范

### FIT Framework 编码规范

参考 `AGENTS.md` 中的规范：

- Java 格式化：使用 `CodeFormatterFromIdea.xml`
- 注释要求：公共API需要Javadoc
- 类头信息：@author 和 @since
- 分支命名：模块前缀 + 意图
- **版权年份更新**：修改任意带版权头的文件时，**必须先通过系统命令 `date +%Y` 获取当前年份**，然后更新版权头（例如 `2024-2025` → `2024-<当前年份>`）。**绝对不要硬编码年份**

### 测试要求

- 单元测试使用 JUnit
- 测试类命名：`*Test.java`
- 每个公共方法都应有测试
- 测试要覆盖正常和异常情况

### 提交规范

- 提交信息：`[模块] 简述`
- 例如：`[fit] 修复某问题`
- 不要自动提交，等待人工确认

