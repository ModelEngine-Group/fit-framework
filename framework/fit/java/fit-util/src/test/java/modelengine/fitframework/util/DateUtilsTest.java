/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import modelengine.fitframework.exception.DateFormatException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.Locale;

/**
 * 为 {@link DateUtils} 提供单元测试。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class DateUtilsTest {
    /** 表示用以测试的年份。 */
    private static final int YEAR = 2020;

    /** 表示用以测试的月份。 */
    private static final int MONTH = 1;

    /** 表示用以测试的日期。 */
    private static final int DAY = 23;

    /** 表示用以测试的小时。 */
    private static final int HOUR = 0;

    /** 表示用以测试的分钟。 */
    private static final int MINUTE = 13;

    /** 表示用以测试的秒。 */
    private static final int SECOND = 48;

    /** 表示用以测试的日期。 */
    private static final Date DATE = DateUtils.create(YEAR, MONTH, DAY, HOUR, MINUTE, SECOND);

    /** 表示用以测试的日期的字符串表现形式。 */
    private static final String DATE_STRING = "2020-01-23 00:13:48.000";

    /** 表示非法的格式化字符串。 */
    private static final String DATE_FORMAT_ILLEGAL = "yMdHyMdhmsS";

    @Test
    public void should_return_correct_date() {
        Date date = DateUtils.create(YEAR, MONTH, DAY);
        Assertions.assertThat(date).hasYear(YEAR).hasMonth(MONTH).hasDayOfMonth(DAY);
    }

    /**
     * 目标方法：{@link DateUtils#toString(Date)}
     * <p>返回按{@link DateUtils#DEFAULT_FORMAT_STRING 默认格式化字符串}对日期进行格式化得到的字符串。</p>
     */
    @Test
    public void should_to_string() {
        String dateString = DateUtils.toString(DATE);
        assertEquals(dateString, DATE_STRING);
    }

    /**
     * 目标方法：{@link DateUtils#toString(Date, String)}
     * <p>当日期为 {@code null} 时，格式化后的字符串也为 {@code null}。</p>
     */
    @Test
    public void should_to_null_string_when_null() {
        String dateString = DateUtils.toString(null, null);
        assertNull(dateString);
    }

    /**
     * 目标方法：{@link DateUtils#parse(String)}
     * <p>返回按{@link DateUtils#DEFAULT_FORMAT_STRING 默认格式化字符串}对包含日期信息的字符串进行解析得到的日期信息。</p>
     */
    @Test
    public void should_return_date() {
        Date date = DateUtils.parse(DATE_STRING);
        assertEquals(date, DATE);
    }

    /**
     * 目标方法：{@link DateUtils#parse(String, String)}
     * <p>当包含日期信息的字符串为 {@code null} 时，解析得到的日期信息也为 {@code null}。</p>
     */
    @Test
    public void should_return_null_when_parse_null() {
        Date date = DateUtils.parse(null, null);
        assertNull(date);
    }

    /**
     * 目标方法：{@link DateUtils#parse(String, String)}
     * <p>当包含日期信息的字符串也格式化字符串不匹配时抛出 {@link DateFormatException} 异常。</p>
     */
    @Test
    public void should_throw_when_format_not_match() {
        assertThrows(DateFormatException.class, () -> DateUtils.parse(DATE_STRING, DATE_FORMAT_ILLEGAL));
    }

    /**
     * 目标方法：{@link DateUtils#tryParse(String)}
     * <p>当包含日期的字符串可以被解析时，解析结果为成功，解析到的数据正确。</p>
     */
    @Test
    public void should_return_success_when_parse_success() {
        ParsingResult<Date> result = DateUtils.tryParse(DATE_STRING);
        assertNotNull(result);
        assertTrue(result.isParsed());
        assertEquals(result.getResult(), DATE);
    }

    /**
     * 目标方法：{@link DateUtils#tryParse(String, String)}
     * <p>当包含日期信息的字符串也格式化字符串不匹配时，解析结果为 {@code false}，解析到的数据为 {@code null}。</p>
     */
    @Test
    public void should_return_failed_when_format_not_match() {
        ParsingResult<Date> result = DateUtils.tryParse(DATE_STRING, DATE_FORMAT_ILLEGAL);
        assertNotNull(result);
        assertFalse(result.isParsed());
        assertNull(result.getResult());
    }

    @Test
    public void should_return_now() {
        Date date = DateUtils.now();
        Assertions.assertThat(date).isBeforeOrEqualTo(new Date());
    }

    @Nested
    @DisplayName("测试方法：toString()")
    class TestToString {
        @Test
        @DisplayName("当提供时间间隔为 null，返回空字符串")
        void givenDurationIsNullThenReturnEmptyString() {
            final String toString = DateUtils.toString(ObjectUtils.<Duration>cast(null));
            assertThat(toString).isEqualTo("");
        }

        @Test
        @DisplayName("当提供 1 秒时间间隔，返回固定的格式字符串")
        void givenDurationWithOneSecondThenReturnFormatString() {
            final String toString = DateUtils.toString(Duration.ofSeconds(1L));
            final String expect = String.format(Locale.ROOT, "%02d:%02d:%02d.%09d", 0, 0, 1, 0);
            assertThat(toString).isEqualTo(expect);
        }

        @Test
        @DisplayName("当提供 1 天时间间隔，返回固定的格式字符串")
        void givenDurationWithOneDayThenReturnFormatString() {
            long days = 1L;
            final String toString = DateUtils.toString(Duration.ofDays(days));
            assertThat(toString).contains(days + " days,");
        }
    }
}
