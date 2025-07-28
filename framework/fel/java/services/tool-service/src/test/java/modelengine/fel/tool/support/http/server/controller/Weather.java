/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support.http.server.controller;

import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fitframework.annotation.Property;

/**
 * 测试 Http 提供的天气的相关信息。
 *
 * @since 2024-06-17
 */
public class Weather {
    @Property(description = "表示天气等级", example = "light")
    @RequestHeader("level")
    private Integer level;
    @Property(description = "表示天气情况", example = "raining")
    @RequestHeader("weather")
    private String weather;

    public Integer getLevel() {
        return this.level;
    }

    public void setAge(Integer level) {
        this.level = level;
    }

    public String getWeather() {
        return this.weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
