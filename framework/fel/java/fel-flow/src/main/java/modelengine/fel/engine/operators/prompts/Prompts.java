/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.prompts;

import modelengine.fel.core.chat.ChatMessage;
import modelengine.fel.core.chat.support.ChatMessages;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.template.support.AiMessageTemplate;
import modelengine.fel.core.template.support.HumanMessageTemplate;
import modelengine.fel.core.template.support.SystemMessageTemplate;
import modelengine.fel.core.template.support.ToolMessageTemplate;
import modelengine.fel.core.tool.ToolCall;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.activities.AiStart;
import modelengine.fel.engine.util.AiFlowSession;
import modelengine.fel.engine.util.StateKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 提示词工具方法。用于 {@link AiStart#prompt(PromptTemplate[])} 构造提示词。
 *
 * @author 刘信宏
 * @since 2024-04-12
 */
public class Prompts {
    /**
     * 构造系统提示词模板。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @return 表示人类提示词模板的 {@link PromptTemplate}{@code <}{@link Tip}{@code >}。
     */
    public static PromptTemplate<Tip> sys(String template) {
        return new DefaultPromptTemplate(new SystemMessageTemplate(template));
    }

    /**
     * 构造人类提示词模板。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @return 表示人类提示词模板的 {@link PromptTemplate}{@code <}{@link Tip}{@code >}。
     */
    public static PromptTemplate<Tip> human(String template) {
        return new DefaultPromptTemplate(new HumanMessageTemplate(template));
    }

    /**
     * 构造人工智能提示词模板。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @param toolCalls 表示工具调用数组的 {@link ToolCall}{@code []}。
     * @return 表示人工智能模板的 {@link PromptTemplate}{@code <}{@link Tip}{@code >}。
     */
    public static PromptTemplate<Tip> ai(String template, ToolCall... toolCalls) {
        return new DefaultPromptTemplate(new AiMessageTemplate(template, Arrays.asList(toolCalls)));
    }

    /**
     * 构造工具调用提示词模板。
     *
     * @param template 表示使用 mustache 模板语法的 {@link String}。
     * @param id 表示工具调用唯一编号的 {@link String}。
     * @return 表示人类提示词模板的 {@link PromptTemplate}{@code <}{@link Tip}{@code >}。
     * @throws IllegalArgumentException 当 {@code template} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    public static PromptTemplate<Tip> tool(String template, String id) {
        return new DefaultPromptTemplate(new ToolMessageTemplate(template, id));
    }

    /**
     * 构造历史记录提示词模板。
     *
     * @param <I> 表示提示词模板入参的类型。
     * @return 表示提示词模板的 {@link PromptTemplate}{@code <}{@link I}{@code >}。
     */
    public static <I> PromptTemplate<I> history() {
        return input -> {
            List<ChatMessage> messages = AiFlowSession.get()
                    .map(session -> session.<Memory>getInnerState(StateKey.HISTORY))
                    .map(Memory::messages)
                    .orElseGet(Collections::emptyList);
            return ChatMessages.from(messages);
        };
    }
}
