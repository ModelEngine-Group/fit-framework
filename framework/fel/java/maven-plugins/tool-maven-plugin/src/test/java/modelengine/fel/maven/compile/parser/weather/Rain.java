/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.compile.parser.weather;

import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fitframework.annotation.Genericable;
import modelengine.fitframework.annotation.Property;

import java.util.Date;

/**
 * 添加测试用的工具的定义。
 *
 * @author 杭潇
 * @author 曹嘉美
 * @since 2024-10-26
 */
@Group(name = "defGroup_weather_Rain", summary = "雨天", description = "下雨的定义组")
public interface Rain {
    /**
     * 获取今天下雨信息的结果。
     *
     * @param location 获取下雨的地址信息的 {@link String}。
     * @param date 获取下雨日期的 {@link Date}。
     * @return 表示今日下雨信息的结果的 {@link String}。
     */
    @ToolMethod(name = "rain_today", description = "该方法获取今天的下雨信息")
    @Genericable("genericable_weather_rain_today")
    String today(@Property(description = "查询地点", required = true) String location,
            @Property(description = "查询日期", required = true) Date date);

    /**
     * 获取明天下雨信息的结果。
     *
     * @param location 获取下雨的地址信息的 {@link String}。
     * @param date 获取下雨日期的 {@link Date}。
     * @return 表示明天下雨信息的结果的 {@link String}。
     */
    @ToolMethod(name = "rain_tomorrow", description = "该方法获取明天的下雨信息")
    @Genericable("genericable_weather_rain_tomorrow")
    String tomorrow(String location, Date date);
}
