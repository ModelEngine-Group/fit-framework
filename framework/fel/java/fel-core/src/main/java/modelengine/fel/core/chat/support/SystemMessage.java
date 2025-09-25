/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.chat.support;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.MessageType;

/**
 * 表现系统消息的 {@link ChatMessage} 实现。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-4-12
 */
public class SystemMessage extends AbstractChatMessage {
    /**
     * 通过文本信息来初始化 {@link SystemMessage} 的新实例。
     *
     * @param text 表示文本信息的 {@link String}。
     */
    public SystemMessage(String text) {
        super(text);
    }

    @Override
    public MessageType type() {
        return MessageType.SYSTEM;
    }
}
