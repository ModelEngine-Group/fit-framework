/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.patterns;

import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fitframework.inspection.Validation;

/**
 * 异步委托单元的 AI 流程实现。
 *
 * @param <I> 表示输入数据的类型。
 * @param <O> 表示流程处理完成的数据类型。
 * @author 刘信宏
 * @since 2024-04-22
 */
public class FlowSupportable<I, O> extends AbstractFlowPattern<I, O> {
    private final AiProcessFlow<I, O> flow;

    /**
     * 通过 AI 流程初始化 {@link FlowSupportable}{@code <}{@link I}{@code , }{@link O}{@code >}。
     *
     * @param flow 表示 AI 流程的 {@link AiProcessFlow}{@code <}{@link I}{@code , }{@link O}{@code >}。
     * @throws IllegalArgumentException 当 {@code flow} 为 {@code null} 时。
     */
    public FlowSupportable(AiProcessFlow<I, O> flow) {
        this.flow = Validation.notNull(flow, "The flow cannot be null.");
    }

    @Override
    protected AiProcessFlow<I, O> buildFlow() {
        return this.flow;
    }
}
