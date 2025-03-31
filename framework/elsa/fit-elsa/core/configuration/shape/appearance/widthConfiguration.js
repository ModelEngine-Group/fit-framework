/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {fieldConfiguration} from "../../fieldConfiguration.js";
import {resizeCommand} from "../../../commands.js";

/**
 * 宽度（width）配置.
 */
export const widthConfiguration = (target) => {
    const self = fieldConfiguration(target, "width");
    self.type = "number";
    self.title = "宽度";
    self.index = 0;
    self.group.push({type: "appearance", name: "形状选项", index: 2});

    /**
     * @inheritDoc
     * @override
     */
    self.doCommand = (changedData) => {
        resizeCommand(target.page, changedData).execute(target.page, "value");
    }

    /**
     * @inheritDoc
     * @override
     */
    self.getChangedData = (value) => {
        const shape = target;
        let dirty = {
            shape,
            x: {},
            y: {},
            width: {},
            height: {},
            rotateDegree: {},
            ox: {},
            oy: {},
            fromShape: {},
            toShape: {},
            fromConn: {},
            toConn: {}
        };
        dirty.x.preValue = shape.x;
        dirty.y.preValue = shape.y;
        dirty.width.preValue = shape.width;
        dirty.height.preValue = shape.height;
        if (shape.textConnector === self) {
            dirty.ox.preValue = dirty.ox.value = shape.textConnector.ox;
            dirty.oy.preValue = dirty.oy.value = shape.textConnector.oy;
        }
        if (shape.rotateConnector === self) {
            dirty.rotateDegree.preValue = shape.get("rotateDegree");
        }

        //history
        dirty.x.value = shape.x;
        dirty.y.value = shape.y;
        dirty.width.value = shape.width;
        dirty.height.value = shape.height;
        if (shape.textConnector === self) {
            dirty.ox.value = shape.textConnector.ox;
            dirty.oy.value = shape.textConnector.oy;
        }
        if (shape.rotateConnector === self) {
            dirty.rotateDegree.value = shape.rotateDegree;
        }
        dirty[self.field].value = value;
        return [dirty];
    }
    return self;
}