---
description: 提交当前变更到 Git
agent: general
subtask: false
---

提交当前变更到 Git。

执行以下步骤:

**步骤 0: 确认用户本地修改(CRITICAL)**

在执行任何编辑操作之前，必须先检查用户的本地修改：

1. 查看所有已修改的文件:
   !`git status --short`

2. 查看每个文件的具体修改内容:
   !`git diff`

3. 处理规则:
   - **仔细阅读 `git diff` 的输出**，理解用户已经做了哪些修改
   - **在用户修改的基础上**进行增量编辑，不要覆盖用户的实现
   - **如果你计划的修改与用户的修改有冲突**，必须先询问用户确认
   - **禁止**按自己的想法重写用户已经实现的代码
   - **禁止**添加用户没有要求的"改进"

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

2. 分析变更并生成**详细的**提交消息:

   **提交消息结构（CRITICAL）**:
   ```
   <type>(<scope>): <subject>

   <body>

   Co-Authored-By: <你的模型名称> <noreply@anthropic.com>
   ```

   **各部分要求**:
   - **标题行**: `<type>(<scope>): <subject>`
     * type: feat/fix/refactor/docs/build/test/chore 等
     * scope: 模块名(如 fit、waterflow、fel)，可省略
     * subject: 中文，约 20 字以内，概括变更

   - **正文(body)**: 详细说明变更内容
     * 用 2-4 个要点说明具体做了什么
     * 每个要点用 `-` 开头
     * 解释"为什么"这样改，而不仅仅是"改了什么"

   - **签名**: 始终添加 Co-Authored-By，模型名称由你自己声明（如 Claude Sonnet 4.5、GPT-5.2、Gemini-3 等）

   **示例**（仅供参考格式，不要直接复制内容）:
   ```
   docs(opencode): 规范命令编写标准并添加风格指南

   优化所有自定义命令的执行方式:
   - 统一使用 `!` 标记可执行命令
   - 添加错误处理和状态检查
   - 改进时间戳获取方式（先获取再引用）
   - 新增 COMMAND_STYLE_GUIDE.md 编写规范文档

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
   ```

   - 不要提交敏感文件(.env, credentials.json等)

**步骤 3: 提交代码**

1. 添加文件到暂存区:
   !`git add .`

   或指定特定文件:
   !`git add <file1> <file2>`

2. 创建提交:

   使用 HEREDOC 格式执行 git commit，支持多行提交消息。

   **命令格式**（仅供参考，实际执行时替换占位符）:
   ```bash
   git commit -m "$(cat <<'EOF'
   <type>(<scope>): <subject>

   <body 详细说明>

   Co-Authored-By: <你的模型名称> <noreply@anthropic.com>
   EOF
   )"
   ```

   **执行要求**:
   - 根据步骤 2 分析的变更内容，生成实际的提交消息
   - 将上述格式中的占位符替换为实际内容
   - Co-Authored-By 中的模型名称由你自己声明（你是什么模型就写什么）
   - 使用 Bash 工具执行完整的 git commit 命令

3. 验证提交成功:
   !`git status`
   !`git log -1`

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
