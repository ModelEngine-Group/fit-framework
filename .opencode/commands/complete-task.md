---
description: 标记任务完成并归档到 completed 目录
agent: general
subtask: false
---

标记任务 $1 为已完成状态,更新任务元数据,并归档到 completed 目录。

**前置条件检查**:

在执行前,确认以下条件全部满足:
- 所有工作流步骤已完成
- 代码已审查通过(review.md 显示批准)
- 代码已提交到 Git
- 代码已合并到目标分支(如果需要)
- 所有测试通过
- Issue 已同步更新(如果有关联 Issue)
- PR 已合并(如果有 PR)

**如果以上条件未全部满足,请勿执行此命令。**

执行以下步骤:

1. 验证任务存在:
   !`test -f .ai-workspace/active/$1/task.md && echo "✅ 任务存在" || echo "❌ ERROR: 任务不存在"`

2. 读取并验证任务状态:
   - 检查所有步骤是否标记为完成 ✅
   - 检查 current_step 是否为最后一步
   - 检查 status 是否为 active
   - 检查文件完整性: analysis.md, plan.md, implementation.md, review.md 都存在

3. 获取当前时间:
   !`date '+%Y-%m-%d %H:%M:%S'`

4. 更新任务状态(CRITICAL):
   使用 Edit 工具更新 task.md 的 YAML front matter:
   - status: completed
   - current_step: finalize
   - updated_at: <使用步骤3获取的时间>
   - completed_at: <使用步骤3获取的时间>

5. 在 task.md 末尾添加完成总结:
   
   使用 Edit 工具在 task.md 末尾添加以下内容:
   
   ```markdown
   ---
   
   ## 任务完成总结
   
   ### 完成信息
   - **完成时间**: <使用步骤3获取的时间>
   - **完成者**: opencode
   - **关联 PR**: #<pr-number>(如果有)
   - **关联 Issue**: #<issue-number>(如果有)
   
   ### 交付成果
   - [x] 需求分析文档: analysis.md
   - [x] 技术方案文档: plan.md
   - [x] 实现报告: implementation.md
   - [x] 代码审查报告: review.md
   - [x] 代码提交: <commit-hash>
   
   ### 任务完成标准
   - [x] 功能完整实现
   - [x] 代码审查通过
   - [x] 所有测试通过
   - [x] 文档完整
   - [x] 代码已合并
   ```

6. 归档任务(CRITICAL):
   !`mkdir -p .ai-workspace/completed`
   !`mv .ai-workspace/active/$1 .ai-workspace/completed/`

7. 验证移动成功:
   !`test ! -d .ai-workspace/active/$1 && echo "✅ 已移除 active 目录" || echo "❌ ERROR: active 目录仍存在"`
   !`test -d .ai-workspace/completed/$1 && echo "✅ 已归档到 completed" || echo "❌ ERROR: 归档失败"`

8. 可选: 同步到 Issue:
   如果有关联 Issue,使用 /sync-issue <issue-number> 更新 Issue 状态

9. 告知用户:
   ```
   🎉 任务 $1 已完成并归档
   
   **任务信息**:
   - 任务ID: $1
   - 完成时间: <当前时间>
   
   **归档位置**:
   - .ai-workspace/completed/$1/
   
   **交付成果**:
   - ✅ 需求分析文档
   - ✅ 技术方案文档
   - ✅ 实现报告
   - ✅ 代码审查报告
   - ✅ 代码提交并合并
   
   任务已成功归档!🎊
   ```

**注意事项**:
- 只有在**真正完成**所有工作后才归档任务
- 归档前确认所有文档都已创建
- 验证移动成功后再告知用户
- 不要过早归档未完成的任务
