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
import modelengine.fit.waterflow.domain.stream.operators.Operators;

/**
 * FlatMap模式的节点
 *
 * @param <T> 入参类型
 * @param <R> 出参类型
 * @author songyongtan
 * @since 1.0
 */
public class FlatMapNode<T, R> extends Node<T, R> {
    /**
     * 构造函数
     *
     * @param streamId 流ID
     * @param wrapper 包装器
     * @param repo 上下文仓库
     * @param messenger 上下文消息
     * @param locks 上下文锁
     */
    public FlatMapNode(String streamId, Operators.Map<FlowContext<T>, R> wrapper, FlowContextRepo repo,
            FlowContextMessenger messenger, FlowLocks locks) {
        super(streamId, wrapper, repo, messenger, locks);
    }

    @Override
    protected From<R> initFrom(FlowContextRepo repo, FlowContextMessenger messenger, FlowLocks locks) {
        return new From<R>(this.getStreamId(), repo, messenger, locks) {
            @Override
            protected void generateIndex(FlowContext context) {
                context.getWindow().generateIndex(context, this);
            }
        };
    }
}
