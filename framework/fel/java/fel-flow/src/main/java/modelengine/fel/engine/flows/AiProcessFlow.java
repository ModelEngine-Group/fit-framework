/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.flow.ProcessFlow;
import modelengine.fit.waterflow.domain.stream.reactive.Publisher;
import modelengine.fitframework.util.ObjectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 数据处理流程，在 {@link AiFlow} 的基础上增加流程间的数据流转能力，并对外提供对话语义。
 *
 * @param <D> 表示流程开始节点传入的数据类型。
 * @param <R> 表示流程结束节点输出的数据类型。
 * @author 刘信宏
 * @since 2024-04-28
 */
public class AiProcessFlow<D, R> extends AiFlow<D, ProcessFlow<D>>
        implements EmitterListener<D, FlowSession>, Emitter<R, FlowSession> {
    private final Map<EmitterListener<R, FlowSession>, EmitterListener<Object, FlowSession>> listeners =
            new ConcurrentHashMap<>();

    public AiProcessFlow(ProcessFlow<D> flow) {
        super(flow);
    }

    @Override
    public void handle(D data, FlowSession session) {
        this.origin().offer(data, session);
    }

    @Override
    public void register(EmitterListener<R, FlowSession> listener) {
        if (listener != null) {
            EmitterListener<Object, FlowSession> wrapperHandler =
                    (data, session) -> listener.handle(ObjectUtils.cast(data), session);
            this.listeners.put(listener, wrapperHandler);
            this.origin().register(wrapperHandler);
        }
    }

    public void unregister(EmitterListener<R, FlowSession> listener) {
        EmitterListener<Object, FlowSession> target = this.listeners.remove(listener);
        if (target != null) {
            this.origin().unregister(target);
        }
    }

    @Override
    public void emit(R data, FlowSession session) {
        this.origin().emit(data, (session));
    }

    /**
     * 获取流程的开始节点。
     *
     * @return 表示流程开始节点的 {@link Publisher}{@code <}{@link D}{@code >}。
     */
    @Override
    public Publisher<D> start() {
        return this.origin().start();
    }

    /**
     * 流程的指定命名节点订阅一个发射源，由发射源驱动流程流转。
     *
     * @param nodeId 表示节点名称的 {@link String}
     * @param emitter 表示被订阅的发射源的 {@link Emitter}{@code <}{@link D}{@code , }{@link FlowSession}{@code >}。
     * @throws IllegalArgumentException 当 {@code nodeId} 为 {@code null} 、空字符串或只有空白字符的字符串时，或
     * {@code emitter} 为 {@code null} 时。
     */
    public void offer(String nodeId, Emitter<D, FlowSession> emitter) {
        notBlank(nodeId, "Invalid nodeId.");
        notNull(emitter, "Emitter can not be null.");
        this.origin().offer(nodeId, emitter);
    }

    /**
     * 根据已有的流程会话实例信息启动一个对话。
     *
     * @param session 表示流程会话实例信息的 {@link FlowSession}。
     * @return 表示对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     */
    public Conversation<D, R> converse(FlowSession session) {
        return new Conversation<>(this, session);
    }

    /**
     * 启动一个新对话。
     *
     * @return 表示对话对象的 {@link Conversation}{@code <}{@link D}{@code , }{@link R}{@code >}。
     */
    public Conversation<D, R> converse() {
        return this.converse(null);
    }
}
