# FIT插件启动顺序说明

## 概述

本文档记录了 FIT Framework 内置插件的启动顺序。FIT 内置插件共定义了 **22 个**，其中：

- **20 个**会自动输出到 `build/plugins/` 目录
- **2 个**不在 `build/plugins/` 目录中（需单独配置）

所有 FIT 插件均配置为 **category=SYSTEM, level=4**。

---

## 启动顺序说明

### 整体启动阶段

```
FIT 框架启动流程
│
├─ 阶段0: 运行时初始化
│  └─ 注册系统 Bean（配置、AOP、Broker等）
│
├─ 阶段1: SYSTEM 插件启动（按 level 排序）
│  ├─ Level 4 (22个) 
│  │  ├─ FIT 内置插件（20个）
│  │  └─ FEL 工具仓库（2个）
│  ├─ Level 5 (4个)
│  │  └─ FEL 工具链（fel-tool-discoverer等）
│  └─ Level 7 (1个)
│     └─ FEL 模型集成（fel-model-openai-plugin）
│
└─ 阶段2: USER 插件启动（按 level 排序）
   ├─ Level 1 (1个)
   │  └─ FEL 用户插件（fel-tokenizer-hanlp-plugin）
   └─ Level 4 (1个)
      └─ FEL 用户插件（fel-langchain-runnable）
```

**FIT 内置插件在 SYSTEM Level 4，与 FEL 工具仓库插件一起，是所有插件中最先启动的一批。**

### 启动规则

- **category=SYSTEM (id=1) 的插件先于 USER (id=2) 插件加载**
- **相同 category 内，level 数值越小，启动优先级越高**
- **相同 category 和 level 的插件，加载顺序不确定**

---

## 自动添加的FIT插件

这 20 个插件会在构建时自动复制到 `build/plugins/` 目录（插件均配置为 category=SYSTEM, level=4）：

| 序号 | 插件名称                                  | 说明                |
|----|---------------------------------------|-------------------|
| 1  | `fit-actuator`                        | 监控端点              |
| 2  | `fit-client-http`                     | HTTP 客户端          |
| 3  | `fit-dynamic-plugin-directory`        | 动态插件目录            |
| 4  | `fit-heartbeat-client`                | 心跳客户端             |
| 5  | `fit-http-client-okhttp`              | OkHttp 客户端        |
| 6  | `fit-http-handler-registry`           | HTTP 处理器注册        |
| 7  | `fit-http-openapi3-swagger`           | OpenAPI 3/Swagger |
| 8  | `fit-http-server-netty`               | Netty HTTP 服务器    |
| 9  | `fit-logger`                          | 日志组件              |
| 10 | `fit-message-serializer-cbor`         | CBOR 序列化          |
| 11 | `fit-message-serializer-json-jackson` | Jackson JSON 序列化  |
| 12 | `fit-security-simple`                 | 简单安全              |
| 13 | `fit-server-http`                     | HTTP 服务器          |
| 14 | `fit-service-coordination-locator`    | 服务定位              |
| 15 | `fit-service-coordination-simple`     | 简单服务协调            |
| 16 | `fit-service-discovery`               | 服务发现              |
| 17 | `fit-service-registry`                | 服务注册              |
| 18 | `fit-validation-hibernate-jakarta`    | Jakarta 验证        |
| 19 | `fit-validation-hibernate-javax`      | Javax 验证          |
| 20 | `fit-value-fastjson`                  | Fastjson 支持       |

**注意**: 表格中的序号仅用于标识，不代表实际加载顺序。相同 category 和 level 的插件之间的加载顺序是不确定的。

---

## 需要手动添加的插件

以下插件在源码中定义，但**不会自动输出**到 `build/plugins/` 目录，需要手动添加（插件均配置为 category=SYSTEM, level=4）：

### 1. fit-dynamic-plugin-mvn

| 属性       | 值                                                               |
|----------|-----------------------------------------------------------------|
| **插件名**  | fit-dynamic-plugin-mvn                                          |
| **源码路径** | `framework/fit/java/fit-builtin/plugins/fit-dynamic-plugin-mvn` |
| **说明**   | Maven 动态插件加载器                                                   |
| **用途**   | 从 Maven 仓库动态加载插件                                                |

### 2. fit-service-coordination-nacos

| 属性       | 值                                                                       |
|----------|-------------------------------------------------------------------------|
| **插件名**  | fit-service-coordination-nacos                                          |
| **源码路径** | `framework/fit/java/fit-builtin/plugins/fit-service-coordination-nacos` |
| **说明**   | Nacos 服务协调实现                                                            |
| **用途**   | 使用 Nacos 进行服务注册和发现                                                      |

---

## 插件功能分类

虽然所有 FIT 插件都在 Level 4 启动，但它们提供不同类别的功能：

### HTTP 服务相关（7个）

- `fit-client-http` - HTTP 客户端接口
- `fit-http-client-okhttp` - OkHttp 客户端实现
- `fit-http-handler-registry` - HTTP 处理器注册
- `fit-http-openapi3-swagger` - OpenAPI 3 和 Swagger UI
- `fit-http-server-netty` - Netty HTTP 服务器实现
- `fit-server-http` - HTTP 服务器接口
- `fit-heartbeat-client` - 心跳客户端

### 序列化相关（3个）

- `fit-message-serializer-cbor` - CBOR 格式序列化
- `fit-message-serializer-json-jackson` - JSON 格式序列化（Jackson）
- `fit-value-fastjson` - Fastjson 值支持

### 服务协调相关（5个，其中1个可选）

- `fit-service-coordination-locator` - 服务定位器
- `fit-service-coordination-simple` - 简单服务协调实现（默认）
- `fit-service-coordination-nacos` - Nacos 服务协调实现（可选，不在 build/plugins）
- `fit-service-discovery` - 服务发现
- `fit-service-registry` - 服务注册

### 验证和安全（3个）

- `fit-validation-hibernate-jakarta` - Jakarta Bean Validation
- `fit-validation-hibernate-javax` - Javax Bean Validation
- `fit-security-simple` - 简单安全实现

### 动态插件加载（2个，其中1个可选）

- `fit-dynamic-plugin-directory` - 从目录加载插件（默认）
- `fit-dynamic-plugin-mvn` - 从 Maven 仓库加载插件（可选，不在 build/plugins）

### 其他（2个）

- `fit-actuator` - 应用监控端点
- `fit-logger` - 日志组件

---

## 源代码位置

所有 FIT 内置插件的源码位于：`framework/fit/java/fit-builtin/plugins/`

```
framework/fit/java/fit-builtin/plugins/
├── pom.xml                                  # 父 POM，统一配置 level=4
│
├── fit-actuator/
├── fit-client-http/
├── fit-dynamic-plugin-directory/
├── fit-dynamic-plugin-mvn/                  # 不会自动输出到build/plugins
├── fit-heartbeat-client/
├── fit-http-client-okhttp/
├── fit-http-handler-registry/
├── fit-http-openapi3-swagger/
├── fit-http-server-netty/
├── fit-logger/
├── fit-message-serializer-cbor/
├── fit-message-serializer-json-jackson/
├── fit-security-simple/
├── fit-server-http/
├── fit-service-coordination-locator/
├── fit-service-coordination-simple/
├── fit-service-coordination-nacos/          # 不会自动输出到build/plugins
├── fit-service-discovery/
├── fit-service-registry/
├── fit-validation-hibernate-jakarta/
├── fit-validation-hibernate-javax/
└── fit-value-fastjson/
```

### 父 POM 配置

所有 FIT 内置插件继承自 `fit-builtin/plugins/pom.xml`，该文件统一配置了：

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.fitframework</groupId>
            <artifactId>fit-build-maven-plugin</artifactId>
            <configuration>
                <category>system</category>
                <level>4</level>  <!-- 所有子插件继承此配置 -->
            </configuration>
            <executions>
                <execution>
                    <id>build-plugin</id>
                    <goals>
                        <goal>build-plugin</goal>
                    </goals>
                </execution>
                <execution>
                    <id>package-plugin</id>
                    <goals>
                        <goal>package-plugin</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
