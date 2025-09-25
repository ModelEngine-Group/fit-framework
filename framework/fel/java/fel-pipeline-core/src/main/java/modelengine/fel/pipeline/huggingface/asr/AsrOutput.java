/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface.asr;

import lombok.Data;

import java.util.List;

/**
 * 表示语音识别任务的输出参数。
 *
 * @author 易文渊
 * @since 2024-06-04
 */
@Data
public class AsrOutput {
    /**
     * 表示被识别的文本的 {@link String}。
     */
    private String text;

    /**
     * 表示包含时间戳的文本片段集合的 {@link List}{@code <}{@link AsrOutputChunk}{@code >}。
     * <p>当 {@link AsrInput#getReturnTimestamps()} 为 {@code true} 时生效。</p>
     */
    private List<AsrOutputChunk> chunks;
}