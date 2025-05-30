/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.patterns;

import modelengine.fel.core.pattern.Pattern;
import modelengine.fel.engine.util.AiFlowSession;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.EmitterListener;
import modelengine.fit.waterflow.domain.emitters.FlowEmitter;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.ObjectUtils;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * {@link FlowPattern} 的简单实现，仅支持注册一个监听器，使用 {@link ThreadPoolExecutor} 创建的线程池执行。
 *
 * @param <I> 表示输入数据类型。
 * @param <O> 表示输出数据类型。
 * @author 刘信宏
 * @since 2024-04-22
 */
public class SimpleFlowPattern<I, O> implements FlowPattern<I, O> {
    private final FlowEmitter<O> emitter = new FlowEmitter<>();
    private final Operators.ProcessMap<I, O> processor;

    /**
     * 使用数据处理器初始化 {@link SimpleFlowPattern}{@code <}{@link I}{@code , }{@link O}{@code >}。
     *
     * @param processor 表示数据处理器的 {@link Operators.Map}{@code <}{@link I}{@code , }{@link O}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public SimpleFlowPattern(Operators.ProcessMap<I, O> processor) {
        this.processor = Validation.notNull(processor, "The processor cannot be null.");
    }

    /**
     * 使用委托单元初始化 {@link SimpleFlowPattern}{@code <}{@link I}{@code , }{@link O}{@code >}。
     *
     * @param pattern 表示委托单元的 {@link Pattern}{@code <}{@link I}{@code , }{@link O}{@code >}。
     * @throws IllegalArgumentException 当 {@code processor} 为 {@code null} 时。
     */
    public SimpleFlowPattern(Pattern<I, O> pattern) {
        this((data, ctx) -> AiFlowSession.applyPattern(pattern, data, ObjectUtils.cast(ctx)));
    }

    @Override
    public FlowEmitter<O> invoke(I data) {
        FlowSession session = AiFlowSession.require();
        this.emitter.emit(this.processor.process(data, session));
        this.emitter.complete();
        return this.emitter;
    }

    @Override
    public void register(EmitterListener<O, FlowSession> handler) {
        this.emitter.register(handler);
    }

    @Override
    public void unregister(EmitterListener<O, FlowSession> handler) {
        this.emitter.unregister(handler);
    }

    @Override
    public void emit(O data, FlowSession session) {
        this.emitter.emit(data, session);
    }
}
