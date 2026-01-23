---
description: 提交当前变更到 Git
agent: general
subtask: false
---

提交当前变更到 Git。

执行以下步骤:

**步骤 1: 检查并更新版权头年份(CRITICAL)**

1. 获取当前年份:
   !`date +%Y`

2. 检查修改的文件:
   !`git status --short`
   !`git diff --cached --name-only`

3. 对于每个修改的文件,检查是否包含版权头:
   - 使用 Read 工具读取文件内容
   - 使用 grep 查找版权头: !`grep -l "Copyright" <file-path> 2>/dev/null || echo "无版权头"`
   - 如果文件包含版权头且年份过期,使用 Edit 工具更新
   - 更新格式示例（假设当前年份为 2026）:
     * "Copyright (C) 2024-2025" → "Copyright (C) 2024-2026"
     * "Copyright (C) 2024" → "Copyright (C) 2024-2026"
   - **绝对不要**硬编码年份，始终使用步骤1获取的当前年份
   - **只更新修改的文件**,不批量更新所有文件

**步骤 2: 分析变更并生成提交信息**

1. 查看变更:
   !`git status`
   !`git diff`
   !`git log --oneline -5`

2. 分析变更:
   - 确定变更类型(新功能/增强/Bug修复/重构/测试/文档等)
   - 生成符合规范的提交消息(参考最近的 commit 格式)
   - 提交消息格式: `<type>(<scope>): <subject>`，subject 使用中文且约 20 字以内
   - scope 为模块名(如 fit、waterflow、fel)，可省略
   - 不要提交敏感文件(.env, credentials.json等)

**步骤 3: 提交代码**

1. 添加文件到暂存区:
   !`git add .`
   
   或指定特定文件:
   !`git add <file1> <file2>`

2. 创建提交:
   !`git commit -m "<提交消息>"`

3. 验证提交成功:
   !`git status`
   !`git log -1 --oneline`

**步骤 4: 更新任务状态(如果是任务相关的提交)**

根据情况更新任务状态:

- 如果这是最终提交(任务完成),执行 /complete <task-id>
- 如果还需要继续工作,更新 task.md 中的 updated_at 和交接信息
- 如果提交后需要审查,更新 current_step 为 code-review
- 如果提交后需要创建 PR,记录提交哈希

**注意事项**:
- **CRITICAL**: 提交前必须检查并更新版权头年份
- **CRITICAL**: 提交后必须根据情况更新任务状态
- 不要提交包含敏感信息的文件
- 确保提交消息清晰描述了变更内容
- 遵循项目的 commit message 规范
- 不要使用 -i 等交互式标志
