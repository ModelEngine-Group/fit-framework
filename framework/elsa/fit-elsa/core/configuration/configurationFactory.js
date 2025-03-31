/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {xConfiguration} from "./shape/location/xConfiguration.js";
import {yConfiguration} from "./shape/location/yConfiguration.js";
import {widthConfiguration} from "./shape/appearance/widthConfiguration.js";
import {heightConfiguration} from "./shape/appearance/heightConfiguration.js";
import {rotateDegreeConfiguration} from "./shape/appearance/rotateDegreeConfiguration.js";
import {cornerRadiusConfiguration} from "./shape/appearance/cornerRadiusConfiguration.js";
import {globalAlphaConfiguration} from "./shape/appearance/globalAlphaConfiguration.js";
import {dashWidthConfiguration} from "./shape/line/dashWidthConfiguration.js";
import {borderWidthConfiguration} from "./shape/line/borderWidthConfiguration.js";
import {borderColorConfiguration} from "./shape/line/borderColorConfiguration.js";
import {backColorConfiguration} from "./shape/appearance/backColorConfiguration.js";
import {beginArrowConfiguration} from "./shape/line/beginArrowConfiguration.js";
import {endArrowConfiguration} from "./shape/line/endArrowConfiguration.js";
import {tagConfiguration} from "./shape/other/tagConfiguration.js";

/**
 * 配置工厂类.
 *
 * @return {{}} 配置工厂类对象.
 */
export const configurationFactory = () => {
    const configMap = new Map();
    configMap.set("x", xConfiguration);
    configMap.set("y", yConfiguration);
    configMap.set("width", widthConfiguration);
    configMap.set("height", heightConfiguration);
    configMap.set("rotateDegree", rotateDegreeConfiguration);
    configMap.set("cornerRadius", cornerRadiusConfiguration);
    configMap.set("globalAlpha", globalAlphaConfiguration);
    configMap.set("dashWidth", dashWidthConfiguration);
    configMap.set("borderWidth", borderWidthConfiguration);
    configMap.set("borderColor", borderColorConfiguration);
    configMap.set("backColor", backColorConfiguration);
    configMap.set("beginArrow", beginArrowConfiguration);
    configMap.set("endArrow", endArrowConfiguration);
    configMap.set("tag", tagConfiguration);

    const self = {};

    /**
     * 获取配置信息.
     *
     * @param field 字段.
     * @param target 目标对象.
     * @return {*} 配置信息.
     */
    self.get = (field, target) => {
        const configuration = configMap.get(field);
        return configuration ? configuration(target) : null;
    }

    return self;
}