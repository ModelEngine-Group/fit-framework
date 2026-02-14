---
description: 根据自然语言描述创建任务并生成需求分析文档
usage: /create-task <description>
argument-hint: <任务描述>
---

# Create Task Command

## 功能说明

根据用户的自然语言描述，创建任务并执行需求分析，输出分析文档。适用于没有 GitHub Issue 的场景，用户可以直接用自然语言描述想做的事情。

## 🔴 CRITICAL: 行为边界

**本命令的唯一产出是 `task.md` 和 `analysis.md` 两个文件。**

- ❌ **禁止**编写、修改、创建任何业务代码或配置文件
- ❌ **禁止**直接实现用户描述的功能
- ❌ **禁止**跳过工作流直接进入 plan/implement 阶段
- ✅ 只做：解析描述 → 创建任务文件 → 需求分析 → 输出分析文档 → 告知用户下一步

用户描述的内容是**待办事项**，不是**立即执行的指令**。

## ⚠️ CRITICAL: 状态更新要求

执行此命令后，你**必须**立即更新任务状态。参见规则 7。

## 执行流程

### 1. 解析用户描述

从用户的自然语言描述中提取以下信息：
- **任务标题**：精简的中文标题（20 字以内）
- **任务类型**：`feature`|`bugfix`|`refactor`|`docs`|`chore`（根据描述推断）
- **工作流**：`feature-development`|`bug-fix`|`refactoring`（根据类型推断）
- **详细描述**：用户原始描述的整理版本

任务类型推断规则：
- 包含"添加"、"新增"、"支持" → `feature`
- 包含"修复"、"解决"、"Bug" → `bugfix`
- 包含"重构"、"优化"、"改进" → `refactor`
- 包含"文档"、"Javadoc"、"注释" → `docs`
- 其他 → `chore`

工作流映射：
- `feature` / `docs` / `chore` → `feature-development`
- `bugfix` → `bug-fix`
- `refactor` → `refactoring`

如果描述不够清晰，**先向用户确认**关键信息后再继续。

### 2. 创建任务目录和文件

- 创建任务目录：`.ai-workspace/active/TASK-{yyyyMMdd-HHmmss}/`
- 使用 `.ai-agents/templates/task.md` 模板创建任务文件：`task.md`

⚠️ **重要**：
- 任务目录命名：`TASK-{yyyyMMdd-HHmmss}`（**必须**包含 `TASK-` 前缀）
- 示例：`TASK-20260213-143022`
- 任务ID（`{task-id}`）即为目录名

任务元数据（在 task.md 的 YAML front matter 中）：
```yaml
id: TASK-{yyyyMMdd-HHmmss}
type: feature|bugfix|refactor|docs|chore
workflow: feature-development|bug-fix|refactoring
status: active
created_at: {yyyy-MM-dd HH:mm:ss}
updated_at: {yyyy-MM-dd HH:mm:ss}
created_by: human
current_step: requirement-analysis
assigned_to: codex
```

注意：`created_by` 设为 `human`，因为任务来源于用户的自然语言描述。

### 3. 执行需求分析

按照 `.ai-agents/workflows/feature-development.yaml` 中的 `requirement-analysis` 步骤：

**必须完成的任务**（仅分析，不编写任何业务代码）：
- [ ] 理解用户描述的需求
- [ ] 搜索相关代码文件（使用 Glob/Grep 工具，**只读不改**）
- [ ] 分析代码结构和影响范围
- [ ] 识别潜在的技术风险和依赖
- [ ] 评估工作量和复杂度

### 4. 输出分析文档

创建 `.ai-workspace/active/{task-id}/analysis.md`，必须包含以下章节：

```markdown
# 需求分析报告

## 需求来源

**来源类型**: 用户自然语言描述
**原始描述**:
> {用户的原始描述}

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
- 风险等级：{高/中/低}
```

### 5. 更新任务状态

更新 `.ai-workspace/active/{task-id}/task.md`：
- `current_step`: requirement-analysis
- `assigned_to`: codex
- `updated_at`: {当前时间}
- 标记 analysis.md 为已完成
- 在"工作流进度"中标记 requirement-analysis 为完成 ✅

### 6. 告知用户

输出格式：
```
✅ 任务创建并分析完成

**任务信息**：
- 任务ID: {task-id}
- 任务标题: {title}
- 任务类型: {type}
- 工作流: {workflow}

**输出文件**：
- 任务文件: .ai-workspace/active/{task-id}/task.md
- 分析文档: .ai-workspace/active/{task-id}/analysis.md

**下一步**：
审查需求分析后，使用以下命令设计技术方案：
/plan-task {task-id}
```

## ✅ 完成检查清单

执行此命令后，确认：

- [ ] 已创建任务文件 `.ai-workspace/active/{task-id}/task.md`
- [ ] 已创建分析文档 `.ai-workspace/active/{task-id}/analysis.md`
- [ ] 已更新 task.md 中的 `current_step` 为 requirement-analysis
- [ ] 已更新 task.md 中的 `updated_at` 为当前时间
- [ ] 已更新 task.md 中的 `assigned_to` 为你的名字
- [ ] 已在"工作流进度"中标记 requirement-analysis 为完成 ✅
- [ ] 已告知用户下一步操作（/plan-task）
- [ ] **未修改任何业务代码或配置文件**（除 task.md 和 analysis.md 外）

## 🛑 STOP: 命令到此结束

完成上述检查清单后，**立即停止**。不要继续执行 plan、implement 或任何后续步骤。
等待用户审查分析文档后，由用户主动调用 `/plan-task` 推进工作流。

## 参数说明

- `<description>`: 自然语言的任务描述（必需）

## 使用示例

```bash
# 添加新功能
/create-task 给 fit-runtime 添加优雅停机功能，在收到 SIGTERM 信号时等待当前请求处理完成后再关闭

# 修复 Bug
/create-task fit-broker 在高并发场景下偶尔出现连接池耗尽的问题，需要排查并修复

# 重构代码
/create-task 将 fit-conf 模块的配置加载逻辑从同步改为异步，提升启动性能

# 改进文档
/create-task 补充 fit-aop 模块的 Javadoc，特别是公共 API 的参数和返回值说明
```

## 注意事项

1. **描述清晰度**：
   - 如果用户描述模糊或缺少关键信息，先向用户确认
   - 例如：缺少具体模块名、预期行为不明确等

2. **与 analyze-issue 的区别**：
   - `analyze-issue`：从 GitHub Issue 获取信息，关联 Issue 编号
   - `create-task`：从用户自然语言描述创建，**相关Issue** 标记为"无"

3. **人工检查点**：
   - 分析完成后建议人工审查
   - 确认需求理解正确后再进入下一步

## 相关命令

- `/analyze-issue <number>` - 从 GitHub Issue 创建任务
- `/plan-task <task-id>` - 设计技术方案
- `/task-status <task-id>` - 查看任务状态

## 错误处理

- 描述为空：提示 "请提供任务描述，例如：/create-task 给 fit-runtime 添加优雅停机功能"
- 描述过于模糊：向用户提问确认关键信息后再创建任务
