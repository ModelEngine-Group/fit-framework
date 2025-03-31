/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { canvasDrawer } from '../../../core/drawers/canvasDrawer.js';
/**
 * flowable起始节点画笔
 * 辉子 2021
 */
export const startDrawer = (shape, div, x, y) => {
    let self = canvasDrawer(shape, div, x, y);
    self.text.style.left = "-1px";
    self.text.style.top = "40px";
    self.drawStatic = (context, x, y) => {
        const OFFSET = 1;
        context.globalAlpha = 1;
        context.dynamicEllipse(x + OFFSET - 1, y + OFFSET - 1, shape.width - 2 * OFFSET, shape.height - 2 * OFFSET, 2, "whitesmoke", shape.getBorderColor(), 1);
    };
    self.drawFocusFrame = (context, x, y) => {
        let pad = 0;
        let x1 = x - pad - shape.width / 2;
        let y1 = y - pad - shape.height / 2;
        context.dynamicEllipse(x1, y1, shape.width, shape.height, 1, "silver", "", 0, 3, 1);
    };
    return self;
};

/**
 * flowable结束节点画笔
 * 辉子 2021
 */
export const endDrawer = (shape, div, x, y) => {
    let self = startDrawer(shape, div, x, y);
    self.text.style.left = "3px";
    self.text.style.top = "40px";
    let drawStatic = self.drawStatic;
    self.drawStatic = (context, x, y) => {
        drawStatic.apply(self, [context, x, y]);
        const OFFSET = 5;
        context.dynamicEllipse(x + OFFSET - 1, y + OFFSET - 1, shape.width - 2 * OFFSET, shape.height - 2 * OFFSET, 2, "white", "", 2);
    };
    return self;
};

/**
 * flowable跳转节点（子工作流节点）画笔
 * 辉子 2021
 */
export const crossSenderDrawer = (shape, div, x, y) => {
    let self = canvasDrawer(shape, div, x, y);
    self.drawStatic = (context, x, y) => {
        const OFFSET = 7;
        const BORDER = 4;
        context.globalAlpha = 1;
        context.dynamicRect(x + OFFSET + 1, y + OFFSET + 1, shape.width - 2 * OFFSET - BORDER, shape.height - 2 * OFFSET - BORDER, BORDER, shape.getBorderColor(), "", 5);
    };
    return self;
};

export const linkerDrawer = (shape, div, x, y) => {
    let self = canvasDrawer(shape, div, x, y);
    self.displayBackground = true;
    self.drawStatic = (context, x, y) => {
    };
    return self;
};