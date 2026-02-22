---
description: 执行版本发布流程（SNAPSHOT 替换、Release commit、Tag、发布分支、下一 SNAPSHOT）
agent: general
subtask: false
---

执行版本发布流程。

**不含构建验证**：仅做版本号替换和 Git 操作。
**不含自动推送**：仅做本地操作，推送由用户手动完成。

执行以下步骤:

**步骤 1: 解析并验证参数**

版本号来自用户输入参数，格式必须为 `X.Y.Z`（X、Y、Z 均为非负整数）。

如果格式不正确，报错退出：`❌ 错误：版本号格式不正确，期望格式为 X.Y.Z（例如 3.6.3）`

解析版本组件:
- SNAPSHOT 版本 = `X.Y.Z-SNAPSHOT`
- 发布版本 = `X.Y.Z`
- 下一 SNAPSHOT 版本 = `X.Y.(Z+1)-SNAPSHOT`

确认 SNAPSHOT 存在:
!`grep -r "X.Y.Z-SNAPSHOT" --include="pom.xml" --include="*.md" --include="*.java" --include="*.js" --include="package.json" . 2>/dev/null | head -20 || echo "❌ ERROR: 未找到 X.Y.Z-SNAPSHOT，请确认当前代码版本是否正确"`

（将上面的 X.Y.Z 替换为实际版本号执行）

如果没有找到任何匹配，报错退出。如果找到匹配，列出所有包含 SNAPSHOT 的文件，告知用户即将替换的范围。

**步骤 2: 确认工作区状态**

!`git status --short`

- 如果有未提交的更改，**报错退出**：`❌ 错误：工作区有未提交的更改，请先提交或暂存（git stash）后再执行发布`
- 工作区必须是干净的才能继续

**步骤 3: 全局替换 SNAPSHOT → Release**

使用 Grep 工具搜索所有包含 `X.Y.Z-SNAPSHOT` 的文件，对于每个匹配的文件，使用 Edit 工具将 `X.Y.Z-SNAPSHOT` 替换为 `X.Y.Z`（replace_all=true）。

覆盖的文件范围（由全局搜索决定，包括但不限于）:
- `**/pom.xml` — Maven 版本号和 `<fit.version>` 属性
- `README.md` — 版本标题
- `docs/**/*.md` — 文档中的版本引用
- `*.java` — 源码中硬编码的版本字符串
- `*.js` — fit.js 中的 VERSION 常量
- `package.json` — Node.js 版本号

**记录所有替换过的文件列表**，步骤 8 需要用到。

替换后验证，再次搜索 `X.Y.Z-SNAPSHOT`，确认没有残留。如果仍有残留，继续替换直到全部清除。

**步骤 4: 创建 Release commit**

!`git add -A && git commit -m "Release X.Y.Z"`

**步骤 5: 创建轻量标签**

!`git tag vX.Y.Z`

**步骤 6: 创建发布分支**

!`git branch release-X.Y.Z`

**步骤 7: 回退当前分支到 Release commit 之前**

Release commit 和 tag 已经通过 `release-X.Y.Z` 分支保留，现在需要将当前开发分支回退到 Release commit 之前的状态，使下一个 SNAPSHOT commit 与 Release commit 成为**兄弟关系**（共享同一个父节点），而非父子关系。

!`git reset --hard HEAD~1 && echo "✅ 已回退到 Release commit 之前" || echo "❌ ERROR: 回退失败"`

执行后，当前分支 HEAD 回到 Release commit 的父节点，而 `release-X.Y.Z` 分支和 `vX.Y.Z` tag 仍然指向 Release commit。

**步骤 8: 全局替换当前 SNAPSHOT → 下一 SNAPSHOT**

由于步骤 7 已将工作区回退到 Release commit 之前，文件中的版本号已恢复为 `X.Y.Z-SNAPSHOT`。

**重要**: 只替换步骤 3 中已知的文件列表，避免误匹配。

对于步骤 3 中每个替换过的文件，使用 Edit 工具将 `X.Y.Z-SNAPSHOT` 替换为 `X.Y.(Z+1)-SNAPSHOT`（replace_all=true）。

替换后验证: 使用 Read 工具抽查几个关键文件（如根 pom.xml），确认版本号已正确更新为 `X.Y.(Z+1)-SNAPSHOT`。

**步骤 9: 创建 SNAPSHOT commit**

!`git add -A && git commit -m "Prepare the next SNAPSHOT version"`

**步骤 10: 输出总结**

输出发布操作的完整总结:

```
✅ 版本发布完成

**发布信息**:
- 发布版本: X.Y.Z
- 下一开发版本: X.Y.(Z+1)-SNAPSHOT
- Release commit: <commit-hash>
- Tag: vX.Y.Z
- 发布分支: release-X.Y.Z
- SNAPSHOT commit: <commit-hash>

**替换文件数**: N 个文件

**后续手动操作**:

1. 推送 Release 分支和 Tag：
   git push origin release-X.Y.Z
   git push origin vX.Y.Z

2. 推送当前开发分支（下一 SNAPSHOT）：
   git push origin <current-branch>

3. （可选）在 GitHub 上基于 vX.Y.Z 标签创建 Release
```

**错误处理**:
- 版本号格式错误 → 提示正确格式并退出
- SNAPSHOT 不存在 → 提示确认当前代码版本并退出
- 工作区不干净 → 提示先提交或暂存更改并退出
- Git 操作失败 → 显示错误信息，提示用户手动处理

**回滚方式**（如出错）:

```bash
# 删除标签
git tag -d vX.Y.Z

# 删除发布分支
git branch -d release-X.Y.Z

# 回退 commit（回退最近的 1 或 2 个 commit）
git reset --soft HEAD~2

# 恢复工作区
git checkout -- .
```

**相关命令**:
- `/commit` - 提交代码
- `/test` - 运行测试
- `/create-pr` - 创建 Pull Request
