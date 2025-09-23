/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.exception;

/**
 * 当访问字段失败时引发的异常。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class FieldVisitException extends RuntimeException {
    /**
     * 使用引发异常的原因初始化 {@link FieldVisitException} 类的新实例。
     *
     * @param cause 表示引发异常的原因的 {@link Throwable}。
     */
    public FieldVisitException(Throwable cause) {
        super(cause);
    }
}
