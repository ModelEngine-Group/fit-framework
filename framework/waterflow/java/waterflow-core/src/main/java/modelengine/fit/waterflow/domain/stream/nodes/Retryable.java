/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.nodes;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.stream.reactive.Subscriber;

import java.util.List;

/**
 * 流程错误重试机制
 * 有业务更新发生错误后的context，并决定是否重试
 *
 * @author 高诗意
 * @since 1.0
 */
public class Retryable<I> {
    private final FlowContextRepo repo;

    private final Subscriber<I, ?> to;

    /**
     * 构造方法
     *
     * @param repo 仓库
     * @param to 订阅者
     */
    public Retryable(FlowContextRepo repo, Subscriber<I, ?> to) {
        this.repo = repo;
        this.to = to;
    }

    /**
     * 发生错误后，处理contexts上下文
     *
     * @param contexts 需要错误处理的context列表
     */
    public void process(List<FlowContext<I>> contexts) {
        this.repo.update(contexts);
        this.repo.updateStatus(contexts, contexts.get(0).getStatus().toString(), contexts.get(0).getPosition());
    }

    /**
     * 发生错误后，处理contexts上下文，并且发起重试
     *
     * @param contexts 需要错误处理的context列表
     */
    public void retry(List<FlowContext<I>> contexts) {
        this.process(contexts);
        to.onProcess(null, contexts, false);
    }
}
