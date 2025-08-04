<div align="center">
  <img src="docs/resources/fit-logo.png" alt="FIT Logo" width="395">

# FIT Framework v3.5.0-SNAPSHOT

**Java 企业级 AI 开发框架，提供多语言函数引擎（FIT）、流式编排引擎（WaterFlow）及 Java 生态的 LangChain 替代方案（FEL）。原生 /
Spring 双模运行，支持插件热插拔与智能聚散部署，无缝统一大模型与业务系统。**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/license/MIT)
[![JDK](https://img.shields.io/badge/JDK-17-green.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
</div>

----------

# FIT: 重新定义 AI 工程化的三维坐标系

## 核心架构

1. **FIT Core：语言无界，算力随需**

   多语言函数计算底座（Java/Python/C++）支持插件化热插拔，独创智能聚散部署——代码无需修改，单体应用与分布式服务一键切换，运行时自动路由本地调用或
RPC，让基础设施成为「隐形的伙伴」。

2. **WaterFlow Engine：流式智能，万物可编排**

   打破 BPM 与响应式编程的次元壁，图形化编排与声明式 API 双模驱动。业务逻辑可像乐高组合般动态拼接，从毫秒级微流程到跨系统长事务，皆以统一范式驾驭。

3. **FEL (FIT Expression for LLM)：Java 生态的 LangChain 革命**

   当 Python 阵营的 LangChain 重塑 AI 应用开发时，FEL 为 Java 开发者带来了更符合工程化实践的答案——基于标准化原语封装大模型、知识库与工具链，让
AI 能力真正融入 Java 技术栈的血脉。

- retrieve 样例

``` java
AiProcessFlow<Tip, Content> retrieveFlow = AiFlows.<Tip>create()
        .runnableParallel(history(), passThrough())
        .conditions()
        .match(tip -> !tip.freeze().get(DEFAULT_HISTORY_KEY).text().isEmpty(),
                node -> node.prompt(Prompts.human(REWRITE_PROMPT))
                        .generate(chatFlowModel)
                        .map(ChatMessage::text))
        .others(node -> node.map(tip -> tip.freeze().get("query").text()))
        .retrieve(new DefaultVectorRetriever(vectorStore, SearchOption.custom().topK(1).build()))
        .synthesize(docs -> Content.from(docs.stream().map(Document::text).collect(Collectors.joining("\n\n"))))
        .close();
```

- agent 样例

``` java
AiProcessFlow<String, ChatMessage> agentFlow = AiFlows.<String>create()
        .map(query -> Tip.fromArray(query))
        .prompt(Prompts.human("{{0}}"))
        .delegate(agent)
        .close();
```

## 设计哲学

- 约定优于配置的工程实践

  FIT 通过智能约定大幅减少胶水代码：

    - 部署无感化：聚散模式自动识别，开发者只需声明业务关系，无需手工标注远程/本地调用
    - 协议透明化：HTTP/gRPC/共享内存等通信方式由框架按上下文智能选择 
    - 资源自管理：插件依赖自动注入，服务发现与熔断机制内置实现

  这种「智能契约」机制，使得80%的通用场景实现零配置，同时保留20%复杂场景的深度定制能力。

## 为什么工程师选择 FIT ？

- 填补空白：首个面向 Java 生态的 AI 全栈框架，让 Java 开发者无需切换技术栈即可构建现代 AI 应用
- 拒绝妥协：既保有 Python 生态的敏捷性，又继承 Java 体系的高性能与工程化优势
- 面向未来：从单机原型到云原生集群，架构弹性随业务共同进化

----------

## 环境配置

开发环境配置

- 开发环境：`IntelliJ IDEA`
- Java 17
- 代码格式化文件：[CodeFormatterFromIdea.xml](CodeFormatterFromIdea.xml)
- `Maven` 配置：推荐版本 Maven 3.8.8+

**构建命令**

```
mvn clean install
```

**输出目录**

```
build/
```

**增加权限**

```
chmod +x build/bin/*
```

**启动命令**

```
mkdir dynamic-plugins
cd dynamic-plugins
../build/bin/fit start
```

> 以上编译构建出的 `fit` 命令可以通过系统操作（别名或添加系统路径）来简化输入。

**配置系统环境变量及创建插件目录**

- 首先用 `maven` 编译打包 `./framework/fit/java`，将 `build` 目录内容存储在本地 `fitframework` 目录下，此目录为 FIT 核心框架目录地址。
- 配置 `FIT` 框架目录的系统环境变量，变量值为 `FIT` 核心框架目录地址，使 `fit` 命令可执行。例如 `FIT` 核心框架位置在
  `/demo/fitframework`，则变量值配置为 `/demo/fitframework`。
- 新建任意目录作为插件目录，在该目录下存放插件，可在插件目录下使用命令 `fit start` 启动服务。

> 以上环境配置步骤请根据使用的操作系统使用相应的路径分隔符和环境变量配置操作。
> 
> `fit` 命令无法在包含 FIT 框架的目录中执行，只能够在一个新建的动态插件目录下执行。

## 快速开始

- FIT 函数框架
  - 请参考 [FIT 快速开始](framework%2Ffit%2Fjava%2FREADME.md)，该指南将简单介绍 FIT 的核心设计概念，并指导您构建基础的应用。
- WaterFlow 流调度引擎
  - 请参考 [WaterFlow 快速开始](framework%2Fwaterflow%2Fjava%2Fwaterflow-core%2FREADME.md)，该指南将简单介绍 WaterFlow
    声明式语法，并构建流程输出 `hello world！`。
- FEL 标准原语
  - 请参考 [FEL 快速开始](docs/framework/fel/java/quick-start-guide/01.%20模型.md)，该指南将简要介绍如何使用 FEL
    构建端到端的大模型应用程序。

## 文档

您可以从 `docs` 目录查看项目的完整文档，文档包含框架的快速入门指南和用户指导手册，并以一个基于本框架开发的大模型应用编排平台（Model
Engine）为例，向您介绍本框架在商业化的成熟产品中是如何应用的。

- [ModelEngine 技术白皮书](docs/model-engine-technical-white-paper/00.%20摘要.md)
- [FIT 快速入门指南](docs/framework/fit/java/quick-start-guide/01.%20构建基础%20Web%20应用.md)、[用户指导手册](docs/framework/fit/java/user-guide-book/01.%20插件%E3%80%81IoC%20容器和%20Bean.md)
- [Waterflow 快速入门指南](docs/framework/waterflow/java/quick-start-guide/01.%20介绍.md)、[用户指导手册](docs/framework/waterflow/java/user-guide-book.md)
- [FEL 快速入门指南](docs/framework/fel/java/quick-start-guide/01.%20模型.md)、[用户指导手册](docs/framework/fel/java/user-guide-book/01.%20AI%20流程.md)

## 贡献

欢迎贡献者加入本项目。
请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)，这将指导您完成分支管理、标签管理、提交规则、代码审查等内容。遵循这些指导有助于项目的高效开发和良好协作。

## 联系我们

1. 如果发现问题，可以在该项目的 `Issue` 模块内提出。
2. 微信公众号：`FitFramework`。
3. 微信技术交流群：通过公众号菜单“技术交流”点击获取最新群二维码。
4. QQ技术交流群：`1029802553`。

![wechat-gh](docs/resources/qrcode_for_wechat_gh.png)
![qq-01](docs/resources/qrcode_for_qq_01.png)
