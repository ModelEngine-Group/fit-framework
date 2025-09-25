/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.embed;

import java.util.List;

/**
 * 表示 OpenAi API 格式的嵌入响应。
 *
 * @author 易文渊
 * @author 张庭怿
 * @since 2024-4-30
 */
public class OpenAiEmbeddingResponse {
    /**
     * 模型生成的 embeddings 列表。
     */
    private List<OpenAiEmbedding> data;

    /**
     * 获取模型生成的嵌入向量列表。
     *
     * @return 表示模型嵌入向量列表的 {@link List}{@code <}{@link OpenAiEmbedding}{@code >}。
     */
    public List<OpenAiEmbedding> data() {
        return this.data;
    }
}
