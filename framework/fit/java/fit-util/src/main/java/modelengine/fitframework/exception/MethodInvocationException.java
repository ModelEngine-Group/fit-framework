/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.exception;

/**
 * 当调用方法失败时引发的异常。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class MethodInvocationException extends RuntimeException {
    /**
     * 使用异常原因初始化 {@link MethodInvocationException} 类的新实例。
     *
     * @param message 表示异常原因的 {@link String}。
     */
    public MethodInvocationException(String message) {
        super(message);
    }

    /**
     * 使用引发异常的原因初始化 {@link MethodInvocationException} 类的新实例。
     *
     * @param cause 表示引发异常的原因的 {@link Throwable}。
     */
    public MethodInvocationException(Throwable cause) {
        super(cause);
    }
}
