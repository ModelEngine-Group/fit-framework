/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.pattern;

/**
 * 模型算子的基类。
 *
 * @param <I> 表示模型算子的输入类型。
 * @param <O> 表示模型算子的输出类型。
 * @author 刘信宏
 * @since 2024-06-11
 */
public interface Model<I, O> extends Pattern<I, O> {}
