---
id: TASK-{yyyyMMdd-HHmmss}
type: feature|bugfix|refactor|review|docs
workflow: feature-development|bug-fix|code-review|refactoring
status: active|blocked|completed
created_at: {yyyy-MM-dd HH:mm:ss}
updated_at: {yyyy-MM-dd HH:mm:ss}
created_by: human|claude|chatgpt|gemini
current_step: {step-id}
assigned_to: {当前处理的AI或空}
---

# {任务标题}

## 任务描述

{详细描述需求、Bug现象或重构目标}

**相关Issue**: #{issue-number} 或 无

## 当前状态

- **工作流**: {workflow}
- **当前步骤**: {current_step}
- **执行者**: {当前由哪个AI处理，留空表示等待认领}
- **状态**: 进行中 / 等待确认 / 已阻塞

## 上下文文件

请阅读以下文件了解任务完整上下文：

- [ ] `.ai-workspace/active/{task-id}/analysis.md` - 需求分析
- [ ] `.ai-workspace/active/{task-id}/plan.md` - 技术方案
- [ ] `.ai-workspace/active/{task-id}/implementation.md` - 实现报告
- [ ] `.ai-workspace/active/{task-id}/review.md` - 审查报告

## 相关资源

- **相关Issue**: #{issue-number}
- **相关PR**: #{pr-number}
- **相关分支**: `{branch-name}`
- **相关文件**:
  - `{file-path}:{line-number}` - {说明}
  - `{file-path}:{line-number}` - {说明}

## 工作流进度

根据 `.ai-agents/workflows/{workflow}.yaml`:

- [ ] {step-1} ({推荐AI}, {日期或待完成})
- [ ] {step-2} (待认领)
- [ ] {step-3}
- [ ] {step-4}

## 当前任务：{current_step}

### 需要做什么

{从workflow中复制该步骤的描述和任务清单}

### 能力要求

- {capability-1}
- {capability-2}

### 推荐AI

- **{AI-name}** (推荐): {原因}
- **{AI-name}**: {原因}

### 任务清单

- [ ] {task-1}
- [ ] {task-2}
- [ ] {task-3}

### 输入文件

- `.ai-workspace/active/{task-id}/{input-file}.md`

### 输出要求

完成后创建/更新：
- `.ai-workspace/active/{task-id}/{output-file}.md`

包含以下章节：
- {section-1}
- {section-2}

## 交接信息

### 来自: {上一个步骤/AI}

{上一步传递的上下文和说明}

### 输出给: {下一个步骤/AI}

{需要传递给下一步的信息}

## 注意事项

- [ ] 完成后将任务文件从 `tasks/active/` 移到对应目录
- [ ] 如果需要人工确认，在任务中标注
- [ ] 遇到问题移到 `tasks/blocked/` 并说明原因
- [ ] 更新 `updated_at` 和 `assigned_to` 字段

## 状态更新检查清单

⚠️ **CRITICAL**: 每次执行命令后，必须确认以下项目（参见规则 7）：

- [ ] `current_step` 已更新为当前步骤
- [ ] `updated_at` 已更新为当前时间（格式：`yyyy-MM-dd HH:mm:ss`）
- [ ] `assigned_to` 已更新为你的名字
- [ ] "工作流进度" 部分已标记当前步骤为完成 ✅
- [ ] 如果步骤完成，已进入下一步骤或等待人工审查
- [ ] 如果任务完成，已执行 `/complete-task` 归档到 `completed` 目录
- [ ] 如果任务阻塞，已执行 `/block-task` 并说明原因，移动到 `blocked` 目录

**违反此检查清单将导致任务状态追踪失败，这是不可接受的。**

## 附加信息

{其他需要说明的内容、特殊要求、参考资料等}

---

## 使用说明

### 如何认领任务

如果你是AI，准备处理这个任务：

1. 更新任务文件的 `assigned_to` 字段为你的名字
2. 更新 `updated_at` 为当前时间
3. 阅读所有上下文文件
4. 开始执行任务清单

### 如何完成任务

1. 完成所有任务清单项
2. 创建输出文件到指定位置
3. 更新任务的 `current_step` 到下一步
4. 更新 `updated_at`
5. 如果需要人工确认，在任务中注明

### 如何交接任务

1. 确保输出文件完整
2. 在"输出给"部分写清楚关键信息
3. 将 `assigned_to` 清空（或设置为推荐的下一个AI）
4. 等待人工确认或下一个AI认领

### 遇到问题怎么办

1. 将任务目录移到 `.ai-workspace/blocked/`
2. 在任务中添加"阻塞原因"章节
3. 说明遇到的问题和需要的帮助
4. 通知人类用户
