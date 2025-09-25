/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.enums;

/**
 * 模型内容生成状态枚举类。
 *
 * @author 孙怡菲
 * @since 2025-04-29
 */
public enum ModelProcessingState {
    /**
     * 表示初始状态。
     */
    INITIAL,

    /**
     * 表示内部推理状态。
     */
    THINKING,

    /**
     * 表示结果生成状态。
     */
    RESPONDING
}
