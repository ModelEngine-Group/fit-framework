/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import modelengine.fitframework.inspection.Validation;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 为枚举提供工具方法。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public final class EnumUtils {
    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private EnumUtils() {}

    /**
     * 查找指定枚举中第一个符合条件的枚举项。
     * <p>枚举的顺序依赖于 {@link EnumSet#allOf(Class)} 返回的集合的顺序。</p>
     * <p>若 {@code predicate} 为 {@code null}，则认为所有枚举项都是符合条件的。</p>
     *
     * @param clz 表示枚举类型的 {@link Class}。
     * @param predicate 表示枚举的判定条件的谓词的 {@link Predicate}。
     * @param <T> 表示枚举的类型。
     * @return 若存在符合条件的枚举，则返回所有符合条件枚举中的第一个；否则返回 {@code null}。
     */
    public static <T extends Enum<T>> T firstOrDefault(Class<T> clz, Predicate<T> predicate) {
        Validation.notNull(clz, "The enum class to find value cannot be null.");
        Predicate<T> actualPredicate = ObjectUtils.nullIf(predicate, item -> true);
        return EnumSet.allOf(clz).stream().filter(actualPredicate).findAny().orElse(null);
    }

    /**
     * 查找指定枚举中所有符合条件的枚举项。
     * <p>枚举的顺序依赖于 {@link EnumSet#allOf(Class)} 返回的集合的顺序。</p>
     * <p>若 {@code predicate} 为 {@code null}，则认为所有枚举项都是符合条件的。</p>
     *
     * @param clz 表示枚举类型的 {@link Class}。
     * @param predicate 表示枚举的判定条件的谓词的 {@link Predicate}。
     * @param <T> 表示枚举的类型。
     * @return 表示所有符合条件的枚举项的列表的 {@link List}。
     */
    public static <T extends Enum<T>> List<T> find(Class<T> clz, Predicate<T> predicate) {
        Validation.notNull(clz, "The enum class to find values cannot be null.");
        Predicate<T> actualPredicate = ObjectUtils.nullIf(predicate, item -> true);
        return EnumSet.allOf(clz).stream().filter(actualPredicate).collect(Collectors.toList());
    }
}
