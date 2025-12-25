# 仓库指南

## 项目结构与模块划分

本仓库包含 FIT 框架及相关引擎。
- `framework/` 为核心实现目录（`framework/fit/java`、`framework/fit/python`、`framework/waterflow`、`framework/fel`）。
- `docs/` 存放用户指南、快速入门与白皮书。
- `examples/` 包含 FIT/FEL 可运行示例。
- `docker/` 提供容器构建资源。
- `build/` 为 Maven 构建产物输出目录（自动生成）。

## 构建、测试与开发命令

- `mvn clean install`（仓库根目录）：构建全部模块并运行测试，产物输出到 `build/`。
- `cd framework/fit/java && mvn clean install`：仅构建 Java FIT 框架。
- `./build/bin/fit start`：启动 Java 运行时（依赖 Node.js）；默认端口 `8080`。
- `./.claude/run-test.sh`：完整验证流程（构建、启动、健康检查）。

## 编码风格与命名规范

- Java 格式化使用 IntelliJ 配置 `CodeFormatterFromIdea.xml`。
- 公共/受保护的 Java API 需要 Javadoc，并包含 `@param`/`@return`。
- 类头需包含 `@author` 与 `@since yyyy-MM-dd`。
- 分支命名使用模块前缀与意图，例如 `fit-feature-xxx`、`waterflow-bugfix-yyy`。

## 测试规范

- Java 测试通过 Maven（Surefire）执行，`mvn clean install` 为基线命令。
- 迭代时可在模块目录运行定向测试。
- 测试与源码同模块放置，命名优先使用 `*Test`。

## 提交与 PR 规范

- 提交信息以模块标签开头，中文简述，例如 `[fit] 修复某问题`。
- 标题建议 20 字左右，必要时在正文补充细节。
- PR 基于正确的模块/版本分支，通常仅包含一次提交。

## 安全与配置提示

- 安全问题请勿在公共 Issue 报告，按 `SECURITY.md` 指引私下提交。
- 请启用 git hooks 进行编码检查（`git-hooks/check-utf8-encoding.sh`）。
