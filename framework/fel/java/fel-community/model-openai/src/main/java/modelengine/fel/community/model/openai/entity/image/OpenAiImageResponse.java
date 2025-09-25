/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.image;

import modelengine.fitframework.resource.web.Media;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示 OpenAi API 格式的图片生成响应。
 *
 * @author 何嘉斌
 * @since 2024-12-17
 */
public class OpenAiImageResponse {
    /**
     * 模型生成的 Image 列表。
     */
    private List<OpenAiImage> data;

    /**
     * 获取模型生成的图片列表。
     *
     * @return 表示模型嵌入向量列表的 {@link List}{@code <}{@link Media}{@code >}。
     */
    public List<Media> media() {
        return this.data.stream().map(OpenAiImage::media).collect(Collectors.toList());
    }
}