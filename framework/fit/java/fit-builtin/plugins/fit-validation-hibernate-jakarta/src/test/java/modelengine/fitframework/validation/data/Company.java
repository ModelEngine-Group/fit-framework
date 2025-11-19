/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 公司实体类。
 *
 * @author 易文渊
 * @author 阮睿
 * @since 2024-09-27
 */
public class Company {
    @NotNull
    @Valid
    private List<Employee> employees;

    /**
     * 默认构造函数。
     */
    public Company() {}

    /**
     * 构造函数。
     *
     * @param employees 表示雇员列表的 {@link List}{@code <}{@link Employee}{@code >}。
     */
    public Company(List<Employee> employees) {
        this.employees = employees;
    }
}