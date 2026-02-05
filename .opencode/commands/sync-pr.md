---
name: "sync-pr"
description: "将任务处理进度同步到 Pull Request 评论"
usage: "/sync-pr <task-id>"
---

# Sync PR Command

## 功能说明

将任务的处理进度摘要同步到对应的 Pull Request 评论区中，方便审查者了解开发进展和关键决策。

## 执行流程

### 1. 验证任务存在

检查任务文件是否存在：
- 查找 `.ai-workspace/active/{task-id}/task.md`
- 如果不存在，检查 `completed/` 和 `blocked/` 目录
- 如果都不存在，提示用户任务不存在

### 2. 读取任务信息

从任务文件中获取：
- PR 号码（`pr_number` 字段）
- 任务标题和描述
- 当前步骤（`current_step`）
- 任务状态（`status`）
- 创建和更新时间
- 关联的 Issue（`issue_number`，如果有）

### 3. 读取上下文文件

注意：`{task-id}` 格式为 `TASK-{yyyyMMdd-HHmmss}`，例如 `TASK-20260205-202013`

检查并读取以下文件（如果存在）：
- `.ai-workspace/{status}/{task-id}/analysis.md` - 需求分析
- `.ai-workspace/{status}/{task-id}/plan.md` - 技术方案
- `.ai-workspace/{status}/{task-id}/implementation.md` - 实现报告
- `.ai-workspace/{status}/{task-id}/review.md` - 审查报告

### 4. 生成进度摘要

根据当前状态生成清晰的进度摘要：

**基本格式**：
```markdown
## 🤖 开发进度更新

**任务ID**: {task-id}
**更新时间**: {当前时间}
**当前状态**: {状态描述}

### ✅ 已完成

- [x] 需求分析 - {完成时间}
  - {核心要点摘要 1-2 条}
- [x] 技术方案设计 - {完成时间}
  - {方案选择和关键决策 1-2 条}
- [x] 代码实现 - {完成时间}
  - 修改文件: {数量}
  - 新增测试: {数量}
- [ ] 代码审查（进行中）
- [ ] 最终合并

### 📋 当前进展

{当前步骤的详细说明}

### 🎯 下一步

{下一步计划}

### 📊 技术要点

{关键的技术决策和实现细节，方便审查者理解}

### 📂 相关文档

- 任务文件: `.ai-workspace/active/{task-id}/task.md`
- 需求分析: `.ai-workspace/active/{task-id}/analysis.md`
- 技术方案: `.ai-workspace/active/{task-id}/plan.md`
- 实现报告: `.ai-workspace/active/{task-id}/implementation.md`

---
*由 Claude Code 自动生成 - [任务管理系统](../.ai-agents/README.md)*
```

**摘要原则**：
- **面向审查者**：突出技术决策和实现要点
- **简洁清晰**：每个阶段只提取核心要点
- **逻辑连贯**：按开发流程展示进展
- **便于审查**：说明关键变更的原因和影响

### 5. 同步到 PR

使用 `gh` 命令将摘要发布到 PR 评论：

```bash
gh pr comment {pr-number} --body "$(cat <<'EOF'
{生成的进度摘要}
EOF
)"
```

### 6. 更新任务状态

在任务文件中记录同步时间：
- 添加或更新 `last_synced_to_pr_at` 字段
- 记录同步的 PR 评论链接（如果 gh 返回）

### 7. 告知用户

输出格式：
```
✅ 任务进度已同步到 PR #{pr-number}

**同步内容**：
- 已完成步骤: {数量}
- 当前状态: {状态}
- 下一步: {下一步说明}

**查看链接**：
https://github.com/{owner}/{repo}/pull/{pr-number}
```

## 参数说明

- `<task-id>`: 任务ID，格式为 TASK-{yyyyMMdd-HHmmss}（必需）

## 使用示例

```bash
# 同步任务进度到对应的 PR
/sync-pr TASK-20251227-104654
```

## 进度摘要示例

### 示例 1：代码实现完成，等待审查

```markdown
## 🤖 开发进度更新

**任务ID**: TASK-20251227-104654
**更新时间**: 2025-12-29 18:30:00
**当前状态**: 代码实现完成，请审查

### ✅ 已完成

- [x] 需求分析 - 2025-12-29 10:46:54
  - 评估了 fastjson 到 fastjson2 的迁移方案
  - 确定迁移范围：fit-value-fastjson 插件

- [x] 技术方案设计 - 2025-12-29 15:05:00
  - 选择升级到 fastjson2（API 兼容性好）
  - 预估工作量：0.5-1 天

- [x] 代码实现 - 2025-12-29 18:20:00
  - 修改文件: 3 个（pom.xml + 2个源文件）
  - 新增测试: 5 个单元测试
  - 测试通过率: 100%

### 📋 当前进展

代码实现已完成并通过所有测试。主要变更：
- 升级 `fastjson` 依赖到 `fastjson2` 2.0.43
- 更新包名导入路径
- 验证 API 兼容性

### 🎯 下一步

请审查以下内容：
- 依赖版本选择是否合理
- 包名更新是否完整
- 测试覆盖是否充分

### 📊 技术要点

**依赖升级**:
```xml
- <dependency>com.alibaba:fastjson:1.2.83</dependency>
+ <dependency>com.alibaba.fastjson2:fastjson2:2.0.43</dependency>
```

**API 兼容性**: fastjson2 保持了与 fastjson 的 API 兼容，仅需修改包名：
- `com.alibaba.fastjson.*` → `com.alibaba.fastjson2.*`

**测试策略**: 增加了边界情况测试，确保序列化/反序列化行为一致

### 📂 相关文档

- 任务文件: `.ai-workspace/active/TASK-20251227-104654/task.md`
- 需求分析: `.ai-workspace/active/TASK-20251227-104654/analysis.md`
- 技术方案: `.ai-workspace/active/TASK-20251227-104654/plan.md`
- 实现报告: `.ai-workspace/active/TASK-20251227-104654/implementation.md`

---
*由 Claude Code 自动生成 - [任务管理系统](../.ai-agents/README.md)*
```

### 示例 2：审查完成，准备合并

```markdown
## 🤖 开发进度更新

**任务ID**: TASK-20251227-104654
**更新时间**: 2025-12-30 10:15:00
**当前状态**: 审查通过，准备合并

### ✅ 已完成

- [x] 需求分析 - 2025-12-29 10:46:54
- [x] 技术方案设计 - 2025-12-29 15:05:00
- [x] 代码实现 - 2025-12-29 18:20:00
- [x] 代码审查 - 2025-12-30 10:00:00
  - 审查通过，无阻塞性问题
  - 修复了 2 处代码风格问题

### 📋 当前进展

代码审查已完成，所有反馈已处理：
- ✅ 代码质量检查通过
- ✅ 测试覆盖率达标
- ✅ 无安全隐患
- ✅ 文档已更新

### 🎯 下一步

准备合并到主分支。合并后将：
- 关闭相关 Issue #207
- 更新版本说明

### 📊 审查总结

**代码变更**: 3 个文件，+156 -89 行
**测试覆盖**: 新增 5 个测试，覆盖率 95%
**破坏性变更**: 无
**向后兼容**: 是

---
*由 Claude Code 自动生成 - [任务管理系统](../.ai-agents/README.md)*
```

## 注意事项

1. **PR 号必须存在**：
   - 任务文件中必须有 `pr_number` 字段
   - 如果没有，提示用户手动指定或更新任务文件

2. **摘要生成原则**：
   - 面向代码审查者
   - 突出技术决策和实现要点
   - 说明关键变更的原因
   - 避免过度技术细节

3. **同步时机**：
   - 代码实现完成，准备审查时
   - 处理完审查反馈后
   - 重大进展或决策变更时
   - PR 长时间等待审查时的进度提醒

4. **评论格式**：
   - 使用 Markdown 格式
   - 使用 emoji 增强可读性
   - 包含时间戳
   - 添加 Claude Code 签名

5. **避免频繁同步**：
   - 不要在每个小改动都同步
   - 建议在完成重要阶段后同步
   - 避免产生过多评论

6. **与 Issue 同步的关系**：
   - `/sync-issue` 面向项目管理者和干系人
   - `/sync-pr` 面向代码审查者和开发者
   - 两者内容侧重点不同

## 相关命令

- `/create-pr` - 创建 Pull Request
- `/sync-issue <task-id>` - 同步进度到 Issue
- `/review-task` - 代码审查
- `/commit` - 提交代码

## 错误处理

- 任务不存在：提示 "任务 {task-id} 不存在，请检查任务ID"
- 缺少 PR 号：提示 "任务文件中缺少 pr_number 字段"
- PR 不存在：提示 "Pull Request #{pr-number} 不存在"
- PR 已关闭：提示 "Pull Request #{pr-number} 已关闭"
- gh 命令失败：提示 "同步失败，请检查 GitHub CLI 是否已登录"
- 网络错误：提示 "网络连接失败，请稍后重试"

## 最佳实践

1. **开发完成后立即同步**：
   ```bash
   # 推荐的工作流程
   /implement-taskTASK-xxx    # 实现功能
   /sync-pr TASK-xxx      # 同步进度，请求审查
   ```

2. **处理审查反馈后再次同步**：
   ```bash
   # 修复审查问题后
   git commit -m "fix: address review comments"
   /sync-pr TASK-xxx      # 告知审查者已处理
   ```

3. **长时间等待时提醒**：
   - PR 等待审查超过 2 天，同步进度提醒
   - 说明紧急程度和阻塞情况

4. **团队协作**：
   - 在进度摘要中 @mention 相关审查者
   - 说明需要特别关注的部分
   - 标注重要的技术决策
