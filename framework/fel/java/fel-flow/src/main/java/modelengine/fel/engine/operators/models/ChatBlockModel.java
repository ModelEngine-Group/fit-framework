/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.models;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.MessageType;
import modelengine.fel.core.chat.Prompt;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.chat.ChatModel;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.model.BlockModel;
import modelengine.fel.engine.util.AiFlowSession;
import modelengine.fel.engine.util.StateKey;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fitframework.inspection.Validation;

import java.util.List;
import java.util.Optional;

/**
 * 阻塞对话模型实现。
 *
 * @author 刘信宏
 * @since 2024-04-16
 */
public class ChatBlockModel implements BlockModel<Prompt, ChatMessage> {
    private final ChatModel provider;
    private final ChatOption option;

    public ChatBlockModel(ChatModel provider) {
        this(provider, ChatOption.custom().build());
    }

    public ChatBlockModel(ChatModel provider, ChatOption option) {
        this.provider = Validation.notNull(provider, "The model provider cannot be null.");
        this.option = Validation.notNull(option, "The chat options cannot be null.");
    }

    @Override
    public ChatMessage invoke(Prompt input) {
        Validation.notNull(input, "The model input data cannot be null.");
        ChatOption dynamicOptions = AiFlowSession.get()
                .map(state -> state.<ChatOption>getInnerState(StateKey.CHAT_OPTION))
                .orElse(this.option);
        List<ChatMessage> chatMessages = this.provider.generate(input, dynamicOptions).blockAll();
        Validation.notEmpty(chatMessages, "The model chat messages can not be empty.");
        ChatMessage message = chatMessages.get(0);
        Validation.equals(message.type(),
                MessageType.AI,
                "The message type must be {0}. [actualMessageType={1}]",
                MessageType.AI,
                message.type());
        AiMessage answer = new AiMessage(message.text(), message.toolCalls());
        this.updateMemory(answer);
        return answer;
    }

    /**
     * 绑定模型超参数。
     *
     * @param option 表示模型超参数的 {@link ChatOption}。
     * @return 表示绑定了超参数的 {@link ChatBlockModel}。
     * @throws IllegalArgumentException 当 {@code options} 为 {@code null} 时。
     */
    public ChatBlockModel bind(ChatOption option) {
        Validation.notNull(option, "The chat options cannot be null.");
        return new ChatBlockModel(this.provider, option);
    }

    private void updateMemory(AiMessage answer) {
        if (answer.isToolCall()) {
            return;
        }
        Optional<FlowSession> session = AiFlowSession.get();
        if (!session.isPresent()) {
            return;
        }
        Memory memory = session.get().getInnerState(StateKey.HISTORY);
        if (memory == null) {
            return;
        }
        memory.add(session.get().getInnerState(StateKey.HISTORY_INPUT));
        memory.add(answer);
    }
}
