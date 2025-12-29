# FIT Framework 组件启动顺序说明

## 概述

本文档记录了 FIT Framework 项目中所有组件的启动顺序（level）配置。Level 值决定了组件在系统启动时的加载顺序，**数值越小，启动优先级越高
**。

**相关文档**:

- FIT 内置插件详细信息请参见：`framework/fit/java/fit-builtin/plugins/FIT组件启动顺序说明.md`

## 启动顺序规则

- **Level 1**: 最先启动（优先级最高）
- **Level 4**: 第二优先级
- **Level 5**: 第三优先级
- **Level 7**: 最后启动（优先级最低）

## 启动流程图

```
启动时间轴 (从上到下)
│
├─ Level 1 (最先) ────────────────────────────
│  ├─ fel-tokenizer-hanlp-plugin (分词器)
│  └─ plugin-log (日志插件)
│
├─ Level 4 (基础设施层) ──────────────────────
│  ├─ fit-plugin-parent (22个FIT内置插件)
│  ├─ fit-validation-hibernate-jakarta (验证)
│  ├─ fit-validation-hibernate-javax (验证)
│  ├─ fel-tool-repository-simple (工具仓库)
│  └─ fel-tool-factory-repository (工具工厂仓库)
│
├─ Level 5 (功能层) ──────────────────────────
│  ├─ fel-tool-discoverer (工具发现器)
│  ├─ fel-tool-executor (工具执行器)
│  ├─ fel-tool-mcp-client (MCP客户端)
│  └─ fel-tool-mcp-server (MCP服务端)
│
└─ Level 7 (最后) ────────────────────────────
   └─ fel-model-openai-plugin (OpenAI模型)
```

**说明**: Level 4 中的 FIT 内置插件（22个）详情请参见 `framework/fit/java/fit-builtin/plugins/FIT组件启动顺序说明.md`

---

## 按启动顺序排列的组件列表

### Level 1 - 最高优先级（用户级基础组件）

| 序号 | 组件名称                       | 类别   | 路径                                                        | 说明               |
|----|----------------------------|------|-----------------------------------------------------------|------------------|
| 1  | fel-tokenizer-hanlp-plugin | user | framework/fel/java/fel-community/tokenizer-hanlp          | FEL 分词器（HanLP）插件 |
| 2  | plugin-log                 | user | examples/fit-example/05-aop-log-plugin/plugins/plugin-log | AOP 日志插件示例       |

**启动说明**: 这些组件最先启动，提供基础的分词和日志功能。

---

### Level 4 - 第二优先级（基础设施层）

| 序号 | 组件名称                             | 类别     | 路径                                                                      | 说明                  |
|----|----------------------------------|--------|-------------------------------------------------------------------------|---------------------|
| 1  | fit-plugin-parent                | system | framework/fit/java/fit-builtin/plugins                                  | FIT 内置插件父模块（22个子插件） |
| 2  | fit-validation-hibernate-jakarta | system | framework/fit/java/fit-builtin/plugins/fit-validation-hibernate-jakarta | Jakarta 验证插件        |
| 3  | fit-validation-hibernate-javax   | system | framework/fit/java/fit-builtin/plugins/fit-validation-hibernate-javax   | Javax 验证插件          |
| 4  | fel-tool-repository-simple       | system | framework/fel/java/plugins/tool-repository-simple                       | FEL 工具仓库（简单实现）      |
| 5  | fel-tool-factory-repository      | system | framework/fel/java/plugins/tool-factory-repository                      | FEL 工具工厂仓库          |

**启动说明**: 基础设施层，包括 FIT 验证组件和 FEL 工具仓库。这些组件提供接口实现，被上层组件依赖。

**FIT 插件详情**: 关于 FIT 内置插件的详细信息，请参见 `framework/fit/java/fit-builtin/plugins/组件启动顺序说明.md`

---

### Level 5 - 第三优先级（FEL 工具链功能层）

| 序号 | 组件名称                | 类别     | 路径                                         | 说明            |
|----|---------------------|--------|--------------------------------------------|---------------|
| 1  | fel-tool-discoverer | system | framework/fel/java/plugins/tool-discoverer | FEL 工具发现器     |
| 2  | fel-tool-executor   | system | framework/fel/java/plugins/tool-executor   | FEL 工具执行器     |
| 3  | fel-tool-mcp-client | system | framework/fel/java/plugins/tool-mcp-client | FEL MCP 客户端工具 |
| 4  | fel-tool-mcp-server | system | framework/fel/java/plugins/tool-mcp-server | FEL MCP 服务端工具 |

**启动说明**: FEL 工具链功能层，依赖 Level 4 的仓库组件。提供工具发现、执行和 MCP 协议支持。

---

### Level 7 - 最低优先级（模型集成组件）

| 序号 | 组件名称                    | 类别     | 路径                                            | 说明              |
|----|-------------------------|--------|-----------------------------------------------|-----------------|
| 1  | fel-model-openai-plugin | system | framework/fel/java/fel-community/model-openai | FEL OpenAI 模型插件 |

**启动说明**: 模型集成组件最后加载，确保所有基础设施和工具链已就绪。

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