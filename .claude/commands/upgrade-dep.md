升级项目依赖。

**用法：**
- `/upgrade-dep <package-name> <from-version> <to-version>` - 升级指定依赖
- 例如：`/upgrade-dep swagger-ui 5.30.0 5.30.2`

**执行步骤：**

1. **解析参数**
   - 包名：第一个参数
   - 原版本：第二个参数
   - 新版本：第三个参数

2. **查找并更新依赖相关文件**
   - 使用 Grep 搜索包含旧版本号的文件
   - 更新 pom.xml、package.json 或其他配置文件
   - 更新相关的静态资源文件（如果需要）

3. **验证变更**
   ```
   git diff
   mvn clean package -Dmaven.test.skip=true
   ```

**注意事项：**
- 升级后必须进行编译测试
- 升级完成后请人工检查变更内容
- 检查无误后可手动提交代码
