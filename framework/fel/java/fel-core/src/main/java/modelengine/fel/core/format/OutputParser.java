/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.format;

import modelengine.fel.core.pattern.Parser;

/**
 * 表示输出解析器的接口，包含语言模型的输出应该如何格式化的指令和解析为某个对象的方法。
 *
 * @param <O> 表示输出对象类型。
 * @author 易文渊
 * @see FormatProvider
 * @see Parser
 * @since 2024-04-28
 */
public interface OutputParser<O> extends FormatProvider, Parser<String, O> {}