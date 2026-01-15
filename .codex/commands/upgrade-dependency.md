---
name: "upgrade-dependency"
description: "升级项目依赖"
usage: "/upgrade-dependency <package-name> <from-version> <to-version>"
---

# Upgrade Dependency Command

## 功能说明

升级项目中的指定依赖包到新版本，并验证变更。

## 用法

```bash
/upgrade-dependency <package-name> <from-version> <to-version>
```

例如：
```bash
/upgrade-dependency swagger-ui 5.30.0 5.30.2
```

## 执行步骤

### 1. 解析参数

- **包名**：第一个参数
- **原版本**：第二个参数
- **新版本**：第三个参数

### 2. 查找依赖文件

使用 Grep 搜索包含该依赖的文件：

```bash
# 搜索包名
grep -r "<package-name>" --include="pom.xml" --include="package.json" --include="build.gradle"

# 搜索旧版本号
grep -r "<from-version>" --include="pom.xml" --include="package.json" --include="build.gradle"
```

### 3. 更新依赖版本

根据项目类型更新对应的依赖文件：

**Maven 项目** (pom.xml):
```xml
<!-- 从 -->
<dependency>
    <groupId>...</groupId>
    <artifactId>{package-name}</artifactId>
    <version>{from-version}</version>
</dependency>

<!-- 更新为 -->
<dependency>
    <groupId>...</groupId>
    <artifactId>{package-name}</artifactId>
    <version>{to-version}</version>
</dependency>
```

**Node.js 项目** (package.json):
```json
{
  "dependencies": {
    "{package-name}": "{to-version}"
  }
}
```

**Gradle 项目** (build.gradle):
```groovy
implementation '{group}:{package-name}:{to-version}'
```

### 4. 更新相关静态资源

如果依赖包含静态资源（如 swagger-ui 的 HTML/CSS/JS 文件），同步更新：
- 版本号引用
- CDN 链接
- 文档中的版本说明

### 5. 验证变更

#### 5.1 查看变更

```bash
git diff
```

确认变更正确，没有误改其他内容。

#### 5.2 编译验证

```bash
# Maven 项目
mvn clean package -Dmaven.test.skip=true

# Node.js 项目
npm install
npm run build

# Gradle 项目
./gradlew clean build -x test
```

#### 5.3 运行测试（推荐）

```bash
# Maven 项目
mvn test

# Node.js 项目
npm test

# Gradle 项目
./gradlew test
```

### 6. 输出变更摘要

告知用户升级完成的情况：

```
✅ 依赖升级完成

**依赖信息**:
- 包名: {package-name}
- 原版本: {from-version}
- 新版本: {to-version}

**变更文件**:
- {file-path-1}
- {file-path-2}

**验证结果**:
- 编译状态: {成功/失败}
- 测试状态: {通过/失败/未运行}

**下一步**:
请人工检查变更内容，确认无误后提交：
/commit
```

## 参数说明

- `<package-name>`: 依赖包名称（必需）
- `<from-version>`: 当前版本号（必需）
- `<to-version>`: 目标版本号（必需）

## 使用示例

### 示例1：升级 Swagger UI

```bash
/upgrade-dependency swagger-ui 5.30.0 5.30.2
```

### 示例2：升级 Spring Boot

```bash
/upgrade-dependency spring-boot 2.7.0 2.7.18
```

### 示例3：升级 React

```bash
/upgrade-dependency react 18.2.0 18.3.1
```

## 注意事项

1. **版本兼容性**：
   - 升级前检查 CHANGELOG 和 Migration Guide
   - 注意破坏性变更（Breaking Changes）
   - 跨大版本升级需要特别谨慎

2. **依赖关系**：
   - 注意传递依赖的兼容性
   - 某些依赖可能需要同步升级
   - 检查依赖冲突

3. **验证测试**：
   - 升级后必须进行编译测试
   - 建议运行完整的测试套件
   - 检查是否有废弃 API 的警告

4. **人工检查**：
   - 升级完成后请人工检查变更内容
   - 确认没有误改其他配置
   - 检查无误后再提交代码

5. **Git 操作**：
   - **不要**自动执行 git commit
   - 等待人工检查后再提交
   - 提交信息应明确说明升级内容

6. **安全升级**：
   - 如果是安全漏洞修复，优先处理
   - 在提交信息中注明相关的 CVE 或 GHSA ID

## 相关命令

- `/analyze-security <alert-number>` - 分析安全告警（如果是安全相关升级）
- `/commit` - 提交代码
- `/test` - 运行测试

## 错误处理

- 依赖文件未找到：提示 "未找到包含 {package-name} 的依赖文件"
- 版本号未找到：提示 "未找到版本 {from-version}，请检查当前版本号"
- 编译失败：提示 "升级后编译失败，请检查错误信息"
- 测试失败：提示 "升级后测试失败，可能存在兼容性问题"

## 最佳实践

1. **小步升级**：
   - 一次只升级一个依赖
   - 避免同时升级多个相关依赖
   - 每次升级都进行完整验证

2. **记录变更**：
   - 在提交信息中说明升级原因
   - 关联相关的 Issue 或安全告警
   - 记录重要的兼容性变更

3. **回滚准备**：
   - 确保有清晰的 git 历史
   - 了解如何回滚到之前的版本
   - 记录升级过程中的问题和解决方案
