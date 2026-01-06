# fel-flow

## 介绍

fel-flow 是一个基于 waterflow 的 AI 流程编排引擎。在 waterflow 水流式流程编排能力的基础上，fel-flow 增强了 AI 应用场景的支持，提供了大模型调用、RAG（检索增强生成）、提示词管理、会话管理等能力，使得构建复杂的 AI 应用流程更加简单。

核心特点：
1. 完全兼容 waterflow 的所有操作（just、map、reduce、conditions、parallel 等）
2. 增加 AI 专用能力（generate、prompt、retrieve、delegate 等）
3. 支持流式和阻塞式大模型调用
4. 支持完整的 RAG 流程（load、split、index、retrieve、synthesize）
5. 支持流程委托和子流程调用
6. 支持会话管理和上下文绑定

## 包结构

fel-flow 基于分层架构设计，主要包含以下几个核心包：

### activities 节点层
定义流程中的各种节点类型，是构建 AI 流程的基础组件：
- **AiStart/AiState**: 流程的开始节点和中间节点，提供数据转换、处理、聚合等能力
- **AiConditions**: 条件分支节点，支持 match/when 条件匹配
- **AiParallel/AiFork**: 并行处理节点，支持多分支并发执行和结果汇聚
- **AiDataStart**: 数据优先流节点，用于从数据直接启动流程

### flows 流程层
管理流程的生命周期和执行：
- **AiFlows**: 流程工厂类，提供流程创建入口（`AiFlows.create()`）
- **AiProcessFlow**: 可处理流程，支持数据投递和流程执行
- **Conversation**: 会话流程，提供会话级别的上下文管理和回调能力

### operators 操作符层
提供各类操作符的抽象和实现：
- **models**: 大模型调用接口（FlowModel 流式、ChatBlockModel 阻塞式）
- **patterns**: 模式抽象（FlowPattern），支持自定义处理逻辑和子流程委托
- **prompts**: 提示词管理（PromptTemplate、Prompts），用于构建模型输入
- **sources**: 数据源接口，用于 RAG 流程的数据加载

### util 工具层
- **AiFlowSession**: 统一的会话管理，负责 Pattern 执行时的上下文传递
- **StateKey**: 会话状态键定义

**设计理念**：
fel-flow 在 waterflow 的基础上，通过装饰器模式增强节点能力，所有 Ai* 节点都是对 waterflow 节点的包装。这样既保持了与 waterflow 的兼容性，又能无缝集成 AI 相关能力（模型调用、提示词、RAG 等）。

## 入门示例

创建一个简单的 AI 对话流程，接收用户问题，调用大模型生成回答：

```java
AiProcessFlow<String, ChatMessage> flow = AiFlows.<String>create()
        .prompt(question -> Prompts.user(question))
        .generate(model)
        .close(message -> System.out.println(message.getContent()));

flow.offer("什么是人工智能？");
```

## 操作方法

### 基础数据处理

fel-flow 完全支持 waterflow 的所有基础操作，以下方法与 waterflow 用法完全一致：

#### just

`AiState<O, D, O, RF, F> just(Operators.Just<O> processor)`

生成一个节点，只处理数据，不进行类型转换。

示例（打印接收到的问题）：

```java
AiFlows.<String>create()
        .just(question -> System.out.println("收到问题: " + question))
        .close()
        .offer("什么是机器学习？");
```

`AiState<O, D, O, RF, F> just(Operators.ProcessJust<O> processor)`

生成一个节点，处理器内可消费自定义上下文（通过 `Conversation.bind` 绑定）。

#### map

`<R> AiState<R, D, O, RF, F> map(Operators.Map<O, R> processor)`

生成一个节点，根据用户提供的处理操作将数据转换为新的类型。

示例（将问题转换为大写）：

```java
AiFlows.<String>create()
        .map(question -> question.toUpperCase())
        .close()
        .offer("什么是深度学习？");
```

`<R> AiState<R, D, O, RF, F> map(Operators.ProcessMap<O, R> processor)`

生成一个节点，处理器内可消费自定义上下文。

#### flatMap

`<R> AiState<R, D, O, RF, F> flatMap(AiFlatMap<O, R> processor)`

生成一个节点，对数据进行一对多的转换。

示例（将一个问题拆分为多个子问题）：

```java
AiFlows.<String>create()
        .flatMap(question -> {
            String[] subQuestions = question.split("和|与|及");
            return new AiDataStart<>(
                AiFlows.<String>create().map(q -> q.trim()),
                subQuestions
            );
        })
        .close()
        .offer("什么是机器学习和深度学习？");
```

#### reduce

`<R> AiState<R, D, O, RF, F> reduce(Supplier<R> init, Operators.Reduce<O, R> processor)`

生成一个数据聚合节点，将每个数据通过指定的方式进行合并。

示例（汇总多个模型的回答）：

```java
AiFlows.<String>create()
        .window(3)
        .reduce(() -> new StringBuilder(), (acc, answer) -> {
            acc.append(answer).append("\n");
            return acc;
        })
        .map(StringBuilder::toString)
        .close()
        .offer(new String[] {"答案1", "答案2", "答案3"});
```

`<R> AiState<R, D, O, RF, F> reduce(Supplier<R> init, Operators.ProcessReduce<O, R> processor)`

生成一个数据聚合节点，处理器内可消费自定义上下文。

#### window

`AiState<O, D, O, RF, F> window(Operators.WindowCondition windowCondition)`

生成一个窗口节点，当窗口中的数据满足条件后关闭窗口，后续的聚合节点才会将聚合的数据往下发送。

示例（每 3 个问题批量处理一次）：

```java
AiFlows.<String>create()
        .window(3)
        .buffer(3)
        .just(questions -> System.out.println("批量处理: " + questions))
        .close()
        .offer(new String[] {"问题1", "问题2", "问题3", "问题4"});
```

`AiState<O, D, O, RF, F> window(int count)`

按照数量创建窗口，简化版本。

#### keyBy

`<R> AiState<Tuple<R, O>, D, O, RF, F> keyBy(Operators.Map<O, R> keyBy)`

设置分组聚合的键，后续的聚合操作按指定的键分组处理。

示例（按照问题类型分组处理）：

```java
AiFlows.<Question>create()
        .keyBy(Question::getCategory)
        .buffer(3)
        .map(list -> processQuestionsByCategory(list))
        .close()
        .offer(questions);
```

#### buffer

`AiState<List<O>, D, O, RF, F> buffer(int size)`

按指定容量缓存流中的数据，为后续节点提供缓存的数据列表。

示例（每 5 个数据批量处理）：

```java
AiFlows.<String>create()
        .buffer(5)
        .map(list -> String.join(", ", list))
        .close()
        .offer(dataArray);
```

#### process

`<R> AiState<R, D, O, RF, F> process(Operators.Process<O, R> processor)`

生成自定义数据处理器，支持往后续节点发射自定义数据。

### 条件分支

#### conditions

`AiConditions<D, O, RF, F> conditions()`

创建条件节点，用于根据不同条件执行不同的处理逻辑。

示例（根据问题类型路由到不同处理逻辑）：

```java
AiFlows.<Question>create()
        .conditions()
        .match(q -> q.getType().equals("factual"),
            node -> node
                .prompt(q -> Prompts.system("你是一个百科知识专家"))
                .prompt(q -> Prompts.user(q.getContent()))
                .generate(factualModel)
        )
        .match(q -> q.getType().equals("creative"),
            node -> node
                .prompt(q -> Prompts.system("你是一个创意写作专家"))
                .prompt(q -> Prompts.user(q.getContent()))
                .generate(creativeModel)
        )
        .others(node -> node
            .prompt(q -> Prompts.user(q.getContent()))
            .generate(defaultModel)
        )
        .close()
        .offer(question);
```

### 并行处理

#### parallel / fork / join

`AiParallel<D, I, RF, F> parallel()`

开启平行节点，后续可以通过 fork 创建平行分支。

`<O> AiFork<O, D, I, RF, F> fork(AiBranchProcessor<O, D, I, RF, F> processor)`

创建一个平行分支节点。

`<R> AiState<R, D, O, RF, F> join(Supplier<R> init, Operators.Reduce<O, R> processor)`

汇聚所有平行分支的结果。

示例（同时调用多个模型并汇总结果）：

```java
AiFlows.<String>create()
        .parallel()
        .fork(node -> node
            .prompt(q -> Prompts.system("模型A"))
            .prompt(q -> Prompts.user(q))
            .generate(modelA)
        )
        .fork(node -> node
            .prompt(q -> Prompts.system("模型B"))
            .prompt(q -> Prompts.user(q))
            .generate(modelB)
        )
        .fork(node -> node
            .prompt(q -> Prompts.system("模型C"))
            .prompt(q -> Prompts.user(q))
            .generate(modelC)
        )
        .join(Tip::new, (acc, message) -> {
            acc.put(message.getModel(), message.getContent());
            return acc;
        })
        .close()
        .offer("什么是量子计算？");
```

### AI 专用操作

#### prompt

`AiState<Prompt, D, O, RF, F> prompt(PromptTemplate<O>... templates)`

通过提示模板生成大模型的输入提示词。

示例（构建多轮对话提示词）：

```java
AiFlows.<Question>create()
        .prompt(q -> Prompts.system("你是一个AI助手"))
        .prompt(q -> Prompts.user(q.getContent()))
        .generate(model)
        .close()
        .offer(question);
```

#### generate (阻塞式)

`<M extends ChatMessage> AiState<M, D, O, RF, F> generate(BlockModel<O, M> model)`

生成大模型阻塞调用节点，等待模型返回完整结果。

示例（阻塞式调用大模型）：

```java
AiFlows.<String>create()
        .prompt(question -> Prompts.user(question))
        .generate(blockModel)
        .map(ChatMessage::getContent)
        .close(answer -> System.out.println("回答: " + answer))
        .offer("什么是神经网络？");
```

#### generate (流式)

`<M extends ChatMessage> AiState<ChatMessage, D, O, RF, F> generate(FlowModel<O, M> model)`

生成大模型流式调用节点，实时接收模型生成的 token 流。

示例（流式调用大模型）：

```java
AiFlows.<String>create()
        .prompt(question -> Prompts.user(question))
        .generate(flowModel)
        .just(chunk -> System.out.print(chunk.getContent()))
        .close()
        .offer("请解释一下Transformer架构");
```

#### parse

`<R> AiState<R, D, O, RF, F> parse(Parser<O, R> parser)`

将模型返回值进行格式化解析，例如解析为 JSON 对象。

示例（解析模型返回的 JSON）：

```java
AiFlows.<String>create()
        .prompt(question -> Prompts.user("请用JSON格式回答: " + question))
        .generate(model)
        .parse(message -> jsonParser.parse(message.getContent(), Answer.class))
        .close(answer -> System.out.println(answer))
        .offer("什么是自然语言处理？");
```

#### delegate (委托给 Pattern)

`<R> AiState<R, D, O, RF, F> delegate(Pattern<O, R> pattern)`

将数据委托给 Pattern 处理，处理后的数据会发送回该节点。

示例（委托给自定义 Pattern）：

```java
Pattern<String, String> translationPattern = input -> translator.translate(input);

AiFlows.<String>create()
        .delegate(translationPattern)
        .close()
        .offer("Hello World");
```

`<R> AiState<R, D, O, RF, F> delegate(Operators.ProcessMap<O, R> operator)`

委托给自定义操作器，操作将在独立线程池中执行，适合 IO 密集型任务。

#### delegate (委托给子流程)

`<R> AiState<R, D, O, RF, F> delegate(AiProcessFlow<O, R> aiFlow)`

将数据委托给子流程处理。

示例（委托给翻译子流程）：

```java
AiProcessFlow<String, String> translationFlow = AiFlows.<String>create()
        .prompt(text -> Prompts.user("请将以下文本翻译成中文: " + text))
        .generate(model)
        .map(ChatMessage::getContent)
        .close();

AiFlows.<String>create()
        .delegate(translationFlow)
        .close(result -> System.out.println("翻译结果: " + result))
        .offer("Artificial Intelligence");
```

`<R> AiState<R, D, O, RF, F> delegate(AiProcessFlow<O, R> aiFlow, String nodeId)`

将数据委托给子流程的指定节点开始处理。

#### runnableParallel

`AiState<Tip, D, Tip, RF, F> runnableParallel(Pattern<O, Tip>... patterns)`

生成平行分支节点，每个分支将输出一个键值对，最终汇总为一个 Tip 对象。

示例（并行调用多个 Pattern）：

```java
AiFlows.<String>create()
        .runnableParallel(
            input -> Tip.of("result1", process1(input)),
            input -> Tip.of("result2", process2(input)),
            input -> Tip.of("result3", process3(input))
        )
        .close(tip -> System.out.println(tip))
        .offer("input data");
```

### RAG 相关操作

#### load

`AiState<List<Document>, D, O, RF, F> load(Source<O> source)`

生成数据加载节点，从数据源加载文档。

示例（加载文档）：

```java
AiFlows.<String>create()
        .load(filePath -> documentLoader.load(filePath))
        .close()
        .offer("docs/readme.md");
```

#### split

`AiState<Document, D, O, RF, F> split(Splitter<O> splitter)`

生成文本切分节点，将文档切分为多个片段。

示例（切分文档）：

```java
AiFlows.<List<Document>>create()
        .split(docs -> textSplitter.split(docs, 500, 50))
        .close()
        .offer(documents);
```

#### index

`AiState<O, D, O, RF, F> index(Store<O> store)`

生成索引节点，将数据存储到向量数据库。

示例（索引文档）：

```java
AiFlows.<Document>create()
        .index(doc -> vectorStore.add(doc))
        .close()
        .offer(documentChunks);
```

#### retrieve

`<M extends Measurable> AiState<List<M>, D, O, RF, F> retrieve(Retriever<O, M> retriever)`

生成数据检索节点，根据查询检索相关文档。

示例（检索相关文档）：

```java
AiFlows.<String>create()
        .retrieve(query -> vectorStore.similaritySearch(query, 5))
        .close()
        .offer("什么是RAG？");
```

#### enhance

`AiState<O, D, O, RF, F> enhance(PostProcessor<O>... processors)`

生成数据增强节点，用于检索后置处理。

示例（重排序检索结果）：

```java
AiFlows.<List<Document>>create()
        .enhance(docs -> reranker.rerank(docs))
        .close()
        .offer(retrievedDocs);
```

#### synthesize

`AiState<Content, D, O, RF, F> synthesize(Synthesizer<O> synthesizer)`

生成检索合成节点，将检索结果合成为上下文。

示例（合成检索上下文）：

```java
AiFlows.<List<Document>>create()
        .synthesize(docs -> contextBuilder.build(docs))
        .close()
        .offer(documents);
```

### 完整 RAG 示例

```java
AiFlows.<String>create()
        .retrieve(query -> vectorStore.similaritySearch(query, 5))
        .enhance(docs -> reranker.rerank(docs))
        .synthesize(docs -> contextBuilder.build(docs))
        .prompt(context -> Prompts.system("参考以下上下文回答问题:\n" + context))
        .prompt((context, session) -> {
            String question = session.get("question");
            return Prompts.user(question);
        })
        .generate(model)
        .close(message -> System.out.println(message.getContent()))
        .offer("什么是检索增强生成？");
```

### 流程控制

#### id

`AiState<O, D, I, RF, F> id(String id)`

设置节点别名，便于调试和流程跳转。

示例：

```java
AiFlows.<String>create()
        .id("input-validation")
        .map(validator::validate)
        .id("model-inference")
        .generate(model)
        .close()
        .offer(input);
```

#### concurrency

`AiState<O, D, I, RF, F> concurrency(int concurrency)`

设置节点的最大并发处理数。

示例（限制并发调用模型）：

```java
AiFlows.<String>create()
        .prompt(question -> Prompts.user(question))
        .concurrency(5)  // 最多 5 个并发
        .generate(model)
        .close()
        .offer(questions);
```

#### block

`AiState<O, D, O, RF, F> block(BlockToken<O> block)`

生成一个阻塞节点，用于系统或人为介入。

示例（人工审核节点）：

```java
BlockToken<String> humanReview = new BlockToken<String>() {
    @Override
    public boolean verify(String data) {
        return isApproved(data);
    }
};

AiFlows.<String>create()
        .generate(model)
        .block(humanReview)
        .close()
        .offer(question);

// 审核通过后继续
humanReview.resume();
```

#### doOnError

`AiState<O, D, I, RF, F> doOnError(Operators.ErrorHandler<I> handler)`

设置当前节点发生异常时的响应。

示例（处理模型调用异常）：

```java
AiFlows.<String>create()
        .generate(model)
        .doOnError((exception, retryable, contexts) -> {
            log.error("模型调用失败", exception);
            // 可以选择重试
            if (shouldRetry(exception)) {
                retryable.retry();
            }
        })
        .close()
        .offer(question);
```

#### to

`void to(String id)`

设置流程跳转到指定节点，可用于实现循环。

示例（实现重试逻辑）：

```java
AiFlows.<String>create()
        .id("start")
        .generate(model)
        .conditions()
        .match(result -> needsRetry(result), node -> {
            node.to("start");  // 跳转回开始节点
        })
        .others(node -> node.close())
        .offer(question);
```

#### close

`AiProcessFlow<D, O> close()`

在流程最后添加终止节点。

`AiProcessFlow<D, O> close(Consumer<O> callback)`

在流程最后添加终止节点，并设置成功回调。

`AiProcessFlow<D, O> close(FlowCallBack<O> callback)`

在流程最后添加终止节点，并设置完整的回调（成功、异常、完成）。

示例（设置完整回调）：

```java
AiFlows.<String>create()
        .generate(model)
        .close(FlowCallBack.<ChatMessage>builder()
            .doOnConsume(message -> {
                System.out.println("成功: " + message.getContent());
            })
            .doOnError(exception -> {
                System.err.println("失败: " + exception.getMessage());
            })
            .doOnCompleted(() -> {
                System.out.println("流程结束");
            })
            .build())
        .offer(question);
```

## 流操作

### offer

向流中投递数据。流的运行是异步的。

```java
// 投递单个数据
flow.offer("问题1");

// 投递数组数据
flow.offer(new String[] {"问题1", "问题2", "问题3"});
```

### Conversation 会话管理

Conversation 提供了会话级别的数据管理和回调监听能力。

#### 创建会话

```java
Conversation<String, ChatMessage> conversation = flow.conversation();
```

#### 绑定上下文

```java
conversation.bind("userId", "user123");
conversation.bind("sessionId", "session456");
```

在流程中可以通过 `ProcessMap`、`ProcessJust` 等访问绑定的上下文：

```java
AiFlows.<String>create()
        .map((question, session) -> {
            String userId = session.get("userId");
            return processWithUser(question, userId);
        })
        .close()
```

#### 设置回调

```java
conversation.onConsume(message -> {
    System.out.println("收到结果: " + message.getContent());
});

conversation.onError(exception -> {
    System.err.println("发生错误: " + exception.getMessage());
});

conversation.onCompleted(() -> {
    System.out.println("会话结束");
});
```

#### 发送消息

```java
// 发送单条消息
conversation.send("你好");

// 发送多条消息
conversation.send(new String[] {"问题1", "问题2"});

// 发送并等待结果
ChatMessage result = conversation.sendAndWait("什么是AI？");

// 发送并等待结果（带超时）
ChatMessage result = conversation.sendAndWait("什么是AI？", 30, TimeUnit.SECONDS);
```

### AiDataStart 数据优先流

AiDataStart 用于直接从数据开始启动流程，而不是先定义流程再投递数据。

```java
AiProcessFlow<String, ChatMessage> flow = AiFlows.<String>create()
        .prompt(question -> Prompts.user(question))
        .generate(model)
        .close();

// 创建数据优先流
AiDataStart<ChatMessage, String, String> dataStart =
    new AiDataStart<>(flow.start(), "什么是深度学习？");

// 发射数据
dataStart.offer();
```

## 核心机制

### AiFlowSession 模式应用

AiFlowSession 提供了统一的模式应用机制，所有 Pattern、Model、Retriever 等组件都通过 AiFlowSession 执行，确保：

1. Pattern 可以访问当前流程的 FlowSession
2. Pattern 可以在执行时绑定和访问上下文数据
3. 支持 Pattern 的异步执行和结果回调

### 节点类型层次

```
AiActivity (基类)
    ├── AiStart (开始节点)
    │       └── AiState (中间节点)
    ├── AiConditions (条件节点)
    ├── AiParallel (并行节点)
    └── AiFork (并行分支节点)
```

