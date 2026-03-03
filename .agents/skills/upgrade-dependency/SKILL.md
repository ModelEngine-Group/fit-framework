---
name: upgrade-dependency
description: 升级项目中的指定依赖包到新版本并验证变更。当用户要求升级依赖、更新包版本时触发。参数为包名、原版本和新版本。
---

# 升级依赖

## 参数

- `<package-name>`: 包名
- `<from-version>`: 原版本
- `<to-version>`: 新版本

## 执行步骤

1. 查找依赖文件:
   ```bash
   grep -r "<package-name>" --include="pom.xml" --include="package.json" .
   grep -r "<from-version>" --include="pom.xml" --include="package.json" .
   ```

2. 更新依赖版本:
   - Maven (pom.xml): 替换 version 标签
   - Node.js (package.json): 更新版本号

3. 更新相关静态资源（如 CDN 链接等）。

4. 验证变更:
   ```bash
   git diff
   mvn clean package -Dmaven.test.skip=true
   mvn test
   ```

5. 输出变更摘要。

**注意**: 升级前检查 CHANGELOG 和 Migration Guide，注意破坏性变更。不要自动 git commit。一次只升级一个依赖。
