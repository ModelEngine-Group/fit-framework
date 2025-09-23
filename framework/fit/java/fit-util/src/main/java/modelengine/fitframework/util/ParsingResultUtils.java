/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fitframework.util;

import modelengine.fitframework.util.support.DefaultParsingResult;

/**
 * 为 {@link ParsingResult} 提供工具方法。
 *
 * @author 梁济时
 * @since 2020-07-24
 */
class ParsingResultUtils {
    /** 表示失败的转换结果。 */
    public static final ParsingResult<?> FAILED = new DefaultParsingResult<>(false, null);

    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private ParsingResultUtils() {}
}
