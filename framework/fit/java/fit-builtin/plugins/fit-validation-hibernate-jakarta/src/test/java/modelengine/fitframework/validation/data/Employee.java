/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 员工实体类。
 *
 * @author 易文渊
 * @author 阮睿
 * @since 2024-09-27
 */
public class Employee {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Min(value = 18, message = "年龄必须大于等于18")
    private int age;

    /**
     * 默认构造函数。
     */
    public Employee() {}

    /**
     * 构造函数。
     *
     * @param name 表示姓名的 {@link String}。
     * @param age 表示年龄的 {@code int}。
     */
    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

    /**
     * 获取员工姓名。
     *
     * @return 表示员工姓名的 {@link String}。
     */
    public String getName() {
        return this.name;
    }
}