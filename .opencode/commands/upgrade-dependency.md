---
description: 升级项目依赖
agent: general
subtask: false
---

升级项目中的指定依赖包到新版本,并验证变更。

使用方式:
/upgrade-dependency <package-name> <from-version> <to-version>

例如: /upgrade-dependency swagger-ui 5.30.0 5.30.2

执行以下步骤:

1. 解析参数:
   - 包名: $1
   - 原版本: $2
   - 新版本: $3

2. 查找依赖文件:
   使用 grep 搜索包含该依赖的文件:
   !`grep -r "$1" --include="pom.xml" --include="package.json" --include="build.gradle"`
   !`grep -r "$2" --include="pom.xml" --include="package.json" --include="build.gradle"`

3. 更新依赖版本:
   根据项目类型更新对应的依赖文件:
   
   **Maven 项目**(pom.xml):
   使用 Edit 工具将 <version>$2</version> 替换为 <version>$3</version>
   
   **Node.js 项目**(package.json):
   使用 Edit 工具将 "$1": "$2" 替换为 "$1": "$3"
   
   **Gradle 项目**(build.gradle):
   使用 Edit 工具更新版本号

4. 更新相关静态资源:
   如果依赖包含静态资源(如 swagger-ui),同步更新:
   - 版本号引用
   - CDN 链接
   - 文档中的版本说明

5. 验证变更:
   
   5.1 查看变更:
   !`git diff`
   
   5.2 编译验证:
   - Maven: !`mvn clean package -Dmaven.test.skip=true`
   - Node.js: !`npm install && npm run build`
   - Gradle: !`./gradlew clean build -x test`
   
   5.3 运行测试(推荐):
   - Maven: !`mvn test`
   - Node.js: !`npm test`
   - Gradle: !`./gradlew test`

6. 输出变更摘要:
   ```
   ✅ 依赖升级完成
   
   **依赖信息**:
   - 包名: $1
   - 原版本: $2
   - 新版本: $3
   
   **变更文件**:
   - <file-path-1>
   - <file-path-2>
   
   **验证结果**:
   - 编译状态: <成功/失败>
   - 测试状态: <通过/失败/未运行>
   
   **下一步**:
   请人工检查变更内容,确认无误后提交: /commit
   ```

**注意事项**:
- 升级前检查 CHANGELOG 和 Migration Guide
- 注意破坏性变更(Breaking Changes)
- 跨大版本升级需要特别谨慎
- 升级后必须进行编译测试
- 建议运行完整的测试套件
- **不要**自动执行 git commit,等待人工检查
- 一次只升级一个依赖,避免同时升级多个相关依赖
