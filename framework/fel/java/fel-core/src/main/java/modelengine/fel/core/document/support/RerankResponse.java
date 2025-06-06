/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 表示 Rerank API 格式的请求。
 *
 * @author 马朝阳
 * @since 2024-09-27
 */
public class RerankResponse {
    private List<RerankOrder> results;

    /**
     * 获取重新排序后的文档列表。
     *
     * @return 表示重新排序后的文档列表的 {@link List}{@code <}{@link RerankOrder}{@code >}。
     */
    public List<RerankOrder> results() {
        return CollectionUtils.isEmpty(this.results)
                ? Collections.emptyList()
                : Collections.unmodifiableList(this.results);
    }

    static class RerankOrder {
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
