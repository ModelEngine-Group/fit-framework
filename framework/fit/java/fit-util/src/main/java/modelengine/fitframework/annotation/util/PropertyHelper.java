/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.annotation.util;

import modelengine.fitframework.annotation.Property;

/**
 * 为 Property 注解提供工具类。
 *
 * @author 杭潇
 * @since 2025-10-29
 */
public final class PropertyHelper {
    /**
     * 隐藏默认构造方法，避免工具类被实例化。
     */
    private PropertyHelper() {}

    /**
     * 判断给定的默认值是否为设置状态。
     *
     * @param defaultValue 要检查的默认值。
     * @return 如果自定义参数值则返回 {@code true}，否则返回 {@code false}。
     */
    public static boolean isCustomValue(String defaultValue) {
        return !Property.UNSET_DEFAULT_VALUE.equals(defaultValue);
    }
}

