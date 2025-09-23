/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import modelengine.fitframework.inspection.Validation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * 为对象类型转换提供工具方法。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public final class Convert {
    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private Convert() {}

    /**
     * 将原始对象按指定的映射方法转换为目标对象。
     *
     * @param value 表示待转换的原始对象。
     * @param mapper 表示用以转换的方法的 {@link Function}。
     * @param <T> 表示原始对象的类型。
     * @param <R> 表示目标对象的类型。
     * @return 若原始对象为 {@code null}，则为 {@code null}；否则为按照转换方法转换成的目标对象。
     */
    public static <T, R> R convert(T value, Function<T, R> mapper) {
        Validation.notNull(mapper, "The mapper to convert object cannot be null.");
        if (value == null) {
            return null;
        } else {
            return mapper.apply(value);
        }
    }

    /**
     * 将原始对象的列表中的每个元素按指定的映射方法进行转换后得到一个新的列表。
     * <p>列表中的每个元素将按照 {@link Convert#convert(Object, Function)} 方法进行转换。</p>
     *
     * @param values 表示待转换的原始对象的列表的 {@link List}。
     * @param mapper 表示用以转换列表中元素的方法的 {@link Function}。
     * @param <T> 表示原始对象的类型。
     * @param <R> 表示目标对象的类型。
     * @return 若原始对象列表为 {@code null}，则为 {@code null}；否则为包含转换后对象的列表的 {@link List}。
     */
    public static <T, R> List<R> convert(List<T> values, Function<T, R> mapper) {
        return convert(values, mapper, null);
    }

    /**
     * 将原始对象的列表中的每个元素按指定的映射方法进行转换后得到一个新的列表。
     * <p>将使用 {@code listFactory} 参数创建具备指定目标容量的列表。若未提供，则将创建 {@link ArrayList}。</p>
     * <p>列表中的每个元素将按照 {@link Convert#convert(Object, Function)} 方法进行转换。</p>
     *
     * @param values 表示待转换的原始对象的列表的 {@link List}。
     * @param mapper 表示用以转换列表中元素的方法的 {@link Function}。
     * @param listFactory 表示用以创建目标列表的工厂的 {@link IntFunction}{@code <}{@link List}{@code >}。
     * @param <T> 表示原始对象的类型。
     * @param <R> 表示目标对象的类型。
     * @return 若原始对象列表为 {@code null}，则为 {@code null}；否则为包含转换后对象的列表的 {@link List}。
     */
    public static <T, R> List<R> convert(List<T> values, Function<T, R> mapper, IntFunction<List<R>> listFactory) {
        Validation.notNull(mapper, "The mapper to convert object cannot be null.");
        List<R> results;
        if (values == null) {
            results = null;
        } else {
            results = ObjectUtils.nullIf(listFactory, ArrayList::new).apply(values.size());
            for (T value : values) {
                results.add(convert(value, mapper));
            }
        }
        return results;
    }

    /**
     * 将指定的单个字节转换成字节数组。
     *
     * @param value 表示待转换的指定字节的 {@code byte}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(byte value) {
        return new byte[] {value};
    }

    /**
     * 将指定的字节数组转换成单个字节。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的单个字节的 {@code byte}。
     */
    public static byte toByte(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert byte cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 1, "Length of bytes to convert byte cannot be greater than 1.");
        return bytes.length > 0 ? bytes[0] : 0;
    }

    /**
     * 将指定的短整型转换成字节数组。
     *
     * @param value 表示待转换的短整型的 {@code short}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(short value) {
        return new byte[] {
                byteValue(value >>> 8), byteValue(value)
        };
    }

    /**
     * 将指定的字节数组转换成短整型。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的短整型的 {@code short}。
     */
    public static short toShort(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert short cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 2, "Length of bytes to convert short cannot be greater than 2.");
        int value = toInteger(bytes);
        return (short) value;
    }

    /**
     * 将指定的整型转换成字节数组。
     *
     * @param value 表示待转换的整型的 {@code int}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(int value) {
        return new byte[] {
                byteValue(value >>> 24), byteValue(value >>> 16), byteValue(value >>> 8), byteValue(value)
        };
    }

    /**
     * 将指定的字节数组转换成整型。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的整型的 {@code int}。
     */
    public static int toInteger(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert integer cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 4, "Length of bytes to convert integer cannot be greater than 4.");
        int value = 0;
        for (byte aByte : bytes) {
            value = (value << 8) | Byte.toUnsignedInt(aByte);
        }
        return value;
    }

    /**
     * 将指定的长整型转换成字节数组。
     *
     * @param value 表示待转换的长整型的 {@code long}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(long value) {
        return new byte[] {
                byteValue(value >>> 56), byteValue(value >>> 48), byteValue(value >>> 40), byteValue(value >>> 32),
                byteValue(value >>> 24), byteValue(value >>> 16), byteValue(value >>> 8), byteValue(value)
        };
    }

    /**
     * 将指定的字节数组转换成长整型。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的长整型的 {@code long}。
     */
    public static long toLong(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert long cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 8, "Length of bytes to convert long cannot be greater than 8.");
        long value = 0L;
        for (byte aByte : bytes) {
            value = (value << 8) | Byte.toUnsignedLong(aByte);
        }
        return value;
    }

    /**
     * 将指定的浮点型转换成字节数组。
     *
     * @param value 表示待转换的浮点数的 {@code float}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(float value) {
        return toBytes(Float.floatToIntBits(value));
    }

    /**
     * 将指定的字节数组转换成浮点数。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的浮点数的 {@code float}。
     */
    public static float toFloat(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert float cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 4, "Length of bytes to convert float cannot be greater than 4.");
        return Float.intBitsToFloat(toInteger(bytes));
    }

    /**
     * 将指定的浮点型转换成字节数组。
     *
     * @param value 表示待转换的浮点数的 {@code double}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(double value) {
        return toBytes(Double.doubleToLongBits(value));
    }

    /**
     * 将指定的字节数组转换成浮点数。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的浮点数的 {@code double}。
     */
    public static double toDouble(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert double cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 8, "Length of bytes to convert double cannot be greater than 8.");
        return Double.longBitsToDouble(toLong(bytes));
    }

    /**
     * 将指定的字符转换成字节数组。
     *
     * @param value 表示待转换的字符的 {@code char}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(char value) {
        return toBytes((short) value);
    }

    /**
     * 将指定的字节数组转换成字符。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的字符的 {@code char}。
     */
    public static char toCharacter(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert character cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 2, "Length of bytes to convert character cannot be greater than 2.");
        return (char) toInteger(bytes);
    }

    /**
     * 将指定的布尔值转换成字节数组。
     *
     * @param value 表示待转换的布尔值的 {@code boolean}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(boolean value) {
        return toBytes((byte) (value ? 1 : 0));
    }

    /**
     * 将指定的字节数组转换成布尔值。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的布尔值的 {@code boolean}。
     */
    public static boolean toBoolean(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert boolean cannot be null.");
        Validation.lessThanOrEquals(bytes.length, 1, "Length of bytes to convert boolean cannot be greater than 1.");
        return toByte(bytes) != 0;
    }

    /**
     * 将指定的字符串转换成字节数组。
     *
     * @param value 表示待转换的字符串的 {@link String}。
     * @return 表示转换后的字节数组的 {@code byte[]}。
     */
    public static byte[] toBytes(String value) {
        return ObjectUtils.nullIf(value, StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将指定的字节数组转换成字符串。
     *
     * @param bytes 表示待转换的字节数组的 {@code byte[]}。
     * @return 表示转换后的字符串的 {@link String}。
     */
    public static String toString(byte[] bytes) {
        Validation.notNull(bytes, "The bytes to convert String cannot be null.");
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 将指定的整型转换成字节。
     *
     * @param value 表示待转换的整型的 {@code int}。
     * @return 表示转换后的字节的 {@code byte}。
     */
    private static byte byteValue(int value) {
        return (byte) (value & 0xFF);
    }

    /**
     * 将指定的长整型转换成字节。
     *
     * @param value 表示待转换的长整型的 {@code long}。
     * @return 表示转换后的字节的 {@code byte}。
     */
    private static byte byteValue(long value) {
        return (byte) (value & 0xFF);
    }
}
