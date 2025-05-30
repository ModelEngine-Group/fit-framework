/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.document.Content;
import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.Measurable;
import modelengine.fel.core.model.BlockModel;
import modelengine.fel.core.pattern.Parser;
import modelengine.fel.core.pattern.Pattern;
import modelengine.fel.core.pattern.PostProcessor;
import modelengine.fel.core.pattern.Retriever;
import modelengine.fel.core.pattern.Source;
import modelengine.fel.core.pattern.Splitter;
import modelengine.fel.core.pattern.Store;
import modelengine.fel.core.pattern.Synthesizer;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.activities.processors.AiBranchProcessor;
import modelengine.fel.engine.activities.processors.AiFlatMap;
import modelengine.fel.engine.flows.AiFlow;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.flows.Conversation;
import modelengine.fel.engine.operators.models.FlowModel;
import modelengine.fel.engine.operators.patterns.AbstractFlowPattern;
import modelengine.fel.engine.operators.patterns.FlowNodeSupportable;
import modelengine.fel.engine.operators.patterns.FlowPattern;
import modelengine.fel.engine.operators.patterns.FlowSupportable;
import modelengine.fel.engine.operators.patterns.SimpleFlowPattern;
import modelengine.fel.engine.operators.prompts.PromptTemplate;
import modelengine.fel.engine.util.AiFlowSession;
import modelengine.fit.waterflow.domain.emitters.FlowEmitter;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.flow.Flows;
import modelengine.fit.waterflow.domain.states.Start;
import modelengine.fit.waterflow.domain.states.State;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.reactive.Processor;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.utils.Tuple;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * AI 流程的开始节点。
 *
 * @param <O> 表示开始节点的输出数据类型。
 * @param <D> 表示流程的初始数据类型。
 * @param <I> 表示入参数据类型。
 * @param <RF> 表示内部数据流程类型，是 {@link Flow}{@code <}{@link D}{@code >} 的扩展。
 * @param <F> 表示 AI 流程的类型，是 {@link AiFlow}{@code <}{@link D}{@code , }{@link RF}{@code >} 的扩展。
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-04-28
 */
public class AiStart<O, D, I, RF extends Flow<D>, F extends AiFlow<D, RF>> extends AiActivity<D, RF, F> {
    private final Start<O, D, I, RF> start;

    /**
     * AI 流程开始节点的构造方法。
     *
     * @param start 表示被装饰的流程开始节点的 {@link Start}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @param flow 表示 AI 流程对象的 {@link F}。
     * @throws IllegalArgumentException 当 {@code start} 为 {@code null} 时。
     */
    public AiStart(Start<O, D, I, RF> start, F flow) {
        super(flow);
        this.start = Validation.notNull(start, "Start node cannot be null.");
    }

    /**
     * 设置节点别名。
     *
     * @param id 表示待设置的节点名称的 {@link String}。
     * @return 表示设置好别名的当前节点的 {@link AiStart}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code id} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    public AiStart<O, D, I, RF, F> id(String id) {
        Validation.notBlank(id, "Node id cannot be blank.");
        this.start.id(id);
        return this;
    }

    /**
     * 获取流程的数据发布者。
     *
     * @return 表示数据发布者的 {@link Publisher}{@code <}{@link O}{@code >}。
     */
    public Publisher<O> publisher() {
        return this.start.publisher();
    }

    /**
     * 将每个数据通过指定的方式进行加工后继续流转，只处理数据，不转换类型。
     *
     * @param processor 表示数据处理器的 {@link Operators.Just}{@code <}{@link O}{@code >}。
     * @return 表示数据加工节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> just(Operators.Just<O> processor) {
        Validation.notNull(processor, "Just processor cannot be null.");
        return new AiState<>(this.start.just(processor), this.flow());
    }

    /**
     * 将每个数据通过指定的方式进行加工后继续流转，只处理数据，不转换类型，处理器内可消费自定义上下文。
     *
     * @param processor 表示数据处理器的 {@link Operators.ProcessJust}{@code <}{@link O}{@code >}，捕获了从
     * {@link Conversation#bind(String, Object)} 绑定的自定义上下文。
     * @return 表示数据加工节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> just(Operators.ProcessJust<O> processor) {
        Validation.notNull(processor, "Just processor cannot be null.");
        return new AiState<>(this.start.just(processor), this.flow());
    }

    /**
     * 将每个数据通过指定的方式进行转换后继续流转，处理数据同时可以转换类型。
     *
     * @param processor 表示数据处理器的 {@link Operators.Map}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示数据转换节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> map(Operators.Map<O, R> processor) {
        Validation.notNull(processor, "Map processor cannot be null.");
        return new AiState<>(this.start.map(processor), this.flow());
    }

    /**
     * 将每个数据通过指定的方式进行加工和转换后继续流转，同时可以转换类型，处理器内可消费自定义上下文。
     *
     * @param processor 表示数据处理器的 {@link Operators.Map}{@code <}{@link O}{@code , }{@link R}{@code >}，
     * 捕获了从 {@link Conversation#bind(String, Object)} 绑定的自定义上下文。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示数据转换节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> map(Operators.ProcessMap<O, R> processor) {
        Validation.notNull(processor, "Map processor cannot be null.");
        return new AiState<>(this.start.map(processor), this.flow());
    }

    /**
     * 将每个数据通过指定的方式转换为一个数据流，并将数据流的数据往下发射流转。
     *
     * @param processor 表示数据处理器的 {@link AiFlatMap}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示数据转换节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> flatMap(AiFlatMap<O, R> processor) {
        return new AiState<>(this.start.flatMap(input -> processor.process(input).toDataStart()), this.flow());
    }

    /**
     * 生成一个数据聚合节点，将每个数据通过指定的方式进行合并后，形成一个新的数据，并继续发送。
     * <p>如果流程是批量注入数据，那么聚合节点会将同一批次的数据聚合。</p>
     * <p>如果流程是逐个注入数据，那么需要配合 {@link AiStart#window(Operators.WindowCondition)} 表达式使用，才会聚合数据。</p>
     *
     * @param init 表示聚合操作初始值提供者的 {@link Supplier}{@code <}{@link R}{@code >}，当 {@code init} 为 {@code null}
     * 时，表示聚合之后的数据类型还是原数据类型。
     * @param processor 表示数据聚合器的 {@link Operators.Reduce}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示数据聚合节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> reduce(Supplier<R> init, Operators.Reduce<O, R> processor) {
        Validation.notNull(processor, "Reduce processor cannot be null.");
        return new AiState<>(this.start.reduce(init, processor), this.flow());
    }

    /**
     * 生成一个数据聚合节点， 将每个数据通过指定的方式进行合并后，形成一个新的数据，并继续发送。处理器内可消费自定义上下文。
     * <p>如果流程是批量注入数据，那么聚合节点会将同一批次的数据聚合。</p>
     * <p>如果流程是逐个注入数据，那么需要配合 {@link AiStart#window(Operators.WindowCondition)} 表达式使用，才会聚合数据。</p>
     *
     * @param init 表示聚合操作初始值提供者的 {@link Supplier}{@code <}{@link R}{@code >}，当 {@code init} 为 {@code null}
     * 时，表示聚合之后的数据类型还是原数据类型。
     * @param processor 表示数据聚合器的 {@link Operators.Reduce}{@code <}{@link O}{@code , }{@link R}{@code >}，
     * 捕获了从 {@link Conversation#bind(String, Object)} 绑定的自定义上下文。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示数据聚合节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> reduce(Supplier<R> init, Operators.ProcessReduce<O, R> processor) {
        Validation.notNull(processor, "Reduce processor cannot be null.");
        return new AiState<>(this.start.reduce(init, processor), this.flow());
    }

    /**
     * 形成一个窗口节点。需要与数据聚合节点配合使用，窗口节点单独使用不生效。
     * <p>当窗口节点中的数据满足条件后，窗口关闭，后续的数据聚合节点才会将聚合的数据往下发送，在此之前， 数据聚合节点将持续聚合数据。</p>
     *
     * @param windowCondition 表示窗口处理器的 {@link Operators.WindowCondition}{@code <}{@link O}{@code >}。
     * @return 表示窗口节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code window} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> window(Operators.WindowCondition windowCondition) {
        Validation.notNull(windowCondition, "Window operator cannot be null.");
        return new AiState<>(this.start.window(windowCondition), this.flow());
    }

    /**
     * 形成一个按照数量处理的窗口节点。需要与数据聚合节点配合使用，窗口节点单独使用不生效。
     * <p>当窗口节点中的数据满足条件后，窗口关闭，后续的数据聚合节点才会将聚合的数据往下发送，在此之前， 数据聚合节点将持续聚合数据。</p>
     *
     * @param count 表示窗口大小。
     * @return 表示窗口节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code window} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> window(int count) {
        return this.window(arg -> arg.countToNow() == count);
    }

    /**
     * 设置分组聚合的键，需要配合 {@link AiStart#window(Operators.WindowCondition)}、
     * {@link AiStart#reduce(Supplier, Operators.Reduce)} 和 {@link AiStart#reduce(Supplier, Operators.ProcessReduce)}
     * 使用，后续的聚合操作按指定的键分组处理。
     *
     * @param keyBy 表示分组配置器的 {@link Operators.Map}{@code <}{@link O}{@code , }{@link R}{@code >}，提供分组聚合的键。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示数据分组节点的 {@link AiState}{@code <}{@link Tuple}{@code <}{@link R}{@code ,
     * }{@link O}{@code >}{@code , }{@link D}{@code , }{@link O}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code keyBy} 为 {@code null} 时。
     */
    public <R> AiState<Tuple<R, O>, D, O, RF, F> keyBy(Operators.Map<O, R> keyBy) {
        Validation.notNull(keyBy, "KeyBy operator cannot be null.");
        return new AiState<>(this.start.keyBy(keyBy), this.flow());
    }

    /**
     * 按指定容量缓存流中的数据，为后续节点提供缓存的数据列表。
     *
     * @param size 表示待指定的数据缓存容量的 {@code int}。
     * @return 表示数据缓存节点的 {@link AiState}{@code <}{@link List}{@code <}{@link O}{@code >}{@code ,
     * }{@link D}{@code , }{@link O}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code size} 不大于0时。
     */
    public AiState<List<O>, D, O, RF, F> buffer(int size) {
        Validation.isTrue(size > 0, "Buffer size must be greater than 0.");
        return this.window(size).reduce(ArrayList::new, (acc, input) -> {
            acc.add(input);
            return acc;
        });
    }

    /**
     * 生成自定义数据处理器，支持往后续的节点发射自定义数据。
     *
     * @param processor 表示自定义数据处理器的 {@link Operators.Process}{@code <}{@link O}{@code , }{@link R}{@code >}，
     * 捕获了从 {@link Conversation#bind(String, Object)} 绑定的自定义上下文，
     * 支持往后续的节点发射自定义数据。
     * @param <R> 表示新节点的输出数据类型。
     * @return 表示自定义数据处理节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> process(Operators.Process<O, R> processor) {
        Validation.notNull(processor, "Process operator cannot be null.");
        return new AiState<>(this.start.process(processor), this.flow());
    }

    /**
     * 创建条件节点。
     *
     * @return 表示条件节点的 {@link AiConditions}{@code <}{@link D}{@code , }{@link O}{@code , }{@link RF}{@code ,
     * }{@link F}{@code >}。
     */
    public AiConditions<D, O, RF, F> conditions() {
        return new AiConditions<>(this.start.conditions(), this.flow());
    }

    /**
     * 生成数据加载节点。
     *
     * @param source 表示数据源的 {@link Splitter}{@code <}{@link O}{@code >}。
     * @return 表示数据加载节点的 {@link AiState}{@code <}{@link List}{@code <}{@link Document}{@code >}{@code ,}
     * {@link D}{@code ,}{@link O}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code splitter} 为 {@code null} 时。
     */
    public AiState<List<Document>, D, O, RF, F> load(Source<O> source) {
        Validation.notNull(source, "Splitter operator cannot be null.");
        return new AiState<>(new State<>(this.publisher()
                .map(input -> AiFlowSession.applyPattern(source, input.getData(), input.getSession()), null)
                .displayAs("load"), this.flow().origin()), this.flow());
    }

    /**
     * 生成文本切分节点。
     *
     * @param splitter 表示文本切分算子的 {@link Splitter}{@code <}{@link O}{@code >}。
     * @return 表示文本切分节点的 {@link AiState}{@code <}{@link Document}{@code , }{@link D}{@code , }{@link O}{@code ,}
     * {@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code splitter} 为 {@code null} 时。
     */
    public AiState<Document, D, O, RF, F> split(Splitter<O> splitter) {
        Validation.notNull(splitter, "Splitter operator cannot be null.");
        return new AiState<>(new State<>(this.publisher()
                .flatMap(input -> Flows.flux(AiFlowSession.applyPattern(splitter, input.getData(), input.getSession())
                        .toArray(new Document[0])), null)
                .displayAs("split"), this.flow().origin()), this.flow());
    }

    /**
     * 生成索引节点。
     *
     * @param store 表示索引存储的 {@link Store}{@code <}{@link O}{@code >}。
     * @return 表示索引节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,}
     * {@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code splitter} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> index(Store<O> store) {
        Validation.notNull(store, "Persistent operator cannot be null.");
        return new AiState<>(new State<>(this.publisher()
                .just(input -> AiFlowSession.applyPattern(store, input.getData(), input.getSession()), null)
                .displayAs("index"), this.flow().origin()), this.flow());
    }

    /**
     * 生成数据检索节点。
     *
     * @param retriever 表示数据检索器的 {@link Retriever}{@code <}{@link O}{@code >}。
     * @param <M> 表示可衡量数据的泛型。
     * @return 表示数据检索节点的
     * {@link AiState}{@code <}{@link List}{@code <}{@link M}{@code >}{@code , }{@link D}{@code ,}
     * {@link O}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code TextRetriever} 为 {@code null} 时。
     */
    public <M extends Measurable> AiState<List<M>, D, O, RF, F> retrieve(Retriever<O, M> retriever) {
        Validation.notNull(retriever, "Retriever operator cannot be null.");
        return new AiState<>(new State<>(this.publisher()
                .map(input -> AiFlowSession.applyPattern(retriever, input.getData(), input.getSession()), null)
                .displayAs("retrieve"), this.flow().origin()), this.flow());
    }

    /**
     * 生成数据增强节点，用于检索后置处理。
     *
     * @param processors 表示检索后置处理器的 {@code  PostProcessor[]}。
     * @return 表示数据检索节点的
     * {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code ,}{@link O}{@code , }{@link RF}
     * {@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code TextRetriever} 为 {@code null} 时。
     */
    @SafeVarargs
    public final AiState<O, D, O, RF, F> enhance(PostProcessor<O>... processors) {
        Validation.isTrue(processors != null, "The postProcessor cannot be null.");
        return new AiState<>(new State<>(this.publisher().map(input -> {
            O result = input.getData();
            for (PostProcessor<O> processor : processors) {
                result = AiFlowSession.applyPattern(processor, input.getData(), input.getSession());
            }
            return result;
        }, null).displayAs("processors"), this.flow().origin()), this.flow());
    }

    /**
     * 生成检索合成节点。
     *
     * @param synthesizer 表示结果合成算子的 {@link Store}{@code <}{@link O}{@code >}。
     * @return 表示检索合成节点的 {@link AiState}{@code <}{@link Content}{@code , }{@link D}{@code , }{@link O}{@code ,}
     * {@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code splitter} 为 {@code null} 时。
     */
    public AiState<Content, D, O, RF, F> synthesize(Synthesizer<O> synthesizer) {
        Validation.notNull(synthesizer, "The synthesizer cannot be null.");
        return new AiState<>(new State<>(this.publisher()
                .map(input -> AiFlowSession.applyPattern(synthesizer, input.getData(), input.getSession()), null)
                .displayAs("synthesizer"), this.flow().origin()), this.flow());
    }

    /**
     * 将模型处理返回值的格式化解析。
     *
     * @param parser 表示格式化解析器的 {@link Parser}{@code <}{@link R}{@code >}。
     * @return 表示格式化解析节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code parser} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> parse(Parser<O, R> parser) {
        Validation.notNull(parser, "Parser operator cannot be null.");
        AiState<R, D, O, RF, F> state = this.map(parser::parse);
        ((Processor<?, ?>) state.publisher()).displayAs("format");
        return state;
    }

    /**
     * 将数据委托给 {@link Pattern}{@code <}{@link O}{@code , }{@link R}{@code >}
     * 处理，然后自身放弃处理数据。处理后的数据会发送回该节点，作为该节点的处理结果。
     *
     * @param pattern 表示异步委托单元的 {@link Pattern}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示委托节点的输出数据类型。
     * @return 表示委托节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code pattern} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> delegate(Pattern<O, R> pattern) {
        Validation.notNull(pattern, "Pattern operator cannot be null.");
        return this.delegate(new SimpleFlowPattern<>(pattern));
    }

    /**
     * 将数据委托给 {@link FlowPattern}{@code <}{@link O}{@code , }{@link R}{@code >}
     * 处理，然后自身放弃处理数据。处理后的数据会发送回该节点，作为该节点的处理结果。
     *
     * @param pattern 表示异步委托单元的 {@link FlowPattern}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示委托节点的输出数据类型。
     * @return 表示委托节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code pattern} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> delegate(FlowPattern<O, R> pattern) {
        Validation.notNull(pattern, "Pattern operator cannot be null.");
        Processor<O, R> orProcessor = this.publisher().flatMap(input -> {
            FlowEmitter<R> emitter = AiFlowSession.applyPattern(pattern, input.getData(), input.getSession());
            return Flows.source(emitter);
        }, null);
        this.displayPatternProcessor(pattern, orProcessor);
        return new AiState<>(new State<>(orProcessor, this.flow().origin()), this.flow());
    }

    private <R> void displayPatternProcessor(FlowPattern<O, R> pattern, Processor<O, R> processor) {
        if (pattern instanceof AbstractFlowPattern) {
            Flow<O> originFlow = ObjectUtils.<AbstractFlowPattern<O, R>>cast(pattern).origin();
            processor.displayAs("delegate to flow", originFlow, originFlow.start().getId());
        } else {
            processor.displayAs("delegate to pattern");
        }
    }

    private <R> FlowPattern<O, R> castFlowPattern(Pattern<O, R> pattern) {
        if (pattern instanceof FlowPattern) {
            return ObjectUtils.cast(pattern);
        } else {
            return new SimpleFlowPattern<>(pattern);
        }
    }

    /**
     * 将数据委托给 {@link Operators.ProcessMap}{@code <}{@link O}{@code , }{@link R}{@code >}
     * 处理，然后自身放弃处理数据。处理后的数据会发送回该节点，作为该节点的处理结果。
     * <p>
     * 数据接收方 {@code operator} 将在 {@link SimpleFlowPattern}{@code <}{@link O}{@code , }{@link R}{@code >}
     * 里面的独立线程池中执行，不占用委托节点的线程。一般用于 {@code operator} 为 IO 密集型任务的场景。
     * </p>
     *
     * @param operator 表示数据接收方的 {@link Operators.ProcessMap}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示委托节点的输出数据类型。
     * @return 表示委托节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code operator} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> delegate(Operators.ProcessMap<O, R> operator) {
        Validation.notNull(operator, "Pattern operator cannot be null.");
        return this.delegate(new SimpleFlowPattern<>(operator));
    }

    /**
     * 将数据委托给 {@link AiProcessFlow}{@code <}{@link O}{@code , }{@link R}{@code >}
     * 处理，然后自身放弃处理数据。处理后的数据会发送回该节点，作为该节点的处理结果。
     *
     * @param aiFlow 表示子流程的 {@link AiProcessFlow}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param <R> 表示委托节点的输出数据类型。
     * @return 表示委托节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code aiFlow} 为 {@code null} 时。
     */
    public <R> AiState<R, D, O, RF, F> delegate(AiProcessFlow<O, R> aiFlow) {
        Validation.notNull(aiFlow, "Flow cannot be null.");
        return this.delegate(new FlowSupportable<>(aiFlow));
    }

    /**
     * 将数据委托给 {@link AiProcessFlow}{@code <}{@link O}{@code , }{@link R}{@code >}
     * 的指定节点开始处理，然后自身放弃处理数据。处理后的数据会发送回该节点，作为该节点的处理结果。
     *
     * @param aiFlow 表示子流程的 {@link AiProcessFlow}{@code <}{@link O}{@code , }{@link R}{@code >}。
     * @param nodeId 表示节点名称的 {@link String}。
     * @param <R> 表示委托节点的输出数据类型。
     * @return 表示委托节点的 {@link AiState}{@code <}{@link R}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException <ul>
     * <li>当 {@code aiFlow} 为 {@code null}时。</il>
     * <li>当 {@code nodeId} 为 {@code null} 、空字符串或只有空白字符的字符串时。</il>
     * </ul>
     */
    public <R> AiState<R, D, O, RF, F> delegate(AiProcessFlow<O, R> aiFlow, String nodeId) {
        Validation.notNull(aiFlow, "Flow cannot be null.");
        Validation.notBlank(nodeId, "Node id cannot be blank.");
        return this.delegate(new FlowNodeSupportable<>(aiFlow, nodeId));
    }

    /**
     * 通过提示模板和参数生成 {@link Prompt}，作为大模型输入参数。
     *
     * @param templates 表示提示词模板数组的 {@link PromptTemplate}{@code <}{@link O}{@code >[]}。
     * @return 表示提示词节点的 {@link AiState}{@code <}{@link Prompt}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     */
    @SafeVarargs
    public final AiState<Prompt, D, O, RF, F> prompt(PromptTemplate<O>... templates) {
        return new AiState<>(new State<>(this.publisher().map(input -> {
            ChatMessages messages = new ChatMessages();
            List<Prompt> prompts =
                    AiFlowSession.applyBatchPattern(Arrays.asList(templates), input.getData(), input.getSession());
            prompts.forEach(prompt -> messages.addAll(prompt.messages()));
            return ObjectUtils.<Prompt>cast(messages);
        }, null).displayAs("prompt"), this.flow().origin()), this.flow());
    }

    /**
     * 生成大模型阻塞调用节点。
     *
     * @param model 表示模型算子实现的 {@link BlockModel}{@code <}{@link M}{@code >}。
     * @param <M> 表示模型节点的输入数据类型。
     * @return 表示大模型阻塞调用节点的 {@link AiState}{@code <}{@link ChatMessage}{@code , }{@link D}{@code ,
     * }{@link O}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code model} 为 {@code null} 时。
     */
    public <M extends ChatMessage> AiState<M, D, O, RF, F> generate(BlockModel<O, M> model) {
        Validation.notNull(model, "Model operator cannot be null.");
        return new AiState<>(new State<>(this.publisher()
                .map(input -> AiFlowSession.applyPattern(model, input.getData(), input.getSession()), null)
                .displayAs("generate"), this.flow().origin()), this.flow());
    }

    /**
     * 生成大模型流式调用节点。
     *
     * @param model 表示流式模型算子实现的 {@link FlowModel}{@code <}{@link O}{@code >}。
     * @param <M> 表示模型节点的输入数据类型。
     * @return 表示大模型流式调用节点的 {@link AiState}{@code <}{@link ChatMessage}{@code , }{@link D}{@code ,
     * }{@link O}{@code , }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code model} 为 {@code null} 时。
     */
    public <M extends ChatMessage> AiState<ChatMessage, D, O, RF, F> generate(FlowModel<O, M> model) {
        Validation.notNull(model, "Streaming Model operator cannot be null.");
        Processor<O, ChatMessage> processor = this.publisher().flatMap(input -> {
            return Flows.source(AiFlowSession.applyPattern(model, input.getData(), input.getSession()));
        }, null).displayAs("generate");
        return new AiState<>(new State<>(processor, this.flow().origin()), this.flow());
    }

    /**
     * 生成平行分支节点。每个分支将输出一个键值对。
     *
     * @param patterns 表示同步委托单元数组的 {@link Pattern}{@code <}{@link O}{@code , }{@link Tip}{@code >[]}。
     * @return 表示平行分支节点的 {@link AiState}{@code <}{@link Tip}{@code , }{@link D}{@code , }{@link Tip}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code patterns} 数组为空时。
     */
    @SafeVarargs
    public final AiState<Tip, D, Tip, RF, F> runnableParallel(Pattern<O, Tip>... patterns) {
        Validation.isTrue(patterns.length > 0, "Patterns can not be empty.");

        F mineFlow = this.flow();
        RF mineOrigin = this.flow().origin();
        AiFork<Tip, D, O, RF, F> aiFork = null;
        for (Pattern<O, Tip> pattern : patterns) {
            AiBranchProcessor<Tip, D, O, RF, F> branchProcessor = node -> {
                Processor<O, Tip> processor = this.getPatternProcessor(pattern, node);
                return new AiState<>(new State<>(processor, mineOrigin), mineFlow);
            };
            aiFork = Optional.ofNullable(aiFork)
                    .map(node -> node.fork(branchProcessor))
                    .orElseGet(() -> new AiParallel<>(this.start.parallel(), mineFlow).fork(branchProcessor));
        }

        AiState<Tip, D, Tip, RF, F> state = aiFork.join(Tip::new, (acc, data) -> {
            acc.merge(data);
            return acc;
        });
        ((Processor<?, ?>) state.publisher()).displayAs("runnableParallel");
        return state;
    }

    private Processor<O, Tip> getPatternProcessor(Pattern<O, Tip> pattern, AiState<O, D, O, RF, F> node) {
        return node.publisher()
                .map(input -> AiFlowSession.applyPattern(pattern, input.getData(), input.getSession()), null);
    }
}
