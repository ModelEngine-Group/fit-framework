---
description: 升级依赖
---

1. 解析输入参数。
   - 包名 (例如 `swagger-ui`)
   - 原版本 (例如 `5.30.0`)
   - 新版本 (例如 `5.30.2`)

2. 更新依赖文件。
   - 搜索旧版本字符串：`grep_search(query="<from-version>", path=".")`
   - 使用 `replace_file_content` 更新 `pom.xml`, `package.json` 或其他相关文件。

3. 验证变更。
   - `run_command("git diff")`
   - `run_command("mvn clean package -Dmaven.test.skip=true")` (或适当的构建命令)
