/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.tool.support;

import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.tool.ToolCallChunk;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.LazyLoader;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

/**
 * 表示工具调用请求的实体片段默认实现。
 * <p>
 * 该实现不保证流式片段聚合的线程安全，需要外部使用方保证线程安全。
 * </p>
 *
 * @author 刘信宏
 * @since 2024-12-23
 */
public class DefaultToolCallChunk implements ToolCallChunk {
    private final String id;
    private final String name;
    private final String arguments;
    private final LazyLoader<StringBuilder> argumentsBuffer;

    /**
     * 使用 {@link ToolCall} 构造一个新的 {@link ToolCallChunk}。
     *
     * @param toolCall 表示工具调用的 {@link ToolCall}。
     */
    public DefaultToolCallChunk(ToolCall toolCall) {
        Validation.notNull(toolCall, "The tool call cannot be null.");
        this.id = Validation.notNull(toolCall.id(), "The tool call id cannot be null.");
        this.name = toolCall.name();
        this.arguments = toolCall.arguments();
        this.argumentsBuffer =
                new LazyLoader<>(() -> new StringBuilder(ObjectUtils.nullIf(this.arguments, StringUtils.EMPTY)));
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Integer index() {
        // 工具调用的片段不需要index字段。
        return null;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String arguments() {
        return this.argumentsBuffer.get().toString();
    }

    @Override
    public void merge(ToolCall toolCall) {
        Validation.notNull(toolCall, "The tool call cannot be null.");
        this.argumentsBuffer.get().append(ObjectUtils.nullIf(toolCall.arguments(), StringUtils.EMPTY));
    }
}
