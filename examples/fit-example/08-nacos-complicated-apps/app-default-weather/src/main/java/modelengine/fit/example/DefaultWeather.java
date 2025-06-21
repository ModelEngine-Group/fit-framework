/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;

/**
 * 表示 {@link Weather} 的默认实现。
 *
 * @author 董智豪
 * @since 2025-06-21
 */
@Component
public class DefaultWeather implements Weather {
    @Override
    @Fitable(id = "default-weather")
    public String get() {
        return "Default weather application is working.";
    }
}
