/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import modelengine.fitframework.exception.DateFormatException;
import modelengine.fitframework.util.support.DefaultParsingResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 为日期提供工具方法。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public final class DateUtils {
    /** 表示默认的格式化字符串。 */
    public static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private DateUtils() {}

    /**
     * 将指定的日期信息转换为字符串表现形式。
     *
     * @param value 表示待转换成字符串表现形式的日期信息的 {@link Date}。
     * @return 表示包含日期信息的字符串的 {@link String}。
     */
    public static String toString(Date value) {
        return toString(value, null);
    }

    /**
     * 将指定的日期信息转换为字符串表现形式。
     *
     * @param value 表示待转换成字符串表现形式的日期信息的 {@link Date}。
     * @param format 表示日期格式的字符串的 {@link String}。
     * @return 表示包含日期信息的字符串的 {@link String}。
     */
    public static String toString(Date value, String format) {
        if (value == null) {
            return null;
        } else {
            return new SimpleDateFormat(ObjectUtils.nullIf(format, DEFAULT_FORMAT_STRING)).format(value);
        }
    }

    /**
     * 从字符串中解析日期信息。
     *
     * @param str 表示包含日期信息的字符串的 {@link String}。
     * @return 表示从字符串中解析到的日期信息的 {@link Date}。
     * @exception DateFormatException 当字符串中未包含有效的日期信息时引发的异常。
     */
    public static Date parse(String str) {
        return parse(str, null);
    }

    /**
     * 从字符串中解析日期信息。
     *
     * @param str 表示包含日期信息的字符串的 {@link String}。
     * @param format 表示日期格式的字符串的 {@link String}。
     * @return 表示从字符串中解析到的日期信息的 {@link Date}。
     * @exception DateFormatException 当字符串中未包含有效的日期信息时引发的异常。
     */
    public static Date parse(String str, String format) {
        if (StringUtils.isBlank(str)) {
            return null;
        } else {
            DateFormat dateFormat = new SimpleDateFormat(ObjectUtils.nullIf(format, DEFAULT_FORMAT_STRING));
            try {
                return dateFormat.parse(str);
            } catch (ParseException ex) {
                throw new DateFormatException(ex);
            }
        }
    }

    /**
     * 尝试从字符串中解析日期信息。
     *
     * @param str 表示包含日期信息的字符串的 {@link String}。
     * @return 表示解析结果的 {@link ParsingResult}。
     */
    public static ParsingResult<Date> tryParse(String str) {
        return tryParse(str, null);
    }

    /**
     * 尝试从字符串中解析日期信息。
     *
     * @param str 表示包含日期信息的字符串的 {@link String}。
     * @param format 表示日期格式的字符串的 {@link String}。
     * @return 表示解析结果的 {@link ParsingResult}。
     */
    public static ParsingResult<Date> tryParse(String str, String format) {
        try {
            Date value = parse(str, format);
            return new DefaultParsingResult<>(true, value);
        } catch (DateFormatException ex) {
            return ParsingResult.failed();
        }
    }

    /**
     * 创建一个日期信息的新实例。
     *
     * @param year 表示年份的32位整数。
     * @param month 表示月份的32位整数。
     * @param day 表示日期的32位整数。
     * @return 表示新创建的日期信息示例的 {@link Date}。
     */
    public static Date create(int year, int month, int day) {
        return create(year, month, day, 0, 0, 0, 0);
    }

    /**
     * 创建一个日期信息的新实例。
     *
     * @param year 表示年份的32位整数。
     * @param month 表示月份的32位整数。
     * @param day 表示日期的32位整数。
     * @param hour 表示小时数的32位整数。
     * @param minute 表示分钟数的32位整数。
     * @param second 表示秒数的32位整数。
     * @return 表示新创建的日期信息示例的 {@link Date}。
     */
    public static Date create(int year, int month, int day, int hour, int minute, int second) {
        return create(year, month, day, hour, minute, second, 0);
    }

    /**
     * 创建一个日期信息的新实例。
     *
     * @param year 表示年份的32位整数。
     * @param month 表示月份的32位整数。
     * @param day 表示日期的32位整数。
     * @param hour 表示小时数的32位整数。
     * @param minute 表示分钟数的32位整数。
     * @param second 表示秒数的32位整数。
     * @param millisecond 表示毫秒数的32位整数。
     * @return 表示新创建的日期信息示例的 {@link Date}。
     */
    public static Date create(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1 + Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
    }

    /**
     * 获取当前系统时间。
     *
     * @return 表示当前系统时间的 {@link Date}。
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 返回一个字符串，用以表示指定的时间长度。
     *
     * @param duration 表示时间长度的 {@link Duration}。
     * @return 若 {@code duration} 为 {@code null}，则返回一个空字符串，否则返回用以表示该时间长度的字符串的 {@link String}。
     */
    public static String toString(Duration duration) {
        if (duration == null) {
            return StringUtils.EMPTY;
        }
        long days = duration.toDays();
        long hours = duration.toHours() % HOURS_PER_DAY;
        long minutes = duration.toMinutes() % MINUTES_PER_HOUR;
        long seconds = duration.getSeconds() % SECONDS_PER_MINUTE;
        long nanos = duration.getNano();
        String result = String.format(Locale.ROOT, "%02d:%02d:%02d.%09d", hours, minutes, seconds, nanos);
        if (days > 0) {
            result = days + " days, " + result;
        }
        return result;
    }
}
