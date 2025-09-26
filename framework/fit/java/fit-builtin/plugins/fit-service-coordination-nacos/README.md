# FIT Service Coordination Nacos 插件

## 简介

FIT Service Coordination Nacos 插件是 FIT Framework 的服务协调插件，提供基于 Nacos 的服务注册与发现功能。该插件将 FIT 框架与 Alibaba Nacos 注册中心集成。

## 重要说明

由于 `fit-service-coordination-nacos` 与 `fit-service-coordination-simple` 两种注册中心插件存在冲突，我们选择默认使用内存版注册中心。

### 不同启动方式的插件使用

- **IDEA 启动方式和 `java -jar` 启动方式**：只需要按需引入依赖即可，无需额外配置。

- **`fit start` 命令启动方式**：
  - 默认只有 `fit-service-coordination-simple` 插件在 `/build/plugins` 目录
  - 如果需要使用 Nacos 版注册中心，操作步骤：
    1. **整体编译项目**：
       ```bash
       cd framework/fit/java
       mvn clean package
       ```
    2. **放入 build/plugins 目录**：
       ```bash
       cp fit-builtin/plugins/fit-service-coordination-nacos/target/fit-service-coordination-nacos-3.6.0-SNAPSHOT.jar ../../../build/plugins/
       ```
    3. **移除 Simple 插件**：
       ```bash
       rm ../../../build/plugins/fit-service-coordination-simple-3.6.0-SNAPSHOT.jar
       ```

> **说明**：`build/plugins` 目录中只能有一个注册中心插件，Nacos 和 Simple 不能同时存在。

## 快速开始

### 1. Nacos 部署

#### 本地安装部署

1. 下载 Nacos Server

```bash
# 下载 Nacos 2.3.0
wget https://github.com/alibaba/nacos/releases/download/2.3.0/nacos-server-2.3.0.tar.gz

# 解压
tar -xvf nacos-server-2.3.0.tar.gz
cd nacos/bin
```

2. 启动 Nacos

```bash
# Linux/Mac
sh startup.sh -m standalone

# Windows
startup.cmd -m standalone
```

访问控制台：http://localhost:8848/nacos 

**默认登录凭据**：
- 用户名：`nacos`
- 密码：`nacos`

> ⚠️ **安全提醒**：默认凭据仅适用于开发环境。在生产环境中，请务必修改默认的用户名和密码以确保系统安全。

### 2. 插件配置

#### Maven 依赖

在你的 FIT 项目 `pom.xml` 中添加插件依赖：

```xml
<dependency>
    <groupId>org.fitframework.plugin</groupId>
    <artifactId>fit-service-coordination-nacos</artifactId>
</dependency>
```

#### 配置文件

在 `application.yml` 中配置 Nacos 连接信息：

```yaml
# 基础配置示例
matata:
  registry:
    # Nacos 服务器地址
    host: 'localhost'
    port: 8848
    # 注册中心连接模式
    mode: 'PROXY'
    # 环境命名空间
    environment: local

# 完整配置示例（可选）
matata:
  registry:
    host: 'localhost'
    port: 8848
    mode: 'PROXY'
    environment: 'local'
    # Nacos 特定配置
    nacos:
      username: 'nacos'          # Nacos 用户名（可选）
      password: 'nacos'          # Nacos 密码（可选）
      weight: 1.0                # 服务权重
      isEphemeral: true          # 是否为临时实例
      heartbeatInterval: 5000    # 心跳间隔（毫秒）
      heartbeatTimeout: 15000    # 心跳超时（毫秒）
```

## 配置参数说明

### 基础配置

| 参数                          | 类型    | 必需 | 默认值 | 说明                                                                   |
| ----------------------------- | ------- | ---- | ------ | ---------------------------------------------------------------------- |
| `matata.registry.host`        | String  | 是   | -      | Nacos 服务器地址                                                       |
| `matata.registry.port`        | Integer | 是   | -      | Nacos 服务器端口                                                       |
| `matata.registry.environment` | String  | 否   | -      | 命名空间，用于环境隔离                                                 |
| `matata.registry.mode`        | String  | 是   | -      | 注册中心连接模式，这里使用 PROXY 表示使用本地插件代理与 nacos 连接通信 |

### Nacos 特定配置

| 参数                                      | 类型    | 必需 | 默认值 | 说明             |
| ----------------------------------------- | ------- | ---- | ------ | ---------------- |
| `matata.registry.nacos.username`          | String  | 否   | -      | Nacos 用户名     |
| `matata.registry.nacos.password`          | String  | 否   | -      | Nacos 密码       |
| `matata.registry.nacos.accessKey`         | String  | 否   | -      | 访问密钥         |
| `matata.registry.nacos.secretKey`         | String  | 否   | -      | 秘密密钥         |
| `matata.registry.nacos.weight`            | Float   | 否   | 1.0    | 服务权重         |
| `matata.registry.nacos.isEphemeral`       | Boolean | 否   | true   | 是否为临时实例   |
| `matata.registry.nacos.heartbeatInterval` | Long    | 否   | 5000   | 心跳间隔（毫秒） |
| `matata.registry.nacos.heartbeatTimeout`  | Long    | 否   | 15000  | 心跳超时（毫秒） |

## 使用示例

### 完整示例项目

FIT Framework 提供了一个完整的 Nacos 服务注册与发现示例项目，位于 `examples/fit-example/08-nacos-complicated-apps` 目录。

#### 项目结构

```
08-nacos-complicated-apps/
├── app-assistant/          # 客户端应用（助手服务）
├── app-default-weather/    # 服务端应用（天气服务）
├── service/               # 共享服务接口
└── pom.xml               # 父项目配置
```

#### 示例说明

该示例演示了如何使用 Nacos 作为注册中心实现微服务之间的服务发现和调用：

1. **服务提供者**（`app-default-weather`）：
   - 实现 `Weather` 接口
   - 注册到 Nacos 注册中心
   - 提供天气查询服务

2. **服务消费者**（`app-assistant`）：
   - 通过 Nacos 发现天气服务
   - 调用远程天气服务
   - 提供 HTTP API 接口

#### 关键配置

**服务提供者配置**（`app-default-weather/src/main/resources/application.yml`）：
```yaml
application:
  name: 'default-weather'

worker:
  id: 'default-weather'
  host: '127.0.0.1'
  environment: 'local'

matata:
  registry:
    mode: 'PROXY'
    host: '127.0.0.1'
    port: 8848
    environment: 'local'

server:
  http:
    port: 8081
```

**服务消费者配置**（`app-assistant/src/main/resources/application.yml`）：
```yaml
application:
  name: 'assistant'

worker:
  id: 'assistant'
  host: '127.0.0.1'
  environment: 'local'

matata:
  registry:
    mode: 'PROXY'
    host: '127.0.0.1'
    port: 8848
    environment: 'local'

server:
  http:
    port: 8080
```

#### 核心代码

**服务接口定义**（`service/src/main/java/modelengine/fit/example/Weather.java`）：
```java
public interface Weather {
    @Genericable(id = "Weather")
    String get();
}
```

**服务实现**（`app-default-weather/src/main/java/modelengine/fit/example/DefaultWeather.java`）：
```java
@Component
public class DefaultWeather implements Weather {
    @Override
    @Fitable(id = "default-weather")
    public String get() {
        return "Default weather application is working.";
    }
}
```

**服务调用**（`app-assistant/src/main/java/modelengine/fit/example/controller/AssistantController.java`）：
```java
@Component
public class AssistantController {
    private final Weather weather;

    public AssistantController(@Fit Weather weather) {
        this.weather = weather;
    }

    @GetMapping(path = "/weather")
    public String getWeather() {
        return this.weather.get();
    }
}
```

#### 运行示例

1. **启动 Nacos 服务器**（确保在 8848 端口运行）

2. **启动服务提供者**：
   ```bash
   cd examples/fit-example/08-nacos-complicated-apps/app-default-weather
   mvn clean package
   java -jar target/app-default-weather-1.0-SNAPSHOT.jar
   ```

3. **启动服务消费者**：
   ```bash
   cd examples/fit-example/08-nacos-complicated-apps/app-assistant
   mvn clean package
   java -jar target/app-assistant-1.0-SNAPSHOT.jar
   ```

4. **测试服务调用**：
   ```bash
   curl http://localhost:8080/weather
   ```

#### 依赖配置

在 `pom.xml` 中添加必要的依赖：

```xml
<!-- FIT 核心依赖 -->
<dependency>
    <groupId>org.fitframework</groupId>
    <artifactId>fit-starter</artifactId>
    <version>3.6.0-SNAPSHOT</version>
</dependency>

<!-- Nacos 注册中心插件 -->
<dependency>
    <groupId>org.fitframework.plugin</groupId>
    <artifactId>fit-service-coordination-nacos</artifactId>
    <version>3.6.0-SNAPSHOT</version>
    <scope>runtime</scope>
</dependency>
```