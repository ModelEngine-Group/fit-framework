/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.reactive;

import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fit.waterflow.domain.context.repo.flowcontext.FlowContextRepo;
import modelengine.fit.waterflow.domain.emitters.Emitter;
import modelengine.fit.waterflow.domain.enums.ProcessType;
import modelengine.fit.waterflow.domain.stream.nodes.Blocks;
import modelengine.fit.waterflow.domain.stream.operators.Operators;

import java.util.List;

/**
 * 数据接受者，processor数据在接收者处的处理方式
 *
 * @param <I> 接收的数据类型
 * @param <O> 处理后的数据类型
 * @since 1.0
 */
public interface Subscriber<I, O> extends StreamIdentity, Emitter<O, FlowSession> {
    /**
     * 节点接收请求事件入口
     *
     * @param type 处理类型
     * @param contexts 待处理的上下文
     */
    void accept(ProcessType type, List<FlowContext<I>> contexts);

    /**
     * 设置节点block
     *
     * @param block block
     */
    void block(Blocks.Block<I> block);

    /**
     * 获取节点block
     *
     * @return block
     */
    Blocks.Block<I> block();

    /**
     * 设置节点preFilter
     *
     * @param filter filter
     */
    void preFilter(Operators.Filter<I> filter);

    /**
     * 获取节点preFilter
     *
     * @return preFilter
     */
    Operators.Filter<I> preFilter();

    /**
     * 设置节点postFilter
     *
     * @param filter filter
     */
    void postFilter(Operators.Filter<I> filter);

    /**
     * 获取节点postFilter
     *
     * @return postFilter
     */
    Operators.Filter<I> postFilter();

    /**
     * onSubscribe
     *
     * @param subscription subscription
     */
    void onSubscribe(Subscription<I> subscription);

    /**
     * 节点真正处理context方法onProcess
     *
     * @param type 任务类型，是pre还是
     * @param preList contexts
     * @param isInThread 是否是在线程中运行
     */
    void onProcess(ProcessType type, List<FlowContext<I>> preList, boolean isInThread);

    /**
     * onNext
     *
     * @param batchId batchId
     */
    void onNext(String batchId);

    /**
     * afterProcess
     *
     * @param pre pre
     * @param after after
     */
    void afterProcess(List<FlowContext<I>> pre, List<FlowContext<O>> after);

    /**
     * onComplete
     *
     * @param callback callback
     */
    void onComplete(Operators.Just<Callback<FlowContext<O>>> callback);

    /**
     * onSessionComplete
     *
     * @param sessionCompleteCallback session完成时的callback
     */
    void onSessionComplete(Operators.Just<FlowSession> sessionCompleteCallback);

    /**
     * isAuto
     *
     * @return Boolean
     */
    Boolean isAuto();

    /**
     * nextContexts
     *
     * @param batchId 批次id
     * @return {@link List}{@code <}{@link FlowContext}{@code <}{@link O}{@code >>}
     */
    List<FlowContext<O>> nextContexts(String batchId);

    /**
     * onError
     *
     * @param handler handler
     */
    void onError(Operators.ErrorHandler<I> handler);

    /**
     * onGlobalError
     *
     * @param handler handler
     */
    void onGlobalError(Operators.ErrorHandler handler);

    /**
     * 获取错误处理器列表
     *
     * @return 错误处理器列表
     */
    List<Operators.ErrorHandler> getErrorHandlers();

    /**
     * 获取context repo
     *
     * @return repo
     */
    FlowContextRepo getFlowContextRepo();
}
