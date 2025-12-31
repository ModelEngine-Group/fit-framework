# FEL插件启动顺序说明

## 概述

本文档记录了 FEL 插件的启动顺序。当应用以 JAR 包方式运行时，FIT 框架会按照 **Level**值从小到大的顺序加载这些插件。

**build/plugins 插件统计**:

- **FEL 插件**: 9个（本文档详细说明）
- **FIT 内置插件**: 20个（详见 `framework/fit/java/fit-builtin/plugins/FIT插件启动顺序说明.md`）

**启动规则**:

- **category=SYSTEM (id=1) 的插件先于 USER (id=2) 插件加载**
- **相同 category 内，level 数值越小，启动优先级越高**
- **相同 category 和 level 的插件，加载顺序不确定**

**默认值**：

- 未指定 category 时，默认为 **category=USER**
- 未指定 level 时，默认为 **level=4**

---

## 启动流程图

**启动顺序（两级排序：category → level）**

```
插件启动顺序（29个插件）
│
├─ 阶段1: SYSTEM 插件启动
│  │
│  ├─ Level 4 (21个)
│  │  ├─ FIT 内置插件 (20个，详见FIT文档)
│  │  └─ fel-tool-repository-simple
│  │
│  ├─ Level 5 (5个)
│  │  ├─ fel-tool-discoverer
│  │  ├─ fel-tool-executor
│  │  ├─ fel-tool-factory-repository
│  │  ├─ fel-tool-mcp-client
│  │  └─ fel-tool-mcp-server
│  │
│  └─ Level 7 (1个)
│     └─ fel-model-openai-plugin
│
└─ 阶段2: USER 插件启动

   ├─ Level 1 (1个)
   │  └─ fel-tokenizer-hanlp-plugin
   │
   └─ Level 4 (1个)
      └─ fel-langchain-runnable
```

---

## 插件依赖关系

### 启动时依赖

| 依赖方插件                           | 被依赖插件                                  | 依赖说明                                       |
|---------------------------------|----------------------------------------|--------------------------------------------|
| `fel-tool-discoverer` (Level 5) | `fel-tool-repository-simple` (Level 4) | 在 `onPluginStarted()` 回调时调用 `addTool()` 方法 |

---

## FEL插件清单

### 阶段1: SYSTEM 插件

#### category=SYSTEM, level=4 (1个)

| 插件名称                         | 说明         |
|------------------------------|------------|
| `fel-tool-repository-simple` | 工具仓库（简单实现） |

**FIT 内置插件**: Level 4 还包含 20 个 FIT 框架内置插件（category=SYSTEM），提供 HTTP 服务、序列化、验证、服务发现等核心功能。详见
`framework/fit/java/fit-builtin/plugins/FIT插件启动顺序说明.md`

---

#### category=SYSTEM, level=5 (5个)

| 插件名称                          | 说明      |
|-------------------------------|---------|
| `fel-tool-discoverer`         | 工具发现器   |
| `fel-tool-executor`           | 工具执行器   |
| `fel-tool-factory-repository` | 工具工厂仓库  |
| `fel-tool-mcp-client`         | MCP 客户端 |
| `fel-tool-mcp-server`         | MCP 服务端 |

---

#### category=SYSTEM, level=7 (1个)

| 插件名称                      | 说明          |
|---------------------------|-------------|
| `fel-model-openai-plugin` | OpenAI 模型集成 |

---

### 阶段2: USER 插件

#### category=USER, level=1 (1个)

| 插件名称                         | 说明        |
|------------------------------|-----------|
| `fel-tokenizer-hanlp-plugin` | HanLP 分词器 |

---

#### category=USER, level=4 (1个)

| 插件名称                     | 说明              |
|--------------------------|-----------------|
| `fel-langchain-runnable` | LangChain 运行时支持 |