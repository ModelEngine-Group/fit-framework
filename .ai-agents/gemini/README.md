# Gemini 配置说明

本目录包含 Google Gemini 在 FIT Framework 项目中的配置。

## 配置文件

- `preferences.yaml` - Gemini 的偏好配置

## 使用 Gemini 参与协作

### 1. 读取项目配置

在开始工作前，Gemini 应该：

1. 读取 `AGENTS.md`（项目根目录）了解项目基本信息
2. 读取 `.ai-agents/README.md` 了解协作流程
3. 读取 `.ai-agents/workflows/` 了解工作流定义

### 2. 接手任务

当人类切换到 Gemini 时：

```
请根据 .ai-workspace/tasks/active/TASK-{id}.md 继续工作
```

Gemini 应该：
1. 读取任务文件
2. 读取相关的上下文文件（context/{task-id}/）
3. 理解当前进度和下一步要做什么
4. 开始执行任务

### 3. Gemini 的优势

在 FIT Framework 项目中，Gemini 擅长：

1. **多模态理解**
   - 同时处理代码、图表、文档
   - 理解复杂的上下文关系

2. **快速代码实现**
   - 高效的代码生成
   - 遵循现有代码风格

3. **代码分析**
   - 理解大型代码库
   - 识别代码模式和问题

4. **测试生成**
   - 生成全面的测试用例
   - 覆盖边界情况

### 4. 与其他 AI 协作

**与 Claude 配合**：
1. Claude 分析需求 → 设计方案
2. Gemini 实现代码和测试
3. Claude 审查代码
4. Gemini 修复问题

**与 ChatGPT 配合**：
- 可以互为替代
- 根据任务特点选择更合适的 AI

### 5. 完成任务后

完成任务后，Gemini 应该：
1. 创建输出文件到 `.ai-workspace/context/{task-id}/`
2. 更新任务状态
3. 说明完成情况

