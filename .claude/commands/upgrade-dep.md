升级项目依赖并创建 PR。

**用法：**
- `/upgrade-dep <package-name> <from-version> <to-version>` - 升级指定依赖
- 例如：`/upgrade-dep swagger-ui 5.30.0 5.30.2`

**执行步骤：**

1. **解析参数**
   - 包名：第一个参数
   - 原版本：第二个参数
   - 新版本：第三个参数

2. **创建功能分支**
   ```
   git checkout -b fit-enhancement-<package-name>-<to-version>
   ```

3. **查找并更新依赖相关文件**
   - 使用 Grep 搜索包含旧版本号的文件
   - 更新 pom.xml、package.json 或其他配置文件
   - 更新相关的静态资源文件（如果需要）

4. **验证变更**
   ```
   git diff
   mvn clean package -Dmaven.test.skip=true
   ```

5. **提交变更**
   ```
   git add .
   git commit -m "Upgrade <package-name> from v<from-version> to v<to-version>

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude <noreply@anthropic.com>"
   ```

6. **推送并创建 PR**
   - 推送到远程：`git push -u origin <branch>`
   - 自动调用 `/pr 3.5.x` 创建 PR
   - PR 标题：`[模块名] Upgrade <package-name> from v<from-version> to v<to-version>`
   - 变更类型选择：📦 依赖升级

**注意事项：**
- 升级后必须进行编译测试
- PR 描述要说明升级原因
- 标记为微小变更，不需要 Issue
