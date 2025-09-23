/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.exception;

/**
 * 当没有指定的方法时引发的异常。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class MethodNotFoundException extends RuntimeException {
    /**
     * 使用引发异常的原因初始化 {@link MethodNotFoundException} 类的新实例。
     *
     * @param cause 表示引发异常的原因的 {@link Throwable}。
     */
    public MethodNotFoundException(Throwable cause) {
        super(cause);
    }
}
