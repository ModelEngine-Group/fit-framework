---
description: 检查并修复项目中的文件权限
---

1. 确定正确的所有者/组。
   - 检查 `README.md` 的所有者作为参考。
   - `run_command("ls -l README.md")`
   - 提取用户和组。

2. 识别权限不正确的文件。
   - 查找由 `root` 拥有的文件。
   - `run_command("find . -user root")`

3. 修复权限。
   - 如果发现 root 拥有的文件，将其所有权更改为参考用户/组。
   - `run_command("sudo chown -R <user>:<group> <path-to-root-owned-files>")`
   - **注意**: 如果在项目外部运行，请小心不要更改系统文件。确保路径在项目内。

4. 验证修复结果。
   - 再次运行 find 命令，确保不再有 root 拥有的文件。
