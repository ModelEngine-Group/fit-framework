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

## 通用最佳实践

### 文件操作
- 创建文件后立即检查并修改权限
- 使用 `ls -l` 验证文件所有者
- 批量操作后统一修改权限

### Git 操作
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
