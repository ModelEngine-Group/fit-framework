/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.engine.flows.AiFlow;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.flows.ConverseListener;
import modelengine.fel.engine.util.StateKey;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.flow.ProcessFlow;
import modelengine.fit.waterflow.domain.states.State;
import modelengine.fit.waterflow.domain.stream.callbacks.ToCallback;
import modelengine.fit.waterflow.domain.stream.nodes.BlockToken;
import modelengine.fit.waterflow.domain.stream.nodes.Retryable;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.reactive.Callback;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.ObjectUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * <p>表示 AI 流程中间的一般节点：开始和结束中间的常规节点，条件节点和平行节点这些特殊节点除外。
 * 该节点既是接收数据者，也是发送数据者。除了开始和结束，其他特殊节点都是这种一般节点的装饰。</p>
 *
 * @param <O> 表示当前节点的输出数据类型。
 * @param <D> 表示当前节点所属流程的初始数据类型。
 * @param <I> 表示入参数据类型。
 * @param <RF> 表示内部的数据流程类型，是 {@link Flow}{@code <}{@link D}{@code >} 的扩展。
 * @param <F> AI 流程类型，是 {@link AiFlow}{@code <}{@link D}{@code , }{@link RF}{@code >} 的扩展。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class AiState<O, D, I, RF extends Flow<D>, F extends AiFlow<D, RF>> extends AiStart<O, D, I, RF, F>
        implements EmitterListener<O, FlowSession>, Emitter<O, FlowSession> {
    /**
     * 表示被装饰的流程一般节点对象。
     */
    protected final State<O, D, I, RF> state;

    /**
     * AI 流程一般节点的构造方法。
     *
     * @param state 表示被装饰的流程一般节点对象的 {@link State}{@code <}{@link O}{@code , }{@link D}{@code ,
     * }{@link I}{@code , }{@link RF}{@code >}。
     * @param flow 表示 AI 流程的对象的 {@link F}。
     * @throws IllegalArgumentException 当 {@code state} 为 {@code null} 时。
     */
    public AiState(State<O, D, I, RF> state, F flow) {
        super(state, flow);
        this.state = Validation.notNull(state, "State node cannot be null.");
    }

    /**
     * 设置节点别名。
     *
     * @param id 表示节点名称的 {@link String}。
     * @return 表示设置好别名的节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code id} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    public AiState<O, D, I, RF, F> id(String id) {
        Validation.notBlank(id, "State node id cannot be blank.");
        this.state.id(id);
        return this;
    }

    /**
     * 获取当前节点的数据订阅者。
     *
     * @return 表示数据订阅者的 {@link Subscriber}{@code <}{@link I}{@code , }{@link O}{@code >}。
     */
    public Subscriber<I, O> subscriber() {
        return this.state.subscriber();
    }

    /**
     * 获取当前节点的数据发布者。
     *
     * @return 表示数据发布者的 {@link Publisher}{@code <}{@link O}{@code >}。
     */
    @Override
    public Publisher<O> publisher() {
        return this.state.publisher();
    }

    @Override
    public void register(EmitterListener<O, FlowSession> handler) {
        this.state.register(handler);
    }

    public void unregister(EmitterListener<O, FlowSession> listener) {
        this.state.unregister(listener);
    }

    @Override
    public void emit(O data, FlowSession session) {
        this.state.emit(data, new FlowSession(session));
    }

    @Override
    public void handle(O data, FlowSession session) {
        this.state.handle(data, new FlowSession(session));
    }

    /**
     * 生成一个阻塞节点，用于系统或人为介入。
     *
     * @param block 表示阻塞节点算子的 {@link BlockToken}{@code <}{@link O}{@code >}。
     * @return 表示阻塞节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link O}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code block} 为 {@code null} 时。
     */
    public AiState<O, D, O, RF, F> block(BlockToken<O> block) {
        Validation.notNull(block, "Block operator cannot be null.");
        return new AiState<>(this.state.block(block), this.flow());
    }

    /**
     * 设置当前节点发生异常时的响应。
     *
     * @param handler 表示异常处理器的 {@link Operators.ErrorHandler}{@code <}{@link I}{@code >}。
     * @return 表示设置了异常处理的节点的 {@link AiState}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link RF}{@code , }{@link F}{@code >}。
     * @throws IllegalArgumentException 当 {@code handler} 为 {@code null} 时。
     */
    public AiState<O, D, I, RF, F> doOnError(Operators.ErrorHandler<I> handler) {
        Validation.notNull(handler, "Error handler cannot be null.");
        return new AiState<>(this.state.error(handler), this.flow());
    }

    /**
     * 设置流程跳转到指定节点，使用节点的别名来标识一个节点。通常在条件分支中使用，用于形成循环。
     *
     * @param id 表示跳转目的节点别名的 {@link String}，通常使用 {@link AiState#id(String)} 指定。
     * @throws IllegalArgumentException 当 {@code id} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    public void to(String id) {
        Validation.notBlank(id, "Node id cannot be blank.");
        this.state.to(id);
    }

    /**
     * 设置流程跳转到指定节点。通常在条件分支中使用，用于形成循环。
     *
     * @param next 表示跳转目的节点的 {@link AiState}{@code <?, }{@link D}{@code , }{@link O}{@code ,
     * }{@link ProcessFlow}{@code <}{@link D}{@code >, ?>}。
     * @throws IllegalArgumentException 当 {@code next} 为 {@code null} 时。
     */
    public void to(AiState<?, D, O, ProcessFlow<D>, ?> next) {
        Validation.notNull(next, "Node cannot be null.");
        ObjectUtils.<State<O, D, I, ProcessFlow<D>>>cast(this.state).to(next.state);
    }

    /**
     * 在流程最后添加终止节点。
     *
     * @return 表示当前流程实例的 {@link ProcessFlow}{@code <}{@link D}{@code , }{@link O}{@code >}。
     */
    public AiProcessFlow<D, O> close() {
        return this.close(FlowCallBack.emptyCallBack());
    }

    /**
     * 在流程最后添加终止节点，并设置成功回调。
     *
     * @param callback 表示流程成功结束后操作的 {@link Consumer}{@code <}{@link O}{@code >}。
     * @return 表示当前流程实例的 {@link ProcessFlow}{@code <}{@link D}{@code , }{@link O}{@code >}。
     */
    public AiProcessFlow<D, O> close(Consumer<O> callback) {
        return this.close(FlowCallBack.<O>builder().doOnConsume(callback).build());
    }

    /**
     * 在流程最后添加终止节点，并设置成功回调和异常回调。
     *
     * @param callback 表示流程成功后操作的 {@link Operators.Just}{@code <}{@link Callback}{@code <}{@link
     * FlowContext}{@code <}{@link O}{@code >>>}。
     * @param errHandler 表示流程异常处理器的 {@link Operators.ErrorHandler}{@code <}{@link Object}{@code >}。
     * @return 表示当前流程实例的 {@link ProcessFlow}{@code <}{@link D}{@code , }{@link O}{@code >}。
     */
    @Deprecated
    public AiProcessFlow<D, O> close(Operators.Just<Callback<FlowContext<O>>> callback,
            Operators.ErrorHandler<Object> errHandler) {
        Consumer<Throwable> errorCbWrapper = exception -> errHandler.handle(new Exception(exception), null, null);
        return this.close(FlowCallBack.<O>builder()
                .doOnConsume(data -> callback.process(new ToCallback<>(Collections.singletonList(new FlowContext<>(null,
                        null,
                        data,
                        new HashSet<>(),
                        null,
                        null)))))
                .doOnError(errorCbWrapper)
                .build());
    }

    /**
     * 在流程最后添加终止节点，并设置回调。
     *
     * @param callback 表示流程回调操作的 {@link FlowCallBack}{@code <}{@link O}{@code >}。
     * @return 表示当前流程实例的 {@link ProcessFlow}{@code <}{@link D}{@code , }{@link O}{@code >}。
     */
    public AiProcessFlow<D, O> close(FlowCallBack<O> callback) {
        Operators.Just<Callback<FlowContext<O>>> successCb = input -> {
            // 流程的全局数据消费回调
            callback.consumeCb().accept(input.get().getData());
            // 对话数据消费回调由直接起会话的流程触发
            String flowId = this.flow().getId();
            getConverseListener(input.get().getSession(), flowId).ifPresent(listener -> listener.onConsume(flowId,
                    input.get().getData()));
        };

        Operators.ErrorHandler<Object> errCb = (exception, retryable, flowContexts) -> {
            // 流程的全局异常回调
            callback.errorCb().accept(exception);
            // 触发对话异常处理，及父流程异常处理
            handleConverseAndParentError(exception, retryable, flowContexts);
        };

        Operators.Just<FlowSession> boundCompletedAction = session -> {
            // 流程的全局结束回调
            callback.completedCb().exec();
            // 对话结束回调由直接起会话的流程触发
            String flowId = this.flow().getId();
            getConverseListener(session, flowId).ifPresent(listener -> listener.onConverseCompleted(flowId));
        };
        this.state.close(successCb, boundCompletedAction, errCb);
        return ObjectUtils.cast(this.flow());
    }

    private void handleConverseAndParentError(Exception exception, Retryable<Object> retryable,
            List<FlowContext<Object>> flowContexts) {
        String flowId = this.flow().getId();
        flowContexts.stream()
                .map(this::getAndClearListenerMap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(listenerMap -> {
                    // 先处理当前流程的对话异常
                    handleConverseError(listenerMap.get(flowId), exception);
                    return listenerMap.entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), flowId));
                })
                .forEach(entry -> {
                    // 处理全部父流程的全局异常和对话异常
                    handleConverseFlowError(entry.getValue(), exception, retryable, flowContexts);
                    handleConverseError(entry.getValue(), exception);
                });
    }

    private void handleConverseError(AtomicReference<ConverseListener<O>> listenerInput, Exception exception) {
        Optional.ofNullable(listenerInput)
                .map(AtomicReference::get)
                .ifPresent(listener -> listener.onConverseError(exception));
    }

    private void handleConverseFlowError(AtomicReference<ConverseListener<O>> listenerInput, Exception exception,
            Retryable<Object> retryable, List<FlowContext<Object>> contexts) {
        Optional.ofNullable(listenerInput)
                .map(AtomicReference::get)
                .ifPresent(listener -> listener.onFlowError(exception, retryable, contexts));
    }

    private Optional<Map<String, AtomicReference<ConverseListener<O>>>> getAndClearListenerMap(FlowContext<?> ctx) {
        AtomicReference<Map<String, AtomicReference<ConverseListener<O>>>> listenerRefMap =
                ctx.getSession().getInnerState(StateKey.CONVERSE_LISTENER);
        return Optional.ofNullable(listenerRefMap).map(mapRef -> mapRef.getAndSet(null));
    }

    private Optional<ConverseListener<O>> getConverseListener(FlowSession session, String flowId) {
        AtomicReference<Map<String, AtomicReference<ConverseListener<O>>>> listenerRefMap =
                session.getInnerState(StateKey.CONVERSE_LISTENER);
        return Optional.ofNullable(listenerRefMap)
                .map(AtomicReference::get)
                .map(listenerMap -> listenerMap.get(flowId))
                .map(AtomicReference::get);
    }
}
