/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.nodes;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.context.repo.flowlock.FlowLocks;
import modelengine.fit.waterflow.domain.enums.FlowNodeType;
import modelengine.fit.waterflow.domain.utils.UUIDUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 条件节点，也是match的起始节点
 * 辉子 2019-12-17
 *
 * @param <I>传入数据类型
 * @author 高诗意
 * @since 1.0
 */
public class ConditionsNode<I> extends Node<I, I> {
    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param locks 流程锁
     */
    public ConditionsNode(String streamId, FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        super(streamId, FlowContext::getData, repo, messenger, locks, () -> initFrom(streamId, repo, messenger, locks));
        super.id = "condition:" + UUIDUtil.uuid();
    }

    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param nodeId stream流程节点ID
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param locks 流程锁
     * @param nodeType 节点类型
     */
    public ConditionsNode(String streamId, String nodeId, FlowContextRepo repo, FlowContextMessenger messenger,
            FlowLocks locks, FlowNodeType nodeType) {
        this(streamId, repo, messenger, locks);
        this.id = nodeId;
        this.nodeType = nodeType;
    }

    /**
     * 只publish给符合条件的subscription
     *
     * @param streamId id
     * @param repo 持久化
     * @param messenger 事件发送器
     * @param locks 流程锁
     * @param <I> 数据类型
     * @return From 数据publisher
     */
    private static <I> From<I> initFrom(String streamId, FlowContextRepo repo, FlowContextMessenger messenger,
            FlowLocks locks) {
        return new ConditionFrom<>(streamId, repo, messenger, locks);
    }

    private static class ConditionFrom<I> extends From<I> {
        public ConditionFrom(String streamId, FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
            super(streamId, repo, messenger, locks);
        }

        @Override
        public void offer(List<FlowContext<I>> contexts) {
            this.getSubscriptions().forEach(subscription -> {
                List<FlowContext<I>> matched = contexts.stream()
                        .filter(context -> subscription.getWhether().is(context.getData()))
                        .collect(Collectors.toList());
                matched.forEach(contexts::remove);
                subscription.cache(matched);
            });
        }

    }
}
