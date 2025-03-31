/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {fieldConfiguration} from "../../fieldConfiguration.js";

const DEFAULT_VALUE = 100;
const FACTOR = 100;

/**
 * 透明度（globalAlpha）配置.
 */
export const globalAlphaConfiguration = (target) => {
    const self = fieldConfiguration(target, "globalAlpha");
    self.type = "number";
    self.title = "透明度";
    self.index = 4;
    self.range = [0, 100];
    self.group.push({type: "appearance", name: "形状选项", index: 2});

    /**
     * @inheritDoc
     * @override
     */
    const action = self.action;
    self.action = (value) => {
        if (value === "" || value === null || isNaN(value)) {
            throw new Error("globalAlpha must be a number.");
        }
        action.apply(self, [(100 - value) / 100]);
    }

    /**
     * 重写getValue方法.
     * 1、若value存在，需要乘100,
     * 2、若value不存在，直接返回.
     *
     * @override
     */
    const getValue = self.getValue;
    self.getValue = () => {
        const value = getValue.apply(self);
        return value ? (1 - value) * FACTOR : DEFAULT_VALUE;
    }
    return self;
}