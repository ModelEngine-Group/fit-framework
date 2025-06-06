/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.chat;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.MessageType;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.serialization.annotation.SerializeStrategy;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.MapBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 此类用于表示 OpenAI 的消息对象。
 *
 * @author 易文渊
 * @author 张庭怿
 * @since 2024-4-30
 */
@SerializeStrategy(include = SerializeStrategy.Include.NON_NULL)
public class OpenAiChatMessage {
    private static final Map<MessageType, String> ROLE_MAP = MapBuilder.<MessageType, String>get()
            .put(MessageType.SYSTEM, "system")
            .put(MessageType.HUMAN, "user")
            .put(MessageType.AI, "assistant")
            .put(MessageType.TOOL, "tool")
            .build();

    private String role;
    private Object content;
    @Property(name = "tool_call_id")
    private String toolCallId;
    @Property(name = "tool_calls")
    private List<OpenAiToolCall> toolCalls;
    @Property(name = "reasoning_content")
    private String reasoningContent;

    /**
     * 将 {@link ChatMessage} 对象转换为 {@link OpenAiChatMessage} 对象。
     *
     * @param chatMessage 表示标准聊天消息的 {@link ChatMessage}。
     * @return 表示 openai 聊天消息的 {@link OpenAiChatMessage}。
     */
    public static OpenAiChatMessage from(ChatMessage chatMessage) {
        OpenAiChatMessage openAiChatMessage = new OpenAiChatMessage();
        openAiChatMessage.role = ROLE_MAP.get(chatMessage.type());
        if (CollectionUtils.isNotEmpty(chatMessage.medias())) {
            List<UserContent> userContents =
                    chatMessage.medias().stream().map(UserContent::from).collect(Collectors.toList());
            userContents.add(UserContent.from(chatMessage.text()));
            openAiChatMessage.content = userContents;
        } else {
            openAiChatMessage.content = chatMessage.text();
        }
        openAiChatMessage.toolCallId = chatMessage.id().orElse(null);
        if (CollectionUtils.isNotEmpty(chatMessage.toolCalls())) {
            openAiChatMessage.toolCalls =
                    chatMessage.toolCalls().stream().map(OpenAiToolCall::from).collect(Collectors.toList());
        }
        return openAiChatMessage;
    }

    /**
     * 获取消息内容，使用 {@link Object} 对类型进行通用表示，有以下两种类型：
     * <ol>
     *     <li>{@link String} 类型，表示单纯文本消息；</li>
     *     <li>由 {@link UserContent} 组成的列表。</li>
     * </ol>
     *
     * @return 表示消息内容的 {@link Object}。
     */
    public Object content() {
        return this.content;
    }

    /**
     * 获取模型推理内容。
     *
     * @return 表示推理内容的 {@link String}。
     */
    public String reasoningContent() {
        return this.reasoningContent;
    }

    /**
     * 获取消息的工具调用。
     *
     * @return 表示工具调用的 {@link List}{@code <}{@link ToolCall}{@code >}。
     */
    public List<ToolCall> toolCalls() {
        return CollectionUtils.asParent(this.toolCalls);
    }
}
