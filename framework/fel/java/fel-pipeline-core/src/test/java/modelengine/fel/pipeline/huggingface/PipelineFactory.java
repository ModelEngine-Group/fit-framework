/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface;

import modelengine.fel.pipeline.Pipeline;
import modelengine.fel.pipeline.huggingface.asr.AsrPipeline;
import modelengine.fel.pipeline.huggingface.img2img.Image2ImagePipeline;
import modelengine.fel.pipeline.huggingface.text2img.Text2ImagePipeline;
import modelengine.fel.pipeline.huggingface.tts.TtsPipeline;
import modelengine.fel.service.pipeline.HuggingFacePipelineService;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * 表示 pipeline 工厂。
 *
 * @author 易文渊
 * @since 2024-06-07
 */
public class PipelineFactory {
    private static final Map<String, Class<?>> PIPELINE_CLAZZ = MapBuilder.<String, Class<?>>get()
            .put(PipelineTask.AUTOMATIC_SPEECH_RECOGNITION.getId(), AsrPipeline.class)
            .put(PipelineTask.TEXT_TO_SPEECH.getId(), TtsPipeline.class)
            .put(PipelineTask.IMAGE_TO_IMAGE.getId(), Image2ImagePipeline.class)
            .put(PipelineTask.TEXT_TO_IMAGE.getId(), Text2ImagePipeline.class)
            .build();

    /**
     * 创建 pipeline 实例。
     *
     * @param task 表示任务类型的 {@link PipelineTask}。
     * @param model 表示模型名的 {@link String}。
     * @param service 表示提供 pipeline 服务的 {@link HuggingFacePipelineService}。
     * @return 表示创建流水线实例的 {@link Pipeline}。
     */
    public static Pipeline create(String task, String model, HuggingFacePipelineService service) {
        Class<?> clazz = PIPELINE_CLAZZ.get(task);
        Validation.notNull(clazz, "The task '{0}' class cannot be null.", task);
        Constructor<?> constructor =
                ReflectionUtils.getDeclaredConstructor(clazz, String.class, HuggingFacePipelineService.class);
        return ObjectUtils.cast(ReflectionUtils.instantiate(constructor, model, service));
    }
}