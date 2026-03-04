# FIT Framework - Claude Code 项目指令

本仓库包含 FIT 框架及相关引擎（`framework/fit/java`、`framework/fit/python`、`framework/waterflow`、`framework/fel`）。

## 快速命令

```bash
# 构建全部模块（包含测试）
mvn clean install

# 仅构建 Java FIT 框架
cd framework/fit/java && mvn clean install

# 启动 FIT 运行时（依赖 Node.js，默认端口 8080）
./build/bin/fit start

# 完整验证流程（构建 + 启动 + 健康检查）
./.agents/scripts/run-test.sh
```

## 编码规范

### Java 代码风格
- 使用 IntelliJ 配置 `CodeFormatterFromIdea.xml` 格式化代码
- 公共/受保护的 API 必须有 Javadoc，包含 `@param`/`@return`
- 类头必须包含 `@author` 和 `@since yyyy-MM-dd`

### 版权头更新
修改任意带版权头的文件时，必须更新版权年份：
1. 先运行 `date +%Y` 获取当前年份（不要硬编码）
2. 更新格式示例（假设当前年份为 2026）：
   - `2024-2025` → `2024-2026`
   - `2024` → `2024-2026`

### 分支命名
使用模块前缀：`fit-feature-xxx`、`waterflow-bugfix-yyy`

## 测试要求

- 基线命令：`mvn clean install`（自动运行所有测试）
- 测试命名：`*Test.java`
- 测试文件与源码同模块放置
- 模块级测试：在模块目录下运行 `mvn test`

## 提交与 PR 规范

### 提交信息格式（Conventional Commits）
```
<type>(<scope>): <subject>

示例：
feat(fit): 添加新的数据验证功能
fix(waterflow): 修复空指针异常
docs(fel): 更新表达式语言文档
```

- **type**: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`
- **scope**: `fit`, `waterflow`, `fel`（可省略）
- **subject**: 中文，20 字以内

### Claude 提交时必须添加
```
🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### PR 检查清单
提交 PR 前必须确保：
- [ ] 所有测试通过（`mvn clean install`）
- [ ] 代码已格式化
- [ ] 公共 API 有 Javadoc
- [ ] 版权头年份已更新

## Claude 特定规则

### 🔴 关键规则
1. **禁止自动提交**：绝对不要自动执行 `git commit`/`git add`，完成代码后提醒用户使用 `/commit` 命令
2. **版权年份更新**：修改带版权头的文件时，运行 `date +%Y` 获取当前年份，使用 Edit 工具更新
3. **任务状态管理**：执行 Slash Command 后必须更新 `task.md` 的 `current_step`、`updated_at`、`assigned_to` 字段

### 🟡 重要规则
4. **任务语义识别**：自动识别用户意图（如"分析 issue 207" → `/analyze-issue 207`）
5. **PR 规范**：创建 PR 时添加生成标记：`🤖 Generated with [Claude Code]`

**详细规则**：`.claude/project-rules.md`

## 工具使用偏好

| 操作   | 推荐工具    | 不推荐                  |
|------|---------|----------------------|
| 文件搜索 | `Glob`  | `find`、`ls`          |
| 内容搜索 | `Grep`  | `grep`、`rg`          |
| 读取文件 | `Read`  | `cat`、`head`、`tail`  |
| 编辑文件 | `Edit`  | `sed`、`awk`          |
| 创建文件 | `Write` | `echo >`、`cat <<EOF` |

**Bash 工具仅用于**：Git 操作、Maven 构建、获取系统信息、启动服务

## 常用 Slash Commands

### 开发相关
```bash
/commit [message]           # 提交代码
/upgrade-dep <name> <old> <new>  # 升级依赖
/test                       # 运行完整测试
/test-integration           # 运行集成测试
```

### 任务管理
```bash
/create-task <description>  # 从自然语言描述创建任务
/analyze-issue <number>     # 分析 GitHub Issue
/plan-task <task-id>        # 设计技术方案
/implement-task <task-id>   # 实施任务
/review-task <task-id>      # 审查代码
/complete-task <task-id>    # 完成任务
/check-task <task-id>       # 查看状态
```

### PR 和同步
```bash
/pr [branch]                # 创建 PR
/sync-issue <number>        # 同步进度到 Issue
```

**完整命令列表**：`.claude/commands/`

## 安全注意事项

- ❌ 不要提交敏感文件：`.env`, `credentials.json`, 密钥等
- ⚠️ 安全问题请按 `SECURITY.md` 指引私下提交（不要公开 Issue）

## 多 AI 协作支持

本项目支持 Claude、ChatGPT、Gemini、Cursor 等多个 AI 工具协同工作。

**协作配置目录**：
- `.agents/` - AI 配置和工作流定义（版本控制）
- `.ai-workspace/` - 协作工作区（临时文件，已被 git ignore）

**详细协作指南**：`.agents/README.md`

---

**配置文件**：
- 详细规则：`.claude/project-rules.md`
- 命令配置：`.claude/commands/`
- 快速参考：`.claude/QUICK-REFERENCE.md`
