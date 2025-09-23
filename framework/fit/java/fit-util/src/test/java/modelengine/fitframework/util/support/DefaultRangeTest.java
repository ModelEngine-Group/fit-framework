/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import modelengine.fitframework.model.support.DefaultRange;

import org.junit.jupiter.api.Test;

/**
 * 为 {@link DefaultRange} 提供单元测试。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
public class DefaultRangeTest {
    /** 表示用以测试的对象实例。 */
    private static final DefaultRange RANGE = new DefaultRange(-100, 100);

    /** 表示用以测试的对象实例的字符串表现形式。 */
    private static final String RANGE_STRING = "[offset=-100, limit=100]";

    /**
     * 目标方法：{@link DefaultRange#toString()}
     * <p>{@link DefaultRange#toString() toString()} 方法返回正确的字符串表现。</p>
     */
    @Test
    public void should_return_correct_string_representation() {
        assertEquals(RANGE_STRING, RANGE.toString());
    }
}
