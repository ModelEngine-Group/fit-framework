/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.memory;

import modelengine.fel.core.chat.ChatMessage;

import java.util.List;

/**
 * 对话历史记录。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-04-28
 */
public interface Memory {
    /**
     * 插入一条历史记录。
     *
     * @param message 表示问题描述的 {@link ChatMessage}。
     */
    void add(ChatMessage message);

    /**
     * 设置历史记录。
     *
     * @param messages 表示历史记录的 {@link List}{@code <}{@link ChatMessage}{@code >}。
     */
    void set(List<ChatMessage> messages);

    /**
     * 清空历史记录。
     */
    void clear();

    /**
     * 获取历史记录的问答对列表。
     *
     * @return 表示问答对列表的 {@link List}{@code <}{@link ChatMessage}{@code >}。
     */
    List<ChatMessage> messages();

    /**
     * 获取历史记录格式化的文本。
     *
     * @return 表示历史记录文本的 {@link String}。
     */
    String text();
}
