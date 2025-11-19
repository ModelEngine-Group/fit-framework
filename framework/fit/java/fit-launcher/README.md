# FIT Discrete Launcher（FIT 离散启动器）

## 模块概述

`fit-discrete-launcher` 是 FIT 框架的应用启动入口模块，提供了完整的应用程序启动、类加载和进程管理能力。该模块支持 FIT 应用以离散（独立进程）模式运行，是 FIT 框架"聚散部署"能力的核心实现之一。

**主要职责：**
- 应用程序的启动引导
- 自定义类加载器体系的初始化
- 跨平台启动脚本的提供
- 应用程序的生命周期管理

## 核心特性

### 1. 离散启动能力

支持 FIT 应用以独立进程的方式启动和运行：
- 每个应用拥有独立的 JVM 进程
- 完全隔离的运行环境
- 支持多应用并行部署

### 2. 自定义类加载体系

实现了分层的类加载器架构：
- **SharedClassLoader**: 加载共享类库
- **FrameworkClassLoader**: 加载框架核心类
- 支持插件化的类隔离机制

### 3. 跨平台启动脚本

提供基于 Node.js 的跨平台启动脚本：
- Unix/Linux/macOS: `fit` 脚本
- Windows: `fit.cmd` 批处理文件
- 统一的命令行接口

## 构建和安装

### 编译模块

在 `framework/fit/java` 目录下执行：

```bash
mvn clean install
```

编译完成后，会生成：
- `fit-discrete-launcher-{version}.jar`: 启动器 JAR 文件
- `build/bin/`: 启动脚本目录

### 构建产物

```
build/
├── fit-discrete-launcher-3.6.1-SNAPSHOT.jar
└── bin/
    ├── fit       # Unix/Linux/macOS 启动脚本
    ├── fit.js    # Node.js 核心脚本
    └── fit.cmd   # Windows 启动脚本
```

## 启动脚本使用说明

### 前置要求

- **Node.js**: 12.0+ （推荐 16.0+）
- **Java**: 17+

### Unix/Linux/macOS 使用方法

#### 初始化新项目

使用 `init` 命令可以快速创建一个新的 FIT 项目（脚手架功能）：

```bash
./fit init <project-name> [options]
```

**选项：**
- `--group-id=<id>`: Maven Group ID（默认：com.example）
- `--artifact-id=<id>`: Maven Artifact ID（默认：项目名称）
- `--package=<name>`: Java 包名（默认：groupId.artifactId）

**示例：**

```bash
# 交互式创建项目（会提示输入信息）
./fit init my-app

# 使用命令行参数创建项目
./fit init my-app --group-id=com.mycompany --artifact-id=my-app --package=com.mycompany.myapp
```

创建的项目包含：
- 标准的 Maven 项目结构
- FIT 框架依赖配置
- 示例启动类（Application.java）
- 示例控制器（HelloController.java）
- 示例领域模型（Message.java）
- README.md 和 .gitignore 文件

创建后即可进入项目目录，编译并运行：

```bash
cd my-app
mvn clean install
./fit start
```

访问 http://localhost:8080/hello 测试应用。

#### 启动应用

```bash
./fit start [Java参数] [程序参数]
```

**示例：**
```bash
# 基本启动
./fit start

# 指定 JVM 内存参数
./fit start -Xmx1g -Xms512m

# 传递应用参数
./fit start -Xmx1g myapp config.yaml
```

#### Debug 模式启动

```bash
./fit debug [Java参数] [程序参数]
```

Debug 模式会自动添加以下 JVM 参数：
```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
```

启动后可以使用 IDE（如 IntelliJ IDEA）连接到端口 5005 进行远程调试。

#### 查看版本

```bash
./fit version
```

#### 查看帮助

```bash
./fit help
```

### Windows 使用方法

使用 `fit.cmd` 命令：

```cmd
# 初始化新项目
fit.cmd init my-app
fit.cmd init my-app --group-id=com.mycompany --artifact-id=my-app

# 启动应用
fit.cmd start

# Debug 模式启动
fit.cmd debug

# 查看版本
fit.cmd version

# 查看帮助
fit.cmd help

# 传递参数
fit.cmd start -Xmx1g -Xms512m myapp config.yaml
```

### 参数传递机制

启动脚本会自动区分 Java 参数和程序参数：

- **Java 参数**: 以 `-` 开头的参数（如 `-Xmx512m`、`-Dproperty=value`）
- **程序参数**: 不以 `-` 开头的参数

**完整示例：**
```bash
./fit start -Xmx1g -Dplugin.path=/custom/path myapp config.yaml
```

实际执行的 Java 命令：
```bash
java -Xmx1g -Dplugin.path=/custom/path \
  -Dsun.io.useCanonCaches=true \
  -Djdk.tls.client.enableSessionTicketExtension=false \
  -Dplugin.fit.dynamic.plugin.directory=/current/working/directory \
  -jar fit-discrete-launcher-3.6.1-SNAPSHOT.jar \
  myapp config.yaml
```

### 环境变量和系统属性

启动脚本会自动设置以下 Java 系统属性：

| 系统属性 | 默认值 | 说明 |
|---------|--------|------|
| `sun.io.useCanonCaches` | `true` | 启用文件路径缓存，提升性能 |
| `jdk.tls.client.enableSessionTicketExtension` | `false` | 禁用 TLS 会话票证扩展 |
| `plugin.fit.dynamic.plugin.directory` | 当前工作目录 | 动态插件加载目录 |

## 技术实现

### 启动脚本实现（Node.js）

#### 为什么使用 Node.js？

相比传统的 Bash 脚本，Node.js 实现具有以下优势：

1. **跨平台兼容性**: 可以在 Windows、Linux、macOS 上无缝运行
2. **无需特殊处理**: 避免了 Shell 脚本在不同系统上的兼容性问题（如 macOS 上的 `readlink -f`）
3. **更好的可维护性**: JavaScript 语法更容易理解和维护
4. **统一的开发体验**: 与现代开发工具保持一致

#### 脚本工作原理

1. 解析命令行参数
2. 区分 Java 参数和程序参数
3. 在脚本所在目录的上级目录查找 `fit-discrete-launcher-*.jar` 文件
4. 构造完整的 Java 命令
5. 使用 `child_process.spawn` 启动 Java 进程
6. 继承标准输入/输出/错误流
7. 正确处理进程信号（SIGINT、SIGTERM）

### 类加载器实现

#### SharedClassLoader

加载共享类库，位于 `lib/shared/` 目录：
- 基础工具类
- 日志框架
- 其他共享依赖

#### FrameworkClassLoader

加载 FIT 框架核心类，位于 `lib/framework/` 目录：
- FIT IoC 容器
- FIT 插件系统
- FIT 运行时

## 故障排除

### 找不到 JAR 文件

**错误信息：**
```
No fit-discrete-launcher-[version].jar file found.
```

**解决方法：**
1. 确保已经编译了项目：`mvn clean install`
2. 检查 `bin/..` 目录下是否存在 `fit-discrete-launcher-*.jar` 文件
3. 确保脚本在正确的位置执行

### Node.js 未安装

**错误信息：**
```
bash: node: command not found
```
或
```
'node' 不是内部或外部命令...
```

**解决方法：**
从 [Node.js 官网](https://nodejs.org/) 下载并安装 Node.js。

### Java 未安装或版本不正确

**错误信息：**
```
Failed to start Java process: spawn java ENOENT
```

**解决方法：**
1. 安装 Java 17 或更高版本
2. 确保 `java` 命令在系统 PATH 中

验证 Java 安装：
```bash
java -version
```

### 类加载错误

**错误信息：**
```
ClassNotFoundException: ...
```

**可能原因：**
1. 缺少必要的依赖库
2. 类加载器配置错误
3. JAR 文件损坏

**解决方法：**
1. 检查 `lib/` 目录下的依赖是否完整
2. 重新编译和打包应用
3. 查看详细的错误日志

## 与其他启动方式的对比

FIT 框架支持多种启动方式：

| 启动方式 | 适用场景 | 类加载方式 | 进程模型 |
|---------|---------|-----------|---------|
| **Discrete Launcher** | 生产环境、独立部署 | 自定义类加载器 | 独立进程 |
| IDE 直接启动 | 开发调试 | IDE 类加载器 | IDE 进程 |
| Spring Boot 集成 | Spring Boot 应用 | Spring Boot 类加载器 | Spring Boot 进程 |
| Aggregated Launcher | 聚合部署 | 共享类加载器 | 多应用单进程 |

## 相关文档

- [FIT 快速入门指南](../../docs/framework/fit/java/quick-start-guide/)
- [FIT 用户指导手册](../../docs/framework/fit/java/user-guide-book/)
- [启动程序设计](./设计文档链接)
- [类加载架构设计](./设计文档链接)

## 兼容性

- **Node.js**: 12.0.0+ (推荐 16.0.0+)
- **操作系统**: Windows 7+, macOS 10.12+, Linux (any modern distribution)
- **Java**: 17+

## 贡献

如果你发现任何问题或有改进建议，请提交 Issue 或 Pull Request。

## 许可证

与 FIT 框架保持一致。
