# FIT Framework 插件启动顺序说明

## 概述

本文档记录了 `build/plugins/` 目录下所有插件的启动顺序。当应用以 JAR 包方式运行时，FIT 框架会按照 **Level**
值从小到大的顺序加载这些插件。

**build/plugins 插件统计**:

- **FEL 插件**: 9个（本文档详细说明）
- **FIT 内置插件**: 20个（详见 `framework/fit/java/fit-builtin/plugins/FIT插件启动顺序说明.md`）

**启动规则**:

- **数值越小，启动优先级越高**
- **同一 Level 内按文件名字母顺序加载**
- **category=SYSTEM 的插件先于 USER 插件加载**

---

## 启动流程图

```
按以下顺序加载 29 个插件:
├─ Level 1 (最先) ────────────────────────────
│  └─ fel-tokenizer-hanlp-plugin
│
├─ Level 4 (基础设施层) ──────────────────────
│  ├─ FEL 插件 (3个)
│  │  ├─ fel-tool-factory-repository
│  │  ├─ fel-tool-repository-simple
│  │  └─ fel-langchain-runnable
│  └─ FIT 内置插件 (20个，详见FIT文档)
│
├─ Level 5 (功能层) ──────────────────────────
│  └─ FEL 工具链 (4个)
│     ├─ fel-tool-discoverer
│     ├─ fel-tool-executor
│     ├─ fel-tool-mcp-client
│     └─ fel-tool-mcp-server
│
└─ Level 7 (最后) ────────────────────────────
   └─ fel-model-openai-plugin
```

---

## FEL插件清单（按启动顺序）

### Level 1 - 第一批启动（1个插件）

| 序号 | 插件名称                         | Category | 说明        |
|----|------------------------------|----------|-----------|
| 1  | `fel-tokenizer-hanlp-plugin` | user     | HanLP 分词器 |

**启动时机**: 应用启动后最先加载，提供基础分词功能。

---

### Level 4 - 第二批启动（23个插件）

#### FEL 插件（3个）

| 序号 | 插件名称                          | Category | 说明              |
|----|-------------------------------|----------|-----------------|
| 1  | `fel-langchain-runnable`      | user     | LangChain 运行时支持 |
| 2  | `fel-tool-factory-repository` | system   | 工具工厂仓库          |
| 3  | `fel-tool-repository-simple`  | system   | 工具仓库（简单实现）      |

#### FIT 内置插件（20个）

Level 4 还包含 20 个 FIT 框架内置插件（category=system），提供 HTTP 服务、序列化、验证、服务发现等核心功能。

**FIT 插件详情**: 请参见 `framework/fit/java/fit-builtin/plugins/FIT插件启动顺序说明.md`

**加载顺序说明**:

- 先加载所有 category=SYSTEM 的插件（按文件名字母顺序）
  - FIT 内置插件（20个，以 fit- 开头）
  - FEL 工具仓库（2个）
- 再加载 category=USER 的插件
  - fel-langchain-runnable

---

### Level 5 - 第三批启动（4个插件）

| 序号 | 插件名称                  | Category | 说明      |
|----|-----------------------|----------|---------|
| 1  | `fel-tool-discoverer` | system   | 工具发现器   |
| 2  | `fel-tool-executor`   | system   | 工具执行器   |
| 3  | `fel-tool-mcp-client` | system   | MCP 客户端 |
| 4  | `fel-tool-mcp-server` | system   | MCP 服务端 |

**启动时机**: Level 4 的工具仓库加载完成后启动。这些插件依赖 Level 4 的 `fel-tool-repository-simple` 和
`fel-tool-factory-repository`，提供工具发现、执行和 MCP 协议支持。

---

### Level 7 - 最后启动（1个插件）

| 序号 | 插件名称                      | Category | 说明          |
|----|---------------------------|----------|-------------|
| 1  | `fel-model-openai-plugin` | system   | OpenAI 模型集成 |

**启动时机**: 所有基础设施和工具链加载完成后最后启动，确保环境完全就绪。