---
name: "analyze-issue"
description: "分析 GitHub Issue 并创建需求分析文档"
usage: "/analyze-issue <issue-number>"
---

# Analyze Issue Command

## 功能说明

分析指定的 GitHub Issue，创建任务并输出需求分析文档。

## 执行流程

### 1. 获取 Issue 信息

```bash
gh issue view <issue-number> --json number,title,body,labels
```

### 2. 创建任务目录和文件

检查是否已存在该 Issue 的任务：
- 在 `.ai-workspace/active/` 中搜索相关任务
- 如果找到，询问是否重新分析
- 如果没有，创建任务目录：`.ai-workspace/active/TASK-{yyyyMMdd-HHmmss}/`
- 使用 `.ai-agents/templates/task.md` 模板创建任务文件：`task.md`

### 3. 执行需求分析

按照 `.ai-agents/workflows/feature-development.yaml` 中的 `requirement-analysis` 步骤：

**必须完成的任务**：
- [ ] 阅读并理解 Issue 描述
- [ ] 搜索相关代码文件（使用 Glob/Grep 工具）
- [ ] 分析代码结构和影响范围
- [ ] 识别潜在的技术风险和依赖
- [ ] 评估工作量和复杂度

### 4. 输出分析文档

创建 `.ai-workspace/active/{task-id}/analysis.md`，必须包含以下章节：

```markdown
# 需求分析报告

## 需求理解
{用自己的话重新描述需求，确保理解正确}

## 相关文件列表
- `{file-path}:{line-number}` - {说明}

## 影响范围评估
**直接影响**：
- {影响的模块和文件}

**间接影响**：
- {可能影响的其他部分}

## 技术风险
- {风险描述和应对思路}

## 依赖关系
- {需要的依赖和其他模块的配合}

## 工作量和复杂度评估
- 复杂度：{高/中/低}
- 工作量：{预估时间}
- 风险等级：{高/中/低}
```

### 5. 更新任务状态

更新 `.ai-workspace/active/{task-id}/task.md`：
- `current_step`: requirement-analysis
- `assigned_to`: claude
- `updated_at`: {当前时间}
- 标记 analysis.md 为已完成

### 6. 告知用户

输出格式：
```
✅ Issue #{number} 分析完成

**任务信息**：
- 任务ID: {task-id}
- 任务标题: {title}
- 工作流: feature-development

**输出文件**：
- 任务文件: .ai-workspace/active/{task-id}/task.md
- 分析文档: .ai-workspace/active/{task-id}/analysis.md

**下一步**：
审查需求分析后，使用以下命令设计技术方案：
/plan-task {task-id}
```

## 参数说明

- `<issue-number>`: GitHub Issue 编号（必需）

## 使用示例

```bash
# 分析 Issue #207
/analyze-issue 207
```

## 注意事项

1. **Issue 验证**：
   - 执行前检查 Issue 是否存在
   - 如果 Issue 不存在，提示用户

2. **任务冲突**：
   - 如果已存在相关任务，询问用户：
     - 重新分析（覆盖现有 analysis.md）
     - 继续使用现有分析

3. **工作流遵循**：
   - 严格遵循 `.ai-agents/workflows/feature-development.yaml` 定义
   - 输出文件必须符合工作流要求

4. **人工检查点**：
   - 分析完成后建议人工审查
   - 确认需求理解正确后再进入下一步

## 相关命令

- `/plan-task <task-id>` - 设计技术方案
- `/task-status <task-id>` - 查看任务状态

## 错误处理

- Issue 不存在：提示 "Issue #{number} 不存在，请检查 Issue 编号"
- 网络错误：提示 "无法连接到 GitHub，请检查网络连接"
- 权限错误：提示 "没有访问该仓库的权限"
