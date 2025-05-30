/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.models;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.nullIf;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.engine.util.AiFlowSession;
import modelengine.fel.engine.util.StateKey;
import modelengine.fit.waterflow.bridge.fitflow.FitBoundedEmitter;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.util.ObjectUtils;

/**
 * 流式对话模型实现。
 *
 * @author 刘信宏
 * @since 2024-05-16
 */
public class ChatFlowModel implements FlowModel<Prompt, ChatMessage> {
    private final ChatModel chatModel;

    private final ChatOption option;

    public ChatFlowModel(ChatModel chatModel, ChatOption option) {
        this.chatModel = notNull(chatModel, "The model provider can not be null.");
        this.option = option;
    }

    /**
     * 绑定模型超参数。
     *
     * @param option 表示模型超参数的 {@link ChatOption}。
     * @return 表示绑定了超参数的 {@link ChatModel}。
     * @throws IllegalArgumentException 当 {@code options} 为 {@code null} 时。
     */
    public ChatFlowModel bind(ChatOption option) {
        notNull(option, "The chat options cannot be null.");
        return new ChatFlowModel(this.chatModel, option);
    }

    @Override
    public FitBoundedEmitter<ChatMessage, ChatMessage> invoke(Prompt input) {
        notNull(input, "The model input data can not be null.");
        FlowSession session =
                AiFlowSession.get().orElseThrow(() -> new IllegalStateException("The ai session cannot be empty."));
        ChatOption dynamicOption = nullIf(session.getInnerState(StateKey.CHAT_OPTION), this.option);
        notNull(dynamicOption, "The chat options can not be null.");
        Choir<ChatMessage> choir = ObjectUtils.cast(this.chatModel.generate(input, dynamicOption));
        return new LlmEmitter<>(choir, input, session);
    }
}
