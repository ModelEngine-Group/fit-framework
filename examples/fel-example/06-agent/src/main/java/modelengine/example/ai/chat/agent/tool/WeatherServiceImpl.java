/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.example.ai.chat.agent.tool;

import modelengine.fel.tool.annotation.Attribute;
import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Property;

/**
 * 表示 {@link WeatherService} 的默认实现。
 *
 * @author 易文渊
 * @author 杭潇
 * @since 2024-09-02
 */
@Component
@Group(name = "default_weather_service")
public class WeatherServiceImpl implements WeatherService {
    @Override
    @Fitable("default")
    @ToolMethod(name = "get_current_temperature", description = "获取指定城市的当前温度",
            extensions = {
                    @Attribute(key = "tags", value = "FIT"), @Attribute(key = "tags", value = "TEST"),
                    @Attribute(key = "attribute", value = "nothing"),
                    @Attribute(key = "attribute", value = "nothing two")
            })
    @Property(description = "当前温度的结果")
    public String getCurrentTemperature(String location, String unit) {
        return "26";
    }

    @Override
    @Fitable("default")
    @ToolMethod(name = "get_rain_probability", description = "获取指定城市下雨的概率")
    @Property(description = "下雨的概率")
    public String getRainProbability(String location) {
        return "0.06";
    }
}