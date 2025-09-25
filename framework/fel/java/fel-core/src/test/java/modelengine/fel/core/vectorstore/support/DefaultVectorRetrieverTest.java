/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.vectorstore.support;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.embed.EmbedOption;
import modelengine.fel.core.embed.support.DefaultDocumentEmbedModel;
import modelengine.fel.core.pattern.Retriever;
import modelengine.fel.core.vectorstore.SearchOption;
import modelengine.fel.core.vectorstore.VectorStore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 表示 {@link DefaultVectorRetriever} 的单元测试。
 *
 * @author 易文渊
 * @since 2024-08-08
 */
@DisplayName("测试 DefaultVectorRetriever")
public class DefaultVectorRetrieverTest {
    @Test
    @DisplayName("测试插入数据后，向量检索正常")
    void shouldOkWhenRetrieve() {
        List<Document> documents = EmbedModelStub.generateTestDocuments();
        VectorStore vectorStore = new MemoryVectorStore(new DefaultDocumentEmbedModel(new EmbedModelStub(),
                EmbedOption.custom().build()));
        vectorStore.persistent(documents);
        Retriever<String, MeasurableDocument> retriever =
                new DefaultVectorRetriever(vectorStore, SearchOption.custom().topK(1).build());
        assertThat(retriever.retrieve("test0")).hasSize(1)
                .first()
                .extracting(Document::text)
                .isEqualTo(documents.get(0).text());
    }
}