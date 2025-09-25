/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.vectorstore.support;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notEmpty;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.document.DocumentEmbedModel;
import modelengine.fel.core.document.MeasurableDocument;
import modelengine.fel.core.embed.Embedding;
import modelengine.fel.core.util.MathUtils;
import modelengine.fel.core.vectorstore.SearchOption;
import modelengine.fel.core.vectorstore.VectorStore;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.TypeUtils;
import modelengine.fitframework.util.UuidUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 表示 {@link VectorStore} 的内存简易实现，不要在生产环境中使用。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public class MemoryVectorStore implements VectorStore {
    private final DocumentEmbedModel embedModel;
    private final Map<String, DocumentWithEmbedding> cache = new ConcurrentHashMap<>();

    /**
     * 创建 {@link MemoryVectorStore} 的实例。
     *
     * @param embedModel 表示嵌入文档模型的 {@link DocumentEmbedModel}。
     */
    public MemoryVectorStore(DocumentEmbedModel embedModel) {
        this.embedModel = embedModel;
    }

    @Override
    public void persistent(List<Document> documents) {
        List<Embedding> embeddings = this.embedModel.embed(documents);
        for (int i = 0; i < documents.size(); ++i) {
            DocumentWithEmbedding document =
                    DocumentWithEmbedding.from(documents.get(i), embeddings.get(i).embedding());
            this.cache.put(document.id(), document);
        }
    }

    @Override
    public List<MeasurableDocument> search(String query, SearchOption option) {
        List<Float> queryEmbedding = this.embedModel.embed(query).embedding();
        return this.cache.values()
                .stream()
                .map(d -> new MeasurableDocument(d, MathUtils.cosineSimilarity(queryEmbedding, d.getEmbedding())))
                .sorted(Comparator.comparingDouble(MeasurableDocument::score).reversed())
                .limit(option.topK())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(List<String> ids) {
        Validation.notNull(ids, "The id list cannot be null.");
        ids.forEach(this.cache::remove);
    }

    /**
     * 从输入流中加载数据到内存中。
     *
     * @param in 表示输入流的 {@link InputStream}。
     * @param objectSerializer 表示对象序列化器的 {@link ObjectSerializer}。
     */
    public void load(InputStream in, ObjectSerializer objectSerializer) {
        Map<String, DocumentWithEmbedding> documents = objectSerializer.deserialize(in,
                TypeUtils.parameterized(Map.class, new Type[] {String.class, DocumentWithEmbedding.class}));
        this.cache.putAll(documents);
    }

    /**
     * 将数据从内存中保存到输出流中。
     *
     * @param out 表示输出流的 {@link OutputStream}。
     * @param objectSerializer 表示对象序列化器的 {@link ObjectSerializer}。
     */
    public void persist(OutputStream out, ObjectSerializer objectSerializer) {
        objectSerializer.serialize(this.cache, out);
    }

    /**
     * 表示携带嵌入向量的 {@link Document}。
     */
    private static class DocumentWithEmbedding implements Document {
        private String id;
        private String text;
        private Map<String, Object> metadata;
        private List<Float> embedding;

        /**
         * 从给定的 {@link Document} 和嵌入向量创建一个新的 {@link DocumentWithEmbedding}。
         *
         * @param document 表示原始文档的 {@link Document}。
         * @param embedding 表示嵌入向量的 {@link List}{@code <}{@link Double}{@code >}。
         * @return 表示创建成功文档的 {@link DocumentWithEmbedding}。
         */
        public static DocumentWithEmbedding from(Document document, List<Float> embedding) {
            if (document instanceof DocumentWithEmbedding) {
                return ObjectUtils.cast(document);
            }
            notEmpty(embedding, "The embedding cannot be empty.");
            DocumentWithEmbedding documentWithEmbedding = new DocumentWithEmbedding();
            documentWithEmbedding.setId(StringUtils.getIfBlank(document.id(), UuidUtils::randomUuidString));
            documentWithEmbedding.setText(notBlank(document.text(), "The document text cannot be blank."));
            documentWithEmbedding.setMetadata(notNull(document.metadata(), "The metadata cannot be null."));
            documentWithEmbedding.setEmbedding(embedding);
            return documentWithEmbedding;
        }

        @Nonnull
        @Override
        public String text() {
            return this.text;
        }

        @Override
        public List<Media> medias() {
            return Collections.emptyList();
        }

        @Override
        public String id() {
            return this.id;
        }

        @Nonnull
        @Override
        public Map<String, Object> metadata() {
            return this.metadata;
        }

        /**
         * 获取文档的唯一标识符。
         *
         * @return 表示文档唯一标识符的 {@link String}。
         */
        public String getId() {
            return id;
        }

        /**
         * 设置文档的唯一标识符。
         *
         * @param id 表示文档唯一标识符的 {@link String}。
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * 获取文档的文本内容。
         *
         * @return 表示文档文本内容的 {@link String}。
         */
        public String getText() {
            return text;
        }

        /**
         * 设置文档的文本内容。
         *
         * @param text 表示文档文本内容的 {@link String}。
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * 获取文档的元数据。
         *
         * @return 表示文档元数据的 {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}。
         */
        public Map<String, Object> getMetadata() {
            return metadata;
        }

        /**
         * 设置文档的元数据。
         *
         * @param metadata 表示文档元数据的 {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}。
         */
        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        /**
         * 获取文档的嵌入向量。
         *
         * @return 表示文档嵌入向量的 {@link List}{@code <}{@link Double}{@code >}。
         */
        public List<Float> getEmbedding() {
            return embedding;
        }

        /**
         * 设置文档的嵌入向量。
         *
         * @param embedding 表示文档嵌入向量的 {@link List}{@code <}{@link Double}{@code >}。
         */
        public void setEmbedding(List<Float> embedding) {
            this.embedding = embedding;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            DocumentWithEmbedding that = (DocumentWithEmbedding) object;
            return Objects.equals(this.id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id);
        }

        @Override
        public String toString() {
            return "DocumentWithEmbedding{" + "id='" + id + '\'' + ", text='" + text + '\'' + ", metadata=" + metadata
                    + ", embedding=" + embedding + '}';
        }
    }
}