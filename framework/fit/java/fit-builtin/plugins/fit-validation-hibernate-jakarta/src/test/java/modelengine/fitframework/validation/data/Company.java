/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
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
 * @author 阮睿
 * @since 2025-07-18
 */
public class Company {
    @NotNull
    @Valid
    private List<Employee> employees;

    public Company() {}

    public Company(List<Employee> employees) {
        this.employees = employees;
    }
}