/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {hitRegion} from '../../../core/hitRegion.js';
import {EVENT_TYPE, FLOWABLE_STATE_STATUS} from '../../../common/const.js';


/**
 * flowable节点运行模式：自动，手动，定时
 * 辉子 2021
 */
const modeRegion = (state, getx, gety, getWidth, getHeight, index) => {
    let self = hitRegion(state, getx, gety, getWidth, getHeight, index);
    self.drawStatic = (context, x, y) => staticMethods[state.triggerMode + "StaticDraw"](state, context, x, y);
    let degree = 0;
    let step = Math.PI / 200;
    let drawDynamic = self.drawDynamic;
    self.drawDynamic = (context, x, y) => {
        drawDynamic.call(self, context, x, y);
        const R = 12;
        let x1 = x + R;
        let y1 = y + R;
        context.strokeStyle = state.getBorderColor();
        context.save();
        context.translate(x1, y1);
        context.rotate(degree);
        dynamicMethods[state.triggerMode + "DynamicDraw"](state, context, x, y);
        context.restore();
        degree += step;
        if (degree >= Math.PI * 2) {
            degree = 0;
        }
    };

    return self;
};

const taskStatRegion = (state, type, getx, gety, getWidth, getHeight, index) => {
    const statType = ['warningTask', 'runningTask', 'completedTask'];
    const statColor = ['red', 'blue', 'green'];
    const ind = statType.findIndex(i => i === type);
    if (ind === -1) {
      return undefined;
    }
    let self = hitRegion(state, getx, gety, getWidth, getHeight, index);
    self.type = type;
    self.drawStatic = (context, x, y, height, width) => {
        drawCountRegion(context, x, y, height, width, statColor[ind], state[type])
    }
    self.drawDynamic = undefined;
    return self;
}

const insertNodeRegion = (event, getx, gety, getWidth, getHeight, index) => {
    let self = hitRegion(event, getx, gety, getWidth, getHeight, index);
    self.type = "insert-node-button";
    self.visible = false;

    // 此热点的UI为蓝色背景的圆，中间是白色的+
    self.drawStatic = (context, x, y, height, width) => {
        // 让图像和点击热区重合
        if (event.from().x > event.to().x) {
            self.context.canvas.style.left = (x - getWidth() / 2 + event.from().x - event.to().x) + "px";
        }
        if (event.from().y > event.to().y) {
            self.context.canvas.style.top = (y - getHeight() / 2 + event.from().y - event.to().y) + "px";
        }
        drawRect(context, x, y, "#0478fc");
        context.beginPath();
        context.lineWidth = 5;
        const w = context.measureText("+").width;
        context.strokeStyle = "white"
        context.fillStyle = "white"
        context.font = (height + 2) + "px Ariel"
        context.fillText("+", x + width / 2 - w - 2, y + height - 5);
        context.fill();
        context.stroke();
    }
    self.drawDynamic = undefined;

    return self;
};

const agentRegion = (state, getx, gety, getWidth, getHeight, index) => {
    let self = hitRegion(state, getx, gety, getWidth, getHeight, index);
    self.type = "agentRegion";

    // 此热点的UI为黄色背景的矩形和白色字
    self.drawStatic = (context, x, y, height, width) => {
        drawRect(context, x, y, "#FFB142");
        context.fillStyle = "white";
        context.font = "12px Ariel";
        context.fillText("Agent", 4, 15, 16);
    }
    self.drawDynamic = undefined;

    return self;
};

const drawCountRegion = (context, x, y, height, width, color, num) =>{
    drawCircle(context, x, y, color);
    context.beginPath();
    context.lineWidth = 3;
    const w = context.measureText(num).width;
    context.strokeStyle = "white"
    context.fillStyle = "white"
    context.font = (height - 8) + "px Ariel"
    context.fillText(num, x + width / 2 - w + 1, y + height - 7);
    context.fill();
    context.stroke();
}

const drawCircle = (context, x, y, color) => {
    const r = 12
    let x2 = x + r;
    let y2 = y + r;
    context.beginPath();
    context.arc(x2, y2, r, 0, 2 * Math.PI);
    context.strokeStyle = color;
    context.fillStyle = color;
    context.fill();
    context.stroke();
};

const drawRect = (context, x, y, color) => {
    context.strokeStyle = color;
    context.fillStyle = color;
    context.beginPath();
    context.fillRect(x, y, 24, 24);
    context.fill();
    context.stroke();
};

const drawFrame = (context, x, y, state) => {
    const r = 12;
    let x2 = x + r;
    let y2 = y + r;
    context.lineWidth = 1;
    context.beginPath();
    context.arc(x2, y2, 10, 0, 2 * Math.PI);
    context.strokeStyle = "white";
    context.stroke();

    context.beginPath();
    context.arc(x2, y2, 9, 0, 2 * Math.PI);
    context.strokeStyle = state.getBorderColor();
    context.fillStyle = "white";
    context.fill();
    context.stroke();
};

const autoStaticDraw = (state, context, x, y) => drawFrame(context, x, y, state);

const autoDynamicDraw = (state, context, x, y) => {
    context.lineWidth = 2;
    context.beginPath();
    context.arc(0, 0, 5, 0, 0.7 * Math.PI);
    context.stroke();
    context.beginPath();
    context.arc(0, 0, 5, Math.PI, 1.7 * Math.PI);
    context.stroke();
};

const autoAsynStaticDraw = autoStaticDraw;
const autoAsynDynamicDraw = autoDynamicDraw;

const scheduleStaticDraw = (state, context, x, y) => {
    drawFrame(context, x, y, state);
    let x2 = x + 12;
    let y2 = y + 12;
    context.lineWidth = 1;
    context.beginPath();
    context.moveTo(x2, y2);
    context.lineTo(x2, y2 - 9);
    context.stroke();
};

const scheduleDynamicDraw = (state, context, x, y) => {
    let R1 = 6;
    context.beginPath();
    context.lineWidth = 1.5
    context.moveTo(0, 0);
    context.lineTo(R1, 0);
    context.stroke();
};

const manualStaticDraw = (state, context, x, y) => {
    drawFrame(context, x, y, state);
    const x1 = x + 7;
    const y1 = y + 9;
    context.beginPath();
    context.lineWidth = 3;
    context.strokeStyle = state.getBorderColor();
    context.moveTo(x1, y1);
    context.lineTo(x1 + 10, y1);
    context.moveTo(x1, y1 + 6);
    context.lineTo(x1 + 10, y1 + 6);
    context.stroke();
}
const manualDynamicDraw = (state, context, x, y) => {
};

const staticMethods = {};
staticMethods.autoStaticDraw = autoStaticDraw;
staticMethods.autoAsynStaticDraw = autoAsynStaticDraw;
staticMethods.scheduleStaticDraw = scheduleStaticDraw;
staticMethods.manualStaticDraw = manualStaticDraw;

const dynamicMethods = {};
dynamicMethods.autoDynamicDraw = autoDynamicDraw;
dynamicMethods.autoAsynDynamicDraw = autoAsynDynamicDraw;
dynamicMethods.scheduleDynamicDraw = scheduleDynamicDraw;
dynamicMethods.manualDynamicDraw = manualDynamicDraw;

export {
    modeRegion,
    taskStatRegion,
    manualDynamicDraw,
    manualStaticDraw,
    scheduleDynamicDraw,
    scheduleStaticDraw,
    autoDynamicDraw,
    autoStaticDraw,
    autoAsynStaticDraw,
    autoAsynDynamicDraw,
    insertNodeRegion,
    agentRegion
};