# waterflow

## 目录

[toc]

## 介绍

waterflow是一个水流式的流程编排引擎。其核心特点是支持有界流和无界流的处理，任意节点均可以单独灌入数据，节点的入参水流和出参水流的关系支持M:N的操作。

## 入门示例

创建一个流用于处理一系列数字，先每个数字加2，然后再将数字转换为字符串，最后将结果输出。

``` java
ProcessFlow<Integer> flow = Flows.<Integer>create()
        .map(i -> i + 2)
        .map(i -> i.toString())
        .close(r -> r.getAll().stream().forEach(i -> System.out.println(i.getData())));
flow.offer(new Integer[] {1, 2, 3, 4, 5});
```

## 操作方法

### just

`State<O, D, O, F> just(Operators.Just<O> processor)`

生成一个节点，只处理，不进行数据转换操作。该节点的入参类型为前一个节点的出参类型。

示例（打印接收到的数据）：

``` java
Flows.<Integer>create()
        .just(System.out::println)
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
```

### map

`<R> State<R, D, O, F> map(Operators.Map<O, R> processor)`

生成一个节点，根据用户提供的处理操作将数据加工后生成新的数据类型。

示例(接收一个数字转换为字符串)：

``` java
Flows.<Integer>create()
        .map(i -> i.toString())
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
```

### flatMap

`<R> State<R, D, O, F> flatMap(Operators.FlatMap<O, R> processor)`

生成一个节点，对数据进行一对多的转换。

示例（将收到的数字按照大小创建对应数量的字符串数据）：

``` java
Flows.<Integer>create()
        .flatMap(num -> {
            String[] maps = new String[num];
            for (int i = 0; i < num; i++) {
                maps[i] = "flat map ";
            }
            return Flows.flux(maps);
        })
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
```

上面提供的操作中将新创建的字符串列表通过`Flows.flux`创建新的流返回。

### reduce

`<R> State<R, D, O, F> reduce(Supplier<R> init, Operators.Reduce<O, R> processor)`

生成一个带初始值的数据聚合节点，将每个数据通过指定的方式进行合并后，形成一个新的数据。

`State<O, D, O, F> reduce(Operators.Reduce<O, O> processor)`

生成一个数据聚合节点，将每个数据通过指定的方式进行合并后，形成一个新的数据。

示例（将数据进行求和）：

``` java
Flows.<Integer>create()
        .reduce(() -> 0, Integer::sum)
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
```

### close

`F close()`

流程创建完毕，相当于给流程上加了终止节点，然后返回创建好的流对象。

`F close(Operators.Just<Callback<FlowContext<O>>> callback)`

流程创建完毕，同时指定数据处理到结束节点的回调处理器，相当于给流程上加了终止节点，然后返回创建好的流对象。

### conditions

`Conditions<D, O, F> conditions()`

开启条件节点，后面可以跟随match方法来添加具体的条件分支节点，最终通过others方法提供一个默认处理并结束条件节点

1. 分支的匹配顺序按照给定match顺序执行，前面的优先级更高。
2. 最后需要other方法来结束该条件节点，然后后面可以再追加其它操作节点。

示例（对于数字数据创建2个处理分支：小于10、小于20、其它处理）：

``` java
Flows.<Integer>create()
        .conditions()
        .match(i -> i < 10, node -> node.map(value -> value + 20))
        .match(i -> i < 20, node -> node.map(value -> value + 10))
        .others(node -> node.just(i -> {}))
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
```

### produce

`<R> State<List<R>, D, ?, F> produce(Operators.Produce<O, R> processor)`

生成一个通用的m->n处理节点，这里处理器处理的入参是List<O>, 出参是List<R>。

示例（将给定的一批数据排序并只保留2个同时转换为字符串）：

``` java
Flows.<Integer>create()
        .produce(list -> list.stream()
                .sorted()
                .limit(2)
                .map(i -> i.toString() + "-okay")
                .collect(Collectors.toList()))
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
```

### block

`State<O, D, O, F> block(BlockToken<O> block)`

生成一个阻塞节点，用于系统或人为介入节点中数据的继续流转。

1. 当数据流转到该节点时，数据将会停下，不再继续向后续节点流转，需要外部调用block.resume()后才能恢复执行。
2. 该操作只能在状态节点后使用，第一个开始节点上不支持该操作。

示例（增加block节点，通过block节点控制数据流转）：

``` java
BlockToken<Integer> block = new BlockToken<Integer>() {
    @Override
    public boolean verify(Integer data) {
        return true;
    }
};
Flows.<Integer>create()
        .map(i -> i + 1)
        .block(block)
        .close()
        .offer(new Integer[] {1, 2, 3, 4, 5});
// 等待数据都走到block节点后执行数据才能走到结束节点
block.resume();
```

### window

`State<O, D, O, F> window(Operators.Window<O> window)`

生成一个window节点，当window中的数据满足给定的条件时才触发数据向后绪节点流转。

1. 同一个window中满足条件的数据往后可以认为是一个批次的数据。
2. 一般情况后面跟随reduce/buffer操作。

示例（一次聚合两条数据进行字符串拼接）：

``` java
Operators.Window<Integer> window = inputs -> inputs.size() == 2;
Flows.<Integer>create()
        .window(window)
        .reduce(() -> "", (acc, value) -> acc + value.toString())
        .close(r -> {
            System.out.println(r.get().getData());
        }).offer(new Integer[] {1, 2, 3, 4, 5, 6});
```

### buffer

`State<List<O>, D, O, F> buffer()`

生成一个数据缓存节点，通用用在window节点后面，当满足window的条件后，为后续节点提供window中缓存的数据列表

示例（一次打印两条数）：

``` java
Operators.Window<Integer> window = inputs -> inputs.size() == 2;
Flows.<Integer>create()
        .map(i -> i.toString())
        .window(window)
        .buffer()
        .map(list -> String.join(",", list))
        .close(r -> System.out.println(r.get().getData()))
        .offer(new Integer[] {1, 2, 3, 4, 5, 6});
```

### keyBy

`<R> State<Tuple<R, O>, D, O, F> keyBy(Operators.Map<O, R> keyGetter)`

生成一个按照指定key聚合处理的节点，这里按照key进行分组，后续的数据处理可以按照分组处理。

1. 这里只是将数据按照指定的key打标，数据聚合时按照key分组处理。
2. 针对有window的情况，是将一个window内的数据按照key分组后交给后续节点处理。

示例（按照指定key分组后拼接输出）：

``` java
Flows.<Data>create()
        .keyBy(Data::getKey)
        .buffer()
        .map(list -> list.stream().map(pair -> pair.second().value).collect(Collectors.joining(",")))
        .close(r -> System.out.println(r.get().getData()))
        .offer(new Data[] {
                Data.from("num", "1"), Data.from("num", "2"), Data.from("num", "3"),
                Data.from("alpha", "A"), Data.from("alpha", "B"), Data.from("alpha", "C")});
```

### parallel

`Parallel<D, O, F> parallel()`

开启平行节点，后续可以通过fork创建平行分支进行数据处理。

1. 后续只能跟随fork操作最终以join结束。

### fork

`<O> Fork<O, D, I, F> fork(Operators.BranchProcessor<O, D, I, F> processor)`

创建一个平行分支节点，在这里可以进行指定的数据处理。

### join

`<R> State<R, D, O, F> join(Supplier<R> init, Operators.Reduce<O, R> processor)`

生成join节点，将平行节点的所有分支进行汇聚，到这里parallel结束，回到一般节点。

示例（将一个数进行两次处理后再汇总结果）：

``` java
Flows.<Integer>create()
        .parallel()
        .fork(node -> node.map(i -> i + 1))
        .fork(node -> node.map(i -> i + 2))
        .join(() -> 0, (acc, i) -> acc + i)
        .close(r -> System.out.println(r.get().getData()))
        .offer(new Integer[] {1, 2, 3, 4, 5, 6});
```

## 流操作

### offer

向流中投递数据。这里需要注意，流的运行是异步的，offer返回的是这次运行的实例ID。

## 核心机制

### FlowSessionCache 会话缓存管理

FlowSessionCache 是 waterflow 的核心资源管理机制，负责统一管理流程执行过程中产生的所有会话相关资源，确保同一批数据的正确汇聚和资源的自动释放。

#### 缓存结构

FlowSessionCache 按照 `flowId -> sessionId -> FlowSessionCache` 的层级结构组织，每个流程会话维护独立的缓存实例，内部管理以下资源：

1. **Session 流转缓存（nextToSessions）**
   - 记录每个节点向下游节点流转数据时使用的 session
   - 以当前窗口的唯一标识（UUID）为索引
   - 确保同一批数据在节点间流转时使用相同的 session 进行汇聚

2. **Emitter 处理缓存（nextEmitterHandleSessions）**
   - 专门用于处理 emitter 操作的 session 缓存
   - 为发射器操作提供独立的会话上下文

3. **FlatMap 窗口缓存（flatMapSourceWindows）**
   - 记录 flatMap 节点产生的源窗口信息
   - 以窗口唯一标识为索引存储 `FlatMapSourceWindow` 实例
   - 用于将 flatMap 操作产生的多个输出数据与原始输入关联

4. **Match 窗口缓存（matchWindows）**
   - 记录条件匹配节点（`conditions`）产生的窗口信息
   - 以 MatchWindow 的唯一标识为索引
   - 用于将条件分支产生的数据进行汇聚

5. **累加器顺序缓存（accOrders）**
   - 记录每个节点的累加操作顺序编号
   - 以节点 ID 为索引，存储递增的序号

#### 资源管理机制

**自动创建与复用**
- 首次访问某个流程会话时，FlowSessionCache 自动创建并初始化
- 相同窗口/会话标识的资源会被复用，避免重复创建
- 通过 `FlowSessionRepo` 提供的静态方法访问各类缓存资源

**会话隔离**
- 不同流程（flowId）的缓存完全隔离
- 同一流程的不同会话（sessionId）也拥有独立的缓存空间
- 避免跨会话或跨流程的数据污染

**生命周期管理**
- 会话结束时调用 `FlowSessionRepo.release(flowId, session)` 自动释放所有关联资源
- 当某个流程的所有会话都释放后，自动清理该流程的缓存映射
- 无需在各个窗口或会话类中实现清理逻辑，避免内存泄漏

## 使用限制

1. 在编排流程时需要保证节点流转上没有死循环，否则处于死循环的数据将一致在这些节点上循环流转。
2. 数据流转的线程池最大是100个，每个节点最大同时处理16个批次的数据，每个批次的数据在每个节点上串行执行。超过限制的数据将排队等待执行。
