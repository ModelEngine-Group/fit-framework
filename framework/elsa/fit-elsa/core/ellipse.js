/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, ELSA_NAME_SPACE} from '../common/const.js';
import {rectangle} from './rectangle.js';
import {canvasRectangleDrawer} from './drawers/rectangleDrawer.js';

export const namespace = ELSA_NAME_SPACE;
/**
 * 椭圆
 * 目前是最简单的椭圆，留待未来发挥
 * 辉子 2020
 */
const ellipse = (id, x, y, width, height, parent) => {
    let self = rectangle(id, x, y, width, height, parent, canvasRectangleDrawer);
    // self.type = "ellipse";
    self.text = "";
    self.lineWidth = 1;
    self.borderWidth = 0;
    //self.borderWidth = self.borderWidth1 = 2;
    self.vAlign = ALIGN.MIDDLE;

    self.drawer.drawBorder = () => {
    }

    self.drawer.drawStatic = (context, x, y) => {
        context.dynamicEllipse(x, y, self.width, self.height, self.lineWidth, self.emphasized ? "red" : self.borderColor,
            self.backColor, self.backAlpha, self.dashWidth, self.globalAlpha);

    };
    let degree = 0;
    self.drawer.drawDynamic = (context, x, y) => {
        if (!self.emphasized) {
            return;
        }
        self.width > self.height ? self.width = self.height : self.height = self.width;
        context.save();
        //context.translate(self.width/2,self.height/2);
        context.rotate(degree);
        context.dynamicEllipse(-self.width / 2 - self.borderWidth / 2,
            -self.height / 2 - self.borderWidth / 2,
            self.width,
            self.height,
            self.borderWidth,
            self.getBorderColor(),
            self.getBackColor(),
            self.backAlpha,
            2,
            1);
        context.restore();
        degree += 0.002;
        if (degree > 360) {
            degree = 0;
        }
    };

    // self.serializedFields.batchAdd("lineWidth");
    return self;
};

export {ellipse};