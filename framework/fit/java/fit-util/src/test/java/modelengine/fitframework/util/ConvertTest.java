/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

/**
 * 为 {@link Convert} 提供单元测试。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class ConvertTest {
    /** 表示待转换的值。 */
    private static final Integer VALUE = 255;

    /** 表示待转换的值的列表。 */
    private static final List<Integer> VALUES =
            ObjectUtils.mapIfNotNull(new Integer[] {VALUE}, java.util.Arrays::asList);

    /** 表示单个值的转换结果。 */
    private static final String CONVERTED_VALUE = "ff";

    /** 表示整个列表的转换结果。 */
    private static final List<String> CONVERTED_VALUES =
            ObjectUtils.mapIfNotNull(new String[] {CONVERTED_VALUE}, java.util.Arrays::asList);

    /** 表示转换方法。 */
    private static final Function<Integer, String> MAPPER = Integer::toHexString;

    /**
     * 目标方法：{@link Convert#convert(Object, Function)}
     * <p>当转换方法为 {@code null} 时抛出异常。</p>
     */
    @Test
    void should_throws_when_mapper_is_null() {
        assertThrows(IllegalArgumentException.class, () -> Convert.convert(VALUE, null));
    }

    /**
     * 目标方法：{@link Convert#convert(Object, Function)}
     * <p>当转换为 {@code null} 的对象时，转换结果也为 {@code null}。</p>
     */
    @Test
    void should_return_null_when_convert_null() {
        String result = Convert.convert(ObjectUtils.<Integer>cast(null), MAPPER);
        assertNull(result);
    }

    /**
     * 目标方法：{@link Convert#convert(Object, Function)}
     * <p>当转换非 {@code null} 的对象时，按转换方法进行转换。</p>
     */
    @Test
    void should_return_converted_when_convert_non_null() {
        String result = Convert.convert(VALUE, MAPPER);
        assertEquals(result, CONVERTED_VALUE);
    }

    /**
     * 目标方法：{@link Convert#convert(List, Function)}
     * <p>当转换为 {@code null} 的列表时，转换结果也为 {@code null}。</p>
     */
    @Test
    void should_return_null_list_when_convert_null_list() {
        List<String> results = Convert.convert((List<Integer>) null, MAPPER);
        assertNull(results);
    }

    /**
     * 目标方法：{@link Convert#convert(List, Function)}
     * <p>当转换非 {@code null} 的列表时，按转换方法转换每个元素。</p>
     */
    @Test
    void should_return_converted_list_when_convert_non_null() {
        List<String> results = Convert.convert(VALUES, MAPPER);
        assertNotNull(results);
        assertIterableEquals(results, CONVERTED_VALUES);
    }

    @Test
    void should_return_0() {
        byte data = Convert.toByte(new byte[0]);
        assertThat(data).isEqualTo((byte) 0);
    }

    @Test
    void should_compute_original_byte_value() {
        final byte value = (byte) 0xff;
        byte[] data = Convert.toBytes(value);
        byte result = Convert.toByte(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_short_value() {
        final short value = (short) 0xf081;
        byte[] data = Convert.toBytes(value);
        short result = Convert.toShort(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_integer_value() {
        final int value = 0x80818283;
        byte[] data = Convert.toBytes(value);
        int result = Convert.toInteger(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_long_value() {
        final long value = 0x8081828384858687L;
        byte[] data = Convert.toBytes(value);
        long result = Convert.toLong(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_float_value() {
        final float value = Float.NaN;
        byte[] data = Convert.toBytes(value);
        float result = Convert.toFloat(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_double_value() {
        final double value = Double.MIN_NORMAL;
        byte[] data = Convert.toBytes(value);
        double result = Convert.toDouble(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_character_value() {
        final char value = Character.MAX_HIGH_SURROGATE;
        byte[] data = Convert.toBytes(value);
        char result = Convert.toCharacter(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_boolean_value() {
        final boolean value = true;
        byte[] data = Convert.toBytes(value);
        boolean result = Convert.toBoolean(data);
        assertEquals(value, result);
    }

    @Test
    void should_compute_original_string_value() {
        final String value = "Hello";
        byte[] data = Convert.toBytes(value);
        String result = Convert.toString(data);
        assertThat(result).isEqualTo("Hello");
    }
}
