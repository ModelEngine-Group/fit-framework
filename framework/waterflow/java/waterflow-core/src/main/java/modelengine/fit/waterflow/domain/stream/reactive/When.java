/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.reactive;

import static modelengine.fit.waterflow.domain.enums.ProcessType.PRE_PROCESS;
import static modelengine.fit.waterflow.domain.enums.ProcessType.PROCESS;

import lombok.Getter;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextMessenger;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.enums.FlowNodeStatus;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fit.waterflow.domain.utils.IdGenerator;
import modelengine.fitframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * fit中的subscription：简单理解为流程图中的连接线
 * 辉子 2019-10-31
 *
 * @param <I> 来源数据类型
 * @author 高诗意
 * @since 1.0
 */
public class When<I> extends IdGenerator implements Subscription<I> {
    /**
     * 满足condition才能进入cache
     */
    @Getter
    private final Operators.Whether<I> whether;

    /**
     * 要发往的接受者
     */
    @Getter
    private final Subscriber<I, ?> to;

    private final FlowContextRepo repo;

    private final FlowContextMessenger messenger;

    private final String streamId;

    /**
     * When
     *
     * @param <R> R
     * @param streamId streamId
     * @param to to
     * @param whether whether
     * @param repo contextRepo
     * @param messenger messenger
     */
    public <R> When(String streamId, Subscriber<I, R> to, Operators.Whether<I> whether, FlowContextRepo repo,
            FlowContextMessenger messenger) {
        this.streamId = streamId;
        this.whether = whether == null ? any -> true : whether;
        this.to = to;
        this.to.onSubscribe(this);
        this.repo = repo;
        this.messenger = messenger;
    }

    /**
     * When
     *
     * @param <R> R
     * @param streamId streamId
     * @param eventId eventId
     * @param to to
     * @param whether whether
     * @param repo contextRepo
     * @param messenger messenger
     */
    public <R> When(String streamId, String eventId, Subscriber<I, R> to, Operators.Whether<I> whether,
            FlowContextRepo repo, FlowContextMessenger messenger) {
        this(streamId, to, whether, repo, messenger);
        this.id = eventId;
    }

    @Override
    public void cache(List<FlowContext<I>> contexts) {
        if (CollectionUtils.isEmpty(contexts)) {
            return;
        }
        // 将context发送到节点边上，更新为PENDING状态，等待下一个节点处理
        // 该过程不产生新的context数据，只更新context的状态
        List<FlowContext<I>> converted = contexts.stream()
                .map(context -> context.convertData(context.getData(), context.getId())
                        .setPosition(this.getId())
                        .setStatus(FlowNodeStatus.PENDING))
                .collect(Collectors.toList());
        repo.updateStatus(converted, converted.get(0).getStatus().toString(), converted.get(0).getPosition());
        messenger.send(this.to.isAuto() ? PROCESS : PRE_PROCESS, this.to, converted);
    }

    @Override
    public String getStreamId() {
        return this.streamId;
    }
}
