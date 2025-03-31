/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {node} from './node.js';
import {canvasDrawer} from "../../../core/drawers/canvasDrawer.js";
import {ALIGN} from "../../../common/const.js";

/**
 * 条件节点
 * 辉子 2020
 */
const condition = (id, x, y, width, height, parent, drawer = canvasDrawer) => {
    let self = node(id, x, y, width, height, parent, false, drawer);
    self.type = "condition";
    self.text = "?";
    self.autoWidth = true;
    self.autoHeight = true;
    self.fontSize = 14;
    self.height = 40;
    self.width = self.minWidth = 20;
    self.maxWidth = 150;
    self.borderWidth = 0;
    self.vAlign = ALIGN.MIDDLE;

    self.drawer.drawStatic = (context, x, y) => {
        context.beginPath();
        context.fillStyle = self.getBackColor();
        context.strokeStyle = self.borderColor;
        context.lineWidth = self.borderWidth;
        context.moveTo(x + self.width / 2, y);
        context.lineTo(x + self.width, y + self.height / 2);
        context.lineTo(x + self.width / 2, y + self.height);
        context.lineTo(x, y + self.height / 2);
        context.fill();
        context.closePath();
        context.stroke();
    };

    /**
     * 这里condition在计算width的时候要重写，改变宽和高使其边框可完全包含text。
     *
     * @param shape 表示condition图形。
     */
    self.drawer.calculateWidth = (shape) => {
        if (!shape.autoWidth || shape.drawer.text.offsetWidth === 0) {
            return Math.abs(shape.width);
        }
        let tWidth = shape.drawer.text.offsetWidth;
        let tHeight = shape.drawer.text.offsetHeight;
        shape.width = tWidth + tHeight;
        shape.height = tWidth + tHeight;
        return shape.width;
    }

    /**
     * 这里在resize后，需要把condition中的text向右平移使文字居中
     */
    const resize = self.drawer.resize;
    self.drawer.resize = () => {
        let size = resize.apply(self);
        self.drawer.text.style.left = (size.width - self.drawer.text.offsetWidth) / 2 + "px";
        self.drawer.text.style.position = "absolute";
    }

    return self;
};

export {condition};