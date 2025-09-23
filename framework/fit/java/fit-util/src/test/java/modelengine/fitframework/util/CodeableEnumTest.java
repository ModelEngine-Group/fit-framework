/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * 为 {@link CodeableEnum} 提供单元测试。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class CodeableEnumTest {
    /**
     * 目标方法：{@link CodeableEnum#getId(CodeableEnum)}
     * <p>当枚举项为 {@code null} 时，返回的唯一标识也为 {@code null}。</p>
     */
    @Test
    public void should_return_null_when_get_id_of_null() {
        Integer id = CodeableEnum.getId(null);
        assertNull(id);
    }

    /**
     * 目标方法：{@link CodeableEnum#getId(CodeableEnum)}
     * <p>当枚举项不为 {@code null} 时，返回的唯一标识为{@link CodeableEnum#getId() 枚举项的唯一标识}。</p>
     */
    @Test
    public void should_return_id_when_enum_is_not_null() {
        Integer id = CodeableEnum.getId(DemoCodeableEnum.ITEM_1);
        assertNotNull(id);
        assertEquals(id, DemoCodeableEnum.ITEM_1.getId());
    }

    /**
     * 目标方法：{@link CodeableEnum#getCode(CodeableEnum)}
     * <p>当枚举项为 {@code null} 时，返回的编号也为 {@code null}。</p>
     */
    @Test
    public void should_return_null_when_get_code_of_null() {
        String code = CodeableEnum.getCode(null);
        assertNull(code);
    }

    /**
     * 目标方法：{@link CodeableEnum#getCode(CodeableEnum)}
     * <p>当枚举项不为 {@code null} 时，返回的编号为{@link CodeableEnum#getCode() 枚举项的编号}。</p>
     */
    @Test
    public void should_return_code_when_enum_is_not_null() {
        String code = CodeableEnum.getCode(DemoCodeableEnum.ITEM_2);
        assertNotNull(code);
        assertEquals(code, DemoCodeableEnum.ITEM_2.getCode());
    }

    /**
     * 目标方法：{@link CodeableEnum#fromId(Class, Integer)}
     * <p>当不存在指定唯一标识的枚举项时，返回 {@code null}。</p>
     */
    @Test
    public void should_return_null_when_id_not_found() {
        DemoCodeableEnum demo = CodeableEnum.fromId(DemoCodeableEnum.class, null);
        assertNull(demo);
    }

    /**
     * 目标方法：{@link CodeableEnum#fromId(Class, Integer)}
     * <p>当存在指定唯一标识的枚举项时，返回枚举项。</p>
     */
    @Test
    public void should_return_item_when_id_found() {
        DemoCodeableEnum demo = CodeableEnum.fromId(DemoCodeableEnum.class, DemoCodeableEnum.ITEM_1.getId());
        assertNotNull(demo);
        assertSame(demo, DemoCodeableEnum.ITEM_1);
    }

    /**
     * 目标方法：{@link CodeableEnum#fromCode(Class, String)}
     * <p>当不存在指定唯一标识的枚举项时，返回 {@code null}。</p>
     */
    @Test
    public void should_return_null_when_code_not_found() {
        DemoCodeableEnum demo = CodeableEnum.fromCode(DemoCodeableEnum.class, null);
        assertNull(demo);
    }

    /**
     * 目标方法：{@link CodeableEnum#fromCode(Class, String)}
     * <p>当存在指定唯一标识的枚举项时，返回枚举项。</p>
     */
    @Test
    public void should_return_item_when_code_found() {
        DemoCodeableEnum demo = CodeableEnum.fromCode(DemoCodeableEnum.class, DemoCodeableEnum.ITEM_2.getCode());
        assertNotNull(demo);
        assertSame(demo, DemoCodeableEnum.ITEM_2);
    }
}

/**
 * 为 {@link CodeableEnum} 提供用以验证的枚举定义。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
enum DemoCodeableEnum implements CodeableEnum<DemoCodeableEnum> {
    /** 表示第一个枚举项。 */
    ITEM_1(1, "item-1"),

    /** 表示第二个枚举项。 */
    ITEM_2(2, "item-2");

    private final Integer id;
    private final String code;

    DemoCodeableEnum(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public String getCode() {
        return this.code;
    }
}
