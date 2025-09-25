/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format;

/**
 * 表示格式化器的接口，提供包含语言模型的输出应该如何格式化的提示词。
 *
 * @author 易文渊
 * @since 2024-04-28
 */
@FunctionalInterface
public interface FormatProvider {
    /**
     * 获取规范模型输出的提示词。
     *
     * @return 表示提示词的 {@link String}。
     */
    String instruction();
}