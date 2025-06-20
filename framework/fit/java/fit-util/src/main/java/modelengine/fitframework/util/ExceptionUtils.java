/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.util;

import modelengine.fitframework.exception.MethodInvocationException;

import java.util.HashSet;
import java.util.Set;

/**
 * 为异常提供工具方法。
 *
 * @author 季聿阶
 * @since 2021-03-04
 */
public class ExceptionUtils {
    /** 表示找错误真实原因的最大深度。 */
    private static final int MAX_DEPTH = 10;

    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private ExceptionUtils() {}

    /**
     * 获取方法调用异常的真实原因。
     *
     * @param exception 表示方法调用异常的 {@link MethodInvocationException}。
     * @return 表示方法调用异常的真实原因的 {@link Throwable}。
     */
    public static Throwable getActualCause(MethodInvocationException exception) {
        Set<Throwable> exist = new HashSet<>();
        Throwable origin = exception;
        exist.add(origin);
        int depth = 0;
        while (origin instanceof MethodInvocationException) {
            depth++;
            if (depth >= MAX_DEPTH) {
                throw new IllegalStateException("Too many duplicated throwable.", exception);
            }
            Throwable cause = origin.getCause();
            if (exist.contains(cause)) {
                throw new IllegalStateException("Cyclic throwable cause.", exception);
            }
            exist.add(cause);
            origin = cause;
        }
        return origin;
    }

    /**
     * 获取异常的原因。
     *
     * @param throwable 表示指定异常的 {@link Throwable}。
     * @return 表示异常原因的 {@link String}。
     */
    public static String getReason(Throwable throwable) {
        if (throwable == null) {
            return "No exception";
        }
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }

    /**
     * 获取从异常链中找出第一个非空的异常消息。
     *
     * @param throwable 表示指定异常的 {@link Throwable}。
     * @return 表示异常原因的 {@link String}。
     */
    public static String getActualMessage(Throwable throwable) {
        Set<Throwable> visited = new HashSet<>();
        while (throwable != null && !visited.contains(throwable)) {
            visited.add(throwable);
            String message = throwable.getMessage();
            if (StringUtils.isNotBlank(message)) {
                return message;
            }
            throwable = throwable.getCause();
        }
        return null;
    }
}
