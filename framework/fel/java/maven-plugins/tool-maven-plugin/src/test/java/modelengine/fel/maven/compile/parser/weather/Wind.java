/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.compile.parser.weather;

import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fitframework.annotation.Genericable;

/**
 * 添加测试用的工具的定义。
 *
 * @since 2024-10-24
 */
@Group(name = "defGroup_weather_Wind")
public interface Wind {
    /**
     * 获取今日的风量数据。
     *
     * @param location 表示获取风量数据地址的 {@link String}。
     * @return 表示今日风量数据的 {@link String}。
     */
    @ToolMethod(name = "wind_today", description = "获取今日的风量数据")
    @Genericable("weather_rain_today")
    String today(String location);

    /**
     * 获取明日的风量数据。
     *
     * @param location 表示获取风量数据地址的 {@link String}。
     * @return 表示明日风量数据的 {@link String}。
     */
    @ToolMethod(name = "wind_tomorrow", description = "获取明日的风量数据")
    @Genericable("weather_rain_tomorrow")
    String tomorrow(String location);
}
