# FIT 注册中心（Java 内存版）

本文说明如何启动 FIT Framework 自带的 Java 内存注册中心，用于本地开发或与 Python 运行时联调。该注册中心随 Java 侧构建产物一起生成，无需额外安装服务。

## 前置要求

- Java 17+
- Maven 3.8.8+
- Node.js 12+（`fit` 启动脚本依赖 Node.js）

## 启动步骤（最简）

1. 编译 Java 侧框架：
   ```bash
   cd framework/fit/java
   mvn clean install
   ```
   产物会输出到仓库根目录 `build/`，其中包含：
   - `build/bin/fit` 启动脚本
   - `build/plugins/fit-service-coordination-simple-*.jar` 内存注册中心插件

2. 启动 FIT 服务（默认启用内存注册中心）：
   ```bash
   ./build/bin/fit start
   ```
   可选：如果希望把当前目录作为动态插件目录，可直接在该目录执行；如需隔离插件，可自行进入其他空目录再执行。

3. 验证服务：
   ```bash
   curl http://localhost:8080/actuator/plugins
   ```

## 常见说明

- 默认端口为 `8080`，若冲突可在启动命令中覆盖，例如：
  ```bash
  ./build/bin/fit start server.http.port=8081
  ```
- 内存注册中心适用于单机或测试场景；需要 Nacos 时，请改用对应的 Nacos 插件和配置。
