# 在 Spring Boot 中使用 FEL

> 本文档说明如何在 Spring Boot 项目中集成 FEL 框架，专注于 Spring 集成层面的配置和注意事项。
> FEL 功能的详细使用请参考 FEL 官方文档。

---

## 一、环境准备

### 1. 基础环境

- **JDK**：17 或更高版本
- **Maven**：3.6+
- **Spring Boot**：3.0+（本项目使用 4.0.1）

### 2. 添加依赖

**核心依赖**（必需）：

```xml
<dependency>
    <groupId>org.fitframework.fel</groupId>
    <artifactId>fel-spring-boot-starter</artifactId>
    <version>${fel.version}</version>
</dependency>
```

这个依赖会自动引入：
- FEL 核心功能（ChatModel、EmbedModel）
- FIT 框架运行时
- Spring Boot 自动配置

其他 Spring 依赖（如 `spring-boot-starter-web`、`spring-boot-starter-webflux`）可按需添加。

### 3. 配置文件

在 `application.yml` 中配置 API 密钥：

```yaml
fel:
  openai:
    api-base: '${api-base}'
    api-key: '${your-api-key}'
```

---

## 二、Spring Boot 集成要点

### 1. Bean 注入

`fel-spring-boot-starter` 会自动将 FEL Bean 注册到 Spring 容器，可以直接注入使用：

```java
@RestController
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")  // ← 必须添加
public class ChatController {

    private final ChatModel chatModel;

    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }
}
```

**⚠️ 注意**：建议添加 `@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")`，否则 IDE 会显示波浪线警告。

**原因**：FEL Bean 是运行时动态注册的，IDE 在编译期无法感知这些 Bean 的存在，会显示 "Could not autowire" 警告。虽然不影响运行，但添加此注解可以消除 IDE 警告。

**自动注入的 FEL Bean**：

| Bean 类型 | 说明 | 注入方式 |
|----------|------|---------|
| `ChatModel` | 聊天模型 | 构造器注入 |
| `EmbedModel` | 嵌入模型 | 构造器注入 |
| `ObjectSerializer` | JSON 序列化器 | 构造器注入，需要 `@Qualifier("json")` |
| `ToolRepository` | 工具仓库 | 构造器注入 |
| `ToolExecuteService` | 工具执行服务 | 构造器注入 |

### 2. 模型调用

```java
// 返回文本或手动转换为 Map
@GetMapping("/chat")
public Map<String, Object> chat(@RequestParam String query) {
    ChatMessage aiMessage = chatModel.generate(ChatMessages.from(query))
        .first().block().get();

    return Map.of(
        "content", aiMessage.text(),
        "toolCalls", aiMessage.toolCalls()
    );
}
```

**⚠️ 注意**：不能直接返回 `ChatMessage` 对象，Spring MVC 无法正确序列化 FEL 的对象。

具体的 FEL API 使用（如 ChatOption、Prompt、Memory 等）请参考 FEL 官方文档。

### 3. 流式响应适配

**关键**：FEL 的 `Choir` 实现了 JDK 的 `Flow.Publisher` 接口，与 Spring Reactor 的 `Publisher` 之间需要用适配器转换。

```java
import reactor.adapter.JdkFlowAdapter;  // ← 导入适配器
import reactor.core.publisher.Flux;

@GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
public Flux<String> stream(@RequestParam String query) {
    ChatOption option = ChatOption.custom().stream(true).build();
    Choir<ChatMessage> choir = chatModel.generate(ChatMessages.from(query), option);
    // ⚠️ 关键：使用 JdkFlowAdapter 转换
    return JdkFlowAdapter.flowPublisherToFlux(choir.map(ChatMessage::text));
}
```

### 4. 工具实现（Agent）

#### 4.1 工具实现

**⚠️ 非常重要**：工具实现类必须使用 **FIT 的 `@Component`**，不能使用 Spring 的 `@Component`。

```java
package com.example.tools;

import modelengine.fitframework.annotation.Component;  // ← FIT 的注解

@Component  // ← 注意：这是 modelengine.fitframework.annotation.Component
@Group(name = "example")
public class WeatherTool {
    @Fitable("default")
    @ToolMethod(description = "查询城市天气")
    public String getWeather(String city) {
        return city + " 的天气是晴天";
    }
}
```

**使用 Spring 的 `@Component` 会导致工具无法被发现，报错**：
```
FitableNotFoundException: No fitables. [genericableId=com.example.tools.WeatherTool]
```

#### 4.2 工具实现类手动注册

在 `application.yml` 中把工具实现类的 FIT Bean 手动注册 spring 里：

```yaml
fit:
  beans:
    packages:
      - 'com.example.tools'  # 工具类所在包
```

#### 4.3 添加 Maven 插件

在 `pom.xml` 中添加 plugin，build 时自动在 `target/classes` 目录里生成工具文件`tools.json`

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.fitframework.fel</groupId>
            <artifactId>tool-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>build-tool</id>
                    <goals>
                        <goal>build-tool</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 三、常见问题

### 1. IDE 显示 "Could not autowire" 警告

**问题**：
```
Could not autowire. No beans of 'ChatModel' type found.
```

**原因**：FEL Bean 是运行时动态注册的，IDE 编译期无法感知。

**解决**：添加注解消除 IDE 警告

```java
@RestController
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")  // ← 必须添加
public class MyController {
    private final ChatModel chatModel;
    // ...
}
```

### 2. 流式响应没有输出

**检查清单**：
- ✅ 使用了 `JdkFlowAdapter.flowPublisherToFlux()` 转换
- ✅ 设置了 `produces = "text/event-stream;charset=UTF-8"`
- ✅ ChatOption 设置了 `.stream(true)`

### 3. 工具无法被发现

**错误**：
```
FitableNotFoundException: No fitables. [genericableId=xxx]
```

**原因**：最常见的原因是使用了 **Spring 的 `@Component`** 而不是 **FIT 的 `@Component`**。

**解决步骤**：

1. ✅ 确认工具类使用 **FIT 的 `@Component`**：
   ```java
   import modelengine.fitframework.annotation.Component;  // ← 正确
   // import org.springframework.stereotype.Component;    // ← 错误

   @Component  // ← FIT 的注解
   public class MyTool { }
   ```

2. ✅ 配置 `fit.beans.packages` 扫描：
   ```yaml
   fit:
     beans:
       packages:
         - 'com.example.tools'
   ```

3. ✅ 添加 `tool-maven-plugin`

4. ✅ 执行 `mvn compile` 生成 `tools.json`

### 4. ObjectSerializer 注入失败

**错误**：
```
No qualifying bean of type 'ObjectSerializer' available
```

**解决**：在构造器注入时添加 `@Qualifier("json")`

```java
public MyController(
    ChatModel chatModel,
    @Qualifier("json") ObjectSerializer serializer  // ← 必须添加
) {
    this.chatModel = chatModel;
    this.serializer = serializer;
}
```

---

## 总结

在 Spring Boot 中使用 FEL 的关键点：

1. ✅ 添加 `fel-spring-boot-starter` 依赖
2. ✅ 配置 `fel.openai.api-key`
3. ✅ 注入时添加 `@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")`
4. ✅ 流式响应必须用 `JdkFlowAdapter.flowPublisherToFlux()`
5. ✅ ObjectSerializer 注入需要 `@Qualifier("json")`
6. ✅ **工具类必须使用 FIT 的 `@Component`，不能用 Spring 的**

FEL 功能的详细使用（ChatModel、RAG、Agent 等）请参考 FEL 官方文档。
