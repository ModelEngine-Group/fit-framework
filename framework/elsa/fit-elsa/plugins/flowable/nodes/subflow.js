/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {node} from './node.js';
import {FLOWABLE_TRIGGER_MODE} from '../../../common/const.js';
import {crossSenderDrawer} from '../drawers/drawer.js';

/**
 * 数据输出节点
 * 数据由该节点输出到其他flowable中的任意节点
 * huiz 2020
 */
const crossSender = (id, x, y, width, height, parent) => {
    const WIDTH = 50;
    let self = node(id, x, y, width, height, parent, false, crossSenderDrawer);
    self.type = "crossSender";
    self.text = "";
    self.hideText = true;
    self.backColor = "white";
    self.borderColor = self.fontColor = "darkorange";
    self.dashWidth = 6;
    self.cornerRadius = 6;
    self.autoWidth = false;
    self.width = WIDTH;
    self.height = WIDTH;
    self.triggerMode = FLOWABLE_TRIGGER_MODE.AUTO;
    self.target = "";
    self.dashWidth = 6;
    self.cornerRadius = 6;
    self.indirect = true;

    //self.statusRegion.visible = 
    self.modeRegion.visible = false;

    let ox = 0;
    self.drawer.drawDynamic = (context, x, y) => {
        const x1 = ox + x - 10;
        const y1 = y;
        const l = 5;
        const w = 15;
        context.save();
        // context.rect(-w,-w,2*w,2*w);
        context.rect(0, -w, w, 2 * w);
        context.clip();
        context.beginPath();
        context.moveTo(x1, y1 - l);
        context.lineTo(x1 + 1.8 * l, y1 - l);
        context.lineTo(x1 + 1.8 * l, y1 - 2 * l);
        context.lineTo(x1 + 3.2 * l, y1);
        context.lineTo(x1 + 1.8 * l, y1 + 2 * l);
        context.lineTo(x1 + 1.8 * l, y1 + l);
        context.lineTo(x1, y1 + l);
        context.closePath();
        context.fillStyle = self.getBorderColor();
        context.strokeStyle = "white";
        context.fill();
        context.stroke();
        context.restore();
        ox += 0.1;
        if (ox > 30) {
            ox = 0;
        }
    };
    return self;
};

export {crossSender};