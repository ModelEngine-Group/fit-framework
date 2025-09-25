/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.activities;

import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.emitters.FlowEmitter;
import modelengine.fit.waterflow.domain.flow.Flows;
import modelengine.fit.waterflow.domain.flow.ProcessFlow;
import modelengine.fit.waterflow.domain.states.DataStart;
import modelengine.fitframework.inspection.Validation;

/**
 * 数据优先流的开始节点。
 *
 * @param <O> 表示返回数据类型。
 * @param <D> 表示流程的初始数据类型。
 * @param <I> 表示当前节点的输入数据类型。
 * @author 刘信宏
 * @since 2024-05-20
 */
public class AiDataStart<O, D, I> {
    private final AiStart<O, D, I, ProcessFlow<D>, AiProcessFlow<D, ?>> state;
    private final Emitter<D, FlowSession> emitter;

    /**
     * 使用 AI 流程的开始节点和数据数组初始化 {@link AiDataStart}。
     *
     * @param state 表示 AI 流程的开始节点的 {@link AiStart}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link ProcessFlow}{@code <}{@link D}{@code >, }{@link AiProcessFlow}{@code <}{@link D}{@code , ?>>}。
     * @param data 表示单个数据的 {@link D}。
     * @throws IllegalArgumentException 当 {@code state} 为 {@code null} 时。
     */
    public AiDataStart(AiStart<O, D, I, ProcessFlow<D>, AiProcessFlow<D, ?>> state, D data) {
        this(state, FlowEmitter.mono(data));
    }

    /**
     * 使用 AI 流程的开始节点和数据数组初始化 {@link AiDataStart}。
     *
     * @param state 表示 AI 流程的开始节点的 {@link AiStart}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link ProcessFlow}{@code <}{@link D}{@code >, }{@link AiProcessFlow}{@code <}{@link D}{@code , ?>>}。
     * @param data 表示数据数组的 {@link D}{@code []}。
     * @throws IllegalArgumentException 当 {@code state} 为 {@code null} 时。
     */
    public AiDataStart(AiStart<O, D, I, ProcessFlow<D>, AiProcessFlow<D, ?>> state, D[] data) {
        this(state, FlowEmitter.flux(data));
    }

    /**
     * 使用 AI 流程的开始节点和数据发射源初始化 {@link AiDataStart}。
     *
     * @param state 表示 AI 流程的开始节点的 {@link AiStart}{@code <}{@link O}{@code , }{@link D}{@code , }{@link I}{@code ,
     * }{@link ProcessFlow}{@code <}{@link D}{@code >, }{@link AiProcessFlow}{@code <}{@link D}{@code , ?>>}。
     * @param emitter 表示数据源的 {@link Emitter}{@code <}{@link D}{@code , }{@link FlowSession}{@code >}。
     * @throws IllegalArgumentException 当 {@code state} 或 {@code emitter} 为 {@code null} 时。
     */
    public AiDataStart(AiStart<O, D, I, ProcessFlow<D>, AiProcessFlow<D, ?>> state, Emitter<D, FlowSession> emitter) {
        this.state = Validation.notNull(state, "Ai start state can not be null.");
        this.emitter = Validation.notNull(emitter, "Emitter can not be null.");
    }

    /**
     * 发射数据。
     */
    public void offer() {
        this.state.flow().offer(this.emitter);
        this.emitter.start(null);
    }

    /**
     * 将当前节点转换为 {@link DataStart}{@code <}{@link D}{@code , }{@link D}{@code , }{@link D}{@code >}。
     *
     * @return 表示数据前置开始节点的 {@link DataStart}{@code <}{@link D}{@code , }{@link D}{@code , }{@link D}{@code >}。
     */
    public DataStart<D, D, D> toDataStart() {
        return Flows.source(this.emitter);
    }
}
