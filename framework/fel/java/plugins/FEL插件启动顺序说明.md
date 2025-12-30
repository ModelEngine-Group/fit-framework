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

---

## 启动流程图

**启动顺序（两级排序：category → level）**

```
插件启动顺序（29个插件）
│
├─ Category 1: SYSTEM (id=1) ─────────── 第一优先级，先启动所有系统插件
│  │
│  ├─ Level 4 (22个)
│  │  ├─ FIT 内置插件 (20个，详见FIT文档)
│  │  ├─ fel-tool-factory-repository
│  │  └─ fel-tool-repository-simple
│  │
│  ├─ Level 5 (4个)
│  │  ├─ fel-tool-discoverer
│  │  ├─ fel-tool-executor
│  │  ├─ fel-tool-mcp-client
│  │  └─ fel-tool-mcp-server
│  │
│  └─ Level 7 (1个)
│     └─ fel-model-openai-plugin
│
└─ Category 2: USER (id=2) ─────────── 第二优先级，再启动所有用户插件
   │
   ├─ Level 1 (1个)
   │  └─ fel-tokenizer-hanlp-plugin
   │
   └─ Level 4 (1个)
      └─ fel-langchain-runnable
```

---

## FEL插件清单

### 阶段1: SYSTEM 插件

#### category=SYSTEM, level=4 (2个)

| 插件名称                          | 说明         |
|-------------------------------|------------|
| `fel-tool-factory-repository` | 工具工厂仓库     |
| `fel-tool-repository-simple`  | 工具仓库（简单实现） |

**FIT 内置插件**: Level 4 还包含 20 个 FIT 框架内置插件（category=SYSTEM），提供 HTTP 服务、序列化、验证、服务发现等核心功能。详见 `framework/fit/java/fit-builtin/plugins/FIT插件启动顺序说明.md`

**启动时机**: 第一批启动，提供基础工具仓库能力。

---

#### category=SYSTEM, level=5 (4个)

| 插件名称                  | 说明      |
|-----------------------|---------|
| `fel-tool-discoverer` | 工具发现器   |
| `fel-tool-executor`   | 工具执行器   |
| `fel-tool-mcp-client` | MCP 客户端 |
| `fel-tool-mcp-server` | MCP 服务端 |

**启动时机**: Level 4 的工具仓库加载完成后启动。这些插件依赖 Level 4 的 `fel-tool-repository-simple` 和 `fel-tool-factory-repository`，提供工具发现、执行和 MCP 协议支持。

---

#### category=SYSTEM, level=7 (1个)

| 插件名称                      | 说明          |
|---------------------------|-------------|
| `fel-model-openai-plugin` | OpenAI 模型集成 |

**启动时机**: 所有 SYSTEM 插件中最后启动，确保基础设施和工具链完全就绪。

---

### 阶段2: USER 插件

#### category=USER, level=1 (1个)

| 插件名称                         | 说明        |
|------------------------------|-----------|
| `fel-tokenizer-hanlp-plugin` | HanLP 分词器 |

**启动时机**: 所有 SYSTEM 插件启动完成后，USER 插件中最先加载，提供基础分词功能。

---

#### category=USER, level=4 (1个)

| 插件名称                     | 说明              |
|--------------------------|-----------------|
| `fel-langchain-runnable` | LangChain 运行时支持 |

**启动时机**: USER 插件中 Level 1 启动完成后加载。