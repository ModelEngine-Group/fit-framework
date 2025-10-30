/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.annotation;

import modelengine.fitframework.util.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示参数的详细约束信息。
 *
 * @author 季聿阶
 * @since 2023-08-27
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface Property {
    /**
     * 表示未设置默认值的标记常量。
     */
    String UNSET_DEFAULT_VALUE = "$$Fit$UnsetDefaultValue$$";

    /**
     * 获取参数的名字。
     *
     * @return 表示参数的名字的 {@link String}。
     */
    String name() default StringUtils.EMPTY;

    /**
     * 获取参数的描述信息。
     *
     * @return 表示参数的描述信息的 {@link String}。
     */
    String description() default StringUtils.EMPTY;

    /**
     * 获取参数是否必须的标志。
     *
     * @return 表示参数是否必须的标志的 {@code boolean}。
     */
    boolean required() default false;

    /**
     * 获取参数的默认值。
     *
     * @return 表示参数的默认值的 {@link String}。
     */
    String defaultValue() default UNSET_DEFAULT_VALUE;

    /**
     * 获取参数的样例值。
     *
     * @return 表示参数的样例值的 {@link String}。
     */
    String example() default StringUtils.EMPTY;
}
