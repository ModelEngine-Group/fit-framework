/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.flows;

import modelengine.fel.engine.activities.FlowCallBack;
import modelengine.fit.waterflow.domain.context.FlowContext;
import modelengine.fit.waterflow.domain.stream.nodes.Retryable;
import modelengine.fit.waterflow.domain.stream.operators.Operators;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

/**
 * 流程对话级别的回调操作。
 *
 * @author 刘信宏
 * @since 2024-04-10
 */
public class Predictable<T> implements ConverseListener<T> {
    private final FlowCallBack<T> converseCallBack;
    private final Operators.ErrorHandler<Object> flowErrorCb;
    private final String flowId;
    private final ConverseLatch<T> latch;

    /**
     * 对话回调操作对象的构造方法。
     *
     * @param flow 表示 AI 数据处理流程的 {@link AiProcessFlow}{@code <}{@code ?, }{@link T}{@code >}。
     * @param converseCallBack 表示流程会话回调对象的 {@link FlowCallBack}{@code <}{@link T}{@code >}。
     * @param latch 表示流程调用同步器的 {@link ConverseLatch}{@code <}{@link T}{@code >}。
     * @throws IllegalArgumentException 当 {@code flow}、{@code converseCallBack} 或 {@code latch} 为 {@code null} 时。
     */
    public Predictable(AiProcessFlow<?, T> flow, FlowCallBack<T> converseCallBack, ConverseLatch<T> latch) {
        Validation.notNull(flow, "Flow can not be null.");
        Validation.notNull(converseCallBack, "Callback can not be null.");
        this.converseCallBack = FlowCallBack.<T>builder()
                .doOnConsume(converseCallBack.consumeCb().andThen(latch::data))
                .doOnError(converseCallBack.errorCb().andThen(latch::throwable))
                .doOnCompleted(converseCallBack.completedCb())
                .doOnFinally(latch::countDown)
                .build();
        this.flowErrorCb = (exception, retryable, contexts) -> {
            List<Operators.ErrorHandler<Object>> handlers = ObjectUtils.cast(flow.end().getErrorHandlers());
            handlers.forEach(handler -> handler.handle(exception, retryable, contexts));
        };
        this.flowId = flow.getId();
        this.latch = Validation.notNull(latch, "Conversation latch can not be null.");
    }

    @Override
    public void onConsume(String flowId, T data) {
        // 对话数据消费回调由直接起会话的流程触发
        if (!Objects.equals(flowId, this.flowId)) {
            return;
        }
        this.converseCallBack.consumeCb().accept(data);
    }

    @Override
    public void onConverseCompleted(String flowId) {
        // 对话结束回调由直接起会话的流程触发
        if (!Objects.equals(flowId, this.flowId)) {
            return;
        }
        this.converseCallBack.completedCb().exec();
    }

    @Override
    public void onFlowError(Exception exception, Retryable<Object> retryable, List<FlowContext<Object>> contexts) {
        this.flowErrorCb.handle(exception, retryable, contexts);
    }

    @Override
    public void onConverseError(Exception exception) {
        this.converseCallBack.errorCb().accept(exception);
    }

    @Override
    public boolean isCompleted() {
        if (this.latch == null) {
            return true;
        }
        return this.latch.countDownLatch().getCount() == 0;
    }
}
