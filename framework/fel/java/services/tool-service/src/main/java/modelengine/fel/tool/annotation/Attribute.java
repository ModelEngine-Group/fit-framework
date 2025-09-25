/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.annotation;

/**
 * 用于工具方法额外参数的定义。
 *
 * @author 易文渊
 * @since 2024-08-19
 */
public @interface Attribute {
    /**
     * 获取属性的键。
     *
     * @return 表示属性键的 {@link String}。
     */
    String key();

    /**
     * 获取属性的值。
     *
     * @return 表示属性值的 {@link String}。
     */
    String value();
}
