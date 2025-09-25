/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.service;

import modelengine.fitframework.annotation.Genericable;

/**
 * 表示工具输出转换器。
 *
 * @author 易文渊
 * @since 2024-08-14
 */
public interface ToolOutputConverter {
    /**
     * 将工具输出进行转换。
     *
     * @param object 表示待序列化对象的 {@link Object}。
     * @return 表示序列化结果的 {@link String}。
     */
    @Genericable(id = "modelengine.fel.tool.convert")
    String convert(Object object);
}