# FIT Framework - Claude Code 配置

本目录包含 FIT Framework 项目的 Claude Code 配置文件。

## 📁 目录结构

```
.claude/
├── README.md              # 本文件
├── project-rules.md       # 项目规则和规范（Claude 会自动遵循）
└── commands/              # 自定义 Slash Commands
    ├── pr.md              # 创建 PR
    ├── pr-update.md       # 更新 PR 描述
    ├── commit.md          # 提交变更
    ├── upgrade-dep.md     # 升级依赖
    ├── review.md          # 审查 PR
    └── fix-permissions.md # 检查和修复文件权限
```

## 📋 项目规则

`project-rules.md` 文件定义了 Claude 在处理本项目时必须遵循的规则：

### 规则 1: 文件权限管理
- 所有新建/修改的文件必须设置正确的所有者权限
- 动态从 `README.md` 等参考文件获取权限信息
- README.md 是通用文件，适用于所有语言的项目
- 避免硬编码用户名和用户组

### 规则 2: PR 提交规范
- **必须**先读取 `.github/pull_request_template.md`
- **必须**查看最近 3 个 merged PR 作为参考
- PR 描述必须完整填写所有必填项
- 结尾添加 Claude Code 签名

## 🚀 Slash Commands 使用指南

### 创建 Pull Request

```bash
# 创建 PR 到默认分支（3.6.x）
/pr

# 创建 PR 到 main 分支
/pr main

# 创建 PR 到指定分支
/pr develop
```

**工作流程：**
1. 自动读取 PR 模板
2. 查看最近的 PR 示例
3. 分析当前分支的变更
4. 按模板格式创建 PR

### 更新现有 PR

```bash
# 更新 PR #369 的描述
/pr-update 369
```

### 提交变更

```bash
# 交互式提交（会询问提交信息）
/commit

# 使用指定消息提交
/commit "feat: add new feature"
```

### 升级依赖

```bash
# 升级 swagger-ui 从 5.30.0 到 5.30.2
/upgrade-dep swagger-ui 5.30.0 5.30.2
```

**自动化流程：**
1. 创建功能分支
2. 更新依赖文件
3. 运行编译测试
4. 提交变更
5. 创建 PR

### 审查 PR

```bash
# 审查 PR #369
/review 369
```

**审查内容：**
- PR 标题和描述是否符合规范
- 代码变更的合理性
- 安全问题检查
- 测试覆盖情况
- 生成审查报告

### 检查和修复权限

```bash
# 检查并修复所有文件和目录权限
/fix-permissions
```

**自动化流程：**
1. 从 README.md 获取正确的所有者信息
2. 扫描所有文件和目录
3. 识别权限不一致的文件
4. 自动修复为正确的所有者
5. 生成检查报告

## 🎯 最佳实践

### 1. 使用 Slash Commands
优先使用自定义命令，而不是手动描述任务：
- ✅ `/pr 3.5.x`
- ❌ "请帮我创建一个PR到3.5.x分支"

### 2. PR 创建流程
始终使用 `/pr` 命令，确保：
- 自动遵循项目规范
- 完整填写 PR 模板
- 参考最近的 PR 格式

### 3. 依赖升级
使用 `/upgrade-dep` 命令，自动化整个流程：
- 创建分支
- 更新文件
- 测试验证
- 提交并创建 PR

## 🔧 参数传递规则

Slash commands 支持参数，参数规则：

1. **单个参数**
   ```bash
   /pr main
   ```
   - `main` 是目标分支参数

2. **多个参数**
   ```bash
   /upgrade-dep swagger-ui 5.30.0 5.30.2
   ```
   - `swagger-ui` - 包名
   - `5.30.0` - 原版本
   - `5.30.2` - 新版本

3. **带空格的参数**
   ```bash
   /commit "feat: add new feature"
   ```
   - 使用引号包裹

## 📝 项目信息

- **项目名称**: FIT Framework
- **主分支**: main
- **开发分支**: 3.5.x
- **仓库**: https://github.com/ModelEngine-Group/fit-framework

## 🔗 相关文件

- **PR 模板**: `.github/pull_request_template.md`
- **贡献指南**: `CONTRIBUTING.md`
- **项目规则**: `.claude/project-rules.md`

## ⚙️ 常用命令

```bash
# 编译项目
mvn clean install

# 快速编译（跳过测试）
mvn -B clean package -Dmaven.test.skip=true

# 运行测试
mvn test

# 启动应用
./fit start

# 查看 Git 状态
git status

# 查看 PR 列表
gh pr list

# 查看某个 PR
gh pr view 369
```

## 🆘 故障排除

### 问题：Claude 没有遵循 PR 模板
**解决方案**：使用 `/pr` 命令而不是手动描述

### 问题：PR 描述格式不对
**解决方案**：使用 `/pr-update <pr-number>` 命令更新

### 问题：文件权限错误
**解决方案**：使用 `/fix-permissions` 命令自动检查和修复

### 问题：想要自定义命令
**解决方案**：在 `.claude/commands/` 目录创建新的 `.md` 文件

## 📚 扩展阅读

- [Claude Code 文档](https://docs.claude.com/claude-code)
- [Slash Commands 指南](https://docs.claude.com/claude-code/slash-commands)
- [项目配置最佳实践](https://docs.claude.com/claude-code/project-setup)

---

**最后更新**: 2025-11-10
**配置版本**: 1.0.0
