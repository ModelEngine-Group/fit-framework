# FIT Framework - Claude Code 项目规则

> 🎯 **本文件定义 Claude Code 的强制规则和执行流程**
>
> 📖 **项目基础规范**（构建命令、编码风格、测试要求等）请参见 `.claude/CLAUDE.md`

---

## 核心规则概览

| 规则                  | 级别    | 说明                   |
|---------------------|-------|----------------------|
| Commit Message 中文格式 | 🔴 关键 | 提交信息必须使用中文描述         |
| 禁止自动提交              | 🔴 关键 | 不自动执行 git commit/add |
| 版权年份更新              | 🔴 关键 | 修改文件时更新版权年份          |
| 任务状态管理              | 🔴 关键 | 执行命令后立即更新任务状态        |
| PR 规范               | 🟡 重要 | 遵循 PR 模板             |
| 任务语义识别              | 🟡 重要 | 自然语言转命令执行            |

---

## 规则 1: Commit Message 格式规范（🔴 关键）

**基础格式**：`<type>(<scope>): <中文描述>`
- **type**: feat/fix/docs/refactor/test/chore
- **scope**: fit/waterflow/fel（可省略）

**Claude 执行要点**:

1. **强制使用中文描述**：`<type>(<scope>): <中文描述>`
2. **末尾添加生成标记和署名**：
   ```
   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude <noreply@anthropic.com>
   ```
3. **使用 HEREDOC 格式传递消息**（避免转义问题）

**标准提交格式**:
```bash
git commit -m "$(cat <<'EOF'
feat(fit): 添加用户认证功能

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## 规则 2: 禁止自动提交代码（🔴 关键）

**核心原则**: **绝对不要**自动执行 `git commit` 或 `git add`，除非用户明确使用 `/commit` 命令

**完成代码修改后的正确做法**:
- ✅ 告知用户修改了哪些文件
- ✅ 提醒用户使用 `/commit` 提交
- ❌ 不要自动执行 git 命令

**例外情况**: 仅当用户明确说"修复后直接提交"或使用 `/commit` 命令时才提交

---

## 规则 3: PR 提交规范（🟡 重要）

**基本要求**：
- 所有测试通过（`mvn clean install`）
- 代码已格式化
- 公共 API 有 Javadoc
- 版权头年份已更新

**Claude 执行流程**:

1. **读取 PR 模板**：`.github/PULL_REQUEST_TEMPLATE.md`
2. **分析所有变更**：`git diff [base-branch]...HEAD`（查看分支分叉后的所有提交）
3. **检查远程分支**：确认是否需要推送
4. **创建 PR**：使用 `gh pr create` + HEREDOC 格式
5. **添加生成标记**：`🤖 Generated with [Claude Code](https://claude.com/claude-code)`

**PR 创建命令示例**:
```bash
gh pr create --title "feat(fit): 添加用户认证功能" --body "$(cat <<'EOF'
## 变更说明
- 添加用户认证模块
- 实现 JWT Token 验证

## 测试计划
- [ ] 单元测试通过
- [ ] 集成测试通过

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

---

## 规则 4: 版权年份更新（🔴 关键）

**触发条件**：修改任意带版权头的文件时

**Claude 自动化执行步骤**:

```bash
# 1. 动态获取当前年份（不要硬编码）
CURRENT_YEAR=$(date +%Y)

# 2. 使用 Edit 工具更新版权头（假设当前年份为 2026）
#    将 "Copyright (C) 2024-2025" → "Copyright (C) 2024-2026"
#    将 "Copyright (C) 2024" → "Copyright (C) 2024-2026"

# 3. 只更新当前修改的文件，不批量更新所有文件
```

**关键原则**:
- ✅ 修改文件时自动触发更新
- ✅ 使用 `date +%Y` 动态获取年份
- ✅ 使用 Edit 工具精确替换
- ❌ 绝对不要硬编码年份
- ❌ 不要批量更新未修改的文件

---

## 规则 5: 任务语义识别（🟡 重要）

**核心原则**: 识别自然语言意图，自动查找并执行相关任务

**语义映射**:
- "分析 issue XXX" → `/analyze-issue`
- "设计方案/plan" → `/plan-task`
- "实施/实现" → `/implement-task`
- "查看状态/进度" → `/task-status`
- "同步进度/更新issue" → `/sync-issue`

**执行流程**: 识别意图 → 查找任务(.ai-workspace) → 执行工作流 → 更新状态 → 告知用户

**查找优先级**:
1. 按任务ID查找 (TASK-xxx)
2. 按Issue号查找 (#207)
3. 按关键词搜索

---

## 规则 6: 任务状态管理规范（🔴 关键）

**核心原则**: 执行命令后**必须立即更新任务状态**

**强制更新映射**:

| 命令                | 必须更新字段                                      |
|-------------------|---------------------------------------------|
| `/analyze-issue`  | `current_step`, `updated_at`, `assigned_to` |
| `/plan-task`      | `current_step`, `updated_at`                |
| `/implement-task` | `current_step`, `updated_at`                |
| `/review-task`    | `current_step`, `updated_at`                |
| `/complete-task`  | `status`, `completed_at`, `updated_at`      |
| `/block-task`     | `status`, `blocked_at`, `blocked_reason`    |

**状态字段**:
```yaml
status: active | blocked | completed
current_step: {step-id}
updated_at: {yyyy-MM-dd HH:mm:ss}  # 必须更新为当前时间
assigned_to: {ai-name}
```

**工作流进度标记**:
```markdown
- [x] requirement-analysis (claude, 2026-01-03)
- [x] technical-design (进行中)
- [ ] implementation (待执行)
```

**检查清单**:
- [ ] 更新 `current_step`
- [ ] 更新 `updated_at` 为当前时间
- [ ] 更新 `assigned_to`
- [ ] 标记工作流进度
- [ ] 任务完成时归档到 `completed`

---

## 项目信息

**基本信息**:
- 项目: FIT Framework
- 主分支: main
- 开发分支: 3.5.x, 3.6.x
- 仓库: https://github.com/ModelEngine-Group/fit-framework

**常用命令**:
```bash
mvn clean install     # 编译项目
mvn test              # 运行测试
./build/bin/fit start # 启动应用
```

**Claude Code 配置架构**:
```
.claude/CLAUDE.md (快速参考 - Claude Code 启动时加载)
    ↓
.claude/project-rules.md (详细规则 - 本文件)
    ↓
.claude/commands/ (Slash Commands 实现)
```

---

## 自定义命令

详细的命令配置位于 `.claude/commands/` 目录，包括:

**PR 相关**: `/pr [branch]`, `/pr-update <pr-number>`

**任务管理**: `/analyze-issue`, `/plan-task`, `/implement-task`, `/review-task`, `/complete-task`, `/block-task`, `/task-status`, `/sync-issue`

**开发工具**: `/commit [message]`, `/upgrade-dep`, `/test`, `/integration-test`

**参数传递**: 使用空格分隔，包含空格时用引号包裹
- 单参数: `/pr main`
- 多参数: `/upgrade-dep swagger-ui 5.30.0 5.30.2`
- 带空格: `/commit "feat: new feature"`

---

## 最佳实践

**Git 操作**:
- 绝对不要自动提交代码
- 提交前检查 `.github/` 规范
- 遵循中文 commit message 格式

**文档更新**:
- 修改代码时同步更新文档
- 确保 README 准确性

**任务协作**:
- 优先使用 Slash Commands
- 自然语言作为补充
- 严格遵循工作流定义
- 及时更新任务状态
