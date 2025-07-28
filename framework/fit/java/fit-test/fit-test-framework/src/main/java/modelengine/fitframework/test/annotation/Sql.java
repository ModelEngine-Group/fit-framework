/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于在测试用例前执行初始化 Sql 语句。
 *
 * @author 易文渊
 * @author 季聿阶
 * @since 2024-07-21
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sql {
    /**
     * 获取前置 SQL 脚本文件路径。
     *
     * @return 表示前置 SQL 脚本文件路径集合的 {@link String}{@code []}。
     */
    String[] before() default {};

    /**
     * 获取后置 SQL 脚本文件路径。
     *
     * @return 表示后置 SQL 脚本文件路径集合的 {@link String}{@code []}。
     */
    String[] after() default {};

    /**
     * 获取 SQL 脚本执行位置。
     */
    enum Position {
        /**
         * 在前置执行。
         */
        BEFORE,

        /**
         * 在后置执行。
         */
        AFTER
    }
}
