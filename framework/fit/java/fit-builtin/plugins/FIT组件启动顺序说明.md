# FIT 内置插件启动顺序说明

## 概述

本文档记录了 FIT Framework 内置插件的启动顺序（level）配置。Level 值决定了组件在系统启动时的加载顺序，**数值越小，启动优先级越高
**。

## 启动流程图

```
启动时间轴 (从上到下)
│
├─ Level 1 (最先) ────────────────────────────
│  └─ plugin-log (日志插件)
│
└─ Level 4 ───────────────────────────────────
   ├─ fit-plugin-parent (插件父模块)
   ├─ fit-validation-hibernate-jakarta (验证)
   └─ fit-validation-hibernate-javax (验证)
```

---

## 按启动顺序排列的组件列表

### Level 1 - 最高优先级（用户级基础组件）

| 序号 | 组件名称       | 类别   | 路径                                                        | 说明         |
|----|------------|------|-----------------------------------------------------------|------------|
| 1  | plugin-log | user | examples/fit-example/05-aop-log-plugin/plugins/plugin-log | AOP 日志插件示例 |

**启动说明**: 用户级示例插件，最先启动。

---

### Level 4 - 第二优先级（FIT 内置系统组件）

| 序号 | 组件名称                             | 类别     | 路径                                                                      | 说明           |
|----|----------------------------------|--------|-------------------------------------------------------------------------|--------------|
| 1  | fit-plugin-parent                | system | framework/fit/java/fit-builtin/plugins                                  | FIT 内置插件父模块  |
| 2  | fit-validation-hibernate-jakarta | system | framework/fit/java/fit-builtin/plugins/fit-validation-hibernate-jakarta | Jakarta 验证插件 |
| 3  | fit-validation-hibernate-javax   | system | framework/fit/java/fit-builtin/plugins/fit-validation-hibernate-javax   | Javax 验证插件   |

**启动说明**: FIT 框架的验证组件，在基础组件之后加载。

---

## FIT 内置插件列表

`fit-plugin-parent` 下的所有子模块（22个）都继承 `level=4` 配置：

1. **fit-actuator** - 监控端点
2. **fit-client-http** - HTTP 客户端
3. **fit-dynamic-plugin-directory** - 动态插件目录加载
4. **fit-dynamic-plugin-mvn** - Maven 动态插件
5. **fit-heartbeat-client** - 心跳客户端
6. **fit-http-client-okhttp** - OkHttp 客户端
7. **fit-http-handler-registry** - HTTP 处理器注册
8. **fit-http-openapi3-swagger** - OpenAPI 3/Swagger 支持
9. **fit-http-server-netty** - Netty HTTP 服务器
10. **fit-logger** - 日志组件
11. **fit-message-serializer-cbor** - CBOR 序列化
12. **fit-message-serializer-json-jackson** - Jackson JSON 序列化
13. **fit-security-simple** - 简单安全实现
14. **fit-server-http** - HTTP 服务器
15. **fit-service-coordination-locator** - 服务定位器
16. **fit-service-coordination-simple** - 简单服务协调
17. **fit-service-coordination-nacos** - Nacos 服务协调
18. **fit-service-discovery** - 服务发现
19. **fit-service-registry** - 服务注册
20. **fit-value-fastjson** - Fastjson 支持
21. **fit-validation-hibernate-jakarta** - Jakarta 验证
22. **fit-validation-hibernate-javax** - Javax 验证

---

## 技术说明

### Level 配置位置

Level 配置在各组件的 `pom.xml` 文件中，通常位于 Maven 插件配置内：

```xml

<plugin>
    <groupId>org.fitframework</groupId>
    <artifactId>fit-build-maven-plugin</artifactId>
    <configuration>
        <category>system</category>
        <level>4</level>
    </configuration>
</plugin>
```
