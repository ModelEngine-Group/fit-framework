/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.enums;

import static modelengine.fit.waterflow.ErrorCodes.ENUM_CONVERT_FAILED;

import lombok.Getter;
import modelengine.fit.waterflow.exceptions.WaterflowParamException;

import java.util.Arrays;

/**
 * 流程定义对应状态
 *
 * @author 杨祥宇
 * @since 1.0
 */
@Getter
public enum FlowDefinitionStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String code;

    FlowDefinitionStatus(String code) {
        this.code = code;
    }

    /**
     * 根据状态码获取definition的状态
     *
     * @param code 状态码
     * @return 状态
     */
    public static FlowDefinitionStatus getFlowDefinitionStatus(String code) {
        return Arrays.stream(values())
                .filter(value -> value.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new WaterflowParamException(ENUM_CONVERT_FAILED, "FlowDefinitionStatus", code));
    }
}
