/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.vectorstore.support;

import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.pattern.Retriever;
import modelengine.fel.core.vectorstore.SearchOption;
import modelengine.fel.core.vectorstore.VectorStore;
import modelengine.fitframework.inspection.Validation;

import java.util.List;

/**
 * 表示默认的向量检索器。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public class DefaultVectorRetriever implements Retriever<String, MeasurableDocument> {
    private final VectorStore vectorStore;
    private final SearchOption searchOption;

    /**
     * 构造默认的向量检索器。
     *
     * @param vectorStore 表示向量数据库的 {@link VectorStore}。
     * @param searchOption 表示查询参数的 {@link SearchOption}。
     */
    public DefaultVectorRetriever(VectorStore vectorStore, SearchOption searchOption) {
        this.vectorStore = Validation.notNull(vectorStore, "The vector store cannot be null.");
        this.searchOption = Validation.notNull(searchOption, "The search option cannot be null.");
    }

    @Override
    public List<MeasurableDocument> retrieve(String query) {
        return this.vectorStore.search(query, searchOption);
    }
}