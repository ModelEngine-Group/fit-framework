/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.annotation;

import modelengine.fitframework.util.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于工具方法的定义。
 *
 * @since 2024-06-13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ToolMethod {
    /**
     * 获取工具方法的命名空间，工具名在命名空间内需要保证唯一。
     *
     * @return 表示工具方法命名空间的 {@link String}。
     */
    String namespace() default "Common";

    /**
     * 获取工具方法的名。
     *
     * @return 表示工具方法名的 {@link String}。
     */
    String name();

    /**
     * 获取工具方法的描述信息。
     *
     * @return 表示工具方法的描述信息的 {@link String}。
     */
    String description() default StringUtils.EMPTY;

    /**
     * 获取工具方法的额外参数名。
     *
     * @return 表示工具方法额外参数名的 {@code String[]}。
     */
    String[] extraParams() default {};

    /**
     * 获取输出转换器的工具方法。
     *
     * @return 表示输出转换器工具方法的 {@link String}。
     */
    String returnConverter() default StringUtils.EMPTY;

    /**
     * 获取工具方法的扩展属性。
     *
     * @return 表示工具方法扩展属性的 {@link Attribute}{@code []}。
     */
    Attribute[] extensions() default {};
}
