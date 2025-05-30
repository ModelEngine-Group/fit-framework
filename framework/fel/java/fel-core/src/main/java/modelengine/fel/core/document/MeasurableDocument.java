/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.util.UuidUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 表示可量化比较的 {@link Document}。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public class MeasurableDocument implements Document, Measurable {
    private final String id;
    private final String text;
    private final String groupId;
    private final double score;
    private final Map<String, Object> metadata;
    private final List<Media> medias;

    /**
     * 创建 {@link MeasurableDocument} 的实体。
     *
     * @param document 表示原始文档的 {@link Document}。
     * @param score 表示文档评分的 {@code double}。
     * @throws IllegalArgumentException 当 {@code document} 为 {@code null} 时。
     */
    public MeasurableDocument(Document document, double score) {
        this(document, score, UuidUtils.randomUuidString());
    }

    /**
     * 创建 {@link MeasurableDocument} 的实体。
     *
     * @param document 表示原始文档的 {@link Document}。
     * @param score 表示文档评分的 {@code double}。
     * @param groupId 表示文档的分组标识的 {@link String}。
     * @throws IllegalArgumentException 当 {@code document} 为 {@code null} 时。
     */
    public MeasurableDocument(Document document, double score, String groupId) {
        notNull(document, "The document cannot be null.");
        this.id = document.id();
        this.text = document.text();
        this.score = score;
        this.groupId = Validation.notBlank(groupId, "The groupId cannot be null.");
        this.metadata = document.metadata();
        this.medias = document.medias();
    }

    @Override
    @Nonnull
    public String text() {
        return this.text;
    }

    @Override
    public List<Media> medias() {
        return this.medias;
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

    @Override
    public double score() {
        return this.score;
    }

    @Override
    @Nonnull
    public String group() {
        return this.groupId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MeasurableDocument that = (MeasurableDocument) object;
        return Double.compare(this.score, that.score) == 0 && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.score);
    }

    @Override
    public String toString() {
        return "MeasurableDocument{" + "id='" + this.id + '\'' + ", text='" + this.text + '\'' + ", groupId='"
                + this.groupId + '\'' + ", score=" + this.score + ", metadata=" + this.metadata + ", medias="
                + this.medias + '}';
    }
}