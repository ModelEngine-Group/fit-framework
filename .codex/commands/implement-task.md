---
description: 根据技术方案实施任务并输出实现报告
usage: /implement-task <task-id>
argument-hint: <task-id>
---

# Implement Task Command

## 使用前：自动识别仓库

命令会默认使用当前工作目录所在的 Git 仓库作为目标，无需传入仓库参数。若当前目录不在 Git 仓库内，请先 `cd` 到目标仓库根目录后再执行。

文中所有路径示例默认以仓库根目录为基准。

## 功能说明

根据技术方案实施任务，编写代码和测试，输出实现报告。

## ⚠️ CRITICAL: 状态更新要求

执行此命令后，你**必须**立即更新任务状态。参见规则 7。

## 执行流程

### 1. 验证前置条件

检查必需文件：
- `.ai-workspace/active/{task-id}/task.md` - 任务文件
- `{task_dir}/plan.md` - 技术方案

如果任一文件不存在，提示用户先完成前置步骤。

### 2. 读取技术方案

仔细阅读 `plan.md`，理解：
- 技术方案和实现策略
- 详细的实施步骤
- 需要创建/修改的文件清单
- 测试策略

### 3. 执行代码实现

按照 `.ai-agents/workflows/feature-development.yaml` 中的 `implementation` 步骤：

**必须完成的任务**：
- [ ] 按照方案实现功能代码
- [ ] 编写完整的单元测试
- [ ] 本地运行测试验证功能
- [ ] 更新相关文档和注释
- [ ] 遵循项目编码规范

**实施原则**：
1. **严格遵循方案**：不要偏离技术方案
2. **分步实施**：按照 plan.md 中的步骤顺序执行
3. **及时测试**：每完成一个步骤就运行测试
4. **保持简洁**：不要过度设计或添加额外功能

### 4. 运行测试验证

```bash
# 根据项目类型运行测试
mvn test -pl :{module-name}  # Maven 项目
npm test                      # Node.js 项目
pytest                        # Python 项目
```

确保所有测试通过。

### 5. 输出实现报告

创建 `{task_dir}/implementation.md`，必须包含以下章节：

```markdown
# 实现报告

## 已修改文件列表

### 新增文件
- `{file-path}` - {说明}

### 修改文件
- `{file-path}` - {修改内容摘要}

## 关键代码说明

### {模块/功能名称}
**文件**: `{file-path}:{line-number}`

**实现逻辑**：
{重要逻辑的解释}

**关键代码**：
```{language}
{关键代码片段}
```

## 测试结果

### 单元测试
- 测试文件: `{test-file-path}`
- 测试用例数: {数量}
- 通过率: {百分比}

**测试输出**：
```
{测试运行结果}
```

### 集成测试
{如果有}

## 与方案的差异

{如果实现与方案有差异，说明原因}

## 待审查事项

**需要 reviewer 特别关注的点**：
- {关注点1}
- {关注点2}

## 已知问题

{实现过程中发现的问题或待优化项}

## 下一步建议

{对代码审查的建议或后续优化方向}
```

### 6. 更新任务状态

更新 `.ai-workspace/active/{task-id}/task.md`：
- `current_step`: implementation
- `assigned_to`: {当前 AI}
- `updated_at`: {当前时间}
- 标记 implementation.md 为已完成
- 在工作流进度中标记代码实现为完成

### 7. 告知用户

输出格式：
```
✅ 任务 {task-id} 实现完成

**实现概要**：
- 修改文件: {数量} 个
- 新增文件: {数量} 个
- 测试通过: {数量}/{总数}

**输出文件**：
- 实现报告: {task_dir}/implementation.md

**下一步**：
使用以下命令进行代码审查：
/prompts:review-task {task-id}
```

## ✅ 完成检查清单

执行此命令后，确认：

- [ ] 已完成所有代码实施
- [ ] 已创建实现报告 `{task_dir}/implementation.md`
- [ ] 已更新 task.md 中的 `current_step` 为 implementation
- [ ] 已更新 task.md 中的 `updated_at` 为当前时间
- [ ] 已更新 task.md 中的 `assigned_to` 为你的名字
- [ ] 已在"工作流进度"中标记 technical-design 为完成 ✅
- [ ] 已在"工作流进度"中标记 implementation 为进行中
- [ ] 已在 task.md 中标记 implementation.md 为已完成
- [ ] 已告知用户下一步操作（/review-task）

## 参数说明

- `<task-id>`: 任务ID，格式为 TASK-{yyyyMMdd-HHmmss}（必需）

## 使用示例

```bash
# 实施任务
/implement-task TASK-20251227-104654
```

## 注意事项

1. **前置条件**：
   - 必须先完成技术方案设计（plan.md 存在）
   - 方案必须经过人工审查批准

2. **实施规范**：
   - 严格按照 plan.md 中的步骤执行
   - 不要添加计划外的功能
   - 遵循项目编码规范

3. **测试要求**：
   - 所有新增代码必须有单元测试
   - 测试覆盖率不低于原有水平
   - 所有测试必须通过

4. **代码质量**：
   - 遵循项目编码规范
   - 添加必要的注释
   - 保持代码简洁

5. **Git 操作**：
   - **不要**自动执行 git commit
   - 实现完成后等待代码审查
   - 审查通过后才能提交

## 相关命令

- `/prompts:plan-task <task-id>` - 设计技术方案（前置步骤）
- `/prompts:review-task <task-id>` - 代码审查（后续步骤）
- `/prompts:task-status <task-id>` - 查看任务状态

## 错误处理

- 任务不存在：提示 "任务 {task-id} 不存在"
- 缺少技术方案：提示 "技术方案不存在，请先执行 /plan-task"
- 测试失败：输出测试错误，询问是否继续
- 编译失败：输出编译错误，停止实施
