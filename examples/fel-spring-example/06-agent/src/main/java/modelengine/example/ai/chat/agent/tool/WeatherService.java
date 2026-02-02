/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.example.ai.chat.agent.tool;

import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fitframework.annotation.Genericable;
import modelengine.fitframework.annotation.Property;

/**
 * 表示天气服务的接口定义。
 *
 * @author 易文渊
 * @author 杭潇
 * @since 2024-09-02
 */
@Group(name = "weather_service")
public interface WeatherService {
    /**
     * 获取指定地点的当前温度。
     *
     * @param location 表示地点名称的 {@link String}。
     * @param unit 表示温度单位的 {@link String}。
     * @return 表示当前温度的 {@link String}。
     */
    @ToolMethod(namespace = "example", name = "current_temperature", description = "获取指定地点的当前温度")
    @Genericable("modelengine.example.weather.temperature")
    String getCurrentTemperature(@Property(description = "城市名称", required = true) String location,
            @Property(description = "使用的温度单位，可选：Celsius，Fahrenheit", defaultValue = "Celsius") String unit);

    /**
     * 获取指定地点的降雨概率。
     *
     * @param location 表示地点名称的 {@link String}。
     * @return 表示降雨概率的 {@link String}。
     */
    @ToolMethod(namespace = "example", name = "rain_probability", description = "获取指定地点的降雨概率")
    @Genericable("modelengine.example.weather.rain")
    String getRainProbability(@Property(description = "城市名称", required = true) String location);
}