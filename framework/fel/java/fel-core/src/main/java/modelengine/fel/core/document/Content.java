/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document;

import modelengine.fel.core.document.support.DefaultContent;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.resource.web.Media;

import java.util.Arrays;
import java.util.List;

/**
 * 表示携带数据的上下文实体。
 *
 * @author 易文渊
 * @since 2024-08-06
 */
public interface Content {
    /**
     * 使用媒体资源创建 {@link Content} 的实例。
     *
     * @param medias 表示 {@code 0..n} 个媒体资源的 {@code Media[]}。
     * @return 表示创建成功的 {@link Content}。
     */
    static Content from(Media... medias) {
        return new DefaultContent(null, Arrays.asList(medias));
    }

    /**
     * 使用文本消息和媒体资源创建 {@link Content} 的实例。
     *
     * @param text 表示文本消息的 {@link String}。
     * @param medias 表示 {@code 0..n} 个媒体资源的 {@code Media[]}。
     * @return 表示创建成功的 {@link Content}。
     */
    static Content from(String text, Media... medias) {
        return new DefaultContent(text, Arrays.asList(medias));
    }

    /**
     * 获取文本内容。
     *
     * @return 表示拼接后文本内容的 {@link String}。
     */
    @Nonnull
    String text();

    /**
     * 获取媒体内容。
     *
     * @return 表示媒体内容的 {@link Media}。
     */
    List<Media> medias();
}