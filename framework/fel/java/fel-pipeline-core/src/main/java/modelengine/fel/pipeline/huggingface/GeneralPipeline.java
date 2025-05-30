/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface;

import modelengine.fel.pipeline.Pipeline;
import modelengine.fel.service.pipeline.HuggingFacePipelineService;
import modelengine.fitframework.inspection.Validation;

import java.util.Map;

/**
 * 表示 huggingface pipeline 的泛化实现。
 * <p>返回结果取决于任务类型，可能是以下值中的一个：
 * <ul>
 *     <li>{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。</li>
 *     <li>{@link java.util.List}{@code <}{@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >>}。</li>
 * </ul>
 * </p>
 *
 * @author 易文渊
 * @since 2024-06-04
 */
public class GeneralPipeline implements Pipeline<Map<String, Object>, Object> {
    private final String taskId;
    private final String model;

    private final HuggingFacePipelineService service;

    /**
     * 创建泛化流水线的实例。
     *
     * @param task 表示任务类型的 {@link PipelineTask}。
     * @param model 表示模型名的 {@link String}。
     * @param service 表示提供 pipeline 服务的 {@link HuggingFacePipelineService}。
     * @throws IllegalArgumentException <ul>
     * <li>当 {@code task} 为 {@code null} 时。</li>
     * <li>当 {@code service} 为 {@code null} 时。</li>
     * </ul>
     */
    public GeneralPipeline(PipelineTask task, String model, HuggingFacePipelineService service) {
        this.taskId = Validation.notNull(task, "The task cannot be null.").getId();
        this.model = Validation.notBlank(model, "The model cannot be blank.");
        this.service = Validation.notNull(service, "The service cannot be null.");
    }

    @Override
    public Object apply(Map<String, Object> args) {
        return this.service.call(this.taskId, this.model, args);
    }
}