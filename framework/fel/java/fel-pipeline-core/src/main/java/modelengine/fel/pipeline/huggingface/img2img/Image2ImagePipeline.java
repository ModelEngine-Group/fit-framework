/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface.img2img;

import modelengine.fel.pipeline.huggingface.ExplicitPipeline;
import modelengine.fel.pipeline.huggingface.PipelineTask;
import modelengine.fel.service.pipeline.HuggingFacePipelineService;
import modelengine.fitframework.resource.web.Media;

import java.util.List;

/**
 * 表示 {@link PipelineTask#IMAGE_TO_IMAGE} 任务的流水线。
 *
 * @author 易文渊
 * @since 2024-06-06
 */
public class Image2ImagePipeline extends ExplicitPipeline<Image2ImageInput, List<Media>> {
    /**
     * 创建图生图流水线的实例。
     *
     * @param model 表示模型名的 {@link String}。
     * @param service 表示提供 pipeline 服务的 {@link HuggingFacePipelineService}。
     */
    public Image2ImagePipeline(String model, HuggingFacePipelineService service) {
        super(PipelineTask.IMAGE_TO_IMAGE, model, service);
    }
}