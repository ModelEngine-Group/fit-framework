/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.validation.Validated;

import javax.validation.Valid;

/**
 * 表示评估注解验证数据接口集。
 *
 * @author 阮睿
 * @since 2025-07-18
 */
@Component
@Validated
@RequestMapping(path = "/validation", group = "评估注解验证数据接口")
public class ValidationDataController {
    /**
     * Company 类默认分组注解验证。
     *
     * @param company 表示注解验证类 {@link Company}。
     */
    @PostMapping(path = "/company/default", description = "验证 Company 类默认分组注解")
    public void validateCompanyDefaultGroup(@RequestBody @Valid Company company) {}

    /**
     * Company 类特定分组注解验证。
     *
     * @param company 表示注解验证类 {@link Company}。
     */
    @PostMapping(path = "/company/companyGroup", description = "验证 Company 类特定分组注解")
    public void validateCompanyGroup(@RequestBody @Valid Company company) {}
}