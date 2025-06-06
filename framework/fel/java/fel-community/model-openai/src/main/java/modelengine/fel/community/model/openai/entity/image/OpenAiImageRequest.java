/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.image;

import static modelengine.fitframework.inspection.Validation.notBlank;

/**
 * 表示 OpenAi Api 格式的图片生成请求。
 *
 * @author 何嘉斌
 * @since 2024-12-17
 */
public class OpenAiImageRequest {
    private final String model;
    private final String size;
    private final String prompt;

    /**
     * 创建一个新的 OpenAi API 格式的图片生成请求。
     *
     * @param model 表示调用的模型名称的 {@link String}。
     * @param size 表示生成图片规格的 {@link String}。
     * @param prompt 表示用户输入提示词的 {@link String}。
     */
    public OpenAiImageRequest(String model, String size, String prompt) {
        this.model = notBlank(model, "The model cannot be blank.");
        this.size = notBlank(size, "The image size cannot be blank.");
        this.prompt = notBlank(prompt, "The prompt cannot be blank.");
    }
}