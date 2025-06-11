/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.enums;

import static java.util.Locale.ROOT;
import static modelengine.fit.waterflow.ErrorCodes.ENUM_CONVERT_FAILED;

import lombok.Getter;
import modelengine.fit.waterflow.exceptions.WaterflowParamException;

import java.util.Arrays;

/**
 * 流程定义节点类型
 *
 * @author 高诗意
 * @since 1.0
 */
@Getter
public enum FlowNodeType {
    /**
     * 开始节点
     */
    START("START", false),

    /**
     * 状态节点
     */
    STATE("STATE", false),

    /**
     * 条件节点
     */
    CONDITION("CONDITION", false),

    /**
     * 并行节点
     */
    PARALLEL("PARALLEL", false),

    /**
     * 分支节点
     */
    FORK("FORK", true),

    /**
     * 合并节点
     */
    JOIN("JOIN", true),

    /**
     * 事件节点
     */
    EVENT("EVENT", true),

    /**
     * 结束节点
     */
    END("END", false);

    private final String code;

    private final boolean subNode;

    FlowNodeType(String code, boolean subNode) {
        this.code = code;
        this.subNode = subNode;
    }

    /**
     * getNodeType
     *
     * @param code code
     * @return FlowNodeType
     */
    public static FlowNodeType getNodeType(String code) {
        return Arrays.stream(values())
                .filter(value -> code.toUpperCase(ROOT).endsWith(value.getCode()))
                .findFirst()
                .orElseThrow(() -> new WaterflowParamException(ENUM_CONVERT_FAILED, "FlowNodeType", code));
    }
}