/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template.support;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.support.AiMessage;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fitframework.resource.web.Media;

import java.util.List;

/**
 * 人工智能消息模板实现。
 *
 * @author 易文渊
 * @since 2024-04-25
 */
public class AiMessageTemplate extends AbstractMessageTemplate {
    private final List<ToolCall> toolCalls;

    /**
     * 使用 mustache 模板语法创建 {@link AiMessageTemplate} 的实例。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @param toolCalls 表示需要调用工具列表的 {@link List}{@code <}{@link ToolCall}{@code >}。
     * @see <a href="https://mustache.github.io/">mustache</a>。
     */
    public AiMessageTemplate(String template, List<ToolCall> toolCalls) {
        this(StringTemplate.create(template), toolCalls);
    }

    /**
     * 使用字符串模板创建 {@link AiMessageTemplate} 的实例。
     *
     * @param template 表示字符串模板的 {@link StringTemplate}。
     * @param toolCalls 表示需要调用工具列表的 {@link List}{@code <}{@link ToolCall}{@code >}。
     */
    public AiMessageTemplate(StringTemplate template, List<ToolCall> toolCalls) {
        super(template);
        this.toolCalls = toolCalls;
    }

    @Override
    protected ChatMessage collect(String text, List<Media> ignore) {
        return new AiMessage(text, this.toolCalls);
    }
}