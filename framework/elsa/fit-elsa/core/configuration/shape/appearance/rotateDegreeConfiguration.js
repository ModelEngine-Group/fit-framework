/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {fieldConfiguration} from "../../fieldConfiguration.js";

/**
 * 旋转角度（rotateDegree）配置.
 */
export const rotateDegreeConfiguration = (target) => {
    const self = fieldConfiguration(target, "rotateDegree");
    self.type = "number";
    self.title = "旋转角度";
    self.range = [-360, 360];
    self.index = 2;
    self.group.push({type: "appearance", name: "形状选项", index: 2});
    return self;
}