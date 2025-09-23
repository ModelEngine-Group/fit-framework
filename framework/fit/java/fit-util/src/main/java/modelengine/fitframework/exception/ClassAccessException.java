/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.exception;

/**
 * 当访问类型发生异常时引发的异常。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class ClassAccessException extends RuntimeException {
    /**
     * 使用引发该异常的原因初始化 {@link ClassAccessException} 类的新实例。
     *
     * @param cause 表示引发异常的原因的 {@link Throwable}。
     */
    public ClassAccessException(Throwable cause) {
        super(cause);
    }
}
