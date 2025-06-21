/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.controller;

import modelengine.fit.example.Weather;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;

/**
 * 表示助手的控制器。
 *
 * @author 董智豪
 * @since 2025-06-21
 */
@Component
public class AssistantController {
    private final Weather weather;

    public AssistantController(@Fit Weather weather) {
        this.weather = weather;
    }

    /**
     * 获取天气信息。
     *
     * @return 表示天气信息的 {@link String}。
     */
    @GetMapping(path = "/weather")
    public String getWeather() {
        return this.weather.get();
    }
}
