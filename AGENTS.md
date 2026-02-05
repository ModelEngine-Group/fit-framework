# FIT Framework - AI 开发指南

本仓库包含 FIT 框架及相关引擎（`framework/fit/java`、`framework/fit/python`、`framework/waterflow`、`framework/fel`）。

## 快速开始命令

```bash
# 构建全部模块（包含测试）
mvn clean install

# 仅构建 Java FIT 框架
cd framework/fit/java && mvn clean install

# 启动 FIT 运行时（依赖 Node.js，默认端口 8080）
./build/bin/fit start

# 完整验证流程（构建 + 启动 + 健康检查）
./.ai-agents/scripts/run-test.sh
```

## 编码规范（必须遵守）

### Java 代码风格
- 使用 IntelliJ 配置 `CodeFormatterFromIdea.xml` 格式化代码
- 公共/受保护的 API 必须有 Javadoc，包含 `@param`/`@return`
- 类头必须包含 `@author` 和 `@since yyyy-MM-dd`

### 版权头更新规则
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

### PR 检查清单
提交 PR 前必须确保：
- [ ] 所有测试通过（`mvn clean install`）
- [ ] 代码已格式化
- [ ] 公共 API 有 Javadoc
- [ ] 版权头年份已更新

## 安全注意事项

- ❌ 不要提交敏感文件：`.env`, `credentials.json`, 密钥等
- ⚠️ 安全问题请按 `SECURITY.md` 指引私下提交（不要公开 Issue）
- 启用 git hooks：`git-hooks/check-utf8-encoding.sh`

## 多 AI 协作支持

本项目支持 Claude、ChatGPT、Gemini、Cursor 等多个 AI 工具协同工作。

**协作配置目录**：
- `.ai-agents/` - AI 配置和工作流定义（版本控制）
- `.ai-workspace/` - 协作工作区（临时文件，已被 git ignore）

**详细协作指南**：`.ai-agents/README.md`

**语言规范**：
- AI 回复语言与用户输入保持一致（中文问→中文答，英文问→英文答）
- 项目文档默认语言：中文
- 代码注释默认语言：中文

---

**基于标准**: [AGENTS.md](https://agents.md) (Linux Foundation AAIF)
