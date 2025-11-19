/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.validation.data;

import jakarta.validation.Valid;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.validation.LocaleContextMessageInterpolator;
import modelengine.fitframework.validation.Validated;
import modelengine.fitframework.validation.ValidationHandler;

/**
 * 用于测试 {@link ValidationHandler} 与 {@link LocaleContextMessageInterpolator} 的集成地区验证控制器。
 *
 * @author 阮睿
 * @since 2025-08-01
 */
@Component
@RequestMapping(path = "/validation/locale", group = "地区验证测试接口")
@Validated
public class LocaleValidationController {
    /**
     * 使用简单参数测试验证消息的地区化。
     *
     * @param company 表示注解验证的测试实体类 {@link Company}。
     */
    @PostMapping(path = "/simple", description = "测试简单参数的地区化验证消息")
    public void validateSimpleParam(@RequestBody @Valid Company company) {}
}