/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.validation.domain;

import modelengine.fitframework.inspection.Validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 校验类型为参数的元数据类。
 *
 * @author 邬涨财
 * @since 2023-05-19
 */
public class ValidationParameter extends AbstractValidationMetadata {
    private final Parameter parameter;

    /**
     * 使用指定的参数、分组、值和验证方法初始化 {@link ValidationParameter} 的新实例。
     *
     * @param parameter 表示参数的 {@link Parameter}。
     * @param groups 表示分组的 {@link Class}{@code <?>[]}。
     * @param value 表示值的 {@link Object}。
     * @param validationMethod 表示校验方法的 {@link Method}。
     * @throws IllegalArgumentException 当 {@code parameter}、{@code groups} 或 {@code validationMethod} 为 {@code null} 时。
     */
    public ValidationParameter(Parameter parameter, Class<?>[] groups, Object value, Method validationMethod) {
        super(groups, value, validationMethod);
        this.parameter =
                Validation.notNull(parameter, "The parameter cannot be null when construct validation parameter.");
    }

    @Override
    public AnnotatedElement element() {
        return this.parameter;
    }

    @Override
    public String name() {
        return this.parameter.getName();
    }

    @Override
    public Annotation[] annotations() {
        return this.parameter.getAnnotations();
    }
}
