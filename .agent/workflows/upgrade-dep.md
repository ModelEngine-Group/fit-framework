---
description: 升级依赖并创建 Pull Request
---

1. 解析输入参数。
   - 包名 (例如 `swagger-ui`)
   - 原版本 (例如 `5.30.0`)
   - 新版本 (例如 `5.30.2`)

2. 创建新的功能分支。
   - `run_command("git checkout -b fit-enhancement-<package>-<to-version>")`

3. 更新依赖文件。
   - 搜索旧版本字符串：`grep_search(query="<from-version>", path=".")`
   - 使用 `replace_file_content` 更新 `pom.xml`, `package.json` 或其他相关文件。

4. 验证变更。
   - `run_command("git diff")`
   - `run_command("mvn clean package -Dmaven.test.skip=true")` (或适当的构建命令)

5. 提交变更。
   - `run_command("git add .")`
   - 提交消息：
     ```
     Upgrade <package> from v<from-version> to v<to-version>

     🤖 Generated with Antigravity

     Co-Authored-By: Antigravity <noreply@google.com>
     ```

6. 推送并创建 Pull Request。
   - `run_command("git push -u origin HEAD")`
   - 触发 `pr` 工作流（或按照 `pr.md` 中的逻辑手动运行 `gh pr create`）。
