---
description: 标记任务阻塞并记录阻塞原因
agent: general
subtask: false
---

标记任务 $1 为阻塞状态,记录详细的阻塞原因,并移动到 blocked 目录。

**使用场景**:

在以下情况下应该标记任务为阻塞:
- ❌ 编译失败且无法修复
- ❌ 测试失败且原因不明
- ❌ 依赖库存在 Bug
- ❓ 需求不明确,需要澄清
- ⏳ 等待外部依赖
- 🤔 需要架构决策或人工审查

**不应该标记为阻塞**:
- ✅ 代码审查发现问题 → 使用 /refine $1 修复
- ✅ 实现遇到困难但可以解决 → 继续实施

执行以下步骤:

1. 验证任务存在:
   !`test -f .ai-workspace/active/$1/task.md && echo "✅ 任务存在" || echo "❌ ERROR: 任务不存在"`

2. 分析并记录阻塞原因:
   用户提供的阻塞原因: $2
   如果上述值为空,需要询问用户确定阻塞原因。
   确定以下信息:
   - 阻塞类型: 技术问题/需求问题/资源问题/决策问题
   - 问题描述: 遇到了什么具体问题?
   - 根本原因: 为什么会出现这个问题?
   - 尝试的解决方案: 已经尝试了哪些方法?
   - 需要的帮助: 需要谁来帮助?需要什么资源?

3. 获取当前时间:
   !`date '+%Y-%m-%d %H:%M:%S'`

4. 更新任务状态(CRITICAL):
   使用 Edit 工具更新 task.md 的 YAML front matter:
   - status: blocked
   - current_step: <保持不变>
   - updated_at: <使用步骤3获取的时间>
   - blocked_at: <使用步骤3获取的时间>
   - blocked_by: opencode
   - blocked_reason: <简短描述阻塞原因>

5. 在 task.md 中添加"阻塞信息"章节:
   
   使用 Edit 工具在 task.md 末尾添加以下内容:
   
   ```markdown
   ---
   
   ## ⚠️ 阻塞信息
   
   ### 阻塞概要
   - **阻塞时间**: <使用步骤3获取的时间>
   - **阻塞步骤**: <current_step>
   - **阻塞者**: opencode
   - **阻塞类型**: <技术问题/需求问题/资源问题/决策问题>
   - **严重程度**: <高/中/低>
   
   ### 问题描述
   <详细描述遇到的问题>
   
   ### 根本原因
   <分析问题的根本原因>
   
   ### 尝试的解决方案
   1. **尝试方法 1**: <描述>
      - 结果: <失败/部分成功>
      - 原因: <为什么没有解决>
   
   ### 需要的帮助
   **需要谁**: <开发者/架构师/产品经理>
   **需要什么**: <明确需求/解决技术问题/提供资源/做出决策>
   
   ### 解除阻塞条件
   满足以下条件后可以解除阻塞并继续:
   - [ ] 条件 1
   - [ ] 条件 2
   ```

6. 移动到阻塞目录(CRITICAL):
   !`mkdir -p .ai-workspace/blocked`
   !`mv .ai-workspace/active/$1 .ai-workspace/blocked/`

7. 验证移动成功:
   !`test ! -d .ai-workspace/active/$1 && echo "✅ 已移除 active 目录" || echo "❌ ERROR: active 目录仍存在"`
   !`test -d .ai-workspace/blocked/$1 && echo "✅ 已移动到 blocked" || echo "❌ ERROR: 移动失败"`

8. 可选: 同步到 Issue:
   如果有关联 Issue,使用 /sync-issue <issue-number> 更新阻塞状态

9. 告知用户:
   ```
   ⚠️  任务 $1 已标记为阻塞
   
   **任务信息**:
   - 任务ID: $1
   - 阻塞步骤: <current_step>
   - 阻塞时间: <当前时间>
   
   **阻塞原因**:
   <阻塞原因简短描述>
   
   **需要的帮助**:
   <需要谁来帮助,需要什么>
   
   **阻塞位置**:
   - .ai-workspace/blocked/$1/
   
   **下一步**:
   1. 查看任务文件中的"阻塞信息"章节了解详情
   2. 解决阻塞问题
   3. 手动移回 active 目录并更新状态
   ```

**解除阻塞**:
当问题解决后,手动解除阻塞:

!`mv .ai-workspace/blocked/$1 .ai-workspace/active/`

然后使用 Edit 工具更新 task.md:
- 将 status 改回 active
- 移除 blocked_at, blocked_by, blocked_reason 字段
- 更新 updated_at 为当前时间

**注意事项**:
- 及时标记,不要拖延
- 阻塞信息要详细、准确、客观
- 避免使用模糊的描述
- 标记阻塞后,要主动跟进问题解决进度
