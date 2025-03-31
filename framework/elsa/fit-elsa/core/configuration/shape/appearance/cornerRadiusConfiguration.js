/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {fieldConfiguration} from "../../fieldConfiguration.js";

/**
 * 角半径（cornerRadius）配置.
 */
export const cornerRadiusConfiguration = (target) => {
    const self = fieldConfiguration(target, "cornerRadius");
    self.type = "number";
    self.title = "圆角";
    self.range = [0, Math.floor(Math.min(target.width, target.height) / 2)];
    self.index = 3;
    self.group.push({type: "appearance", name: "形状选项", index: 2});
    return self;
}