/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface.tts;

import lombok.Data;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.resource.web.Media;

/**
 * 表示语音合成任务的输出参数。
 *
 * @author 易文渊
 * @since 2024-06-05
 */
@Data
public class TtsOutput {
    /**
     * 表示输出音频的 {@link Media}。
     */
    private Media audio;

    @Property(name = "sampling_rate")
    private Integer samplingRate;
}