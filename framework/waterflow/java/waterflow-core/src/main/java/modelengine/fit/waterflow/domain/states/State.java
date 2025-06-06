/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.states;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowsession.FlowSessionRepo;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.enums.FlowNodeStatus;
import modelengine.fit.waterflow.domain.flow.Flow;
import modelengine.fit.waterflow.domain.stream.nodes.BlockToken;
import modelengine.fit.waterflow.domain.stream.nodes.Node;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.stream.reactive.Callback;
import modelengine.fit.waterflow.domain.stream.reactive.Processor;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;
import modelengine.fit.waterflow.domain.utils.FlowDebug;
import modelengine.fitframework.util.ObjectUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Node Activity
 * 一般节点：开始和结束中间的，既不是判定，也不是平行节点的后续节点的常规节点。
 * Processor，既是接收数据者，也是发送数据者。
 * 除了开始和结束，其他特殊节点都是来自于这 Node Activity （没有通过继承方式表示）。
 *
 * @param <O> 表示节点的输出数据类型的 {@link O}。
 * @param <D> 表示节点对应流初始数据类型的 {@link D}。
 * @param <I> 表示输入数据类型的 {@link I}。
 * @param <F> 表示对应的是处理流，还是生产流，用于泛型推演的 {@link F}。
 * @since 1.0
 */
public class State<O, D, I, F extends Flow<D>> extends Start<O, D, I, F>
        implements EmitterListener<O, FlowSession>, Emitter<O, FlowSession> {
    /**
     * 处理符合条件的数据的操作逻辑。
     */
    protected final Processor<I, O> processor;

    /**
     * 创建一个 {@link State} 实例。
     *
     * @param processor 表示处理器的 {@link Processor}{@code <}{@link I}{@code ,}{@link O}{@code >}。
     * @param flow 表示对应流程的 {@link F}。
     */
    public State(Processor<I, O> processor, F flow) {
        super(processor, flow);
        this.processor = processor;
        flow.nodes().add(this.processor);
    }

    @Override
    public Publisher<O> publisher() {
        return this.processor;
    }

    @Override
    public void handle(O data, FlowSession trans) {
        this.processor.handle(data, trans);
    }

    /**
     * 注册一个节点的监听器。
     * 用于形成一个流程间的数据交互或者与非流程的数据交互。
     *
     * @param handler 表示监听器的 {@link EmitterListener}{@code <}{@link O}{@code ,}{@link FlowSession}{@code >}。
     */
    public void register(EmitterListener<O, FlowSession> handler) {
        this.processor.register(handler);
    }

    public void unregister(EmitterListener<O, FlowSession> listener) {
        this.processor.unregister(listener);
    }

    @Override
    public void emit(O data, FlowSession token) {
        this.processor.emit(data, token);
    }

    /**
     * 标识一个节点的唯一标识。
     * 设定一个唯一标识后，通常用于 to 跳转，或者向一个该节点发射数据。
     *
     * @param id 表示唯一标识的 {@link String}。
     * @return 返回节点本身，便于后续的链式调用 {@link State}{@code <}{@link O}{@code ,}
     * {@link D}{@code ,}{@link I}{@code ,}{@link F}{@code >}。
     */
    public State<O, D, I, F> id(String id) {
        ObjectUtils.<Node<I, O>>cast(this.processor).setId(id);
        return ObjectUtils.cast(super.setId(id));
    }

    /**
     * 跳转到指定节点，使用节点的唯一标识来标识一个节点。
     * <p>
     * 类似于 goto，通常在 conditions 的分支中使用，用于形成循环。
     * </p>
     *
     * @param id 表示节点唯一标识的 {@link String}。
     */
    public void to(String id) {
        this.processor.subscribe(ObjectUtils.<State>cast(this.getFlow().getNode(id)).processor, null);
    }

    /**
     * 跳转到指定节点。
     * 类似于 goto，通常在 conditions 的分支中使用，用于形成循环。
     *
     * @param state 表示指定节点的 {@link State}{@code <}{@link O}{@code ,}
     * {@link D}{@code ,}{@link I}{@code ,}{@link F}{@code >}。
     */
    public void to(State<?, D, O, F> state) {
        this.processor.subscribe(state.processor, null);
    }

    /**
     * 将流程阻塞，直到符合 BlockToken 条件的情况下恢复。通常用于控制数据流的执行节奏。
     *
     * @param block 表示控制阻塞和恢复逻辑的 {@link BlockToken}{@code <}{@link O}{@code >}。
     * @return 表示 block 节点的 {@link State}{@code <}{@link O}{@code ,}{@link D}
     * {@code ,}{@link O}{@code ,}{@link F}{@code >}。
     */
    public State<O, D, O, F> block(BlockToken<O> block) {
        State<O, D, O, F> state = new State<>(this.from.map(new Operators.Map<FlowContext<O>, O>() {
            @Override
            public O process(FlowContext<O> input) {
                block.setHost(input);
                return null;
            }
        }, null), this.getFlow());
        block.setPublisher(state.from);
        return state;
    }

    /**
     * 处理发生错误时的处理方式。
     *
     * @param handler 表示错误处理器的 {@link Operators.ErrorHandler}{@code <}{@link I}{@code >}。
     * @return 表示对应的流对象的 {@link State}{@code <}{@link O}{@code ,}{@link D}{@code ,}
     * {@link I}{@code ,}{@link F}{@code >}。
     */
    public State<O, D, I, F> error(Operators.ErrorHandler<I> handler) {
        this.processor.onError(handler);
        return this;
    }

    /**
     * close 流程，也就是加终止节点。
     *
     * @return 表示对应的流对象的 {@link F}。
     */
    public F close() {
        return this.close(data -> {});
    }

    /**
     * close 流程，也就是加终止节点。
     *
     * @param callback 表示流程结束的回调处理器的 {@link Callback}{@code <}{@link FlowContext}
     * {@code <}{@link O}{@code >}{@code >}。
     * @return 表示对应的流对象的 {@link F}。
     */
    public F close(Operators.Just<Callback<FlowContext<O>>> callback) {
        return this.close(callback, null, null);
    }

    /**
     * close 流程，也就是加终止节点。
     *
     * @param callback 表示流程结束的回调处理器的 {@link Callback}{@code <}{@link FlowContext}
     * {@code <}{@link O}{@code >}{@code >}。
     * @param errHandler 表示流程错误处理器的 {@link Operators.ErrorHandler}{@code <}{@link Object}{@code >}。
     * @return 表示对应的流对象的 {@link F}。
     */
    public F close(Operators.Just<Callback<FlowContext<O>>> callback, Operators.ErrorHandler<Object> errHandler) {
        return this.close(callback, null, errHandler);
    }

    /**
     * close 流程，也就是加终止节点，提供session维度的数据消费
     *
     * @param callback 走到end节点的session的id和数据
     * @param sessionComplete 走到end节点的session的系统通知
     * @param errHandler 走到end节点的session的错误通知
     * @return F 流程实例
     */
    public F close(Operators.Just<Callback<FlowContext<O>>> callback, Operators.Just<FlowSession> sessionComplete,
            Operators.ErrorHandler<Object> errHandler) {
        getFlow().setEnd(this.processor.close());
        List<Publisher> nodes = this.getFlow()
                .nodes()
                .stream()
                .map(node -> ObjectUtils.<Publisher>cast(node))
                .collect(Collectors.toList());
        nodes.add(this.getFlow().start());
        nodes.stream().filter(node -> !node.subscribed()).forEach(node -> node.subscribe(getFlow().end()));
        getFlow().end().onComplete((Operators.Just<Callback<FlowContext<O>>>) input -> {
            FlowDebug.log(input.get().getSession(),
                    "[close] " + this.getFlow().end().getStreamId() + ":" + "end. data:" + input.get().getData());
            callback.process(input);
            input.get().getWindow().peekAndConsume().finishConsume();
            input.get().getWindow().onDone(this.getFlow().end().getId(), () -> {
                FlowSessionRepo.release(this.processor.getStreamId(), input.get().getSession());
                this.getFlow().completeSession(input.get().getSession().getId());
            });
        });
        if (sessionComplete != null) {
            getFlow().end().onSessionComplete(session -> {
                FlowSession session1 = ObjectUtils.cast(session);
                FlowDebug.log(session1, "========================end session end===========================");
                sessionComplete.process(session1);
            });
        }
        this.getFlow()
                .nodes()
                .forEach(node -> node.onGlobalError(this.buildGlobalHandler(errHandler, node.getFlowContextRepo())));
        this.getFlow()
                .end()
                .onGlobalError(this.buildGlobalHandler(errHandler, this.getFlow().end().getFlowContextRepo()));
        return this.getFlow();
    }

    /**
     * close 流程，也就是加终止节点，提供session维度的数据消费
     *
     * @param sessionConsumer 走到end节点的session的id和数据
     * @param sessionComplete 走到end节点的session的系统通知
     * @param sessionError 走到end节点的session的错误通知
     * @return F 流程实例
     */
    public F close(BiConsumer<FlowSession, O> sessionConsumer, Consumer<FlowSession> sessionComplete,
            BiConsumer<FlowSession, Throwable> sessionError) {
        return this.close(processor -> sessionConsumer.accept(processor.get().getSession(), processor.get().getData()),
                sessionComplete::accept,
                (exception, retryable, flowContexts) -> sessionError.accept(flowContexts.get(0).getSession(),
                        exception));
    }

    private Operators.ErrorHandler<Object> buildGlobalHandler(Operators.ErrorHandler<Object> errHandler,
            FlowContextRepo repo) {
        return (exception, retryable, contexts) -> {
            contexts.stream()
                    .findFirst()
                    .ifPresent(context -> FlowSessionRepo.release(this.processor.getStreamId(), context.getSession()));
            contexts.forEach(context -> context.setStatus(FlowNodeStatus.ERROR));
            repo.save(contexts);
            if (errHandler != null) {
                errHandler.handle(exception, retryable, contexts);
            }
        };
    }

    /**
     * 获取该节点内部实际的 Subscriber。
     *
     * @return 表示内部的 Subscriber 的 {@link Subscriber}{@code <}{@link I}{@code ,}{@link O}{@code >}。
     */
    public Subscriber<I, O> subscriber() {
        return this.processor;
    }
}
