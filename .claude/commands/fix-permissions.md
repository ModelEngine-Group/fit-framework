检查并修复项目中所有文件和文件夹的权限，确保符合项目规范。

**用法：**
- `/fix-permissions` - 检查并自动修复所有权限问题

**执行步骤：**

1. **获取参考文件的所有者信息**
   ```bash
   ls -l README.md | awk '{print $3":"$4}'
   ```
   - 从项目根目录的 `README.md` 动态获取正确的所有者信息
   - README.md 是所有项目都有的通用文件
   - 不硬编码用户名和用户组

2. **统计当前文件权限分布**
   ```bash
   # 检查所有文件的所有者分布
   find . -type f -print0 2>/dev/null | xargs -0 ls -l 2>/dev/null | awk '{print $3":"$4}' | sort | uniq -c | sort -rn

   # 检查所有目录的所有者分布
   find . -type d -print0 2>/dev/null | xargs -0 ls -ld 2>/dev/null | awk '{print $3":"$4}' | sort | uniq -c | sort -rn
   ```

3. **识别权限不一致的文件**
   ```bash
   # 找出所有者不是参考用户的文件
   find . -type f -print0 2>/dev/null | xargs -0 ls -l 2>/dev/null | awk '$3 == "root" {print $9}'

   # 找出所有者不是参考用户的目录
   find . -type d -print0 2>/dev/null | xargs -0 ls -ld 2>/dev/null | awk '$3 == "root" {print $9}'
   ```

4. **修复文件权限**
   ```bash
   # 获取正确的所有者信息
   OWNER_GROUP=$(ls -l README.md | awk '{print $3":"$4}')

   # 修复所有文件权限
   find . -type f -print0 2>/dev/null | xargs -0 ls -l 2>/dev/null | awk '$3 == "root" {print $9}' | xargs sudo chown $OWNER_GROUP

   # 修复所有目录权限
   find . -type d -print0 2>/dev/null | xargs -0 ls -ld 2>/dev/null | awk '$3 == "root" {print $9}' | xargs sudo chown $OWNER_GROUP
   ```

5. **最终验证**
   ```bash
   # 统计修复后的权限分布
   find . \( -type f -o -type d \) -print0 2>/dev/null | xargs -0 ls -ld 2>/dev/null | awk '{print $3":"$4}' | sort | uniq -c | sort -rn

   # 抽查几个之前有问题的文件
   ls -ld ./.claude/settings.local.json ./framework/.claude/settings.local.json
   ```

6. **生成检查报告**
   - 显示参考所有者信息
   - 显示发现的问题文件和目录数量
   - 显示修复的文件和目录列表
   - 显示最终验证结果

**规范说明：**

根据 `.claude/project-rules.md` 中的规则 1：
- 所有文件必须设置正确的所有者权限
- 不要硬编码用户名和用户组
- 动态从 `README.md` 等参考文件获取权限信息
- README.md 是通用文件，适用于所有语言的项目
- 确保用户可以自主修改所有文件

**常见问题：**

**为什么有些文件是 root 所有者？**
- 可能是使用 `sudo` 创建的文件或目录
- Git 操作时使用了 sudo 权限
- Claude Code 创建文件时权限设置不当

**修复后会影响 Git 吗？**
- 不会影响 Git 历史和提交
- 只改变文件系统级别的所有者
- Git 内容和状态保持不变

**需要 sudo 权限吗？**
- 修复 root 所有者的文件时需要 sudo
- 确保当前用户在 sudoers 列表中

**参考文档：**
- 项目规则：`.claude/project-rules.md` (规则 1)
- 动态获取权限方法的详细说明
