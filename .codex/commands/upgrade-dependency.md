---
description: 升级项目依赖
argument-hint: <package-name> <from-version> <to-version>
---

升级项目中的指定依赖包到新版本,并验证变更。

例如: /upgrade-dependency swagger-ui 5.30.0 5.30.2

参数:
- 包名: $1
- 原版本: $2
- 新版本: $3

执行以下步骤:

1. 查找依赖文件:
   ```bash
   grep -r "$1" --include="pom.xml" --include="package.json" --include="build.gradle" --include="requirements.txt" .
   grep -r "$2" --include="pom.xml" --include="package.json" --include="build.gradle" --include="requirements.txt" .
   ```

2. 更新依赖版本:
   根据项目类型更新对应的依赖文件:
   - Maven(pom.xml): 使用 Edit 工具将 <version>$2</version> 替换为 <version>$3</version>
   - Node.js(package.json): 使用 Edit 工具更新版本号
   - Gradle(build.gradle): 使用 Edit 工具更新版本号

3. 更新相关静态资源:
   如果依赖包含静态资源(如 swagger-ui),同步更新版本号引用、CDN 链接等。

4. 验证变更:
   ```bash
   git diff
   git diff --name-only | wc -l
   ```
   编译验证:
   ```bash
   mvn clean package -Dmaven.test.skip=true
   ```
   运行测试(推荐):
   ```bash
   mvn test
   ```

5. 输出变更摘要:
   ```
   ✅ 依赖升级完成
   包名: $1
   原版本: $2
   新版本: $3
   变更文件: <列表>
   编译状态: <成功/失败>
   测试状态: <通过/失败/未运行>
   下一步: 请人工检查变更后提交 /commit
   ```

**注意事项**:
- 升级前检查 CHANGELOG 和 Migration Guide
- 注意破坏性变更(Breaking Changes)
- **不要**自动执行 git commit,等待人工检查
- 一次只升级一个依赖
