/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示校验的注解。
 * <p>
 * 用来标识所要校验的类，该类的所有公共方法中，所有被 javax 或 jakarta 校验注解标注的参数都会被校验。
 * </p>
 *
 * @author 邬涨财
 * @author 阮睿
 * @since 2023-03-14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface Validated {
    /**
     * 表示校验的分组。
     *
     * @return 表示校验分组的 {@link Class}{@code <?>[]}。
     */
    Class<?>[] value() default {};
}
