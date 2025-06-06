/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * 为日期提供工具方法。
 *
 * @author 陈镕希
 * @since 2023-08-07
 */
public final class Dates {
    private static final List<String> PATTERNS = Arrays.asList("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSSSSS", "yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private Dates() {
    }

    /**
     * 返回一个字符串，用以描述指定的日期时间。
     *
     * @param value 表示待转为字符串表现形式的日期时间的 {@link LocalDateTime}。
     * @return 表示该日期时间的字符串的 {@link String}。
     */
    public static String toString(LocalDateTime value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return value.format(formatter);
    }

    /**
     * 从字符串中解析日期时间。
     *
     * @param text 表示包含日期时间信息的字符串的 {@link String}。
     * @return 从字符串中解析到的日期时间的 {@link LocalDateTime}。
     */
    public static LocalDateTime parse(String text) {
        for (String pattern : PATTERNS) {
            if (text.length() == pattern.length()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(text, formatter);
            }
        }
        throw new DateTimeParseException("Invalid datetime format.", text, 0);
    }

    /**
     * 当本地时间转为 UTC 时间。
     *
     * @param value 表示本地时间的 {@link LocalDateTime}。
     * @return 表示 UTC 时间的 {@link LocalDateTime}。
     */
    public static LocalDateTime toUtc(LocalDateTime value) {
        ZonedDateTime zoned = value.atZone(ZoneId.systemDefault());
        ZonedDateTime utc = zoned.withZoneSameInstant(ZoneOffset.UTC);
        return utc.toLocalDateTime();
    }

    /**
     * 将 UTC 时间转为本地时间。
     *
     * @param value 表示 UTC 时间的 {@link LocalDateTime}。
     * @return 表示本地时间的 {@link LocalDateTime}。
     */
    public static LocalDateTime fromUtc(LocalDateTime value) {
        ZonedDateTime zoned = value.atZone(ZoneOffset.UTC);
        ZonedDateTime local = zoned.withZoneSameInstant(ZoneId.systemDefault());
        return local.toLocalDateTime();
    }
}
