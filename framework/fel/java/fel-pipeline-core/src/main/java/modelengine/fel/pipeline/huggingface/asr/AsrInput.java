/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface.asr;

import lombok.Data;
import modelengine.fel.pipeline.PipelineInput;
import modelengine.fitframework.annotation.Property;

import java.util.Map;

/**
 * 表示语音识别任务的输入参数。
 *
 * @author 易文渊
 * @since 2024-06-04
 */
@Data
public class AsrInput implements PipelineInput {
    /**
     * 表示音频文件的公共 URL 地址的 {@link String}。
     */
    private String inputs;

    /**
     * 表示是否返回文本中每个单词的时间戳的 {@link Boolean}。
     * <p>仅适用于以下模型，不适用于其他模型：
     * <ul>
     *      <li>纯 CTC 模型（Wav2Vec2、HuBERT 等）。</li>
     *      <li>Whisper 模型。</li>
     * </ul>
     * </p>
     */
    @Property(name = "return_timestamps")
    private Boolean returnTimestamps;

    /**
     * 表示用于模型生成的超参数的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    @Property(name = "generate_kwargs")
    private Map<String, Object> generateKwargs;

    /**
     * 表示生成的最大令牌数的 {@link Integer}。
     */
    @Property(name = "max_new_tokens")
    private Integer maxNewTokens;
}