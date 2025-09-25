/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.entity.embed;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notEmpty;

import java.util.List;

/**
 * 表示 OpenAi API 格式的 嵌入请求。
 *
 * @author 易文渊
 * @author 张庭怿
 * @since 2024-4-30
 */
public class OpenAiEmbeddingRequest {
    private final String model;
    private final List<String> input;

    /**
     * 创建一个新的 OpenAi API 格式的 嵌入请求。
     *
     * @param input 表示输入的文本列表的 {@link List}{@code <}{@link String}{@code >}。
     * @param model 表示调用的模型名称的 {@link String}。
     * @throws IllegalArgumentException <ul>
     * <li>当 {@code model} 为空字符串时；</li>
     * <li>当 {@code input} 为空集合时。</li>
     * </ul>
     */
    public OpenAiEmbeddingRequest(List<String> input, String model) {
        notEmpty(input, "The input cannot be empty");
        this.model = notBlank(model, "The model cannot be blank.");
        this.input = input;
    }
}
