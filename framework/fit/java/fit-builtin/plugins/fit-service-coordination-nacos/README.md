# FIT Service Coordination Nacos 插件

## 简介

FIT Service Coordination Nacos 插件是 FIT Framework 的服务协调插件，提供基于 Nacos 的服务注册与发现功能。该插件将 FIT 框架与 Alibaba Nacos 注册中心集成。

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

访问控制台：http://localhost:8848/nacos （默认用户名/密码：nacos/nacos）

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
# Matata 服务配置
matata:
  registry:
    # Nacos 服务器地址
    host: 'localhost'
    port: 8848
    # 注册中心连接模式
    mode: 'PROXY'
    # 环境命名空间
    environment: local
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