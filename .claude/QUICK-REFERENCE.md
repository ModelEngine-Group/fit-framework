# AI 协作快速参考

本文档提供 FIT Framework AI 协作的快速参考，包括 Slash Commands 和自然语言使用示例。

## 📌 Slash Commands

### 核心命令

| 命令 | 用途 | 示例 |
|------|------|------|
| `/analyze-issue <number>` | 分析 GitHub Issue 并创建任务 | `/analyze-issue 207` |
| `/plan <task-id>` | 为任务设计技术方案 | `/plan TASK-20251227-104654` |
| `/implement <task-id>` | 实施任务（编写代码和测试） | `/implement TASK-20251227-104654` |
| `/check-task <task-id>` | 查看任务当前状态和进度 | `/check-task TASK-20251227-104654` |
| `/sync-issue <task-id>` | 同步任务进度到 GitHub Issue | `/sync-issue TASK-20251227-104654` |

### 已有命令

| 命令 | 用途 | 示例 |
|------|------|------|
| `/commit [message]` | 提交变更到 Git | `/commit` 或 `/commit "feat: xxx"` |
| `/pr [branch]` | 创建 Pull Request | `/pr` 或 `/pr main` |
| `/review <pr-number>` | 审查 Pull Request | `/review 369` |
| `/code-review:code-review` | 代码审查（使用插件） | `/code-review:code-review` |

### 工具命令

| 命令 | 用途 | 示例 |
|------|------|------|
| `/fix-permissions` | 检查和修复文件权限 | `/fix-permissions` |
| `/test` | 执行完整测试流程 | `/test` |
| `/upgrade-dep <package> <from> <to>` | 升级依赖 | `/upgrade-dep swagger-ui 5.30.0 5.30.2` |

## 💬 自然语言示例

### 需求分析

```
"分析 issue 207"
"帮我分析 fastjson 迁移需求"
"需求分析 #207"
```

**等效命令**：`/analyze-issue 207`

**AI 会做什么**：
1. 获取 Issue 信息
2. 创建任务文件
3. 执行需求分析
4. 输出 analysis.md
5. 更新任务状态

### 方案设计

```
"设计 TASK-20251227-104654 的技术方案"
"plan TASK-xxx"
"给我一个实施计划"
```

**等效命令**：`/plan TASK-20251227-104654`

**AI 会做什么**：
1. 读取需求分析
2. 设计技术方案
3. 输出 plan.md
4. 标记为等待人工审查

### 代码实施

```
"实施 TASK-20251227-104654"
"开始实现 fastjson 迁移"
"implement TASK-xxx"
```

**等效命令**：`/implement TASK-20251227-104654`

**AI 会做什么**：
1. 读取技术方案
2. 编写代码和测试
3. 运行测试验证
4. 输出 implementation.md

### 任务状态

```
"查看 TASK-20251227-104654 的状态"
"任务进度如何"
"TASK-xxx 当前在哪一步"
```

**等效命令**：`/check-task TASK-20251227-104654`

**AI 会输出**：
- 任务基本信息
- 当前步骤和进度
- 已完成的上下文文件
- 下一步建议

### 同步进度

```
"同步任务进度到 Issue"
"更新 Issue 进展"
"sync TASK-xxx to issue"
```

**等效命令**：`/sync-issue TASK-20251227-104654`

**AI 会做什么**：
1. 读取任务和上下文文件
2. 生成清晰的进度摘要
3. 发布到对应的 GitHub Issue
4. 更新任务同步时间

## 🔄 完整工作流

### 从 Issue 到提交的标准流程

```bash
# 1. 分析 Issue
/analyze-issue 207

# 2. 审查需求分析（人工）
# 查看 .ai-workspace/active/TASK-xxx/analysis.md

# 3. 设计技术方案
/plan TASK-xxx

# 4. 审查技术方案（人工）⚠️  必需
# 查看 .ai-workspace/active/TASK-xxx/plan.md

# 5. 同步进度到 Issue（可选，推荐在重要节点）
/sync-issue TASK-xxx

# 6. 实施任务
/implement TASK-xxx

# 7. 代码审查
/code-review:code-review

# 8. 同步最终进度（可选）
/sync-issue TASK-xxx

# 9. 提交代码（人工确认后）
/commit
```

### 使用自然语言的流程

```
1. "分析 issue 207"
   → AI 创建任务并完成需求分析

2. （人工审查 analysis.md）

3. "设计 TASK-xxx 的技术方案"
   → AI 设计方案并输出 plan.md

4. （人工审查 plan.md）⚠️  必需

5. "实施 TASK-xxx"
   → AI 编写代码和测试

6. "审查代码"
   → AI 执行代码审查

7. /commit
   → 提交变更
```

## 📋 任务 ID 格式

任务 ID 格式：`TASK-{yyyyMMdd-HHmmss}`

示例：
- `TASK-20251227-104654`
- `TASK-20251229-150530`

## 📂 文件路径约定

### 任务文件

- 活动任务：`.ai-workspace/active/TASK-xxx/task.md`
- 已完成：`.ai-workspace/completed/TASK-xxx.md`
- 已阻塞：`.ai-workspace/blocked/TASK-xxx.md`

### 上下文文件

- 需求分析：`.ai-workspace/active/TASK-xxx/analysis.md`
- 技术方案：`.ai-workspace/active/TASK-xxx/plan.md`
- 实现报告：`.ai-workspace/active/TASK-xxx/implementation.md`
- 审查报告：`.ai-workspace/active/TASK-xxx/review.md`

## ⚠️  人工检查点

以下步骤需要人工确认：

1. **需求分析后**：确认需求理解正确（建议）
2. **技术方案设计后**：审查方案是否合理（**必需**）
3. **代码审查后**：确认审查结果和修改建议（建议）
4. **最终提交前**：确认所有变更无误（**必需**）

## 🎯 最佳实践

### 使用 Slash Commands 的场景

- **精确任务**：知道具体要执行什么操作
- **标准流程**：按照工作流逐步执行
- **批量操作**：快速执行多个相似任务

### 使用自然语言的场景

- **探索性任务**：不确定从哪里开始
- **灵活表达**：用自己的话描述需求
- **上下文对话**：在对话中自然提及任务

### 提示

1. **优先使用 Slash Commands**：更精确、快速、标准化
2. **自然语言作为补充**：提供灵活性和对话式体验
3. **明确任务 ID**：避免歧义，确保操作正确的任务
4. **遵循工作流**：不要跳过人工检查点

## 🆘 常见问题

### Q: 如何知道任务当前在哪一步？

```bash
/check-task TASK-xxx
```

### Q: 忘记任务 ID 怎么办？

```bash
# 查看活动任务列表
ls .ai-workspace/active/

# 或通过 Issue 号查找
grep -r "#207" .ai-workspace/active/
```

### Q: 可以跳过某些步骤吗？

工作流是推荐流程，可以灵活调整，但不建议跳过人工检查点。

### Q: 自然语言识别不准确怎么办？

使用 Slash Commands 更精确。

### Q: 多个 AI 如何协作？

所有 AI 都可以读取 `.ai-workspace/` 下的任务和上下文文件，实现无缝切换。

### Q: 什么时候应该同步进度到 Issue？

建议在以下时机使用 `/sync-issue`：
- 完成重要阶段后（需求分析、技术方案、实现完成）
- 遇到阻塞问题需要讨论时
- 长时间任务需要定期更新进展时

**避免**：在每个小步骤都同步，会产生过多评论。

## 📚 更多信息

- **详细文档**：`.ai-agents/README.md`
- **快速开始**：`.ai-agents/QUICKSTART.md`
- **工作流定义**：`.ai-agents/workflows/`
- **项目规则**：`.claude/project-rules.md`

---

**提示**：收藏本文档，随时查阅！
