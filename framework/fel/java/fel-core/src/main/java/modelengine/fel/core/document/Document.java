/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document;

import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.pattern.builder.BuilderFactory;
import modelengine.fitframework.resource.web.Media;

import java.util.List;
import java.util.Map;

/**
 * 表示检索文档的实体。
 *
 * @author 刘信宏
 * @author 易文渊
 * @since 2024-06-13
 */
public interface Document extends Content {
    /**
     * 获取文档的唯一编号。
     *
     * @return 表示文档唯一编号的 {@link String}。
     */
    String id();

    /**
     * 获取文档的元数据。
     *
     * @return 表示文档元数据的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    @Nonnull
    Map<String, Object> metadata();

    /**
     * 表示 {@link Document} 的构建器。
     */
    interface Builder {
        /**
         * 设置文档的唯一编号。
         *
         * @param id 表示文档唯一编号的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder id(String id);

        /**
         * 设置文档的内容。
         *
         * @param text 表示文档内容的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder text(String text);

        /**
         * 设置文档的多媒体内容。
         *
         * @param medias 表示文档多媒体内容的 {@link List}{@code <}{@link Media}{@code >}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder medias(List<Media> medias);

        /**
         * 设置文档的元数据。
         *
         * @param metadata 表示文档元数据的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder metadata(Map<String, Object> metadata);

        /**
         * 构建 {@link Document} 的实例。
         *
         * @return 表示创建成功的 {@link Document}。
         */
        Document build();
    }

    /**
     * 创建 {@link Builder} 的实例。
     *
     * @return 表示创建成功的 {@link Builder}。
     */
    static Builder custom() {
        return BuilderFactory.get(Document.class, Builder.class).create(null);
    }
}
