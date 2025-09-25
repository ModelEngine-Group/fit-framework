/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.template;

import java.util.Set;

/**
 * 模板的泛型接口定义。
 *
 * @param <I> 表示输入参数的类型。
 * @param <O> 表示渲染结果的类型。
 * @author 易文渊
 * @since 2024-04-25
 */
public interface GenericTemplate<I, O> {
    /**
     * 根据输入参数渲染模板，生成结果。
     *
     * @param values 表示输入参数的 {@link Object}。
     * @return 返回表示渲染结果的 {@link Object}。
     */
    O render(I values);

    /**
     * 获取模板占位符集合。
     *
     * @return 返回表示模板占位符集合的 {@link Set}{@code <}{@link String}{@code >}。
     */
    Set<String> placeholder();
}