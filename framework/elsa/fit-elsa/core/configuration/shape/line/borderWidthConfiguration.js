/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {fieldConfiguration} from "../../fieldConfiguration.js";

/**
 * 边框宽度（borderWidth）配置.
 */
export const borderWidthConfiguration = (target) => {
    const self = fieldConfiguration(target, "borderWidth");
    self.type = "number";
    self.title = "粗细";
    self.index = 1;
    self.range = [0, 100];
    self.group.push({type: "line", name: "线条选项", index: 3});

    /**
     * @override
     */
    self.getValue = () => {
        if (target.isTypeof("line") || target.isTypeof("ellipse")) {
            return target.get("lineWidth");
        }
        return target.get(self.field);
    }

    /**
     * @inheritDoc
     * @override
     */
    self.getChangedData = (value) => {
        const data = {shape: target};

        // ellipse获取borderWidth
        const field = target.isTypeof("line") || target.isTypeof("ellipse") ? "lineWidth" : "borderWidth";
        data[field] = {};
        data[field].preValue = target.get(field);
        data[field].value = value;
        return [data];
    }

    return self;
}