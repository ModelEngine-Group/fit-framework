/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {fieldConfiguration} from "../../fieldConfiguration.js";

/**
 * 线段终点的类型（endArrow）配置.
 */
export const endArrowConfiguration = (target) => {
    const self = fieldConfiguration(target, "endArrow");
    self.type = "string";
    self.title = "终点";
    self.index = 4;
    self.group.push({type: "line", name: "线条选项", index: 3});
    self.options = [];
    self.options.push({key: "endArrow.none", value: 'none'});
    self.options.push({key: "endArrow.defaultArrow", value: 'defaultArrow'});

    /**
     * @inheritDoc
     * @override
     */
    const action = self.action;
    self.action = (value) => {
        action.apply(self, [value !== "none"]);
    }

    /**
     * 重写getValue方法.
     * 属性beginArrow为true,返回"defaultArrow".
     * 属性beginArrow为false,返回"none".
     *
     * @override
     */
    const getValue = self.getValue;
    self.getValue = () => {
        const value = getValue.apply(self);
        return value ? "defaultArrow" : "none";
    }

    return self;
}