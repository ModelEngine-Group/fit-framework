/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.utils;

import modelengine.fitframework.model.RangedResultSet;
import modelengine.fitframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 为数据实体提供工具方法。
 *
 * @author 梁济时
 * @since 2023-08-08
 */
public final class Entities {
    private static final String EMPTY_ID = "00000000000000000000000000000000";

    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private Entities() {
    }

    /**
     * 生成实体的唯一标识。
     *
     * @return 表示实体唯一标识的 {@link String}。
     */
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取空的唯一标识。
     *
     * @return 表示空的唯一标识的 {@link String}。
     */
    public static String emptyId() {
        return EMPTY_ID;
    }

    /**
     * 校验唯一标识。
     *
     * @param id 表示待校验的唯一标识的 {@link String}。
     * @param exceptionSupplier 表示当唯一标识的格式不正确时引发的异常的创建方法的 {@link Supplier}。
     * @return 表示符合校验规则的唯一标识的 {@link String}。
     */
    public static String validateId(String id, Supplier<RuntimeException> exceptionSupplier) {
        if (isId(id)) {
            return canonicalizeId(id);
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 规范化唯一标识。
     *
     * @param id 表示待规范化的唯一标识的 {@link String}。
     * @return 表示规范化后的唯一标识的 {@link String}。
     */
    public static String canonicalizeId(String id) {
        return StringUtils.toLowerCase(id);
    }

    /**
     * 检查指定的字符串是否包含有效格式的唯一标识信息。
     *
     * @param value 表示待检查的字符串的 {@link String}。
     * @return 若包含了有效格式的唯一标识，则为 {@code true}，否则为 {@code false}。
     */
    public static boolean isId(String value) {
        if (value == null || value.length() != 32) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isInvalidId(ch)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInvalidId(char ch) {
        return (ch < '0' || ch > '9') && (ch < 'a' || ch > 'f') && (ch < 'A' || ch > 'F');
    }

    /**
     * 忽略空的唯一标识。
     *
     * @param id 表示唯一标识的 {@link String}。
     * @return 当 {@code id} 为 {@link #emptyId()} 时，返回 {@code null}，否则返回输入的唯一标识的 {@link String}。
     */
    public static String ignoreEmpty(String id) {
        if (emptyId().equals(id)) {
            return null;
        } else {
            return id;
        }
    }

    /**
     * 设置实体的跟踪信息，包括创建人、创建时间、修改人、修改时间。
     *
     * @param entity 表示待填充跟踪信息的实体的 {@link Object}。
     * @param row 表示数据行的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public static void fillTraceInfo(Object entity, Map<String, Object> row) {
        if (entity instanceof CreationTraceable) {
            CreationTraceable traceable = (CreationTraceable) entity;
            if (row.get("created_by") instanceof String) {
                traceable.setCreator((String) row.get("created_by"));
            }
            if (row.get("created_at") instanceof Timestamp) {
                traceable.setCreationTime(Dates.fromUtc(((Timestamp) row.get("created_at")).toLocalDateTime()));
            }
        }
        if (entity instanceof ModificationTraceable) {
            ModificationTraceable traceable = (ModificationTraceable) entity;
            if (row.get("updated_by") instanceof String) {
                traceable.setLastModifier((String) row.get("updated_by"));
            }
            if (row.get("updated_at") instanceof Timestamp) {
                traceable.setLastModificationTime(Dates.fromUtc(((Timestamp) row.get("updated_at")).toLocalDateTime()));
            }
        }
    }

    /**
     * 生成一个空的分页结果集。
     *
     * @param offset 表示待查询的分页结果集在全量集中的偏移量的 64 位整数。
     * @param limit 表示期望的分页结果集中包含数据记录的最大数量的 32 位整数。
     * @param <T> 表示结果集中元素的类型。
     * @return 表示空的分页结果集的 {@link RangedResultSet}。
     */
    public static <T> RangedResultSet<T> emptyRangedResultSet(long offset, int limit) {
        return RangedResultSet.create(Collections.emptyList(), (int) offset, limit, 0);
    }

    /**
     * 检查指定的唯一标识是否为空。
     *
     * @param id 表示待检查的唯一标识的 {@link String}。
     * @return 若唯一标识为空，则为 {@code true}，否则为 {@code false}。
     */
    public static boolean isEmpty(String id) {
        return StringUtils.isEmpty(id) || StringUtils.equalsIgnoreCase(id, emptyId());
    }

    /**
     * 检查两个唯一标识是否匹配。
     *
     * @param expectedId 表示所期望的唯一标识的 {@link String}。
     * @param actualId 表示实际的唯一标识的 {@link String}。
     * @return 若唯一标识匹配成功，则为 {@code true}，否则为 {@code false}。
     */
    public static boolean match(String expectedId, String actualId) {
        return StringUtils.equalsIgnoreCase(ignoreEmpty(expectedId), ignoreEmpty(actualId));
    }

    /**
     * equals
     *
     * @param map1 map1
     * @param map2 map2
     * @return boolean
     */
    public static <K, V> boolean equals(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == null) {
            return map2 == null;
        } else if (map2 == null || map1.size() != map2.size()) {
            return false;
        } else {
            for (Map.Entry<K, V> entry : map1.entrySet()) {
                V value1 = entry.getValue();
                V value2 = map2.get(entry.getKey());
                if (!Objects.equals(value1, value2)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * equals
     *
     * @param list1 list1
     * @param list2 list2
     * @return boolean
     */
    public static <T> boolean equals(List<T> list1, List<T> list2) {
        if (list1 == null) {
            return list2 == null;
        } else if (list2 == null || list1.size() != list2.size()) {
            return false;
        } else {
            Set<T> set1 = new HashSet<>(list1);
            Set<T> set2 = new HashSet<>(list2);
            if (set1.size() != set2.size()) {
                return false;
            }
            set1.removeAll(set2);
            return set1.isEmpty();
        }
    }

    /**
     * 表示创建人和创建时间可跟踪的对象。
     */
    public interface CreationTraceable {
        /**
         * setCreator
         *
         * @param creator creator
         */
        void setCreator(String creator);

        /**
         * setCreationTime
         *
         * @param creationTime creationTime
         */
        void setCreationTime(LocalDateTime creationTime);
    }

    /**
     * ModificationTraceable
     *
     * @since 2023-09-15
     */
    public interface ModificationTraceable {
        /**
         * setLastModifier
         *
         * @param lastModifier lastModifier
         */
        void setLastModifier(String lastModifier);

        /**
         * setLastModificationTime
         *
         * @param lastModificationTime lastModificationTime
         */
        void setLastModificationTime(LocalDateTime lastModificationTime);
    }

    /**
     * 将字符串列表规范化为标准格式
     *
     * @param values 字符串列表
     * @return 标准格式列表
     */
    public static List<String> canonicalizeStringList(List<String> values) {
        return Optional.ofNullable(values)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(StringUtils::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }
}
