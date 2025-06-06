/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.pipeline.huggingface;

import lombok.Data;

/**
 * 表示 pipline 测试用例。
 *
 * @author 易文渊
 * @since 2024-06-07
 */
@Data
public class PipelineTestCase {
    private String task;
    private String model;
    private Object input;
    private Object output;
}