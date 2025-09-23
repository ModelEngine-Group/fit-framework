/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 为 {@link EnumUtils} 提供工具方法。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class EnumUtilsTest {
    /**
     * 目标方法：{@link EnumUtils#firstOrDefault(Class, Predicate)}
     * <p>当没有符合条件的枚举项时，返回 {@code null}。</p>
     */
    @Test
    public void should_return_null_when_no_matched_found() {
        DemoEnum value = EnumUtils.firstOrDefault(DemoEnum.class, DemoEnum.NO_MATCH_PREDICATE);
        assertNull(value);
    }

    /**
     * 目标方法：{@link EnumUtils#firstOrDefault(Class, Predicate)}
     * <p>当存在符合条件的枚举项时返回第一个结果。</p>
     */
    @Test
    public void should_return_first_matched_when_found() {
        DemoEnum value = EnumUtils.firstOrDefault(DemoEnum.class, DemoEnum.ONLY_ITEM_2_MATCH_PREDICATE);
        assertNotNull(value);
        assertEquals(value, DemoEnum.ITEM_2);
    }

    @Test
    public void should_return_first_when_no_predicate() {
        DemoEnum value = EnumUtils.firstOrDefault(DemoEnum.class, null);
        assertNotNull(value);
        assertEquals(value, DemoEnum.ITEM_1);
    }

    /**
     * 目标方法：{@link EnumUtils#find(Class, Predicate)}
     * <p>当没有符合条件的枚举项时，返回一个空列表。</p>
     */
    @Test
    public void should_return_empty_list_when_no_matched_found() {
        List<DemoEnum> values = EnumUtils.find(DemoEnum.class, DemoEnum.NO_MATCH_PREDICATE);
        assertNotNull(values);
        assertEquals(values.size(), 0);
    }

    /**
     * 目标方法：{@link EnumUtils#find(Class, Predicate)}
     * <p>当有符合条件的枚举项时，返回所有符合条件的枚举项。</p>
     */
    @Test
    public void should_return_all_matched_when_found() {
        List<DemoEnum> values = EnumUtils.find(DemoEnum.class, DemoEnum.ALL_MATCH_PREDICATE);
        assertNotNull(values);
        assertEquals(values.size(), DemoEnum.values().length);
        assertIterableEquals(values, ObjectUtils.mapIfNotNull(DemoEnum.values(), java.util.Arrays::asList));
    }

    @Test
    public void should_return_all_matched_when_no_predicate() {
        List<DemoEnum> values = EnumUtils.find(DemoEnum.class, null);
        assertNotNull(values);
        assertEquals(values.size(), DemoEnum.values().length);
        assertIterableEquals(values, ObjectUtils.mapIfNotNull(DemoEnum.values(), java.util.Arrays::asList));
    }

    /**
     * 为测试 {@link EnumUtils} 提供用以验证的枚举定义。
     *
     * @author 梁济时
     * @since 2020-07-24
     */
    enum DemoEnum {
        /** 表示第一个枚举项。 */
        ITEM_1,

        /** 表示第二个枚举项。 */
        ITEM_2;

        /** 表示所有枚举项都满足的谓词。 */
        public static final Predicate<DemoEnum> ALL_MATCH_PREDICATE = item -> true;

        /** 表示所有枚举项都不满足的谓词。 */
        public static final Predicate<DemoEnum> NO_MATCH_PREDICATE = item -> false;

        /** 表示只有 {@link DemoEnum#ITEM_2} 满足的谓词。 */
        public static final Predicate<DemoEnum> ONLY_ITEM_2_MATCH_PREDICATE = item -> Objects.equals(item, ITEM_2);
    }
}

