---
description: 标记任务阻塞并记录阻塞原因
argument-hint: <task-id> [reason]
---

标记任务 $1 为阻塞状态,记录详细的阻塞原因,并移动到 blocked 目录。

**使用场景**:
- ❌ 编译失败且无法修复
- ❌ 测试失败且原因不明
- ❌ 依赖库存在 Bug
- ❓ 需求不明确,需要澄清
- ⏳ 等待外部依赖

**不应该标记为阻塞**:
- ✅ 代码审查发现问题 → 使用 /refinement-task $1 修复
- ✅ 实现遇到困难但可以解决 → 继续实施

执行以下步骤:

1. 验证任务存在:
   ```bash
   test -f .ai-workspace/active/$1/task.md && echo "✅ 任务存在" || echo "❌ ERROR: 任务不存在"
   ```

2. 分析并记录阻塞原因:
   用户提供的阻塞原因: $2
   如果上述值为空,需要询问用户确定阻塞原因。
   确定以下信息:
   - 阻塞类型: 技术问题/需求问题/资源问题/决策问题
   - 问题描述、根本原因、尝试的解决方案、需要的帮助

3. 获取当前时间:
   ```bash
   date '+%Y-%m-%d %H:%M:%S'
   ```

4. 更新任务状态:
   使用 Edit 工具更新 task.md:
   - status: blocked
   - updated_at: 当前时间
   - blocked_at: 当前时间
   - blocked_by: codex
   - blocked_reason: 简短描述阻塞原因

5. 在 task.md 中添加"⚠️ 阻塞信息"章节(阻塞概要、问题描述、根本原因、尝试方案、解除条件)

6. 移动到阻塞目录:
   ```bash
   mkdir -p .ai-workspace/blocked && mv .ai-workspace/active/$1 .ai-workspace/blocked/
   ```

7. 验证移动成功:
   ```bash
   test ! -d .ai-workspace/active/$1 && echo "✅ 已移除 active" || echo "❌ active 仍存在"
   test -d .ai-workspace/blocked/$1 && echo "✅ 已移动到 blocked" || echo "❌ 移动失败"
   ```

8. 告知用户:
   ```
   ⚠️  任务 $1 已标记为阻塞
   阻塞位置: .ai-workspace/blocked/$1/
   下一步: 解决阻塞问题后手动移回 active 目录
   ```

**解除阻塞**:
```bash
mv .ai-workspace/blocked/$1 .ai-workspace/active/
```
然后更新 task.md: status 改回 active,移除 blocked 相关字段。

**注意事项**:
- 阻塞信息要详细、准确、客观
- 标记后要主动跟进问题解决进度
