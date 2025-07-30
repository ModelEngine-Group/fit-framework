/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.rerank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 表示 OpenAI API 格式的重排响应。
 *
 * @since 2024-09-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiRerankResponse {
    private List<OpenAiRerankResponse.RerankOrder> results;

    /**
     * 获取重新排序后的文档列表。
     *
     * @return 表示重新排序后的文档列表的 {@link List}{@code <}{@link OpenAiRerankResponse.RerankOrder}{@code >}。
     */
    public List<OpenAiRerankResponse.RerankOrder> results() {
        return CollectionUtils.isEmpty(this.results)
                ? Collections.emptyList()
                : Collections.unmodifiableList(this.results);
    }

    /**
     * 表示重排序后的文档项，包含文档在原始列表中的索引和重新计算的相关性评分。
     * 用于存储和访问重新排序后的文档信息。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RerankOrder {
        private int index;
        @Property(name = "relevance_score")
        private double relevanceScore;

        /**
         * 获取文档在原始列表中的索引。
         *
         * @return 表示文档在原始列表中的索引的 {@code int}。
         */
        public int index() {
            return this.index;
        }

        /**
         * 获取文档的相关性评分。
         *
         * @return 表示文档的相关性评分的 {@code double}。
         */
        public double relevanceScore() {
            return this.relevanceScore;
        }
    }
}
