/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {canvasDrawer} from './canvasDrawer.js';
import {drawer} from './htmlDrawer.js';

/**
 * 绘制发光的边缘
 * 辉子 2021
 */
const emphasizeShine = (context, x, y, shape, control) => {
  let xVal = x;
  let yVal = y;
  xVal -= shape.width / 2;
  yVal -= shape.height / 2;
    const STEP = 0.01
    let DIRECTION = { VERTICAL: 1, HORIZONTAL: 0 }
    let drawShine = function (context, x, y, direction, lineLen) {
        let x1;
        let y1;
        let x2;
        let y2;
        if (direction === DIRECTION.HORIZONTAL) {
            x1 = x - lineLen / 2;
            x2 = x + lineLen / 2;
            y1 = y2 = y;
        } else {
            x1 = x2 = x;
            y1 = y - lineLen / 2;
            y2 = y + lineLen / 2;
        }
        let gradient = context.createLinearGradient(x1, y1, x2, y2);
        gradient.addColorStop(0, "rgba(255,255,255,0");
        let color1 = "sandybrown";

        gradient.addColorStop(0.5, color1);
        gradient.addColorStop(1, "rgba(255,255,255,0");
        context.beginPath();
        context.strokeStyle = gradient;
        context.lineWidth = 1;
        context.moveTo(x1, y1);
        context.lineTo(x2, y2);
        context.stroke();

        context.beginPath();
        context.fillStyle = "rgba(250,250,250,0.2)";
        context.arc(x, y, 3, 0, 2 * Math.PI);
        context.fill();

        let r1 = 1;
        let r2 = 4;
        context.beginPath();
        let gStar = context.createRadialGradient(x, y, r1, x, y, r2);
        gStar.addColorStop(0, color1);
        gStar.addColorStop(1, 'RGBA(255,255,255,0.01)');
        context.fillStyle = gStar;
        context.arc(x, y, r2, 0, 2 * Math.PI);
        context.fill();
    };
    control.percent += STEP;
    if (control.percent >= 1) {
        control.times = 100;
        control.percent = 0;
        return;
    }
    context.beginPath();
    const os = 0;
    drawShine(context, xVal + (control.percent * shape.width), yVal - os + shape.emphasizedOffset, DIRECTION.HORIZONTAL, shape.width); // up
    // right
    drawShine(context, xVal + shape.width - os - shape.emphasizedOffset + 1, yVal + (control.percent * shape.height), DIRECTION.VERTICAL, shape.height);
    drawShine(context,
      xVal + shape.width - (control.percent * shape.width),
      yVal + shape.height - os - shape.emphasizedOffset + 1,
        DIRECTION.HORIZONTAL,
        shape.width
    );// bottom
    // left
    drawShine(context, xVal - os + shape.emphasizedOffset, yVal + shape.height - (control.percent * shape.height), DIRECTION.VERTICAL, shape.height + 1);
};

/**
 * 绘制流光特效
 * 辉子 2021
 */
const drawGradientLight = (context, x, y, shape, control) => {
    const OFFSET = 50;
    const RATE = 1.5;
    context.fillStyle = "red";
    let g = context.createLinearGradient(control.times - shape.width / 2, shape.height / 2, control.times - shape.width / 2 + OFFSET, -shape.height / 2)
    g.addColorStop(0, "transparent");
    g.addColorStop(0.4, shape.shineColor2 ? shape.shineColor2 : "rgba(255,255,255,0.7)");
    g.addColorStop(0.5, shape.shineColor1 ? shape.shineColor1 : "rgba(255,255,255,0.8)");
    g.addColorStop(0.6, shape.shineColor2 ? shape.shineColor2 : "rgba(255,255,255,0.7)");
    g.addColorStop(1, "transparent");
    context.fillStyle = g;
    context.beginPath();
    context.rect(-shape.width / 2, -shape.height / 2, shape.width, shape.height);
    context.fill();

    control.times += 1;
    if (control.times > (shape.width + OFFSET * RATE)) {
        control.times = -OFFSET * RATE;
    }

};

/**
 * 绘制动画
 * 辉子 2021
 */
const drawDynamic = (context, x, y, shape, control) => {
    if (!shape.visible) {
        return;
    }
    if (shape.dynamicCode) {
        try {
            eval("(async function eventCode(){" + shape.dynamicCode + "})();");
        } catch (e) {
            // 没关系，继续，不影响其他错误信息的处理.
        }
    } else {
        if (!shape.emphasized) {
            return;
        }
        switch (shape.emphasizeType) {
            case 1:
                drawGradientLight(context, x, y, shape, control);
                break;
            // add more here
            default:
                emphasizeShine(context, x, y, shape, control);
                break;
        }
    }
};

/**
 * 以html形式绘制
 * 辉子 2021
 */
const rectangleDrawer = (shape, div, x, y) => {
    let self = drawer(shape, div, x, y);
    self.type = "rectangle html drawer";
    self.drawStatic = (context, varX, varY) => {
        self.parent.style.opacity = shape.globalAlpha;
    };

    self.drawFocusFrame = context => drawFocusFrame(shape, context);

    self.drawLinkingFrame = context => drawLinkingFrame(shape, context);

    let resize = self.resize;
    self.resize = () => {
        let size = resize.call(self);
        return autoHeightResize(self, shape, size);
    };

    let control = { percent: 0, times: 0 };
    self.drawDynamic = (context, x, y) => {
        drawDynamic(context, x, y, shape, control);
    };

    return self;
};

/**
 * 自定义的contextMenu drawer
 *
 * @param shapeLength 选中的图形数量
 * @param baseDrawer 基类drawer
 * @returns {function(*, *, *, *): {}}
 */
const contextMenuDrawer = (shapeLength, baseDrawer = drawer) => {
    return (shape, div, x, y) => {
        const self = baseDrawer(shape, div, x, y);
        self.type = "context menu html drawer";
        let drawStatic = self.drawStatic;

        self.drawStatic = (x, y) => {
            if (shapeLength > 1) {
                self.parent.style.border = "2px dashed #aeb5c0";
            } else {
                self.parent.style.border = "0px dashed #aeb5c0";
            }
            drawStatic.call(self, x, y);
        };
        return self;
    }
};

/**
 * 以画布形式绘制
 */
const canvasRectangleDrawer = function (shape, div, x, y) {
    let self = canvasDrawer(shape, div, x, y);
    self.type = "rectangle canvas drawer";
    self.drawStatic = (context, x, y) => {
        context.beginPath();
        context.rect(x, y, shape.width - 2, shape.height - 2);
        context.fillStyle = shape.backColor;
        context.globalAlpha = shape.backAlpha;
        context.fill();
        context.globalAlpha = 1;
    };

    self.drawFocusFrame = context => drawFocusFrame(shape, context);

    self.drawLinkingFrame = context => drawLinkingFrame(shape, context);

    let control = { percent: 0, times: 0 };
    self.drawDynamic = (context, x, y) => {
        drawDynamic(context, x, y, shape, control);
    };

    let resize = self.resize;
    self.resize = () => {
        let size = resize.call(self);
        return autoHeightResize(self, shape, size);
    };

    return self;
};

const drawFocusFrame = (shape, context) => {
    if (!shape.ifDrawFocusFrame()) {
        return;
    }
    const frame = shape.getFrame();
    const x0 = frame.x - shape.x;
    const y0 = frame.y - shape.y;
    let pad = 1;
    let focusMargin = shape.focusMargin;
    let x1 = x0 - shape.width / 2 - focusMargin;
    let y1 = y0 - shape.height / 2 - focusMargin;
    if (shape.page.focusFrameColor !== undefined && shape.page.focusFrameColor !== "") {
        if (shape.drawer.customizedDrawFocus) {
            shape.drawer.customizedDrawFocus(context, x1, y1, frame.width + 2 * pad + 2 * focusMargin, frame.height + 2 * pad + 2 * focusMargin);
        } else {
            context.dashedRect(x1, y1, frame.width + 2 * focusMargin, frame.height + 2 * focusMargin, 2, 1, shape.page.focusFrameColor);
        }
    }
};

const drawLinkingFrame = (shape, context) => {
    const frame = shape.getFrame();
    const x0 = frame.x - shape.x;
    const y0 = frame.y - shape.y;
    let pad = 3;
    let focusMargin = shape.focusMargin;
    let x1 = x0 - shape.width / 2 - focusMargin;
    let y1 = y0 - shape.height / 2 - focusMargin;
    context.dashedRect(x1 - shape.borderWidth / 2, y1 - shape.borderWidth / 2, frame.width + 2 * focusMargin, frame.height + 2 * focusMargin, 2, 1, "darkred");
};

const autoHeightResize = (self, shape, originSize) => {
    if (!shape.autoHeight) {
        return originSize;
    }

    const height = self.parent.offsetHeight < shape.fontSize ? originSize.height : self.parent.offsetHeight;
    if (shape.minHeight && height < shape.minHeight) {
        shape.height = shape.minHeight;
        self.parent.style.height = shape.height + "px";
        originSize.height = shape.height;
    } else {
        shape.height = height < 22 ? 22 : height;
        self.parent.style.height = "auto";
        originSize.height = shape.height;
    }

    return originSize;
};

export { rectangleDrawer, canvasRectangleDrawer, contextMenuDrawer};