/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.patterns;

import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.emitters.FlowEmitter;
import modelengine.fitframework.inspection.Validation;

import java.util.Collections;

/**
 * 指定流程节点的异步委托单元的流程实现。
 *
 * @param <I> 表示输入数据的类型。
 * @param <O> 表示流程处理完成的数据类型。
 * @author 宋永坦
 * @since 2025-05-16
 */
public class FlowNodeSupportable<I, O> extends AbstractFlowPattern<I, O> {
    private final AiProcessFlow<I, O> flow;
    private final String nodeId;

    /**
     * 通过 AI 流程初始化 {@link FlowNodeSupportable}{@code <}{@link I}{@code , }{@link O}{@code >}。
     *
     * @param flow 表示 AI 流程的 {@link AiProcessFlow}{@code <}{@link I}{@code , }{@link O}{@code >}。
     * @param nodeId 表示流程节点标识的 {@link String}。
     * @throws IllegalArgumentException 当 {@code flow} 为 {@code null} 时。
     */
    public FlowNodeSupportable(AiProcessFlow<I, O> flow, String nodeId) {
        this.flow = Validation.notNull(flow, "The flow cannot be null.");
        this.nodeId = Validation.notBlank(nodeId, "The node id cannot be null.");
    }

    @Override
    protected AiProcessFlow<I, O> buildFlow() {
        return this.flow;
    }

    @Override
    public FlowEmitter<O> invoke(I data) {
        FlowEmitter<O> emitter = new FlowEmitter.AutoCompleteEmitter<>();
        FlowSession flowSession =  buildFlowSession(emitter);
        this.flow.converse(flowSession).offer(this.nodeId, Collections.singletonList(data));
        return emitter;
    }
}
