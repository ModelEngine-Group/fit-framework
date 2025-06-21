/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context.repo.flowcontext;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.enums.ProcessType;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;
import modelengine.fitframework.util.CollectionUtils;

import java.util.List;

/**
 * 异步化节点间操作
 * 辉子 2020-05-15
 *
 * @author 高诗意
 * @since 1.0
 */
public interface FlowContextMessenger {
    /**
     * 通知subscriber有新的数据到达
     * 数据会堆积在subscription节点
     * subscriber自行按照自己的压力要求request相应数量的数据
     *
     * @param <I> 流程实例执行时的入参数据类型，用于泛型推倒
     * @param type 发送节点处理事件类型，PRE_PROCESS类型为发送人工任务通知，PROCESS类型为节点本身的任务处理
     * @param subscriber 订阅者
     * @param context 流程实例执行过程产生的context
     */
    default <I> void send(ProcessType type, Subscriber<I, ?> subscriber, List<FlowContext<I>> context) {
        if (CollectionUtils.isEmpty(context)) {
            return;
        }
        subscriber.accept(type, context);
    }

    /**
     * 发送事件到引擎外部
     *
     * @param nodeId 节点ID
     * @param contexts 流程实例执行过程产生的contexts
     * @param <I> 流程实例执行时的入参数据类型，用于泛型推倒
     */
    <I> void send(String nodeId, List<FlowContext<I>> contexts);

    /**
     * 发送回调函数事件到引擎外部
     *
     * @param contexts 流程实例执行过程产生的contexts
     * @param <I> 流程实例执行时的入参数据类型，用于泛型推倒
     */
    <I> void sendCallback(List<FlowContext<I>> contexts);

    /**
     * Directly processes a list of flow contexts through the specified subscriber.
     * This method serves as a default implementation for immediate processing without
     * any intermediate transformations or routing.
     *
     * @param <I> The type of input data contained in the flow contexts.
     * @param type The type of processing to be performed.
     * @param subscriber The subscriber that will handle the processing.
     * @param context List of flow contexts to be processed.
     */
    default <I> void directProcess(ProcessType type, Subscriber<I, ?> subscriber, List<FlowContext<I>> context) {
        subscriber.process(type, context);
    }
}
