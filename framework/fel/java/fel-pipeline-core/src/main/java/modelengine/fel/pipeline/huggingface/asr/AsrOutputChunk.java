/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface.asr;

import lombok.Data;

import java.util.List;

/**
 * 表示包含时间戳的文本片段。
 * <pre>
 *     {
 *         "text": "hi",
 *         "timestamp": [
 *              0.5,
 *              0.9
 *         ]
 *     }
 * </pre>
 *
 * @author 易文渊
 * @since 2024-06-04
 */
@Data
public class AsrOutputChunk {
    /**
     * 表示文本片段的 {@link String}。
     */
    private String text;

    /**
     * 表示时间区间的 {@link List}{@code <}{@link Double}{@code >}。
     */
    private List<Double> timestamp;
}