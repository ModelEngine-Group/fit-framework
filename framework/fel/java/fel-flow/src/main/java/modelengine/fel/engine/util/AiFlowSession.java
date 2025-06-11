/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.engine.util;

import modelengine.fel.core.pattern.Pattern;
import modelengine.fit.waterflow.domain.context.FlowSession;
import modelengine.fitframework.inspection.Validation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link FlowSession} 线程变量。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-06-18
 */
public class AiFlowSession {
    private static final ThreadLocal<FlowSession> SESSION_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取线程变量 {@link FlowSession}。
     *
     * @return {@link FlowSession} 对象。
     */
    public static Optional<FlowSession> get() {
        return Optional.ofNullable(SESSION_THREAD_LOCAL.get());
    }

    /**
     * 获取线程变量 {@link FlowSession}，如果变量为空，则返回一个新构造的对象。
     *
     * @return {@link FlowSession} 对象。
     */
    public static FlowSession require() {
        return AiFlowSession.get().orElseGet(FlowSession::new);
    }

    /**
     * 在 {@link AiFlowSession} 上下文执行委托单元。
     *
     * @param <I> 输入数据类型。
     * @param <O> 输出数据类型。
     * @param pattern 表示委托单元的 {@link Pattern}{@code <}{@link I}{@code , }{@link O}{@code >}。
     * @param input 表示输入数据的 {@link I}。
     * @param session 表示流程实例运行标识信息的 {@link FlowSession}。
     * @return 表示输出数据的 {@link O}。
     */
    public static <I, O> O applyPattern(Pattern<I, O> pattern, I input, FlowSession session) {
        List<O> output = AiFlowSession.applyBatchPattern(Collections.singletonList(pattern), input, session);
        Validation.notEmpty(output, "The output cannot be empty.");
        return output.get(0);
    }

    /**
     * 在 {@link AiFlowSession} 上下文批量执行委托单元。
     *
     * @param <I> 输入数据类型。
     * @param <O> 输出数据类型。
     * @param patterns 表示委托单元的 {@link List}{@code <}{@link Pattern}{@code <}{@link I}{@code , }{@link O}{@code >>}。
     * @param input 表示输入数据的 {@link I}。
     * @param session 表示流程实例运行标识信息的 {@link FlowSession}。
     * @return 表示输出数据的 {@link O}。
     */
    public static <I, O> List<O> applyBatchPattern(List<Pattern<I, O>> patterns, I input, FlowSession session) {
        SESSION_THREAD_LOCAL.set(session);
        try {
            return patterns.stream().map(pattern -> pattern.invoke(input)).collect(Collectors.toList());
        } finally {
            SESSION_THREAD_LOCAL.remove();
        }
    }
}
