/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.chat.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.MessageType;

import java.util.Optional;

/**
 * 表示工具消息的 {@link ChatMessage} 实现。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-4-3
 */
public class ToolMessage extends AbstractChatMessage {
    private final String id;

    /**
     * 通过请求id和工具响应来初始化 {@link ToolMessage} 的新实例。
     *
     * @param id 表示请求编码的 {@link String}。
     * @param text 表示工具调用结果的 {@link String}。
     * @throws IllegalArgumentException 当 {@code id} 为空字符串时。
     */
    public ToolMessage(String id, String text) {
        super(text);
        this.id = notBlank(id, "The id cannot be blank.");
    }

    @Override
    public Optional<String> id() {
        return Optional.of(this.id);
    }

    @Override
    public MessageType type() {
        return MessageType.TOOL;
    }

    @Override
    public String toString() {
        return "tool " + this.id + ": " + this.text();
    }
}