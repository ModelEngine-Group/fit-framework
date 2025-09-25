/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.pattern;

import modelengine.fel.core.document.Content;

/**
 * 表示检索结果合成算子。
 *
 * @param <I> 表示合成结果的泛型。
 * @author 易文渊
 * @since 2024-08-05
 */
@FunctionalInterface
public interface Synthesizer<I> extends Pattern<I, Content> {
    /**
     * 将检索的数据进行合成。
     *
     * @param input 表示检索数据的 {@link I}。
     * @return 表示合成结果的 {@link Content}。
     */
    Content synthesize(I input);

    @Override
    default Content invoke(I input) {
        return this.synthesize(input);
    }
}