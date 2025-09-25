/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.pattern;

import modelengine.fel.core.document.Measurable;

import java.util.List;

/**
 * 表示检索算子。
 *
 * @param <I> 表示输入参数的类型。
 * @author 刘信宏
 * @since 2024-04-28
 */
@FunctionalInterface
public interface Retriever<I, O extends Measurable> extends Pattern<I, List<O>> {
    /**
     * 根据用户输入进行检索。
     *
     * @param query 表示用户输入的 {@link I}。
     * @return 返回可量化数据的 {@link List}{@code <}{@link O}{@code >}。
     */
    List<O> retrieve(I query);

    @Override
    default List<O> invoke(I query) {
        return this.retrieve(query);
    }
}
