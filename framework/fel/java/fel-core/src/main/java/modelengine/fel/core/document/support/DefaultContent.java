/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support;

import static modelengine.fitframework.util.ObjectUtils.getIfNull;
import static modelengine.fitframework.util.StringUtils.blankIf;

import modelengine.fel.core.document.Content;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.resource.web.Media;
import modelengine.fitframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 表示消息内容的实体。
 *
 * @author 易文渊
 * @since 2024-06-17
 */
public final class DefaultContent implements Content {
    private final String text;
    private final List<Media> medias;

    /**
     * 使用文本消息和媒体资源创建 {@link Content} 的实例。
     *
     * @param text 表示文本消息的 {@link String}。
     * @param medias 表示媒体资源列表的 {@link List}{@code <}{@link Media}{@code >}。
     */
    public DefaultContent(String text, List<Media> medias) {
        this.text = blankIf(text, StringUtils.EMPTY);
        this.medias = getIfNull(medias, Collections::emptyList);
    }

    @Nonnull
    @Override
    public String text() {
        return this.text;
    }

    @Override
    public List<Media> medias() {
        return this.medias;
    }

    @Override
    public String toString() {
        return this.text;
    }
}