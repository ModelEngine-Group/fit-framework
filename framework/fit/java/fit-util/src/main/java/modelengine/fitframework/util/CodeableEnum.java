/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import java.util.Objects;

/**
 * 为可编号的枚举提供定义。
 * <p>可编号枚举提供整型唯一标识和字符串编号，可用以为枚举提供整型到字符串间的映射。</p>
 * <p>可用于为三层架构提供友好的枚举定义：</p>
 * <ul>
 *     <li>表现层使用字符串作为友好的输入和输出。</li>
 *     <li>业务逻辑层使用枚举本身以提供友好的编程支持。</li>
 *     <li>数据访问层使用整型唯一标识以便于数据存储。</li>
 * </ul>
 * <p>在定义过程中应保证唯一标识和编号均在枚举中唯一。</p>
 *
 * @param <T> 表示枚举的实际类型。
 * @author 梁济时
 * @since 2020-07-24
 */
public interface CodeableEnum<T extends Enum<T> & CodeableEnum<T>> {
    /**
     * 获取枚举的唯一标识。
     *
     * @return 表示枚举唯一标识的 {@link Integer}。
     */
    Integer getId();

    /**
     * 获取枚举的编号。
     *
     * @return 表示枚举编号的 {@link String}。
     */
    String getCode();

    /**
     * 获取指定可编号枚举项的唯一标识。
     *
     * @param value 表示待获取唯一标识的可编号枚举项。
     * @param <T> 表示枚举的实际类型。
     * @return 若枚举项为 {@code null}，则为 {@code null}；否则为表示枚举项的唯一标识的 {@link Integer}。
     */
    static <T extends Enum<T> & CodeableEnum<T>> Integer getId(CodeableEnum<T> value) {
        return ObjectUtils.mapIfNotNull(value, item -> item.getId());
    }

    /**
     * 获取指定可编号枚举项的编号。
     *
     * @param value 表示待获取编号的可编号枚举项。
     * @param <T> 表示枚举的实际类型。
     * @return 若枚举项为 {@code null}，则为 {@code null}；否则为表示枚举项的编号的 {@link String}。
     */
    static <T extends Enum<T> & CodeableEnum<T>> String getCode(CodeableEnum<T> value) {
        return ObjectUtils.mapIfNotNull(value, item -> item.getCode());
    }

    /**
     * 获取具备指定唯一标识的可编号枚举项。
     * <p>唯一标识的唯一性由定义枚举的编程人员保障。</p>
     *
     * @param clz 表示可编号枚举类型的 {@link Class}。
     * @param id 表示待查找的枚举项的唯一标识的 {@link Integer}。
     * @param <T> 表示枚举的实际类型。
     * @return 若存在指定唯一标识的枚举，则为该枚举项；否则为 {@code null}。
     */
    static <T extends Enum<T> & CodeableEnum<T>> T fromId(Class<T> clz, Integer id) {
        return EnumUtils.firstOrDefault(clz, item -> Objects.equals(item.getId(), id));
    }

    /**
     * 获取具备指定编号的可编号枚举项。
     * <p>编号的唯一性由定义枚举的编程人员保障。</p>
     *
     * @param clz 表示可编号枚举类型的 {@link Class}。
     * @param code 表示待查找的枚举项的编号的 {@link String}。
     * @param <T> 表示枚举的实际类型。
     * @return 若存在指定编号的枚举，则为该枚举项；否则为 {@code null}。
     */
    static <T extends Enum<T> & CodeableEnum<T>> T fromCode(Class <T> clz, String code) {
        return EnumUtils.firstOrDefault(clz, item -> StringUtils.equalsIgnoreCase(item.getCode(), code));
    }
}
