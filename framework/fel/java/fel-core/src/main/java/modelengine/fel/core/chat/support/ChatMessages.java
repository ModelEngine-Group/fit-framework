/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.chat.support;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.Prompt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 表示 {@link Prompt} 的默认实现。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-05-06
 */
public class ChatMessages implements Prompt {
    private final List<ChatMessage> messages = new ArrayList<>();

    /**
     * 使用聊天消息创建 {@link ChatMessages} 的实例。
     *
     * @param message 表示聊天消息的 {@link ChatMessage}{@code []}。
     * @return 表示创建成功的 {@link ChatMessages}。
     */
    public static ChatMessages from(ChatMessage... message) {
        return from(Arrays.asList(message));
    }

    /**
     * 使用聊天消息数组创建 {@link ChatMessages} 的实例。
     *
     * @param messages 表示聊天消息的 {@link List}{@code <? extends }{@link ChatMessage}{@code >}。
     * @return 表示创建成功的 {@link ChatMessages}。
     */
    public static ChatMessages fromList(List<? extends ChatMessage> messages) {
        ChatMessages chatMessages = new ChatMessages();
        chatMessages.messages().addAll(messages);
        return chatMessages;
    }

    /**
     * 从给定的提示中创建 {@link ChatMessages} 的实例。
     *
     * @param prompt 表示提示的 {@link Prompt}。
     * @return 表示创建成功的 {@link ChatMessages}。
     */
    public static ChatMessages from(Prompt prompt) {
        if (prompt == null) {
            return new ChatMessages();
        }
        if (prompt instanceof ChatMessages) {
            return cast(prompt);
        }
        return from(prompt.messages());
    }

    /**
     * 使用聊天消息数组创建 {@link ChatMessages} 的实例。
     *
     * @param messages 表示聊天消息的 {@link List}{@code <? extends }{@link ChatMessage}{@code >}。
     * @return 表示创建成功的 {@link ChatMessages}。
     */
    public static ChatMessages from(List<ChatMessage> messages) {
        ChatMessages chatMessages = new ChatMessages();
        chatMessages.messages().addAll(messages);
        return chatMessages;
    }

    /**
     * 添加聊天消息。
     *
     * @param message 表示聊天消息的 {@link ChatMessage}。
     */
    public void add(ChatMessage message) {
        this.messages.add(message);
    }

    /**
     * 批量添加聊天消息。
     *
     * @param messages 表示聊天消息数组的 {@link List}{@code <}{@link ChatMessage}{@code >}。
     */
    public void addAll(List<ChatMessage> messages) {
        this.messages.addAll(messages);
    }

    @Override
    public List<ChatMessage> messages() {
        return this.messages;
    }
}
