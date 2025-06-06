/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.service.pipeline;

import modelengine.fitframework.annotation.Genericable;

import java.util.Map;

/**
 * 表示 pipeline 推理服务。
 *
 * @author 易文渊
 * @since 2024-06-03
 */
public interface HuggingFacePipelineService {
    /**
     * 调用 HuggingFace pipeline 生成结果。
     * <p>返回结果取决于任务类型，可能是以下值中的一个：
     * <ul>
     * <li>{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。</li>
     * <li>{@link java.util.List}{@code <}{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >>}。</li>
     * </ul>
     * </p>
     *
     * @param task 表示任务类型的 {@link String}。
     * @param model 表示模型名的 {@link String}。
     * @param args 表示调用参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @return 表示生成结果的 {@link Object}。
     */
    @Genericable("modelengine.fel.pipeline.huggingface")
    Object call(String task, String model, Map<String, Object> args);
}