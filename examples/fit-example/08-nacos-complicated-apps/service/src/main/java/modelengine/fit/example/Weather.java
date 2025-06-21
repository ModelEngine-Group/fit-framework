/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example;

import modelengine.fitframework.annotation.Genericable;

/**
 * 表示通用接口服务。
 *
 * @author 董智豪
 * @since 2025-06-21
 */
public interface Weather {
    /**
     * 获取天气信息。
     *
     * @return 表示天气信息的 {@link String}。
     */
    @Genericable(id = "Weather")
    String get();
}
