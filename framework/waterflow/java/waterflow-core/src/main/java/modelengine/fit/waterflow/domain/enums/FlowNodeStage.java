/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.enums;

/**
 * 流程节点的阶段信息
 *
 * @author songyongtan
 * @since 2024/10/17
 */
public enum FlowNodeStage {
    /**
     * 节点调用前
     */
    BEFORE,

    /**
     * 节点调用后
     */
    AFTER;
}
