/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.chat;

import modelengine.fel.core.chat.ChatOption;
import modelengine.fel.core.chat.Prompt;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.serialization.annotation.SerializeStrategy;
import modelengine.fitframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenAi API 格式的会话补全请求。
 *
 * @author 易文渊
 * @author 张庭怿
 * @since 2024-4-30
 */
@SerializeStrategy(include = SerializeStrategy.Include.NON_NULL)
public class OpenAiChatCompletionRequest {
    private final List<OpenAiChatMessage> messages;
    private final String model;
    @Property(name = "frequency_penalty")
    private final Double frequencyPenalty;
    @Property(name = "max_tokens")
    private final Integer maxTokens;
    @Property(name = "presence_penalty")
    private final Double presencePenalty;
    private final List<String> stop;
    private final boolean stream;
    private final Double temperature;
    @Property(name = "top_p")
    private final Double topP;
    @Property(name = "tool_choice")
    private final String toolChoice;
    private final List<OpenAiTool> tools;

    /**
     * 创建 {@link OpenAiChatCompletionRequest} 的实例。
     *
     * @param prompt 表示用户输入提示词的 {@link Prompt}。
     * @param chatOption 表示聊天模型参数的 {@link ChatOption}。
     */
    public OpenAiChatCompletionRequest(Prompt prompt, ChatOption chatOption) {
        this.messages = prompt.messages().stream().map(OpenAiChatMessage::from).collect(Collectors.toList());
        this.model = chatOption.model();
        this.frequencyPenalty = chatOption.frequencyPenalty();
        this.maxTokens = chatOption.maxTokens();
        this.presencePenalty = chatOption.presencePenalty();
        this.stop = chatOption.stop();
        this.stream = chatOption.stream();
        this.temperature = chatOption.temperature();
        this.topP = chatOption.topP();
        if (CollectionUtils.isEmpty(chatOption.tools())) {
            this.toolChoice = null;
            this.tools = null;
        } else {
            this.toolChoice = "auto";
            this.tools = chatOption.tools().stream().map(OpenAiTool::from).collect(Collectors.toList());
        }
    }
}
