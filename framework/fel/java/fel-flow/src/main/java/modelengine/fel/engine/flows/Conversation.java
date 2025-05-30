/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.engine.activities.AiStart;
import modelengine.fel.engine.activities.FlowCallBack;
import modelengine.fel.engine.operators.models.StreamingConsumer;
import modelengine.fel.engine.operators.sources.Source;
import modelengine.fel.engine.util.StateKey;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.inspection.Validation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * AI 数据处理流程对话对象，是启动流程的入口。
 *
 * @param <D> 流程开始节点传入的数据类型。
 * @param <R> 流程结束节点输出的数据类型。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class Conversation<D, R> {
    private final AiProcessFlow<D, R> flow;
    private final FlowSession session;
    private final AtomicReference<ConverseListener<R>> converseListener = new AtomicReference<>(null);
    private FlowCallBack.Builder<R> callBackBuilder;

    /**
     * AI 数据处理流程对话的构造方法。
     *
     * @param flow 表示 AI 流程对象的 {@link AiProcessFlow}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @param session 表示流程会话实例信息的 {@link FlowSession}。
     * @throws IllegalArgumentException 当 {@code flow} 为 {@code null} 时。
     */
    public Conversation(AiProcessFlow<D, R> flow, FlowSession session) {
        this.flow = Validation.notNull(flow, "Flow cannot be null.");
        this.session =
                (session == null) ? this.setConverseListener(new FlowSession(true)) : this.setSubConverseListener(session);
        this.session.begin();
        this.callBackBuilder = FlowCallBack.builder();
    }

    /**
     * 往流程注入数据数组，驱动流程流转。
     *
     * @param data 表示待注入数据数组的 {@link D}{@code []}。
     * @return 表示线程同步器的 {@link ConverseLatch}{@code <}{@link R}{@code >}。
     * @throws IllegalStateException 相同对话的上一次 offer 未完成时。
     */
    @SafeVarargs
    public final ConverseLatch<R> offer(D... data) {
        ConverseLatch<R> latch = setListener(this.flow);
        FlowSession newSession = FlowSession.newRootSession(this.session, this.session.preserved());
        newSession.getWindow().setFrom(null);
        this.flow.start().offer(data, newSession);
        newSession.getWindow().complete();
        return latch;
    }

    /**
     * 从一个命名节点批量注入数据，驱动流程流转。
     *
     * @param nodeId 表示节点名称的 {@link String}。
     * @param data 表示数据数组的 {@link List}{@code <}{@code ? extends }{@link Object}{@code >}。
     * @return 表示线程同步器的 {@link ConverseLatch}{@code <}{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code nodeId} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     * @throws IllegalStateException 相同对话的上一次数据注入未完成时。
     */
    public ConverseLatch<R> offer(String nodeId, List<?> data) {
        Validation.notBlank(nodeId, "invalid nodeId.");
        ConverseLatch<R> latch = setListener(this.flow);
        FlowSession newSession = new FlowSession(this.session);
        newSession.getWindow().setFrom(null);
        this.flow.origin().offer(nodeId, data.toArray(new Object[0]), newSession);
        newSession.getWindow().complete();
        return latch;
    }

    /**
     * 绑定大模型超参数到对话上下文，用于流程后续的大模型节点。
     *
     * @param option 表示大模型超参数的 {@link ChatOption}。
     * @return 表示绑定了大模型超参数的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code options} 为 {@code null} 时。
     */
    public Conversation<D, R> bind(ChatOption option) {
        Validation.notNull(option, "Chat options cannot be null.");
        this.session.setInnerState(StateKey.CHAT_OPTION, option);
        return this;
    }

    /**
     * 绑定历史记录句柄到对话上下文，只有绑定了历史记录句柄，该对话才会保存历史记录。
     *
     * @param memory 表示历史记录句柄的 {@link Memory}。
     * @return 表示绑定了历史记录句柄的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code memory} 为 {@code null} 时。
     */
    public Conversation<D, R> bind(Memory memory) {
        Validation.notNull(memory, "Memory cannot be null.");
        this.session.setInnerState(StateKey.HISTORY, memory);
        return this;
    }

    /**
     * 绑定流式响应信息消费者到对话上下文，用于消费流程流转过程中的流式信息。
     *
     * @param consumer 表示流式响应信息消费者的 {@link StreamingConsumer}{@code <}{@link ChatMessage}{@code ,
     * }{@link ChatMessage}{@code >}。
     * @return 表示绑定了流式响应信息消费者的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code consumer} 为 {@code null} 时。
     */
    public Conversation<D, R> bind(StreamingConsumer<ChatMessage, ChatMessage> consumer) {
        Validation.notNull(consumer, "Streaming consumer cannot be null.");
        this.session.setInnerState(StateKey.STREAMING_CONSUMER, consumer);
        return this;
    }

    /**
     * 绑定自定义参数到对话上下文，后续可以在流程中的如下节点获取：
     * <ul>
     *     <li>{@link AiStart#map(Operators.ProcessMap)}</li>
     *     <li>{@link AiStart#just(Operators.ProcessJust)}</li>
     *     <li>{@link AiStart#process(Operators.Process)}</li>
     *     <li>{@link AiStart#reduce(java.util.function.Supplier,
     *     Operators.ProcessReduce)}</li>
     * </ul>
     *
     * @param key 表示自定义键的 {@link String}。
     * @param value 表示自定义值的 {@link Object}。
     * @return 表示绑定了自定义参数的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     */
    public Conversation<D, R> bind(String key, Object value) {
        if (value != null) {
            this.session.setState(key, value);
        }
        return this;
    }

    /**
     * 绑定自定义参数到对话上下文。用途与 {@link Conversation#bind(String, Object)} 一致。
     *
     * @param ctx 表示自定义键值对的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @return 表示绑定了自定义参数的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code ctx} 为 {@code null} 时。
     */
    public Conversation<D, R> bind(Map<String, Object> ctx) {
        Validation.notNull(ctx, "Context map cannot be null.");
        ctx.forEach(this::bind);
        return this;
    }

    /**
     * 设置对话成功回调，本轮对话成功触发该回调。
     *
     * @param processor 表示指定的对话成功时操作的 {@link Consumer}{@code <}{@link R}{@code >}。
     * @return 表示设置完成功回调的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public Conversation<D, R> doOnConsume(Consumer<R> processor) {
        this.callBackBuilder.doOnConsume(processor);
        return this;
    }

    /**
     * 设置对话级错误回调，本轮对话发生异常时触发该回调。
     *
     * @param errorHandler 表示指定的对话发生异常时操作的 {@link Consumer}{@code <? extends }{@link Throwable}{@code >}。
     * @return 表示设置完异常回调的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code errorHandler} 为 {@code null} 时。
     */
    public Conversation<D, R> doOnError(Consumer<Throwable> errorHandler) {
        this.callBackBuilder.doOnError(errorHandler);
        return this;
    }

    /**
     * 设置对话结束回调，无论本轮对话成功或失败都会触发该回调。
     *
     * @param finallyAction 表示本轮对话结束时操作的 {@link Action}。
     * @return 表示设置完结束回调的对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code finallyAction} 为 {@code null} 时。
     */
    public Conversation<D, R> doOnFinally(Action finallyAction) {
        this.callBackBuilder.doOnFinally(finallyAction);
        return this;
    }

    private ConverseLatch<R> setListener(AiProcessFlow<D, R> currFlow) {
        ConverseLatch<R> latch = new ConverseLatch<>();
        Predictable<R> predictable = new Predictable<>(currFlow, this.callBackBuilder.build(), latch);
        ConverseListener<R> listener = this.converseListener.getAndSet(predictable);
        if (listener != null && !listener.isCompleted()) {
            throw new IllegalStateException("conversation is running.");
        }

        AtomicReference<Map<String, AtomicReference<ConverseListener<R>>>> listenerMap =
                this.session.getInnerState(StateKey.CONVERSE_LISTENER);
        listenerMap.get().put(currFlow.getId(), converseListener);

        // 清空临时 builder，用于同一会话的下一次 offer 数据
        this.callBackBuilder = FlowCallBack.builder();
        return latch;
    }

    private FlowSession setSubConverseListener(FlowSession session) {
        FlowSession flowSession = new FlowSession(session);
        if (flowSession.getInnerState(StateKey.CONVERSE_LISTENER) == null) {
            this.setConverseListener(flowSession);
        }
        return flowSession;
    }

    private FlowSession setConverseListener(FlowSession session) {
        session.setInnerState(StateKey.CONVERSE_LISTENER, new AtomicReference<>(new ConcurrentHashMap<>()));
        return session;
    }
}
