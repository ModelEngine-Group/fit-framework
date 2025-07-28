/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.compile.parser.weather.impl;

import modelengine.fel.maven.compile.parser.weather.dto.RainPosition;
import modelengine.fel.tool.annotation.Attribute;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fel.maven.compile.parser.weather.Rain;

import java.util.Date;

/**
 * 添加测试用的工具的实现。
 *
 * @since 2024-10-26
 */
@Group(name = "implGroup_weather_rain_city_b")
public class CityBRainImpl implements Rain {
    private static final String FITABLE_ID = "weather_rain_city_b";

    @Fitable(FITABLE_ID)
    @ToolMethod(name = "city_b_rain_today", description = "城市B提供的今日下雨信息", extensions = {
            @Attribute(key = "tags", value = "FIT"), @Attribute(key = "tags", value = "TEST")
    })
    @Override
    public String today(String location, Date date, RainPosition rainPosition, Object info) {
        return null;
    }

    @Fitable(FITABLE_ID)
    @ToolMethod(name = "city_b_rain_tomorrow", description = "城市B提供的明日下雨信息", extensions = {
            @Attribute(key = "tags", value = "FIT"), @Attribute(key = "tags", value = "TEST")
    })
    @Override
    public String tomorrow(String location, Date date) {
        return null;
    }
}
