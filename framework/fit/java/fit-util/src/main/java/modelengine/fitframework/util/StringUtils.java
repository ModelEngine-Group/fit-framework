/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import static modelengine.fitframework.util.ObjectUtils.getIfNull;
import static modelengine.fitframework.util.ObjectUtils.nullIf;

import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.io.ByteReader;
import modelengine.fitframework.parameterization.ParameterizationMode;
import modelengine.fitframework.parameterization.ParameterizedString;
import modelengine.fitframework.parameterization.ParameterizedStringResolver;
import modelengine.fitframework.util.support.ArrayIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 为字符串提供工具方法。
 *
 * @author 梁济时
 * @author 季聿阶
 * @since 2020-07-24
 */
public final class StringUtils {
    /** 表示空字符串。 */
    public static final String EMPTY = "";

    /** 表示空的字符串数组。 */
    public static final String[] EMPTY_ARRAY = new String[0];

    private static final ParameterizedStringResolver FORMATTER =
            ParameterizedStringResolver.create("{", "}", '/', ParameterizationMode.STRICT);
    private static final ParameterizedStringResolver NON_STRICT_FORMATTER =
            ParameterizedStringResolver.create("{", "}", '/', false);

    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private StringUtils() {}

    /**
     * 当 {@code value} 为 {@code null} 或者是空白字符串时，使用 {@code defaultValue}，否则继续使用 {@code value}。
     *
     * @param value 表示指定字符串的 {@link String}。
     * @param defaultValue 表示当指定字符串为 {@code null} 时使用的默认字符串的 {@link String}。
     * @return 若 {@code value} 为 {@code null} 或者是空白字符串时，则为 {@code defaultValue}，否则为 {@code value}。
     */
    public static String blankIf(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    /**
     * 当 {@code value} 为 {@code null} 或者是空白字符串时，使用 {@code defaultValue}，否则继续使用 {@code value}。
     *
     * @param value 表示指定字符串的 {@link String}。
     * @param factory 表示当指定字符串为 {@code null} 时指定 {@link Supplier}{@code <}{@link String}{@code >}。
     * @return 若 {@code value} 为 {@code null}，则为 {@link Supplier#get()}；否则为 {@code value}。
     * @throws IllegalArgumentException 当 {@code factory} 为 {@code null} 时。
     */
    public static String getIfBlank(String value, Supplier<String> factory) {
        Validation.notNull(factory, "The factory to create default value cannot be null.");
        return isBlank(value) ? factory.get() : value;
    }

    /**
     * 使用 {@link String#compareTo(String) 默认的比较方式} 比较两个字符串。
     *
     * @param str1 表示待比较的第一个字符串的 {@link String}。
     * @param str2 表示待比较的第二个字符串的 {@link String}。
     * @return 若第一个字符串大于第二个字符串，则为一个正数；若第一个字符串小于第二个字符串，则为一个负数；否则为 {@code 0}。
     * @see ObjectUtils#compare(Comparable, Comparable)
     */
    public static int compare(String str1, String str2) {
        return ObjectUtils.compare(str1, str2);
    }

    /**
     * 使用 {@link String#compareToIgnoreCase(String) 忽略大小写的比较方式} 比较两个字符串。
     *
     * @param str1 表示待比较的第一个字符串的 {@link String}。
     * @param str2 表示待比较的第二个字符串的 {@link String}。
     * @return 若第一个字符串大于第二个字符串，则为一个正数；若第一个字符串小于第二个字符串，则为一个负数；否则为 {@code 0}。
     * @see ObjectUtils#compare(Object, Object, java.util.Comparator)
     * @see String#compareToIgnoreCase(String)
     */
    public static int compareIgnoreCase(String str1, String str2) {
        return ObjectUtils.compare(str1, str2, String::compareToIgnoreCase);
    }

    /**
     * 将指定迭代器中的元素按照指定方法转换成字符串后进行拼接并得到新的字符串。
     *
     * @param mapper 表示用以将对象转换成字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param iterator 表示包含待拼接元素的迭代器的 {@link Iterator}{@code <}{@link T}{@code >}。
     * @param <T> 表示迭代器中元素的类型的 {@link T}。
     * @return 表示拼接得到的新的字符串的 {@link String}。
     * @see #join(String, Function, Iterator)
     */
    public static <T> String concat(Function<T, String> mapper, Iterator<T> iterator) {
        return join(EMPTY, mapper, iterator);
    }

    /**
     * 将指定列表中的元素按照指定方法转换成字符串后进行拼接并得到新的字符串。
     *
     * @param mapper 表示用以将对象转换成字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param list 表示待拼接元素的列表的 {@link List}{@code <}{@link T}{@code >}。
     * @param <T> 表示列表中元素的类型的 {@link T}。
     * @return 表示拼接得到的新的字符串的 {@link String}。
     */
    public static <T> String concat(Function<T, String> mapper, List<T> list) {
        return concat(mapper, CollectionUtils.iterator(list));
    }

    /**
     * 将指定数组中的元素按照指定方法转换成字符串后进行拼接并得到新的字符串。
     *
     * @param mapper 表示用以将对象转换成字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param array 表示待拼接元素的数组的 {@link T}{@code []}。
     * @param <T> 表示数组中元素的类型的 {@link T}。
     * @return 表示拼接得到的新的字符串的 {@link String}。
     */
    @SafeVarargs
    public static <T> String concat(Function<T, String> mapper, T... array) {
        return concat(mapper, new ArrayIterator<>(array));
    }

    /**
     * 将指定迭代器中的元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串后进行拼接并得到新的字符串。
     *
     * @param iterator 表示包含待拼接元素的迭代器的 {@link Iterator}。
     * @param <T> 表示迭代器中元素的类型的 {@link T}。
     * @return 表示拼接得到的新的字符串的 {@link String}。
     */
    public static <T> String concat(Iterator<T> iterator) {
        return concat(null, iterator);
    }

    /**
     * 将指定列表中的元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串后进行拼接并得到新的字符串。
     *
     * @param list 表示待拼接元素的列表的 {@link List}{@code <}{@link T}{@code >}。
     * @param <T> 表示列表中元素的类型的 {@link T}。
     * @return 表示拼接得到的新的字符串的 {@link String}。
     */
    public static <T> String concat(List<T> list) {
        return concat(null, CollectionUtils.iterator(list));
    }

    /**
     * 将指定数组中的元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串后进行拼接并得到新的字符串。
     *
     * @param array 表示待拼接元素的数组的 {@link T}{@code []}。
     * @param <T> 表示数组中元素的类型的 {@link T}。
     * @return 表示拼接得到的新的字符串的 {@link String}。
     */
    @SafeVarargs
    public static <T> String concat(T... array) {
        return concat(null, new ArrayIterator<>(array));
    }

    /**
     * 检查指定字符串在忽略大小写的情况下是否以指定后缀结尾。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param suffix 表示待检查的后缀的 {@link String}。
     * @return 若以指定后缀（忽略大小写）结尾，则为 {@code true}；否则为 {@code false}。
     */
    public static boolean endsWithIgnoreCase(String source, String suffix) {
        if (source == null) {
            return suffix == null;
        } else if (suffix == null || source.length() < suffix.length()) {
            return false;
        } else {
            return source.regionMatches(true, source.length() - suffix.length(), suffix, 0, suffix.length());
        }
    }

    /**
     * 返回一个比较器，用以比较两个字符串是否包含相同的数据。
     *
     * @param ignoreCase 若为 {@code true}，则返回一个忽略大小写的比较器；否则返回常规比较器。
     * @return 表示用以比较两个字符串的比较器的 {@link Equalizer}{@code <}{@link String}{@code >}。
     * @see StringUtils#equals(String, String)
     * @see StringUtils#endsWithIgnoreCase(String, String)
     */
    public static Equalizer<String> equalizer(boolean ignoreCase) {
        return ignoreCase ? StringUtils::equalsIgnoreCase : StringUtils::equals;
    }

    /**
     * 检查两个字符串内容是否完全一致。
     *
     * @param str1 表示待检查的第一个字符串的 {@link String}。
     * @param str2 表示待检查的第二个字符串的 {@link String}。
     * @return 若两个字符串完全一致，则为 {@code true}；否则为 {@code false}。
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 检查两个字符串是否除了大小写以外完全一致。
     *
     * @param str1 表示待检查的第一个字符串的 {@link String}。
     * @param str2 表示待检查的第二个字符串的 {@link String}。
     * @return 若两个字符串除了大小写以外完全一致，则为 {@code true}；否则为 {@code false}。
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p>采用严格校验模式。</p>
     *
     * @param format 表示格式化字符串的 {@link String}。
     * @param args 表示用以格式化字符串的参数的 {@link Object}{@code []}。如果参数中存在 {@code null}，其对应的格式化后会变成空字符串。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     * @see #format(String, boolean, Map)
     */
    public static String format(String format, Object... args) {
        Object[] actual = getIfNull(args, () -> new Object[0]);
        Map<String, Object> params = new HashMap<>(actual.length);
        for (int i = 0; i < actual.length; i++) {
            params.put(Integer.toString(i), actual[i]);
        }
        return format(format, params);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p>采用严格校验模式。</p>
     *
     * @param format 表示格式化字符串的 {@link String}。
     * @param args 表示用以格式化字符串的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。如果参数中存在
     * {@code null}，其对应的格式化后会变成空字符串。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     * @see #format(String, boolean, Map)
     */
    public static String format(String format, Map<String, Object> args) {
        return format(format, true, args);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p><b>注意：{@code format} 中如果含有如下特殊字符（{@code '\u007b'}，{@code '\u007d'}，{@code '/'}），需要在该字符前增加转义字符
     * {@code '/'} 进行转义。</b></p>
     * <p>严格校验模式：格式化字符串中的占位符需要和传入的变量一一对应。</p>
     *
     * @param format 表示格式化字符串的 {@link String}。
     * @param isStrict 表示是否采用严格校验模式的 {@code boolean}。
     * @param args 表示用以格式化字符串的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。如果参数中存在
     * {@code null}，其对应的格式化后会变成空字符串。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     */
    public static String format(String format, boolean isStrict, Map<String, Object> args) {
        if (isBlank(format)) {
            return format;
        }
        Map<String, Object> params = nullIf(args, Collections.emptyMap());
        ParameterizedStringResolver resolver = isStrict ? FORMATTER : NON_STRICT_FORMATTER;
        ParameterizedString parameterizedString = resolver.resolve(format);
        return parameterizedString.format(params);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     *
     * @param format 表示格式化字符串的 {@link String}。
     * @param mode 表示参数化字符串的格式化模式的 {@link ParameterizationMode}。
     * @param args 表示用以格式化字符串的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。如果参数中存在
     * {@code null}，其对应的格式化后会变成空字符串。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     * @see #format(String, ParameterizationMode, Map, String)
     */
    public static String format(String format, ParameterizationMode mode, Map<String, Object> args) {
        return format(format, mode, args, null);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p><b>注意：{@code format} 中如果含有如下特殊字符（{@code '\u007b'}，{@code '\u007d'}，{@code '/'}），需要在该字符前增加转义字符
     * {@code '/'} 进行转义。</b></p>
     *
     * @param format 表示格式化字符串的 {@link String}。
     * @param mode 表示参数化字符串的格式化模式的 {@link ParameterizationMode}。
     * @param args 表示用以格式化字符串的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @param defaultValue 占位符匹配模式为 {@link ParameterizationMode#LENIENT_DEFAULT} 时，如果参数中不存在对应的参数，则返回该值。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     */
    public static String format(String format, ParameterizationMode mode, Map<String, Object> args,
            String defaultValue) {
        if (isBlank(format)) {
            return format;
        }
        Map<String, Object> params = nullIf(args, Collections.emptyMap());
        ParameterizedStringResolver resolver = ParameterizedStringResolver.create("{", "}", '/', mode);
        ParameterizedString parameterizedString = resolver.resolve(format);
        return parameterizedString.format(params, defaultValue);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p>采用宽松校验模式 {@link ParameterizationMode#LENIENT_EMPTY}，同时不存在参数时使用 {@link StringUtils#EMPTY} 代替。</p>
     *
     * @param format 表示待格式化的字符串的 {@link String}。
     * @param args 表示待格式化的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     * @see #format(String, ParameterizationMode, Map, String)
     */
    public static String formatLenient(String format, Map<String, Object> args) {
        return format(format, ParameterizationMode.LENIENT_EMPTY, args);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p>采用宽松校验模式 {@link ParameterizationMode#LENIENT_DEFAULT}，同时不存在参数时使用 {@code defaultValue} 代替。</p>
     *
     * @param format 表示待格式化的字符串的 {@link String}。
     * @param args 表示待格式化的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @param defaultValue 表示参数不存在时的默认值的 {@link String}。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     * @see #format(String, ParameterizationMode, Map, String)
     */
    public static String formatLenientWithDefault(String format, Map<String, Object> args, String defaultValue) {
        return format(format, ParameterizationMode.LENIENT_DEFAULT, args, defaultValue);
    }

    /**
     * 使用指定的格式化字符串对参数进行格式化，并返回格式化后的字符串。
     * <p>采用宽松校验模式 {@link ParameterizationMode#LENIENT_KEEP_PLACEHOLDER}，同时不存在参数时使用占位符代替。</p>
     *
     * @param format 表示待格式化的字符串的 {@link String}。
     * @param args 表示待格式化的参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @return 表示格式化得到的字符串的 {@link String}。
     * @throws modelengine.fitframework.parameterization.StringFormatException 当所提供的格式化字符串与格式化参数不匹配时。
     * @see #format(String, ParameterizationMode, Map, String)
     */
    public static String formatKeepPlaceholder(String format, Map<String, Object> args) {
        return format(format, ParameterizationMode.LENIENT_KEEP_PLACEHOLDER, args);
    }

    /**
     * 检查指定字符串是否为 {@code null}、空字符串或只有空白字符的字符串。
     *
     * @param source 表示待检查的字符串的 {@link String}。
     * @return 若字符串为 {@code null}、空字符串或只有空白字符的字符串，则为 {@code true}；否则为 {@code false}。
     * @see #isNotBlank(String)
     */
    public static boolean isBlank(String source) {
        if (isEmpty(source)) {
            return true;
        }
        for (int i = 0; i < source.length(); i++) {
            if (!Character.isWhitespace(source.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查指定字符串是否为 {@code null} 或空字符串。
     *
     * @param source 表示待检查的字符串的 {@link String}。
     * @return 若字符串为 {@code null} 或空字符串，则为 {@code true}；否则为 {@code false}。
     * @see #isNotEmpty(String)
     */
    public static boolean isEmpty(String source) {
        return source == null || source.isEmpty();
    }

    /**
     * 检查指定字符串是否不为 {@code null}、不为空字符串并且不为只有空白字符的字符串。
     *
     * @param source 表示待检查的字符串的 {@link String}。
     * @return 若字符串为 {@code null}、空字符串或只有空白字符的字符串，则为 {@code false}；否则为 {@code true}。
     * @see #isBlank(String)
     */
    public static boolean isNotBlank(String source) {
        return !isBlank(source);
    }

    /**
     * 检查指定字符串是否不为 {@code null} 并且不为空字符串。
     *
     * @param source 表示待检查的字符串的 {@link String}。
     * @return 若字符串为 {@code null} 或空字符串，则为 {@code false}；否则为 {@code true}。
     * @see #isEmpty(String)
     */
    public static boolean isNotEmpty(String source) {
        return !isEmpty(source);
    }

    /**
     * 使用指定的分隔符，拼接迭代器中所有元素按转换方法得到的字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符的 {@code char}。
     * @param mapper 表示将元素转换为字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param iterator 表示包含待拼接元素的迭代器的 {@link Iterator}{@code <}{@link T}{@code >}。
     * @param <T> 表示迭代器中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(char separator, Function<T, String> mapper, Iterator<T> iterator) {
        return join(Character.toString(separator), mapper, iterator);
    }

    /**
     * 使用指定的分隔符，拼接列表中所有元素按转换方法得到的字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符的 {@code char}。
     * @param mapper 表示将元素转换为字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param list 表示包含待拼接元素的列表的 {@link List}{@code <}{@link T}{@code >}。
     * @param <T> 表示列表中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(char separator, Function<T, String> mapper, List<T> list) {
        return join(Character.toString(separator), mapper, CollectionUtils.iterator(list));
    }

    /**
     * 使用指定的分隔符，拼接数组中所有元素按转换方法得到的字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符的 {@code char}。
     * @param mapper 表示将元素转换为字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param array 表示包含待拼接元素的数组的 {@link T}{@code []}。
     * @param <T> 表示数组中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    @SafeVarargs
    public static <T> String join(char separator, Function<T, String> mapper, T... array) {
        return join(Character.toString(separator), mapper, ArrayUtils.iterator(array));
    }

    /**
     * 使用指定的分隔符，拼接迭代器中所有元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符的 {@code char}。
     * @param iterator 表示包含待拼接元素的迭代器的 {@link Iterator}{@code <}{@link T}{@code >}。
     * @param <T> 表示迭代器中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(char separator, Iterator<T> iterator) {
        return join(Character.toString(separator), null, iterator);
    }

    /**
     * 使用指定的分隔符，拼接列表中所有元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符的 {@code char}。
     * @param list 表示包含待拼接元素的列表的 {@link List}{@code <}{@link T}{@code >}。
     * @param <T> 表示列表中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(char separator, List<T> list) {
        return join(Character.toString(separator), null, CollectionUtils.iterator(list));
    }

    /**
     * 使用指定的分隔符，拼接数组中所有元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符的 {@code char}。
     * @param array 表示包含待拼接元素的数组的 {@link T}{@code []}。
     * @param <T> 表示数组中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    @SafeVarargs
    public static <T> String join(char separator, T... array) {
        return join(Character.toString(separator), null, ArrayUtils.iterator(array));
    }

    /**
     * 使用指定的分隔符，拼接迭代器中所有元素按转换方法得到的字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符串的 {@link String}。
     * @param mapper 表示将元素转换为字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param iterator 表示包含待拼接元素的迭代器的 {@link Iterator}{@code <}{@link T}{@code >}。
     * @param <T> 表示迭代器中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(String separator, Function<T, String> mapper, Iterator<T> iterator) {
        StringBuilder builder = new StringBuilder();
        if (iterator != null && iterator.hasNext()) {
            Function<T, String> actualMapper = nullIf(mapper, ObjectUtils::toNormalizedString);
            builder.append(actualMapper.apply(iterator.next()));
            while (iterator.hasNext()) {
                builder.append(separator).append(actualMapper.apply(iterator.next()));
            }
        }
        return builder.toString();
    }

    /**
     * 使用指定的分隔符，拼接列表中所有元素按转换方法得到的字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符串的 {@link String}。
     * @param mapper 表示将元素转换为字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param list 表示包含待拼接元素的列表的 {@link List}{@code <}{@link T}{@code >}。
     * @param <T> 表示列表中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(String separator, Function<T, String> mapper, List<T> list) {
        return join(separator, mapper, CollectionUtils.iterator(list));
    }

    /**
     * 使用指定的分隔符，拼接数组中所有元素按转换方法得到的字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符串的 {@link String}。
     * @param mapper 表示将元素转换为字符串的转换方法的 {@link Function}{@code <}{@link T}{@code , }{@link String}{@code >}。
     * <p>当 {@code mapper} 为 {@code null} 时，会默认使用 {@link ObjectUtils#toNormalizedString(Object)} 方法代替。</p>
     * @param array 表示包含待拼接元素的数组的 {@link T}{@code []}。
     * @param <T> 表示数组中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    @SafeVarargs
    public static <T> String join(String separator, Function<T, String> mapper, T... array) {
        return join(separator, mapper, ArrayUtils.iterator(array));
    }

    /**
     * 使用指定的分隔符，拼接迭代器中所有元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符串的 {@link String}。
     * @param iterator 表示包含待拼接元素的迭代器的 {@link Iterator}{@code <}{@link T}{@code >}。
     * @param <T> 表示迭代器中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(String separator, Iterator<T> iterator) {
        return join(separator, null, iterator);
    }

    /**
     * 使用指定的分隔符，拼接列表中所有元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符串的 {@link String}。
     * @param list 表示包含待拼接元素的列表的 {@link List}{@code <}{@link T}{@code >}。
     * @param <T> 表示列表中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    public static <T> String join(String separator, List<T> list) {
        return join(separator, null, CollectionUtils.iterator(list));
    }

    /**
     * 使用指定的分隔符，拼接数组中所有元素按照 {@link ObjectUtils#toNormalizedString(Object)} 方法转换成字符串，以获取一个新的字符串。
     *
     * @param separator 表示作为分隔符的字符串的 {@link String}。
     * @param array 表示包含待拼接元素的数组的 {@link T}{@code []}。
     * @param <T> 表示数组中元素的类型的 {@link T}。
     * @return 表示拼接得到的字符串的 {@link String}。
     */
    @SafeVarargs
    public static <T> String join(String separator, T... array) {
        return join(separator, null, ArrayUtils.iterator(array));
    }

    /**
     * 检查指定字符串的长度是否在有效区间内。
     * <p>区间为前开后闭区间。</p>
     *
     * @param source 表示待检查长度的字符串的 {@link String}。
     * @param min 表示字符串长度的最小值的 {@code int}。最小值在区间范围内。
     * @param max 表示字符串长度的最大值的 {@code int}。最大值不在区间范围内。
     * @return 若字符串长度在有效区间内，则为 {@code true}；否则为 {@code false}。如果字符串为 {@code null}，则为 {@code false}。
     */
    public static boolean lengthBetween(String source, int min, int max) {
        return lengthBetween(source, min, max, true, false);
    }

    /**
     * 检查指定字符串的长度是否在有效区间内。
     *
     * @param source 表示待检查长度的字符串的 {@link String}。
     * @param min 表示字符串长度的最小值的 {@code int}。
     * @param max 表示字符串长度的最大值的 {@code int}。
     * @param allowMinimum 若允许字符串长度为最小值，则为 {@code true}；否则为 {@code false}。
     * @param allowMaximum 若允许字符串长度为最大值，则为 {@code true}；否则为 {@code false}。
     * @return 如果字符串长度在有效区间内，则为 {@code true}；否则为 {@code false}。如果字符串为 {@code null}，则为 {@code false}。
     */
    public static boolean lengthBetween(String source, int min, int max, boolean allowMinimum, boolean allowMaximum) {
        if (source == null) {
            return false;
        }
        return IntegerUtils.between(source.length(), min, max, allowMinimum, allowMaximum);
    }

    /**
     * 当指定的字符串不是一个空白字符串时，使用指定的映射程序对原字符串进行映射，以得到结果。
     *
     * @param value 表示待映射的字符串的 {@link String}。
     * @param mapper 表示当不是空白字符串时使用的映射程序的 {@link Function}。
     * @param <T> 表示待映射到的类型。
     * @return 若待映射的字符串是一个空白字符串，则为 {@code null}；否则为使用映射程序映射得到的结果。
     */
    public static <T> T mapIfNotBlank(String value, Function<String, T> mapper) {
        Validation.notNull(mapper, "The mapper cannot be null.");
        return isBlank(value) ? null : mapper.apply(value);
    }

    /**
     * 对指定字符串进行标准化操作。
     * <p>标准化操作为如果指定字符串为 {@code null}，则返回空字符串，否则，返回指定字符串自身。</p>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @return 表示标准化之后的字符串的 {@link String}。
     * @see ObjectUtils#toNormalizedString(Object)
     */
    public static String normalize(String source) {
        return nullIf(source, EMPTY);
    }

    /**
     * 当指定字符串长度小于目标长度时，在左侧填充字符。
     * <p>相关约束如下：</p>
     * <ul>
     *     <li>如果指定字符串为 {@code null}，填充到的目标长度为 {@code 0}，则输出空字符串。</li>
     *     <li>如果指定字符串为 {@code null}，填充到的目标长度小于 {@code 0}，则输出 {@code null}。</li>
     * </ul>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param padding 表示填充字符的 {@code char}。
     * @param length 表示目标长度的 {@code int}。
     * @return 表示填充字符后的字符串的 {@link String}。
     */
    public static String padLeft(String source, char padding, int length) {
        if (length < 0) {
            return source;
        }
        String actual = normalize(source);
        if (length > actual.length()) {
            StringBuilder builder = new StringBuilder();
            for (int i = actual.length(); i < length; i++) {
                builder.append(padding);
            }
            builder.append(actual);
            return builder.toString();
        } else {
            return actual;
        }
    }

    /**
     * 当指定字符串长度小于目标长度时，在右侧填充字符。
     * <p>相关约束如下：</p>
     * <ul>
     *     <li>如果指定字符串为 {@code null}，填充到的目标长度为 {@code 0}，则输出空字符串。</li>
     *     <li>如果指定字符串为 {@code null}，填充到的目标长度小于 {@code 0}，则输出 {@code null}。</li>
     * </ul>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param padding 表示填充字符的 {@code char}。
     * @param length 表示目标长度的 {@code int}。
     * @return 表示填充字符后的字符串的 {@link String}。
     */
    public static String padRight(String source, char padding, int length) {
        if (length < 0) {
            return source;
        }
        String actual = normalize(source);
        if (length > actual.length()) {
            StringBuilder builder = new StringBuilder();
            builder.append(actual);
            for (int i = actual.length(); i < length; i++) {
                builder.append(padding);
            }
            return builder.toString();
        } else {
            return actual;
        }
    }

    /**
     * 在指定的字符串范围中，用一个字符替换所有的另一个字符。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param oldChar 表示需要被替换的字符的 {@code char}。
     * @param newChar 表示指定的替换字符的 {@code char}。
     * @return 表示被替换后的字符串的 {@link String}。
     * @see String#replace(char, char)
     */
    public static String replace(String source, char oldChar, char newChar) {
        if (isEmpty(source)) {
            return source;
        }
        return source.replace(oldChar, newChar);
    }

    /**
     * 在指定的字符串范围中，用一个字符串替换所有的另一个字符串。
     * <p>当 {@code newStr} 为 {@code null} 时，返回源字符串。</p>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param oldStr 表示需要被替换的字符串的 {@link String}。
     * @param newStr 表示指定的替换字符串的 {@code String}。
     * @return 表示被替换后的字符串的 {@link String}。
     * @see String#replace(CharSequence, CharSequence)
     */
    public static String replace(String source, String oldStr, String newStr) {
        if (isEmpty(source) || isEmpty(oldStr) || newStr == null) {
            return source;
        }
        int index = source.indexOf(oldStr);
        if (index == -1) {
            return source;
        }
        StringBuilder sb = new StringBuilder(source.length());
        int pos = 0;
        int patLen = oldStr.length();
        while (index >= 0) {
            sb.append(source, pos, index);
            sb.append(newStr);
            pos = index + patLen;
            index = source.indexOf(oldStr, pos);
        }
        sb.append(source, pos, source.length());
        return sb.toString();
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符的 {@code char}。
     * @return 表示分隔后的字符串数组的 {@link String}{@code []}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @see #split(String, char, Supplier, Predicate)
     */
    public static String[] split(String source, char separator) {
        Collection<String> parts = split(source, separator, ArrayList::new);
        return parts.toArray(new String[0]);
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符的 {@code char}。
     * @param collectionSupplier 表示存储分隔后的字符串数组的容器的提供者的 {@link Supplier}{@code <}{@link C}{@code
     * >}，允许设置初始值。
     * @param <C> 表示字符串集合的某个子类的类型的 {@link C}。
     * @return 表示分隔后的字符串容器的 {@link C}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 或者 {@code collectionSupplier} 为 {@code null}
     * 时。
     * @see #split(String, char, Supplier, Predicate)
     */
    public static <C extends Collection<String>> C split(String source, char separator,
            Supplier<C> collectionSupplier) {
        return split(source, separator, collectionSupplier, null);
    }

    /**
     * 分隔字符串。
     * <p>使用指定的分隔符对字符串进行分隔，分隔后的每一部分不做任何特殊处理。如果字符串中存在连续两个分隔符，则分隔之后的结果中存在空字符串，即 {@link
     * StringUtils#EMPTY}。首尾如果存在分隔符，则分隔后的数组的首尾也同样存在空字符串。</p>
     * <ul>
     *      <li>输入为 {@code "boo:and:foo"}，分隔符为 {@code ':'}，输出为 {@code ["boo", "and", "foo"]}。</li>
     *      <li>输入为 {@code "boo:and:foo"}，分隔符为 {@code 'o'}，输出为 {@code ["b", "", ":and:f", "", ""]}。</li>
     * </ul>
     * <p><b>注意：该方法的行为和 {@link String#split(String)} 的行为是不一致的。</b></p>
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符的 {@code char}。
     * @param collectionSupplier 表示存储分隔后的字符串数组的容器的提供者的 {@link Supplier}{@code <}{@link C}{@code
     * >}，允许设置初始值。
     * @param partPredicate 表示处理每一个分隔后字符串的断言器的 {@link Predicate}{@code <}{@link String}{@code >}。
     * @param <C> 表示字符串集合的某个子类的类型的 {@link C}。
     * @return 表示分隔后的字符串容器的 {@link C}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 或者 {@code collectionSupplier} 为 {@code null}
     * 时。
     */
    public static <C extends Collection<String>> C split(String source, char separator, Supplier<C> collectionSupplier,
            Predicate<String> partPredicate) {
        return split(source, Character.toString(separator), collectionSupplier, partPredicate);
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符串的 {@link String}。
     * @return 表示分隔后的字符串数组的 {@link String}{@code []}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @see #split(String, String, Supplier, Predicate)
     */
    public static String[] split(String source, String separator) {
        Collection<String> parts = split(source, separator, ArrayList::new);
        return parts.toArray(new String[0]);
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符串的 {@link String}。
     * @param collectionSupplier 表示存储分隔后的字符串数组的容器的提供者的 {@link Supplier}{@code <}{@link C}{@code
     * >}，允许设置初始值。
     * @param <C> 表示字符串集合的某个子类的类型的 {@link C}。
     * @return 表示分隔后的字符串容器的 {@link C}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 或者 {@code collectionSupplier} 为 {@code null}
     * 时。
     * @see #split(String, String, Supplier, Predicate)
     */
    public static <C extends Collection<String>> C split(String source, String separator,
            Supplier<C> collectionSupplier) {
        return split(source, separator, collectionSupplier, null);
    }

    /**
     * 分隔字符串。
     * <p>使用指定的分隔符对字符串进行分隔，分隔后的每一部分不做任何特殊处理。如果字符串中存在连续两个分隔符，则分隔之后的结果中存在空字符串，即 {@link
     * StringUtils#EMPTY}。首尾如果存在分隔符，则分隔后的数组的首尾也同样存在空字符串。</p>
     * <ul>
     *      <li>输入为 {@code "boo:and:foo"}，分隔符为 {@code ":"}，输出为 {@code ["boo", "and", "foo"]}。</li>
     *      <li>输入为 {@code "boo:and:foo"}，分隔符为 {@code "o"}，输出为 {@code ["b", "", ":and:f", "", ""]}。</li>
     *      <li>输入为 {@code "boo:and:foo"}，分隔符为 {@code "oo"}，输出为 {@code ["b", ":and:f", ""]}。</li>
     *      <li>输入为 {@code "boo:and:foo"}，分隔符为 {@code ""}，输出为 {@code ["b", "o", "o", ":", "a", "n", "d", ":", "f",
     *      "o", "o"]}。</li>
     * </ul>
     * <p><b>注意：该方法的行为和 {@link String#split(String)} 的行为是不一致的。</b></p>
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符串的 {@link String}。
     * @param collectionSupplier 表示存储分隔后的字符串数组的容器的提供者的 {@link Supplier}{@code <}{@link C}{@code
     * >}，允许设置初始值。
     * @param partPredicate 表示处理每一个分隔后字符串的断言器的 {@link Predicate}{@code <}{@link String}{@code >}。
     * @param <C> 表示字符串集合的某个子类的类型的 {@link C}。
     * @return 表示分隔后的字符串容器的 {@link C}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 或者 {@code collectionSupplier} 为 {@code null}
     * 时。
     */
    public static <C extends Collection<String>> C split(String source, String separator,
            Supplier<C> collectionSupplier, Predicate<String> partPredicate) {
        Validation.notNull(source, "The string to be split cannot be null.");
        Validation.notNull(collectionSupplier, "The collectionSupplier cannot be null.");
        C parts = collectionSupplier.get();
        Predicate<String> actualPartPredicator = nullIf(partPredicate, value -> true);
        if (StringUtils.isEmpty(separator)) {
            return splitWithEmptySeparator(source, collectionSupplier, actualPartPredicator);
        }
        int start = 0;
        int stop;
        int limit = separator.length();
        while ((stop = source.indexOf(separator, start)) != -1) {
            String part = source.substring(start, stop);
            appendParts(parts, part, actualPartPredicator);
            start = stop + limit;
        }
        String part = source.substring(start);
        appendParts(parts, part, actualPartPredicator);
        return parts;
    }

    private static <C extends Collection<String>> C splitWithEmptySeparator(String source,
            Supplier<C> collectionSupplier, Predicate<String> partPredicator) {
        return source.chars()
                .mapToObj(ch -> (char) ch)
                .map(String::valueOf)
                .filter(partPredicator)
                .collect(collectionSupplier, Collection::add, Collection::addAll);
    }

    private static <C extends Collection<String>> void appendParts(C parts, String part,
            Predicate<String> partPredicate) {
        if (partPredicate.test(part)) {
            parts.add(part);
        }
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符的 {@code char}。
     * @return 表示分隔后的字符串列表的 {@link List}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @see #split(String, char, Supplier, Predicate)
     */
    public static List<String> splitToList(String source, char separator) {
        return split(source, separator, ArrayList::new);
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符串的 {@link String}。
     * @return 表示分隔后的字符串列表的 {@link List}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @see #split(String, String, Supplier, Predicate)
     */
    public static List<String> splitToList(String source, String separator) {
        return split(source, separator, ArrayList::new);
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符的 {@code char}。
     * @return 表示分隔后的字符串集合的 {@link Set}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @see #split(String, char, Supplier, Predicate)
     */
    public static Set<String> splitToSet(String source, char separator) {
        return split(source, separator, HashSet::new);
    }

    /**
     * 分隔字符串。
     *
     * @param source 表示待分隔的原始字符串的 {@link String}。
     * @param separator 表示分隔符的字符串的 {@link String}。
     * @return 表示分隔后的字符串集合的 {@link Set}{@code <}{@link String}{@code >}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @see #split(String, String, Supplier, Predicate)
     */
    public static Set<String> splitToSet(String source, String separator) {
        return split(source, separator, HashSet::new);
    }

    /**
     * 检查指定字符串在忽略大小写的情况下是否以指定前缀开始。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param prefix 表示待检查的前缀的 {@link String}。
     * @return 若以指定前缀（忽略大小写）结尾，则为 {@code true}；否则为 {@code false}。
     */
    public static boolean startsWithIgnoreCase(String source, String prefix) {
        if (source == null) {
            return prefix == null;
        } else if (prefix == null || source.length() < prefix.length()) {
            return false;
        } else {
            return source.regionMatches(true, 0, prefix, 0, prefix.length());
        }
    }

    /**
     * 获取指定字符串的一个子串。
     * <p><b>注意：当索引值为负数时，代表从字符串结尾向前的索引。</b></p>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param startIndex 表示子串在指定字符串中的开始位置的索引的 {@code int}。开始位置的字符会被包含在子串中。
     * @param endIndex 表示子串在指定字符串中的结束位置的索引的 {@code int}。结束位置的字符不会被包含在子串中。
     * @return 表示子串的 {@link String}。
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 时。
     * @throws IllegalStateException 起始位置或结束位置的索引超出原始字符串的范围，或结束位置在起始位置之前。
     */
    public static String substring(String source, int startIndex, int endIndex) {
        Validation.notNull(source, "The source string is null.");
        int start = Validation.greaterThanOrEquals(canonicalizeIndex(source, startIndex),
                0,
                () -> new IllegalStateException(format("The start index is out of range: {0}.", startIndex)));
        int end = Validation.greaterThanOrEquals(canonicalizeIndex(source, endIndex),
                0,
                () -> new IllegalStateException(format("The end index is out of range: {0}.", endIndex)));
        if (start > end) {
            String format = "The canonical start index is greater than the canonical end index. "
                    + "[startIndex={0}, endIndex={1}, canonicalStartIndex={2}, canonicalEndIndex={3}]";
            String message = format(format, startIndex, endIndex, start, end);
            throw new IllegalStateException(message);
        }
        return source.substring(start, end);
    }

    /**
     * 标准化索引。
     * <ul>
     *     <li>若索引是非负数，则为常规索引</li>
     *     <li>若索引为负数，则为从结尾向前的索引</li>
     * </ul>
     *
     * @param value 表示原始字符串的 {@link String}。
     * @param index 表示索引的值的32位整数。
     * @return 表示标准化后的索引的32位整数。
     */
    private static int canonicalizeIndex(String value, int index) {
        int canonical = index;
        if (canonical < 0) {
            canonical += value.length();
        }
        if (canonical < 0 || canonical > value.length()) {
            return -1;
        } else {
            return canonical;
        }
    }

    /**
     * 获取指定字符串中第一个出现的分隔符之后的子串。
     * <p>相关约束及其顺序如下：</p>
     * <ul>
     *     <li>如果指定字符串为 {@code null}，则输出 {@code null}。</li>
     *     <li>如果指定字符串为空字符串，则输出空字符串。</li>
     *     <li>如果目标分隔符为 {@code null} 或者空字符串时，则结果为指定字符串自身。</li>
     *     <li>如果指定字符串中不存在目标分隔符，则结果为空字符串。</li>
     * </ul>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param separator 表示搜索的目标分隔字符串的 {@link String}。
     * @return 表示指定字符串中第一个出现的分隔符之后的子串的 {@link String}。
     */
    public static String substringAfter(String source, String separator) {
        if (isEmpty(source) || isEmpty(separator)) {
            return source;
        }
        int index = source.indexOf(separator);
        return index == -1 ? EMPTY : source.substring(index + separator.length());
    }

    /**
     * 使用指定的字符包围指定字符串。
     *
     * @param source 表示指定的待包围的字符串的 {@link String}。
     * @param surroundWith 表示用于包围指定字符串的字符的 {@code char}。
     * @return 表示包围后得到的新字符串的 {@link String}。
     */
    public static String surround(String source, char surroundWith) {
        return surround(source, surroundWith, surroundWith);
    }

    /**
     * 使用指定的前缀和后缀字符包围指定字符串。
     *
     * @param source 表示指定的待包围的字符串的 {@link String}。
     * @param prefix 表示用于包围指定字符串的前缀字符的 {@code char}。
     * @param suffix 表示用于包围指定字符串的后缀字符的 {@code char}。
     * @return 表示包围后得到的新字符串的 {@link String}。
     */
    public static String surround(String source, char prefix, char suffix) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        if (isNotEmpty(source)) {
            builder.append(source);
        }
        builder.append(suffix);
        return builder.toString();
    }

    /**
     * 返回指定字符串的全小写表现形式。
     * <p>使用默认的语言环境 {@link Locale#ROOT} 进行大小写转换。</p>
     *
     * @param source 表示待转换为全小写表现形式的字符串的 {@link String}。
     * @return 表示转换为全小写表现形式的字符串的 {@link String}。
     * @see StringUtils#toLowerCase(String, Locale)
     */
    public static String toLowerCase(String source) {
        return toLowerCase(source, Locale.ROOT);
    }

    /**
     * 返回指定字符串的 {@link String#toLowerCase(Locale) 全小写表现形式}。
     *
     * @param source 表示待转换为全小写表现形式的字符串的 {@link String}。
     * @param locale 表示语言文字转换大小写规则的 {@link Locale}，如果为 {@code null}，则使用 {@link Locale#ROOT} 替代。
     * @return 表示转换为全小写表现形式的字符串的 {@link String}。
     */
    public static String toLowerCase(String source, Locale locale) {
        return isBlank(source) ? source : source.toLowerCase(nullIf(locale, Locale.ROOT));
    }

    /**
     * 返回指定字符串的全大写表现形式。
     * <p>使用默认的语言环境 {@link Locale#ROOT} 进行大小写转换。</p>
     *
     * @param source 表示待转换为全大写表现形式的字符串的 {@link String}。
     * @return 表示转换为全大写表现形式的字符串的 {@link String}。
     * @see StringUtils#toUpperCase(String, Locale)
     */
    public static String toUpperCase(String source) {
        return toUpperCase(source, Locale.ROOT);
    }

    /**
     * 返回指定字符串的 {@link String#toUpperCase(Locale) 全大写表现形式}。
     *
     * @param source 表示待转换为全大写表现形式的字符串的 {@link String}。
     * @param locale 表示语言文字转换大小写规则的 {@link Locale}，如果为 {@code null}，则使用 {@link Locale#ROOT} 替代。
     * @return 表示转换为全大写表现形式的字符串的 {@link String}。
     */
    public static String toUpperCase(String source, Locale locale) {
        return isBlank(source) ? source : source.toUpperCase(nullIf(locale, Locale.ROOT));
    }

    /**
     * 去除指定字符串开头和结尾的 {@link Character#isWhitespace(char) 空白字符}。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @return 表示去除开头和结尾空白字符后的字符串的 {@link String}。
     * @see String#trim()
     */
    public static String trim(String source) {
        if (source == null) {
            return null;
        }
        return source.trim();
    }

    /**
     * 去除指定字符串开头和结尾的指定字符。
     * <p>具体步骤如下：</p>
     * <ul>
     *     <li>从字符串开头处向后去除目标字符，直到遇到不为目标字符的其他字符或字符串结尾；</li>
     *     <li>然后再以此为基础，从结尾处向前去除目标字符，直到遇到不为目标字符的其他字符或字符串开头；</li>
     *     <li>返回去除字符后的字符串。</li>
     * </ul>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param ch 表示待去除的指定字符的 {@code char}。
     * @return 表示去除开头和结尾指定字符后的字符串的 {@link String}。
     */
    public static String trim(String source, char ch) {
        return trim(source, ch, ch);
    }

    /**
     * 去除指定字符串开头和结尾的指定字符。
     * <p>具体步骤如下：</p>
     * <ul>
     *     <li>从字符串开头处向后去除开头的目标字符，直到遇到不为开头目标字符的其他字符或字符串结尾；</li>
     *     <li>然后再以此为基础，从结尾处向前去除结尾的目标字符，直到遇到不为结尾目标字符的其他字符或字符串开头；</li>
     *     <li>返回去除字符后的字符串。</li>
     * </ul>
     *
     * @param source 表示原始字符串的 {@link String}。
     * @param startCh 表示开头待去除的字符的 {@code char}。
     * @param endCh 表示结尾待去除的字符的 {@code char}。
     * @return 表示去除指定字符后的字符串的 {@link String}。
     */
    public static String trim(String source, char startCh, char endCh) {
        return trimEnd(trimStart(source, startCh), endCh);
    }

    /**
     * 去除指定字符串结尾的 {@link Character#isWhitespace(char) 空白字符}。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @return 表示去除结尾空白字符后的字符串的 {@link String}。
     */
    public static String trimEnd(String source) {
        if (source == null) {
            return null;
        }
        int index = source.length() - 1;
        while (index >= 0 && Character.isWhitespace(source.charAt(index))) {
            index--;
        }
        return source.substring(0, index + 1);
    }

    /**
     * 去除指定字符串结尾的指定字符。
     * <p>具体步骤如下：</p>
     * <ul>
     *     <li>从字符串结尾处向前去除目标字符，直到遇到不为目标字符的其他字符或字符串开头；</li>
     *     <li>返回去除字符后的字符串。</li>
     * </ul>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param ch 表示待去除的指定字符的 {@code char}。
     * @return 表示去除结尾指定字符后的字符串的 {@link String}。
     */
    public static String trimEnd(String source, char ch) {
        if (source == null) {
            return null;
        }
        int index = source.length() - 1;
        while (index >= 0 && source.charAt(index) == ch) {
            index--;
        }
        return source.substring(0, index + 1);
    }

    /**
     * 去除指定字符串开头的 {@link Character#isWhitespace(char) 空白字符}。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @return 表示去除开头空白字符后的字符串的 {@link String}。
     */
    public static String trimStart(String source) {
        if (source == null) {
            return null;
        }
        int index = 0;
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        return source.substring(index);
    }

    /**
     * 去除指定字符串开头的指定字符。
     * <p>具体步骤如下：</p>
     * <ul>
     *     <li>从字符串开头处向后去除目标字符，直到遇到不为目标字符的其他字符或字符串结尾；</li>
     *     <li>返回去除字符后的字符串。</li>
     * </ul>
     *
     * @param source 表示指定字符串的 {@link String}。
     * @param ch 表示待去除的指定字符的 {@code char}。
     * @return 表示去除开头指定字符后的字符串的 {@link String}。
     */
    public static String trimStart(String source, char ch) {
        if (source == null) {
            return null;
        }
        int index = 0;
        while (index < source.length() && source.charAt(index) == ch) {
            index++;
        }
        return source.substring(index);
    }

    /**
     * 检查指定的输入流中是否包含有效的 <a href="https://zh.wikipedia.org/wiki/UTF-8">UTF-8</a> 数据。
     *
     * @param reader 表示用以读取待检查数据的读取程序的 {@link Reader}。
     * @return 若包含有效的 UTF-8 数据，则为 {@code true}，否则为 {@code false}。
     * @throws IOException 读取输入流过程发生输入输出异常。
     */
    public static boolean isUtf8(ByteReader reader) throws IOException {
        int initial;
        while ((initial = reader.read()) > -1) {
            int bits = measureUtf8(initial);
            if (bits < 1) {
                return false;
            }
            for (int i = 1; i < bits; i++) {
                int next = reader.read();
                if (next < 0 || !isUtf8Subsequent(next)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查指定的输入流中是否包含有效的 <a href="https://zh.wikipedia.org/wiki/UTF-8">UTF-8</a> 数据。
     *
     * @param bytes 表示待检查的数据的字节数组。
     * @return 若包含有效的 UTF-8 数据，则为 {@code true}，否则为 {@code false}。
     */
    public static boolean isUtf8(byte[] bytes) {
        try {
            return isUtf8(ByteReader.fromBytes(bytes));
        } catch (IOException e) {
            // 不会发生该异常
            throw new IllegalStateException(e);
        }
    }

    /**
     * 检查指定的输入流中是否包含有效的 <a href="https://zh.wikipedia.org/wiki/UTF-8">UTF-8</a> 数据。
     *
     * @param in 表示用以读取待检查数据的输入流的 {@link InputStream}。
     * @return 若包含有效的 UTF-8 数据，则为 {@code true}，否则为 {@code false}。
     * @throws IOException 读取输入流过程发生输入输出异常。
     */
    public static boolean isUtf8(InputStream in) throws IOException {
        return isUtf8(ByteReader.fromInputStream(in));
    }

    private static int measureUtf8(int initial) {
        int bits = 0;
        int mask = 0x80;
        while ((initial & mask) > 0) {
            bits++;
            mask >>>= 1;
        }
        if (bits < 1) {
            return 1;
        } else if (bits > 1) {
            return bits;
        } else {
            return 0;
        }
    }

    private static boolean isUtf8Subsequent(int value) {
        return (value & 0xc0) == 0x80;
    }

    /**
     * 判断指定字符串中是否全部都是 ASCII 字符。
     *
     * @param source 表示指定字符串的 {@link String}。
     * @return 如果指定字符串全部都是 ASCII 字符，则返回 {@code true}，否则，返回 {@code false}。
     */
    public static boolean isAscii(String source) {
        for (char c : source.toCharArray()) {
            if (c > 127) {
                return false;
            }
        }
        return true;
    }
}
