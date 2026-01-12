# FIT Framework 项目规则

## 文件权限管理规则

### 规则 1: 新增和修改文件的所有权

**重要：** 所有新增或修改的文件必须设置正确的所有者权限，确保用户可以自主修改。

**原则：** 不要硬编码用户名和用户组，而是从项目中已有文件动态获取权限信息。

**动态获取权限的方法：**

```bash
# 方法1: 从项目根目录的 README.md 获取所有者
REF_FILE="README.md"  # 所有项目都有的通用文件
OWNER=$(ls -l $REF_FILE | awk '{print $3}')
GROUP=$(ls -l $REF_FILE | awk '{print $4}')

# 方法2: 获取当前工作目录的所有者
OWNER=$(ls -ld . | awk '{print $3}')
GROUP=$(ls -ld . | awk '{print $4}')

# 方法3: 使用 stat 命令（更可靠）
OWNER=$(stat -f "%Su" $REF_FILE)  # macOS
GROUP=$(stat -f "%Sg" $REF_FILE)  # macOS
# 或
OWNER=$(stat -c "%U" $REF_FILE)   # Linux
GROUP=$(stat -c "%G" $REF_FILE)   # Linux
```

**实施方法：**

```bash
# 1. 先获取参考文件的所有者
OWNER_GROUP=$(ls -l README.md | awk '{print $3":"$4}')

# 2. 应用到新文件
sudo chown $OWNER_GROUP <file_path>

# 或者分步执行
OWNER=$(ls -l README.md | awk '{print $3}')
GROUP=$(ls -l README.md | awk '{print $4}')
sudo chown $OWNER:$GROUP <file_path>
```

**⚠️ 强制执行规则（CRITICAL）：**

每次使用 `Write` 或 `Edit` 工具创建/修改文件后，**必须立即**在同一个响应中使用 `Bash` 工具修复权限。

**标准流程（必须遵循）：**

```bash
# 步骤 1: 创建或修改文件
Write(file_path, content)  # 或 Edit(...)

# 步骤 2: 立即动态获取正确的所有者并修复权限（在同一个响应中）
Bash("OWNER_GROUP=$(ls -l README.md | awk '{print $3\":\"$4}') && sudo chown $OWNER_GROUP " + file_path)

# 或者批量修复多个文件
Bash("OWNER_GROUP=$(ls -l README.md | awk '{print $3\":\"$4}') && sudo chown $OWNER_GROUP file1 file2 file3")
```

**为什么必须这样做：**
- Write 和 Edit 工具可能会将文件所有者更改为 root
- 这会导致用户无法修改自己的文件
- 必须动态获取权限，不能硬编码用户名
- 必须在工具调用后立即修复，不能延后到下一个响应

**检查清单：**
- [ ] 使用 Write 工具创建新文件后，**立即**在同一响应中动态获取并修复权限
- [ ] 使用 Edit 工具修改文件后，**立即**在同一响应中动态获取并修复权限
- [ ] 批量创建/修改文件后，统一修改所有权
- [ ] 创建目录后，递归修改目录及其内容的所有权
- [ ] 每次修改后验证权限是否正确
- [ ] **绝对不要**硬编码用户名和用户组

**完整示例：**

```bash
# ❌ 错误做法：硬编码用户信息
Write(file_path, content)
Bash("sudo chown jiyujie:staff " + file_path)  # 在其他人电脑上会失败

# ✅ 正确做法：动态获取权限
Write(file_path, content)
# 从项目根目录的 README.md 获取所有者信息
Bash("OWNER_GROUP=$(ls -l README.md | awk '{print $3\":\"$4}') && sudo chown $OWNER_GROUP " + file_path)

# ✅ 更简洁的做法：批量处理
Write(file1, content1)
Write(file2, content2)
Write(file3, content3)
Bash("OWNER_GROUP=$(ls -l README.md | awk '{print $3\":\"$4}') && sudo chown $OWNER_GROUP file1 file2 file3")
```

## Pull Request 提交规范

### 规则 2: PR 提交必须遵循项目规范

本项目的 PR 规范定义在 `.github/PULL_REQUEST_TEMPLATE.md` 文件中。

**强制要求：**
1. 创建 PR 前，**必须先阅读** `.github/PULL_REQUEST_TEMPLATE.md`
2. PR 描述必须完整填写模板中的所有必填项
3. 不需要每次让用户提醒查看 PR 模板

**PR 模板位置：**
```
.github/PULL_REQUEST_TEMPLATE.md
```

**必填项清单：**

1. **相关问题 / Related Issue**
   - [ ] Issue 链接或说明这是微小修改

2. **变更类型 / Type of Change**
   - [ ] 选择适当的变更类型（Bug修复/新功能/破坏性变更/文档/重构/性能优化/依赖升级/功能增强/代码清理）

3. **变更目的 / Purpose of the Change**
   - [ ] 详细描述变更的目的和必要性

4. **主要变更 / Brief Changelog**
   - [ ] 列出主要的变更内容（使用项目符号）

5. **验证变更 / Verifying this Change**
   - [ ] 测试步骤（编号列表）
   - [ ] 测试覆盖情况（勾选项）

6. **贡献者检查清单 / Contributor Checklist**
   - [ ] 基本要求
   - [ ] 代码质量
   - [ ] 测试要求
   - [ ] 文档和兼容性

7. **附加信息 / Additional Notes**
   - [ ] 提供额外的上下文信息

8. **审查者注意事项 / Reviewer Notes**
   - [ ] 为审查者提供特殊说明

9. **结尾签名**
   - [ ] 必须添加：`🤖 Generated with [Claude Code](https://claude.com/claude-code)`

**自动化流程（强制执行）：**

当用户要求创建 PR 时，**必须按照以下顺序执行**：

1. **读取 PR 模板**
   ```bash
   Read(".github/PULL_REQUEST_TEMPLATE.md")
   ```

2. **查看最近的 PR 示例**（参考格式和风格）
   ```bash
   Bash("gh pr list --limit 3 --state merged --json number,title,body")
   ```

3. **分析当前分支的变更**
   ```bash
   Bash("git status")
   Bash("git log <base-branch>..HEAD --oneline")
   Bash("git diff <base-branch>...HEAD --stat")
   ```

4. **检查远程分支状态**
   ```bash
   Bash("git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>&1 || echo 'No upstream branch'")
   ```

5. **如果需要推送**
   ```bash
   Bash("git push -u origin <branch>")
   ```

6. **创建 PR**（使用 HEREDOC 格式）
   ```bash
   gh pr create --base <target-branch> --title "<title>" --body "$(cat <<'EOF'
   <按照模板格式填写的完整内容>
   EOF
   )"
   ```

**PR 标题格式：**
- 依赖升级：`[模块名] Upgrade <package> from vX.Y.Z to vA.B.C`
- Bug 修复：`[模块名] Fix <issue-description>`
- 新功能：`[模块名] Add <feature-description>`
- 功能增强：`[模块名] Enhance <feature-description>`

## Git 提交和版本控制规范

### 规则 3: Commit Message 格式规范

**⚠️ 重要规则（CRITICAL）：**

本项目的提交信息**必须使用中文作为核心内容**，遵循以下格式：

**标准格式：**
```
<type>: <中文描述>

<可选的详细说明>

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

**Type 类型（使用英文）：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响代码逻辑）
- `refactor`: 重构（既不是新功能也不是 Bug 修复）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动（包括依赖升级）
- `revert`: 回滚之前的提交

**子类型（可选）：**
- `chore(deps)`: 依赖升级，格式：`chore(deps): 升级 <package> 到 v<version>`

**示例：**
```bash
# 新功能
feat: 添加用户认证功能

# Bug 修复
fix: 修复登录失败的问题

# 文档更新
docs: 更新 API 文档

# 依赖升级
chore(deps): 升级 fastjson2 到 v2.0.60

# 代码重构
refactor: 重构用户服务模块

# 性能优化
perf: 优化数据库查询性能

# 构建相关
chore: 更新构建脚本
```

**强制要求：**
1. 描述部分**必须使用中文**
2. Type 类型使用英文小写
3. 描述要简洁明了，概括核心变更
4. 多个变更应该分多次提交
5. 必须在提交消息末尾添加 Claude Code 签名

### 规则 4: 禁止自动提交代码

**⚠️ 重要规则（CRITICAL）：**

**绝对不要**自动执行 `git commit` 或 `git add` 命令，除非用户明确使用 `/commit` 命令请求提交。

**原因：**
- 项目配置了 `/commit` 命令，说明用户希望自己控制提交时机
- 用户可能需要在提交前进行额外的检查或修改
- 自动提交会剥夺用户对版本控制的控制权

**正确做法：**

1. **完成代码修改后**
   - ✅ 告知用户有哪些文件被修改
   - ✅ 展示修改的内容摘要
   - ✅ 提醒用户可以使用 `/commit` 命令进行提交
   - ❌ 不要自动执行 `git add` 或 `git commit`

2. **仅在用户明确请求时才提交**
   - ✅ 用户运行 `/commit` 命令
   - ✅ 用户明确说"提交这些修改"、"commit these changes"
   - ❌ 不要在用户只是让你"修复问题"或"完成任务"时自动提交

3. **示例对比**

   **❌ 错误做法：**
   ```
   User: 请修复测试命令的清理步骤
   Assistant:
   [修复代码]
   [自动执行 git add 和 git commit]  # ❌ 错误！
   ```

   **✅ 正确做法：**
   ```
   User: 请修复测试命令的清理步骤
   Assistant:
   [修复代码]
   我已经完成了修复，修改了以下文件：
   - .claude/commands/test.md
   - .agent/workflows/test.md

   你可以使用 `/commit` 命令来提交这些修改。
   # ✅ 正确！等待用户决定何时提交
   ```

**例外情况：**

只有在以下情况下可以自动提交：
1. 用户明确说"修复后直接提交"或"fix and commit"
2. 用户使用了 `/commit` 命令
3. 某个 workflow 或 slash command 的定义中明确要求自动提交

## 通用最佳实践

### 文件操作
- 创建文件后立即检查并修改权限
- 使用 `ls -l` 验证文件所有者
- 批量操作后统一修改权限

### Git 操作
- **绝对不要自动提交代码**（参见规则 3）
- 提交前检查 `.github/` 目录中的规范
- 遵循项目的 commit message 格式
- PR 描述要完整、清晰

### 文档更新
- 修改代码时同步更新相关文档
- 确保 README 的准确性
- 添加必要的使用示例

## 项目特定信息

**项目名称**: FIT Framework
**主分支**: main
**开发分支**: 3.5.x
**仓库**: https://github.com/ModelEngine-Group/fit-framework

**常用命令：**

```bash
# 编译项目
mvn clean install

# 运行测试
mvn test

# 启动应用
./fit start

# 检查文件权限
ls -l <file>

# 动态修改文件权限（从 README.md 获取所有者）
OWNER_GROUP=$(ls -l README.md | awk '{print $3":"$4}')
sudo chown $OWNER_GROUP <file>
```

---

## 自定义 Slash Commands

本项目配置了以下自定义命令，位于 `.claude/commands/` 目录：

### PR 相关命令

**`/pr [branch]`** - 创建 Pull Request
- `/pr` - 创建PR到默认分支（3.6.x）
- `/pr main` - 创建PR到 main 分支
- `/pr 3.5.x` - 创建PR到 3.5.x 分支

**`/pr-update <pr-number>`** - 更新现有PR的描述
- `/pr-update 369` - 更新 #369 PR 的描述

**`/review <pr-number>`** - 审查 Pull Request
- `/review 369` - 审查 #369 PR

### 项目管理命令

**`/fix-permissions`** - 检查和修复文件权限
- `/fix-permissions` - 检查并自动修复所有权限问题

### 开发相关命令

**`/commit [message]`** - 提交变更
- `/commit` - 交互式提交
- `/commit "feat: add new feature"` - 使用指定消息提交

**`/upgrade-dep <package> <from> <to>`** - 升级依赖
- `/upgrade-dep swagger-ui 5.30.0 5.30.2` - 升级 swagger-ui

### 命令参数说明

Slash commands 支持参数传递，参数使用空格分隔：
- 单个参数：`/pr main`
- 多个参数：`/upgrade-dep swagger-ui 5.30.0 5.30.2`
- 带引号的参数：`/commit "feat: new feature"`

**参数解析规则：**
- 命令和参数之间用空格分隔
- 第一个词是命令名称（不含 `/`）
- 其余部分是参数
- 如果参数包含空格，用引号包裹

---

## 版权年份管理规范

### 规则 5: 版权年份更新（CRITICAL）

**⚠️ 重要规则**：修改任意带版权头的文件时，**必须同步更新版权年份到当前年份**。

**强制要求：**

1. **动态获取当前年份**
   - **绝对不要**硬编码年份（如 2026）
   - **必须**使用系统命令获取当前年份

   ```bash
   # 获取当前年份
   CURRENT_YEAR=$(date +%Y)
   echo $CURRENT_YEAR
   ```

2. **版权头格式检测与更新**

   常见格式：

   **格式 1：年份范围**
   ```
   Copyright (C) 2024-2025 FIT Framework
   ↓ 更新为
   Copyright (C) 2024-2026 FIT Framework  # 假设当前年份为 2026
   ```

   **格式 2：单一年份**
   ```
   Copyright (C) 2024 FIT Framework
   ↓ 更新为
   Copyright (C) 2024-2026 FIT Framework  # 假设当前年份为 2026
   ```

   **格式 3：Java 文件头**
   ```java
   /*
    * Copyright (C) 2024-2025 FIT Framework
    * All rights reserved.
    */
   ↓ 更新为
   /*
    * Copyright (C) 2024-2026 FIT Framework
    * All rights reserved.
    */
   ```

3. **自动化更新流程**

   **标准流程（必须遵循）：**

   **⚠️ 重要**：只更新**当前修改的文件**，不要批量更新所有文件！

   ```bash
   # 步骤 1: 获取当前年份
   CURRENT_YEAR=$(date +%Y)

   # 步骤 2: 检查当前修改的文件是否有版权头
   grep -l "Copyright" <current_modified_file>

   # 步骤 3: 检查版权年份
   grep "Copyright.*[0-9]\{4\}" <current_modified_file>

   # 步骤 4: 如果年份不是最新的，使用 Edit 工具更新
   # 将 2024-2025 更新为 2024-<CURRENT_YEAR>
   # 或将 2024 更新为 2024-<CURRENT_YEAR>
   ```

4. **仅更新当前修改的文件**

   **原则**：
   - ✅ 只更新你正在修改的文件
   - ❌ 不要批量更新项目中所有文件
   - ✅ 一次只处理一个文件的版权头
   - ❌ 不要使用 find 批量查找和替换

   **示例（仅供参考，查看当前修改了哪些文件）**：

   ```bash
   # 查看当前修改的文件
   git status --short

   # 查看某个修改文件的版权头
   grep "Copyright" <specific_file_you_modified>
   ```

5. **检查清单**

   - [ ] 使用 `date +%Y` 命令动态获取当前年份
   - [ ] 检查修改的文件是否包含版权头
   - [ ] 如果包含版权头，检查年份是否为当前年份
   - [ ] 如果不是，使用 Edit 工具更新版权年份
   - [ ] **绝对不要**在代码中写死年份（如 2026）
   - [ ] 保持版权头的其他内容不变，只更新年份

**完整示例：**

```bash
# ❌ 错误做法：硬编码年份
Edit(file_path, old_string="2024-2025", new_string="2024-2026")  # 硬编码 2026！

# ✅ 正确做法：动态获取年份
# 步骤 1: 先获取当前年份
CURRENT_YEAR=$(date +%Y)

# 步骤 2: 使用变量更新版权头
# 在 Edit 工具调用前，确保知道当前年份
# 例如当前年份为 2026：
Edit(file_path, old_string="Copyright (C) 2024-2025", new_string="Copyright (C) 2024-2026")
# 或
Edit(file_path, old_string="Copyright (C) 2024", new_string="Copyright (C) 2024-2026")
```

**为什么必须这样做：**
- 确保版权声明的准确性和法律有效性
- 遵循项目规范，保持一致性
- 避免年份过时导致的合规问题
- 动态获取年份确保在任何时间点执行都是正确的

## AI 协作任务识别和处理规则

### 规则 6: 任务语义识别（CRITICAL）

**⚠️ 重要规则**：当用户使用自然语言描述任务时，AI 必须进行语义分析并自动查找相关任务。

**执行流程**：

1. **语义分析**：识别用户意图

   **需求分析任务**：
   - 关键词：分析、issue、需求、问题、功能
   - 示例："分析 issue 207"、"需求分析任务 TASK-xxx"
   - 行为：执行 requirement-analysis 步骤，等同于 `/analyze-issue`

   **方案设计任务**：
   - 关键词：方案、设计、plan、技术方案、实施计划
   - 示例："设计 task xxx 的技术方案"、"plan TASK-xxx"
   - 行为：执行 technical-design 步骤，等同于 `/plan`

   **代码实施任务**：
   - 关键词：实施、实现、开发、implement
   - 示例："实施 TASK-xxx"、"开始实现"
   - 行为：执行 implementation 步骤，等同于 `/implement`

   **任务状态查询**：
   - 关键词：状态、进度、查看任务
   - 示例："查看 TASK-xxx 的状态"、"任务进度如何"
   - 行为：查看任务状态，等同于 `/task-status`

   **同步进度任务**：
   - 关键词：同步、更新进度、sync、发布进度、更新 issue
   - 示例："同步任务进度到 Issue"、"更新 Issue 进展"、"sync TASK-xxx"
   - 行为：同步进度到 GitHub Issue，等同于 `/sync-issue`

2. **任务查找**：在 .ai-workspace 中自动查找相关任务

   **查找优先级**：
   - 按任务 ID 查找（如 TASK-20251227-104654）
   - 按 Issue 号查找（如 #207 或 issue 207）
   - 按关键词搜索任务标题和描述

   **查找路径**：
   - `.ai-workspace/active/` - 优先查找活动任务
   - `.ai-workspace/completed/` - 其次查找已完成任务
   - `.ai-workspace/blocked/` - 最后查找被阻塞任务

3. **自动执行**：执行标准协作流程

   - 读取任务文件和已有上下文
   - 按照 `.ai-agents/workflows/` 定义的工作流执行
   - 输出到标准位置（`.ai-workspace/active/{task-id}/`）
   - 更新任务状态

4. **明确告知**：向用户说明识别结果

   必须告知用户：
   - 识别的任务类型和ID
   - 当前执行的步骤
   - 输出文件的位置
   - 下一步建议

### 示例执行流程

**示例 1：分析 Issue**

```
用户："分析 issue 207"

AI 行为：
1. 识别为需求分析任务
2. 在 .ai-workspace/tasks 中查找 issue #207 相关任务
3. 如果找到，读取并继续；如果没有，创建新任务
4. 执行需求分析（等同于 /analyze-issue 207）
5. 输出 analysis.md
6. 更新任务状态
7. 告知用户：
   "✅ 识别为需求分析任务
   已为 Issue #207 创建任务 TASK-20251227-104654
   分析文档：.ai-workspace/active/TASK-20251227-104654/analysis.md
   下一步：/plan TASK-20251227-104654"
```

**示例 2：设计方案**

```
用户："设计 TASK-20251227-104654 的技术方案"

AI 行为：
1. 识别为方案设计任务
2. 查找任务 TASK-20251227-104654
3. 读取 analysis.md
4. 执行技术方案设计（等同于 /plan TASK-20251227-104654）
5. 输出 plan.md
6. 更新任务状态
7. 告知用户：
   "✅ 识别为技术方案设计任务
   任务：TASK-20251227-104654
   方案文档：.ai-workspace/active/TASK-20251227-104654/plan.md
   下一步：等待人工审查后执行 /implement TASK-20251227-104654"
```

### 注意事项

1. **优先使用 Slash Commands**：
   - Slash Commands 更精确、标准化
   - 自然语言作为补充，提供灵活性

2. **明确沟通**：
   - 始终告知用户识别的结果
   - 如果有歧义，询问用户确认

3. **遵循工作流**：
   - 严格按照 `.ai-agents/workflows/` 定义执行
   - 遵守人工检查点

4. **保持一致性**：
   - 无论通过命令还是自然语言，输出格式保持一致
   - 使用相同的文件路径和命名规范

---

## 规则 7: 任务状态管理规范（CRITICAL）

### 强制要求

你**必须**在执行以下命令后立即更新任务状态。这是一个 **CRITICAL** 规则，违反此规则是不可接受的。

| 命令 | 必须更新的字段 | 必须执行的操作 |
|------|---------------|---------------|
| `/analyze-issue` | `current_step`, `updated_at`, `assigned_to` | 创建任务文件 |
| `/plan-task` | `current_step`, `updated_at` | 标记分析完成，进入技术设计 |
| `/implement-task` | `current_step`, `updated_at` | 标记方案完成，进入代码实施 |
| `/review-task` | `current_step`, `updated_at` | 标记实现完成，进入代码审查 |
| `/refinement-task` | `current_step`, `updated_at` | 记录修复情况，标记为修复中 |
| `/complete-task` | `status`, `completed_at`, `updated_at` | 移动到 `completed` 目录 |
| `/block-task` | `status`, `blocked_at`, `blocked_reason`, `updated_at` | 移动到 `blocked` 目录 |
| `/commit` | `updated_at` | 记录提交信息 |

### 状态更新检查清单

每次完成命令后，必须确认：

- [ ] `task.md` 中的 `current_step` 已更新
- [ ] `task.md` 中的 `updated_at` 已更新为当前时间（格式：`yyyy-MM-dd HH:mm:ss`）
- [ ] `task.md` 中的 `assigned_to` 已更新为你的名字
- [ ] "工作流进度" 部分已标记当前步骤为完成 ✅
- [ ] 如果任务完成，已执行 `/complete-task` 归档
- [ ] 如果任务阻塞，已执行 `/block-task` 并说明原因

### 状态字段说明

**基本状态字段**：
```yaml
status: active | blocked | completed  # 任务整体状态
current_step: {step-id}               # 当前所在步骤
created_at: {yyyy-MM-dd HH:mm:ss}     # 创建时间
updated_at: {yyyy-MM-dd HH:mm:ss}     # 最后更新时间
assigned_to: {ai-name}                # 当前负责的 AI
```

**完成状态字段**（仅当 status = completed）：
```yaml
completed_at: {yyyy-MM-dd HH:mm:ss}   # 完成时间
```

**阻塞状态字段**（仅当 status = blocked）：
```yaml
blocked_at: {yyyy-MM-dd HH:mm:ss}     # 阻塞时间
blocked_by: {ai-name}                 # 标记阻塞的 AI
blocked_reason: {简短描述}             # 阻塞原因
```

### 工作流进度标记

在 `task.md` 的 "工作流进度" 部分，必须使用以下格式：

```markdown
## 工作流进度

根据 `.ai-agents/workflows/{workflow-name}.yaml`:

- [x] requirement-analysis (claude, 2026-01-03)
- [x] technical-design (claude, 2026-01-03)
- [x] implementation (claude, 2026-01-04)
- [x] code-review (claude, 2026-01-04)
- [ ] finalize (待执行)
```

**标记规则**：
- 已完成的步骤：`[x] {step-name} ({ai-name}, {date})`
- 正在进行的步骤：`[x] {step-name} (进行中)`
- 未开始的步骤：`[ ] {step-name} (待执行)`

### 违反规则的后果

如果你忘记更新状态，将导致：

1. **追踪失败**：人类无法追踪任务进度
2. **接手困难**：其他 AI 无法接手任务
3. **任务遗失**：任务可能被遗忘在 `active` 目录中
4. **报告不准确**：无法生成准确的进度报告
5. **工作流混乱**：工作流程无法正常流转

**这是一个 CRITICAL 规则，违反此规则会严重影响项目协作效率。**

### 最佳实践

1. **立即更新**：
   - 不要延迟。完成命令后，第一件事就是更新状态
   - 不要批量更新。每个命令执行后立即更新

2. **完整更新**：
   - 更新所有必需的字段
   - 更新工作流进度标记
   - 如果有输出文件，在 task.md 中标记为已完成

3. **准确记录**：
   - `updated_at` 必须是当前实际时间
   - `assigned_to` 必须是你的名字（claude / chatgpt / gemini 等）
   - `current_step` 必须与实际步骤一致

4. **及时归档**：
   - 任务完成后，立即使用 `/complete-task` 归档
   - 任务阻塞后，立即使用 `/block-task` 标记
   - 不要让已完成的任务停留在 `active` 目录

5. **使用检查清单**：
   - 每个命令文件末尾都有"完成检查清单"
   - 执行命令后，逐项检查
   - 确认所有项目都已完成

### 示例

**正确示例**：

```yaml
# 执行 /implement-task TASK-20260103-135501 后
id: TASK-20260103-135501
status: active
current_step: implementation
created_at: 2026-01-03 13:55:01
updated_at: 2026-01-04 10:30:15  # 立即更新为当前时间
assigned_to: claude
```

**错误示例**：

```yaml
# 执行 /implement-task 后忘记更新
id: TASK-20260103-135501
status: active
current_step: technical-design  # ❌ 未更新为 implementation
updated_at: 2026-01-03 14:22:11  # ❌ 未更新为当前时间
assigned_to: claude
```

### 相关命令

- `/task-status` - 查看任务当前状态
- `/complete-task` - 标记任务完成并归档
- `/block-task` - 标记任务阻塞
- `/sync-issue` - 同步状态到 GitHub Issue

### 参考资料

- 任务模板：`.ai-agents/templates/task.md`
- 工作流定义：`.ai-agents/workflows/`
- 命令文档：`.claude/commands/`
