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
import modelengine.fit.waterflow.domain.enums.ParallelMode;
import modelengine.fit.waterflow.domain.stream.callbacks.PreSendCallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * 平行节点，也是fork的起始节点
 * 有两个模式：either只要一个fork就算完成；all所有fork完成才完成
 * parallel节点覆盖了from的subscribe方法，允许subscribe到多个subscriber
 * parallel只允许just处理器，并且之后的节点在出现join前都只允许just处理器，否则join不了
 * 辉子 2019-12-17
 *
 * @param <I>传入数据类型
 * @author 高诗意
 * @since 1.0
 */
public class ParallelNode<I> extends Node<I, I> {
    private final ParallelMode mode;

    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param mode 并行节点执行模式
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param locks 流程锁
     */
    public ParallelNode(String streamId, ParallelMode mode, FlowContextRepo repo, FlowContextMessenger messenger,
            FlowLocks locks) {
        super(streamId, FlowContext::getData, repo, messenger, locks,
                () -> initFrom(streamId, mode, repo, messenger, locks));
        this.mode = mode;
    }

    /**
     * 1->1处理节点
     *
     * @param streamId stream流程ID
     * @param nodeId stream流程节点ID
     * @param mode 并行节点的模式
     * @param repo 上下文持久化repo，默认在内存
     * @param messenger 上下文事件发送器，默认在内存
     * @param locks 流程锁
     * @param nodeType 节点类型
     */
    public ParallelNode(String streamId, String nodeId, ParallelMode mode, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks, FlowNodeType nodeType) {
        this(streamId, mode, repo, messenger, locks);
        this.id = nodeId;
        this.nodeType = nodeType;
    }

    /**
     * either:只publish给一个subscription
     * all：publish给所有的subscription
     *
     * @param streamId id
     * @param mode mode
     * @param repo 持久化
     * @param messenger 发送器
     * @param locks 流程锁
     * @return From
     */
    private static <I> From<I> initFrom(String streamId, ParallelMode mode, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks) {
        return new From<I>(streamId, repo, messenger, locks) {
            @Override
            public void offer(List<FlowContext<I>> contexts, Consumer<PreSendCallbackInfo<I>> preSendCallback) {
                contexts.forEach(c -> c.setParallel(c.getId()).setParallelMode(mode.name()));
                super.offer(contexts, preSendCallback);
            }
        };
    }
}
