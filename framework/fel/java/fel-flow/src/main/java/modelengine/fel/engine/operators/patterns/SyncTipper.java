/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.operators.patterns;

import modelengine.fel.core.document.Content;
import modelengine.fel.core.fewshot.ExampleSelector;
import modelengine.fel.core.format.FormatProvider;
import modelengine.fel.core.memory.Memory;
import modelengine.fel.core.pattern.Pattern;
import modelengine.fel.core.util.Tip;
import modelengine.fel.engine.activities.AiStart;
import modelengine.fel.engine.flows.AiFlows;
import modelengine.fel.engine.flows.AiProcessFlow;
import modelengine.fel.engine.util.AiFlowSession;
import modelengine.fel.engine.util.StateKey;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.StringUtils;

/**
 * 平行分支工具。<p>用于 {@link AiStart#runnableParallel(Pattern[])} 表达式。
 * 每个分支生成一个键值对。</p>
 *
 * @since 2024-05-07
 */
public interface SyncTipper<I> extends Pattern<I, Tip> {
    /**
     * 表示用户问题的默认键。
     */
    String DEFAULT_QUESTION_KEY = "question";

    /**
     * 表示问答对示例的默认键。
     */
    String DEFAULT_EXAMPLE_KEY = "examples";

    /**
     * 表示格式化提示词的默认键。
     */
    String DEFAULT_FORMAT_KEY = "format";

    /**
     * 表示历史记录的默认键。
     */
    String DEFAULT_HISTORY_KEY = "history";

    /**
     * 同步委托分支。
     *
     * @param key 表示同步委托单元结果所在的键的 {@link String}。
     * @param pattern 表示同步委托单元的 {@link P}，它是 {@link Pattern}{@code <}{@link I}{@code ,
     * }{@link Content}{@code >} 的拓展。
     * @param <I> 表示委托单元的入参类型。
     * @param <P> 表示委托单元的类型。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link I}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 、空字符串或只有空白字符的字符串时，或
     * {@code pattern} 为 {@code null} 时。
     */
    static <I, P extends Pattern<I, Content>> SyncTipper<I> value(String key, P pattern) {
        Validation.notBlank(key, "Key cannot be blank.");
        Validation.notNull(pattern, "Pattern cannot be null.");
        return arg -> Tip.from(key, pattern.invoke(arg));
    }

    /**
     * 子流程分支。
     *
     * @param key 表示子流程输出结果所在的键的 {@link String}。
     * @param flow 表示子流程的 {@link AiProcessFlow}{@code <}{@link I}{@code , }{@link Content}{@code >}。
     * @param <I> 表示委托的入参类型。
     * @return 表示一个同步委托单元的 {@link Pattern}{@code <}{@link I}{@code , }{@link Tip}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 、空字符串或只有空白字符的字符串时，或 {@code flow}
     * 为 {@code null} 时。
     */
    static <I> Pattern<I, Tip> value(String key, AiProcessFlow<I, Content> flow) {
        Validation.notBlank(key, "Key cannot be blank.");
        Validation.notNull(flow, "Flow cannot be null.");
        return new FlowSupportable<>(AiFlows.<I>create()
                .delegate(flow)
                .map(input -> Tip.from(key, input))
                .close()).sync();
    }

    /**
     * 简单键值对分支。
     *
     * @param <I> 输入数据类型。
     * @param key 表示自定义键的 {@link String}。
     * @param value 表示自定义键对应的值的 {@link String}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link I}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    static <I> SyncTipper<I> value(String key, String value) {
        Validation.notBlank(key, "Key cannot be blank.");
        return arg -> Tip.from(key, value);
    }

    /**
     * 自定义键的用户问题键值对分支。
     *
     * @param questionKey 表示用户问题的键的 {@link String}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code questionKey} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    static SyncTipper<String> question(String questionKey) {
        Validation.notBlank(questionKey, "Question key cannot be blank.");
        return arg -> Tip.from(questionKey, arg);
    }

    /**
     * 默认键的用户问题键值对分支。
     *
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link String}{@code >}。
     */
    static SyncTipper<String> question() {
        return SyncTipper.question(DEFAULT_QUESTION_KEY);
    }

    /**
     * 自定义键的问答对示例键值对分支。
     *
     * @param key 表示问答对示例的键的 {@link String}。
     * @param selector 表示示例选择器的 {@link ExampleSelector}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 、空字符串或只有空白字符的字符串时，或 {@code selector}
     * 为 {@code null} 时。
     */
    static SyncTipper<String> fewShot(String key, ExampleSelector selector) {
        Validation.notBlank(key, "FewShot key cannot be blank.");
        Validation.notNull(selector, "Flow cannot be null.");
        return arg -> Tip.from(key, selector.select(arg));
    }

    /**
     * 默认键的问答对示例键值对分支。
     *
     * @param selector 表示示例选择器的 {@link ExampleSelector}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code selector} 为 {@code null} 时。
     */
    static SyncTipper<String> fewShot(ExampleSelector selector) {
        return fewShot(DEFAULT_EXAMPLE_KEY, selector);
    }

    /**
     * 自定义键的格式化提示词键值对分支。
     *
     * @param <I> 输入数据类型。
     * @param key 表示格式化提示词的键的 {@link String}。
     * @param formatter 表示格式化提示词的 {@link FormatProvider}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link I}{@code >}。
     * @throws IllegalArgumentException 当 {@code key} 为 {@code null} 、空字符串或只有空白字符的字符串时，或 {@code formatter}
     * 为 {@code null} 时。
     */
    static <I> SyncTipper<I> format(String key, FormatProvider formatter) {
        Validation.notBlank(key, "Formatter key cannot be blank.");
        Validation.notNull(formatter, "Formatter cannot be null.");
        return arg -> Tip.from(key, formatter.instruction());
    }

    /**
     * 默认键的格式化提示词键值对分支。
     *
     * @param <I> 输入参数类型。
     * @param formatter 表示格式化提示词的 {@link FormatProvider}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link I}{@code >}。
     * @throws IllegalArgumentException 当 {@code formatter} 为 {@code null} 时。
     */
    static <I> SyncTipper<I> format(FormatProvider formatter) {
        return SyncTipper.format(DEFAULT_FORMAT_KEY, formatter);
    }

    /**
     * 参数透传分支。
     *
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link Tip}{@code >}。
     */
    static SyncTipper<Tip> passThrough() {
        return arg -> arg;
    }

    /**
     * 自定义键的历史记录键值对分支。
     *
     * @param <I> 表示输入参数的类型。
     * @param historyKey 表示历史记录所在键的 {@link String}。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link I}{@code >}。
     * @throws IllegalArgumentException 当 {@code historyKey} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    static <I> SyncTipper<I> history(String historyKey) {
        Validation.notBlank(historyKey, "History key cannot be blank.");
        return input -> {
            String memoryStr = AiFlowSession.get()
                    .map(session -> session.<Memory>getInnerState(StateKey.HISTORY))
                    .map(Memory::text)
                    .orElse(StringUtils.EMPTY);
            return Tip.from(historyKey, memoryStr);
        };
    }

    /**
     * 默认键的历史记录键值对分支。
     *
     * @param <I> 输入数据类型。
     * @return 表示一个平行分支的 {@link SyncTipper}{@code <}{@link I}{@code >}。
     */
    static <I> SyncTipper<I> history() {
        return SyncTipper.history(DEFAULT_HISTORY_KEY);
    }
}
