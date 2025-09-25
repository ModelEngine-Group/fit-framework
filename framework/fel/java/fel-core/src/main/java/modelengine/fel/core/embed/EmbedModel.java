/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.embed;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notEmpty;

import java.util.Collections;
import java.util.List;

/**
 * 表示嵌入模型模型服务。
 *
 * @author 易文渊
 * @since 2024-04-13
 */
public interface EmbedModel {
    /**
     * 根据可选参数，调用嵌入模型生成嵌入向量。
     *
     * @param input 表示用户输入的 {@link String}。
     * @param option 表示嵌入模型可选参数的 {@link EmbedOption}。
     * @return 表示模型生成嵌入响应的 {@link Embedding}。
     */
    default Embedding generate(String input, EmbedOption option) {
        notBlank(input, "The input cannot be blank.");
        List<Embedding> embeddings = this.generate(Collections.singletonList(input), option);
        notEmpty(embeddings, "The embedding cannot be empty.");
        return embeddings.get(0);
    }

    /**
     * 根据可选参数，调用嵌入模型批量生成嵌入向量。
     *
     * @param inputs 表示用户输入的 {@link List}{@code <}{@link String}{@code >}。
     * @param option 表示嵌入模型可选参数的 {@link EmbedOption}。
     * @return 表示模型生成嵌入响应的 {@link List}{@code <}{@link Embedding}{@code >}。
     */
    List<Embedding> generate(List<String> inputs, EmbedOption option);
}