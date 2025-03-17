/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.annotation;

import modelengine.fitframework.validation.Validated;
import modelengine.fitframework.validation.constraints.Constraint;
import modelengine.fitframework.validation.validator.OneValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 表示单集合元素的测试注解类。
 *
 * @author 李金绪
 * @since 2025-03-17
 */
@Retention(RetentionPolicy.RUNTIME)
@Constraint({OneValidator.class})
@Validated
public @interface One {
    String message() default "元素数必须是 1.";

    Class<?>[] groups() default {};
}