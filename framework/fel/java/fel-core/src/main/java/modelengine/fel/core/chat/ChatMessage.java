/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.chat;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 表示聊天消息的接口。
 *
 * @author 易文渊
 * @since 2024-04-16
 */
public interface ChatMessage {
    /**
     * 获取消息id。
     *
     * @return 表示消息id的 {@link Optional}{@code <}{@link String}{@code >}。
     */
    default Optional<String> id() {
        return Optional.empty();
    }

    /**
     * 获取消息类型。
     *
     * @return 表示消息类型的 {@link MessageType}。
     */
    MessageType type();

    /**
     * 获取消息文本内容。
     *
     * @return 表示文本内容的 {@link String}。
     */
    String text();

    /**
     * 获取消息媒体内容。
     *
     * @return 表示媒体内容的 {@link List}{@code <}{@link Media}{@code >}。
     */
    default List<Media> medias() {
        return Collections.emptyList();
    }

    /**
     * 获取消息工具调用。
     *
     * @return 表示消息工具调用的 {@link List}{@code <}{@link ToolCall}{@code >}。
     */
    default List<ToolCall> toolCalls() {
        return Collections.emptyList();
    }

    /**
     * 判断模型响应是否是调用工具。
     *
     * @return 表示是否是调用工具的 {@code boolean}。
     */
    default boolean isToolCall() {
        return CollectionUtils.isNotEmpty(this.toolCalls());
    }
}