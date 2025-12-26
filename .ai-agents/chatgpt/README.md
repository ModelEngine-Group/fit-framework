# ChatGPT 配置说明

本目录包含 ChatGPT 在 FIT Framework 项目中的配置。

## 配置文件

- `preferences.yaml` - ChatGPT 的偏好配置

## 使用 ChatGPT 参与协作

### 1. 读取项目配置

在开始工作前，ChatGPT 应该：

1. 读取 `AGENTS.md`（项目根目录）了解项目基本信息
2. 读取 `.ai-agents/README.md` 了解协作流程
3. 读取 `.ai-agents/workflows/` 了解工作流定义

### 2. 接手任务

当人类切换到 ChatGPT 时：

```
请根据 .ai-workspace/tasks/active/TASK-{id}.md 继续工作
```

ChatGPT 应该：
1. 读取任务文件
2. 读取相关的上下文文件（context/{task-id}/）
3. 理解当前进度和下一步要做什么
4. 开始执行任务

### 3. 常见任务

#### 代码实现

ChatGPT 擅长的任务：
- 根据设计方案编写代码
- 编写单元测试
- 重构代码
- 修复Bug

**示例提示词**:
```
请根据 .ai-workspace/context/TASK-{id}/plan.md 中的方案实现代码。
需要包括：
1. 功能实现
2. 单元测试
3. 必要的注释
4. 更新相关文档
```

#### Bug 修复

**示例提示词**:
```
请分析并修复 TASK-{id} 中描述的Bug。
步骤：
1. 复现问题
2. 定位根因
3. 修复代码
4. 添加回归测试
```

### 4. 完成任务后

完成任务后，ChatGPT 应该：

1. 创建输出文件到 `.ai-workspace/context/{task-id}/`
2. 更新任务状态
3. 说明完成情况和需要注意的事项

**输出模板**:
```markdown
任务完成，已完成以下工作：

## 完成的文件
- src/main/java/... - 实现了...功能
- src/test/java/... - 添加了...测试

## 测试结果
- [x] 所有单元测试通过
- [x] 本地验证通过

## 需要注意
- ...

## 下一步建议
建议切换到 Claude 进行代码审查
```

## ChatGPT 的优势

在 FIT Framework 项目中，ChatGPT 擅长：

1. **快速代码实现**
   - 根据设计方案快速编写代码
   - 遵循现有代码风格

2. **单元测试编写**
   - 生成全面的测试用例
   - 覆盖边界情况

3. **代码重构**
   - 小范围的代码优化
   - 提取方法、简化逻辑

4. **Bug修复**
   - 快速定位和修复问题
   - 添加回归测试

5. **文档编写**
   - API 文档
   - 使用示例
   - README 更新

## 与其他 AI 协作

### 与 Claude 配合

典型流程：
1. **Claude** 分析需求，设计方案
2. **ChatGPT** 实现代码和测试
3. **Claude** 审查代码
4. **ChatGPT** 修复问题（如果有）
5. **Claude** 最终确认和提交

### 与 Gemini 配合

ChatGPT 和 Gemini 可以互为替代：
- 根据任务特点选择更合适的 AI
- 都擅长代码实现和测试编写

### 交接要点

从 Claude 接手时：
- 仔细阅读 plan.md 理解设计意图
- 严格按照方案实现，不要擅自改动设计
- 有疑问及时询问

交接给 Claude 时：
- 说明实现细节和关键决策
- 列出需要重点审查的地方
- 标注已知问题或待优化项

## 项目特定配置

### FIT Framework 编码规范

参考 `AGENTS.md` 中的规范：

- Java 格式化：使用 `CodeFormatterFromIdea.xml`
- 注释要求：公共API需要Javadoc
- 类头信息：@author 和 @since
- 分支命名：模块前缀 + 意图

### 测试要求

- 单元测试使用 JUnit
- 测试类命名：`*Test.java`
- 每个公共方法都应有测试
- 测试要覆盖正常和异常情况

### 提交规范

- 提交信息：`[模块] 简述`
- 例如：`[fit] 修复某问题`
- 不要自动提交，等待人工确认

## 常见问题

### Q: 如何知道现在该做什么？
A: 查看 `.ai-workspace/tasks/active/` 下的任务文件

### Q: 找不到上下文信息？
A: 检查 `.ai-workspace/context/{task-id}/` 目录

### Q: 任务文件中的 workflow 是什么？
A: 查看 `.ai-agents/workflows/{workflow}.yaml` 了解完整流程

### Q: 需要切换到其他 AI吗？
A: 根据任务类型和 workflow 中的推荐，但最终由人类决定

## 参考资料

- 主配置：`AGENTS.md`
- 协作指南：`.ai-agents/README.md`
- 工作流定义：`.ai-agents/workflows/`
- 任务模板：`.ai-agents/templates/`
