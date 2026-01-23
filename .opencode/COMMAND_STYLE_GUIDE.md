# OpenCode 命令编写规范

本文档定义了 `.opencode/commands/` 目录下自定义命令的编写规范，确保命令清晰、可靠、易于维护。

---

## 一、执行方式选择

### ✅ 使用 `!` 符号的场景（强制）

以下场景**必须**使用 `!` 符号标记可执行命令：

#### 1. 所有 Shell 命令
```markdown
!`ls -la`
!`mkdir -p .ai-workspace/active`
!`grep -r "pattern" .`
```

#### 2. 所有 Git 操作
```markdown
!`git status`
!`git diff`
!`git add .`
!`git commit -m "message"`
```

#### 3. 所有 GitHub CLI 调用
```markdown
!`gh issue view $1 --json number,title,body`
!`gh pr list --limit 3 --state merged`
!`gh api "repos/{owner}/{repo}/dependabot/alerts/$1"`
```

#### 4. 获取动态值
```markdown
# ✅ 正确 - 获取当前年份
!`date +%Y`

# ✅ 正确 - 获取当前时间戳
!`date '+%Y-%m-%d %H:%M:%S'`

# ❌ 错误 - 不要硬编码
使用 2026 作为年份  # 会过时
```

#### 5. 文件系统操作
```markdown
!`mkdir -p .ai-workspace/completed`
!`mv .ai-workspace/active/$1 .ai-workspace/completed/`
!`test -f .ai-workspace/active/$1/task.md && echo "✅ 存在" || echo "❌ 不存在"`
```

#### 6. 构建和测试命令
```markdown
!`mvn clean install`
!`npm test`
!`./gradlew build`
```

---

### ✅ 使用 ````bash```` 代码块的场景

**仅用于**展示示例代码（不执行）：

````markdown
提交消息格式示例:

```bash
git commit -m "feat(fit): 添加新功能"
git commit -m "fix(waterflow): 修复Bug"
```

**实际执行时使用**:
!`git commit -m "<实际的提交消息>"`
````

---

### ✅ 使用自然语言的场景

适用于以下情况：

#### 1. 解释性说明
```markdown
分析变更并生成提交消息:
- 确定变更类型(新功能/增强/Bug修复/重构等)
- 生成符合 Conventional Commits 规范的提交消息
- 提交消息格式: `<type>(<scope>): <subject>`
```

#### 2. 指导 AI 使用工具
```markdown
使用 Read 工具读取文件内容
使用 Edit 工具更新 task.md 的 YAML front matter:
- status: completed
- updated_at: <使用步骤3获取的时间>
```

#### 3. 条件判断和逻辑说明
```markdown
如果文件包含版权头且年份过期,使用 Edit 工具更新:
- 更新格式: "Copyright (C) 2024-2025" → "Copyright (C) 2024-2026"
- **绝对不要**硬编码年份
```

---

## 二、错误处理规范

### 1. 命令执行验证

所有关键命令都应该添加执行状态检查：

```markdown
# ❌ 不好 - 无错误检查
!`git status`

# ✅ 好 - 基本错误检查
!`git status || echo "ERROR: Git status failed"`

# ✅ 更好 - 详细错误提示
!`git status && echo "✅ Git status OK" || echo "❌ ERROR: Git status failed, is this a git repository?"`
```

### 2. 文件/目录存在性验证

```markdown
# 验证文件存在
!`test -f .ai-workspace/active/$1/task.md && echo "✅ 任务存在" || echo "❌ ERROR: 任务不存在"`

# 验证目录存在
!`test -d .ai-workspace/active/$1 && echo "✅ 目录存在" || echo "❌ ERROR: 目录不存在"`

# 验证移动操作成功
!`test ! -d .ai-workspace/active/$1 && echo "✅ 已移除 active 目录" || echo "❌ ERROR: active 目录仍存在"`
```

### 3. 搜索命令容错

```markdown
# ✅ 好 - 提供友好的未找到提示
!`grep -r "$1" --include="pom.xml" . || echo "⚠️  未找到包含 $1 的文件"`

# ✅ 好 - 抑制错误输出
!`grep -r "security_alert_number: $1" .ai-workspace/ 2>/dev/null || echo "⚠️  无关联任务"`
```

### 4. API 调用错误处理

```markdown
# ✅ 好 - 捕获错误输出
!`gh issue view $1 --json number,title,body 2>&1`

# ✅ 好 - 链式错误处理
!`gh api "repos/{owner}/{repo}/dependabot/alerts/$1" && echo "✅ 获取成功" || echo "❌ ERROR: 获取失败"`
```

---

## 三、参数验证规范

在命令开始时验证必需参数：

```markdown
执行以下步骤:

0. 参数验证:
   !`test -n "$1" || (echo "❌ ERROR: 任务ID不能为空，用法: /command <task-id>" && exit 1)`
   !`echo "任务ID: $1"`

1. 查找任务文件:
   ...
```

---

## 四、时间戳处理规范

### 1. 获取时间戳

始终通过命令获取，不要硬编码：

```markdown
# ✅ 正确方式
1. 获取当前时间:
   !`date '+%Y-%m-%d %H:%M:%S'`

2. 更新任务状态:
   使用 Edit 工具更新 task.md:
   - updated_at: <使用步骤1获取的时间>
```

### 2. ❌ 错误示例

```markdown
# ❌ 错误 - YAML 代码块中的 ! 不会被执行
```yaml
updated_at: !`date '+%Y-%m-%d %H:%M:%S'`
```

# ❌ 错误 - 硬编码时间
updated_at: 2026-01-23 10:30:00
```

---

## 五、GitHub API 路径规范

### 使用占位符而非硬编码

```markdown
# ✅ 推荐 - 使用 gh cli 的占位符
!`gh api "repos/{owner}/{repo}/dependabot/alerts/$1"`

# ✅ 可选 - 动态获取仓库信息
!`gh repo view --json owner,name -q '.owner.login + "/" + .name'`

# ❌ 避免 - 硬编码仓库路径
!`gh api repos/username/repo-name/dependabot/alerts/$1`
```

---

## 六、命令可读性规范

### 1. 使用 Emoji 增强可读性

```markdown
!`test -f file.txt && echo "✅ 文件存在" || echo "❌ 文件不存在"`
!`echo "⚠️  警告: 这是一个危险操作"`
!`echo "📋 任务列表"`
```

### 2. 长命令拆分

```markdown
# ❌ 不好 - 难以阅读
!`gh api --method PATCH "repos/{owner}/{repo}/dependabot/alerts/$1" -f state=dismissed -f dismissed_reason="tolerable_risk" -f dismissed_comment="已评估风险可接受"`

# ✅ 好 - 分步骤说明
执行关闭操作:
- 使用 PATCH 方法更新告警状态
- 设置 state=dismissed
- 设置 dismissed_reason 和 dismissed_comment

!`gh api --method PATCH "repos/{owner}/{repo}/dependabot/alerts/$1" -f state=dismissed -f dismissed_reason="<API参数>" -f dismissed_comment="<用户的详细说明>" && echo "✅ 告警已关闭" || echo "❌ ERROR: 关闭失败"`
```

### 3. 复杂逻辑抽取为脚本

```markdown
# ❌ 不推荐 - 复杂逻辑内嵌在命令中
!`for file in $(git diff --cached --name-only); do grep -l "Copyright" "$file" && ...; done`

# ✅ 推荐 - 抽取为独立脚本
!`./.ai-agents/scripts/update-copyright.sh`
```

---

## 七、步骤编号规范

步骤编号应该连续且清晰：

```markdown
执行以下步骤:

1. 获取当前时间:
   !`date '+%Y-%m-%d %H:%M:%S'`

2. 验证任务存在:
   !`test -f .ai-workspace/active/$1/task.md && echo "✅ 任务存在" || echo "❌ ERROR: 任务不存在"`

3. 更新任务状态:
   使用 Edit 工具更新 task.md...

4. 告知用户:
   输出任务完成信息...
```

---

## 八、常见模式示例

### 模式 1: 查找并验证文件

```markdown
1. 查找任务文件:
   !`if [ -d .ai-workspace/active/$1 ]; then echo "active"; elif [ -d .ai-workspace/completed/$1 ]; then echo "completed"; elif [ -d .ai-workspace/blocked/$1 ]; then echo "blocked"; else echo "not_found"; fi`
   
   根据上面的结果确定任务位置,然后读取 task.md
```

### 模式 2: 条件执行

```markdown
1. 尝试获取 Issue 信息:
   !`gh issue view $1 --json number,title,body 2>&1`
   
2. 如果上面返回错误(不是 Issue),尝试获取 PR 信息:
   !`gh pr view $1 --json number,title,body 2>&1`
   
3. 如果两者都失败:
   !`echo "❌ ERROR: #$1 不是有效的 Issue 或 PR 编号"`
```

### 模式 3: 构建验证流程

```markdown
1. 查看变更:
   !`git diff`

2. 验证变更文件数量:
   !`git diff --name-only | wc -l`

3. 编译验证:
   !`mvn clean package -Dmaven.test.skip=true 2>&1 | tee /tmp/build.log && echo "✅ 编译成功" || echo "❌ 编译失败"`

4. 运行测试:
   !`mvn test 2>&1 | tee /tmp/test.log && echo "✅ 测试通过" || echo "❌ 测试失败"`
```

---

## 九、不要做的事情

### ❌ 1. 不要在 YAML/Markdown 代码块中使用 `!`

```markdown
# ❌ 错误 - 代码块中的 ! 不会被执行
```yaml
updated_at: !`date '+%Y-%m-%d %H:%M:%S'`
```

# ✅ 正确 - 先获取时间,再在说明中引用
1. 获取当前时间:
   !`date '+%Y-%m-%d %H:%M:%S'`

2. 更新 YAML 字段:
   使用 Edit 工具将 updated_at 设置为步骤1获取的时间
```

### ❌ 2. 不要硬编码动态值

```markdown
# ❌ 错误
当前年份是 2026

# ✅ 正确
!`date +%Y`
```

### ❌ 3. 不要忽略错误处理

```markdown
# ❌ 错误 - 静默失败
!`mv file1 file2`

# ✅ 正确 - 明确反馈
!`mv file1 file2 && echo "✅ 移动成功" || echo "❌ 移动失败"`
```

### ❌ 4. 不要在执行意图不明的地方使用 ````bash```` 代码块

```markdown
# ❌ 模糊 - 这是示例还是要执行？
```bash
git status
git add .
```

# ✅ 清晰 - 明确标记执行
!`git status`
!`git add .`
```

---

## 十、检查清单

在编写或审查命令时,使用此清单：

- [ ] 所有可执行命令都使用 `!` 符号标记
- [ ] 所有关键命令都有错误处理
- [ ] 动态值通过命令获取,不硬编码
- [ ] 参数已验证
- [ ] 文件/目录操作前已验证存在性
- [ ] 步骤编号连续清晰
- [ ] 长命令有适当的拆分和说明
- [ ] 使用 Emoji 增强可读性
- [ ] API 路径使用占位符而非硬编码
- [ ] ````bash```` 代码块仅用于示例展示

---

## 十一、版本历史

- **2026-01-23**: 初始版本，基于 17 个命令的优化经验总结

---

**维护者**: OpenCode Team  
**最后更新**: 2026-01-23
