/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.document.support.postprocessor;

/**
 * RRF 算法 score 选择策略。
 *
 * @author 马朝阳
 * @since 2024-09-29
 */
public enum RrfScoreStrategyEnum {
    /**
     * 相同文档的分数取最大值。
     */
    MAX,

    /**
     * 相同文档的分数取平均值。
     */
    AVG;
}
