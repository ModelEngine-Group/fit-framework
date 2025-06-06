/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.models;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.engine.util.StateKey;
import modelengine.fit.waterflow.bridge.fitflow.FitBoundedEmitter;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fitframework.flowable.Publisher;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.ObjectUtils;

/**
 * 流式模型发射器。
 *
 * @author 刘信宏
 * @since 2024-05-16
 */
public class LlmEmitter<O extends ChatMessage> extends FitBoundedEmitter<O, ChatMessage> {
    private static final StreamingConsumer<ChatMessage, ChatMessage> EMPTY_CONSUMER = (acc, chunk) -> {};

    private final ChatChunk chunkAcc = new ChatChunk();
    private final StreamingConsumer<ChatMessage, ChatMessage> consumer;

    /**
     * 初始化 {@link LlmEmitter}。
     *
     * @param publisher 表示数据发布者的 {@link Publisher}{@code <}{@link O}{@code >}。
     * @param prompt 表示模型输入的 {@link Prompt}， 用于获取默认用户问题。
     * @param session 表示流程实例运行标识的 {@link FlowSession}。
     */
    public LlmEmitter(Publisher<O> publisher, Prompt prompt, FlowSession session) {
        super(publisher, data -> data);
        Validation.notNull(session, "The session cannot be null.");
        this.consumer = ObjectUtils.nullIf(session.getInnerState(StateKey.STREAMING_CONSUMER), EMPTY_CONSUMER);
    }

    @Override
    public void emit(ChatMessage data, FlowSession trans) {
        super.emit(data, this.flowSession);
        this.chunkAcc.merge(data);
        this.consumer.accept(this.chunkAcc, data);
    }
}
