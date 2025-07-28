/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.vectorstore.support;

import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.embed.EmbedOption;
import modelengine.fel.core.embed.support.DefaultDocumentEmbedModel;
import modelengine.fel.core.vectorstore.SearchOption;
import modelengine.fel.core.vectorstore.VectorStore;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.serialization.ObjectSerializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

/**
 * 表示 {@link MemoryVectorStore} 的单元测试。
 *
 * @since 2024-08-08
 */
@DisplayName("测试 MemoryVectorStore")
public class MemoryVectorStoreTest {
    @Test
    @DisplayName("插入文档后，查询成功")
    void shouldOkWhenAddDocumentsThenSearch() {
        VectorStore vectorStore = new MemoryVectorStore(new DefaultDocumentEmbedModel(new EmbedModelStub(),
                EmbedOption.custom().build()));
        vectorStore.persistent(EmbedModelStub.generateTestDocuments());
        assertThat(vectorStore.search("test", SearchOption.custom().topK(3).build())).hasSize(3)
                .isSortedAccordingTo(Comparator.comparingDouble(MeasurableDocument::score).reversed());
    }

    @Test
    @DisplayName("插入文档后，持久化后重新加载成功")
    void shouldOkWhenPersistThenLoad() throws IOException {
        ObjectSerializer serializer = new JacksonObjectSerializer(null, null, null, true);

        MemoryVectorStore vectorStore = new MemoryVectorStore(new DefaultDocumentEmbedModel(new EmbedModelStub(),
                EmbedOption.custom().build()));
        vectorStore.persistent(EmbedModelStub.generateTestDocuments());
        List<MeasurableDocument> first = vectorStore.search("test", SearchOption.custom().topK(3).build());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            vectorStore.persist(out, serializer);
            byte[] bytes = out.toByteArray();
            try (InputStream in = new ByteArrayInputStream(bytes)) {
                MemoryVectorStore resumeStore =
                        new MemoryVectorStore(new DefaultDocumentEmbedModel(new EmbedModelStub(),
                                EmbedOption.custom().build()));
                resumeStore.load(in, serializer);
                List<MeasurableDocument> second = resumeStore.search("test", SearchOption.custom().topK(3).build());
                assertThat(first).isEqualTo(second);
            }
        }
    }
}