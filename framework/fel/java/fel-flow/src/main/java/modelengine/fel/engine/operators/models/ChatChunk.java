/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.models;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.MessageType;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.tool.ToolCallChunk;
import modelengine.fel.core.tool.support.DefaultToolCallChunk;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 大模型流式响应内容片段。
 *
 * @author 刘信宏
 * @since 2024-05-16
 */
public class ChatChunk implements ChatMessage {
    private final StringBuilder text = new StringBuilder();
    private final List<ToolCall> toolCalls = new ArrayList<>();

    public ChatChunk() {
    }
    /**
     * 使用文本数据、媒体数据和工具请求初始化 {@link ChatChunk}。
     *
     * @param text 表示字符串数据的 {@link String}。
     * @param toolCalls 表示工具请求的 {@link List}{@code <}{@link ToolCall}{@code >}。
     */
    public ChatChunk(String text, List<ToolCall> toolCalls) {
        this.text.append(ObjectUtils.nullIf(text, StringUtils.EMPTY));
        this.toolCalls.addAll(ObjectUtils.getIfNull(toolCalls, Collections::emptyList));
    }

    /**
     * 聚合流式响应内容片段。
     *
     * @param message 表示大模型流式响应内容片段的 {@link ChatMessage}。
     */
    public void merge(ChatMessage message) {
        Validation.notNull(message, "Chat message can not be null.");
        this.merge(message.text(), message.toolCalls());
    }

    @Override
    public MessageType type() {
        return MessageType.AI;
    }

    @Override
    public String text() {
        return this.text.toString();
    }

    @Override
    public List<ToolCall> toolCalls() {
        return this.toolCalls;
    }

    @Override
    public String toString() {
        String textVal = this.toolCalls.isEmpty() ? this.text() : this.toolCalls.toString();
        return this.type().getRole() + ": " + textVal;
    }

    /**
     * 合并文本数据、媒体数据和工具请求。
     *
     * @param text 表示字符串数据的 {@link String}。
     * @param toolCalls 表示工具请求的 {@link List}{@code <}{@link ToolCall}{@code >}。
     */
    private void merge(String text, List<ToolCall> toolCalls) {
        this.text.append(ObjectUtils.nullIf(text, StringUtils.EMPTY));
        if (CollectionUtils.isEmpty(toolCalls)) {
            return;
        }
        toolCalls.stream().filter(Objects::nonNull).forEach(toolCall -> {
            if (StringUtils.isNotBlank(toolCall.id())) {
                this.toolCalls.add(new DefaultToolCallChunk(toolCall));
                return;
            }
            if (toolCall.index() == null || this.toolCalls.size() <= toolCall.index()) {
                return;
            }
            ToolCall tarToolCall = this.toolCalls.get(toolCall.index());
            if (tarToolCall instanceof ToolCallChunk) {
                ObjectUtils.<ToolCallChunk>cast(tarToolCall).merge(toolCall);
            }
        });
    }
}
