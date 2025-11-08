# FIT Framework 项目规则

## 文件权限管理规则

### 规则 1: 新增和修改文件的所有权

**重要：** 所有新增或修改的文件必须设置正确的所有者权限，确保用户可以自主修改。

**原则：** 不要硬编码用户名和用户组，而是从项目中已有文件动态获取权限信息。

**动态获取权限的方法：**

```bash
# 方法1: 从项目根目录的 pom.xml 获取所有者
REF_FILE="pom.xml"  # 或其他稳定存在的文件
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
OWNER_GROUP=$(ls -l pom.xml | awk '{print $3":"$4}')

# 2. 应用到新文件
sudo chown $OWNER_GROUP <file_path>

# 或者分步执行
OWNER=$(ls -l pom.xml | awk '{print $3}')
GROUP=$(ls -l pom.xml | awk '{print $4}')
sudo chown $OWNER:$GROUP <file_path>
```

**检查清单：**
- [ ] 使用 Write 工具创建新文件后，立即从已有文件获取权限并设置
- [ ] 使用 Edit 工具修改文件时，确认文件所有者与项目其他文件一致
- [ ] 批量创建文件后，统一修改所有权
- [ ] 创建目录后，递归修改目录及其内容的所有权

**完整示例：**

```bash
# ❌ 错误做法：硬编码用户信息
Write(file_path, content)
Bash("sudo chown jiyujie:staff " + file_path)  # 在其他人电脑上会失败

# ✅ 正确做法：动态获取权限
Write(file_path, content)
# 从项目根目录的 pom.xml 获取所有者信息
Bash("OWNER_GROUP=$(ls -l pom.xml | awk '{print $3\":\"$4}') && sudo chown $OWNER_GROUP " + file_path)

# ✅ 更简洁的做法：批量处理
Write(file1, content1)
Write(file2, content2)
Write(file3, content3)
Bash("OWNER_GROUP=$(ls -l pom.xml | awk '{print $3\":\"$4}') && sudo chown $OWNER_GROUP file1 file2 file3")
```

## Pull Request 提交规范

### 规则 2: PR 提交必须遵循项目规范

本项目的 PR 规范定义在 `.github/PULL_REQUEST_TEMPLATE.md` 文件中。

**强制要求：**
1. 创建 PR 前，必须先阅读 `.github/PULL_REQUEST_TEMPLATE.md`
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
   - [ ] 列出主要的变更内容

5. **验证变更 / Verifying this Change**
   - [ ] 测试步骤
   - [ ] 测试覆盖情况

6. **贡献者检查清单 / Contributor Checklist**
   - [ ] 基本要求
   - [ ] 代码质量
   - [ ] 测试要求
   - [ ] 文档和兼容性

**自动化流程：**

当用户要求创建 PR 时：
1. 自动读取 `.github/PULL_REQUEST_TEMPLATE.md`
2. 根据当前变更内容填写 PR 模板
3. 生成完整的 PR 描述
4. 提供推送和创建 PR 的命令

**示例工作流：**

```bash
# 1. 自动读取 PR 模板
Read(".github/PULL_REQUEST_TEMPLATE.md")

# 2. 分析当前变更
Bash("git diff --stat")
Bash("git log -1")

# 3. 生成 PR 描述（根据模板）
# ... 填写各个部分

# 4. 推送并创建 PR
Bash("git push -u origin <branch>")
Bash("gh pr create --base <target> --title <title> --body <description>")
```

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

# 动态修改文件权限（从 pom.xml 获取所有者）
OWNER_GROUP=$(ls -l pom.xml | awk '{print $3":"$4}')
sudo chown $OWNER_GROUP <file>
```

---

**最后更新**: 2025-11-08
**Claude Code 版本**: 最新版
