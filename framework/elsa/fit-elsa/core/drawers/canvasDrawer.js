/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {compareAndSet} from '../../common/util.js';
import {drawer} from './htmlDrawer.js';

/**
 * 使用canvas绘制技术绘制shape
 * 辉子 2021
 */
const canvasDrawer = (shape, div, x = 0, y = 0) => {
    let self = drawer(shape, div);
    self.type = "canvas drawer";

    // for basic drawing
    self.pixelRate = {ratioX: 1, ratioY: 1};
    self.canvas = self.createElement("canvas", "static:" + shape.id);
    self.canvas.style.position = "absolute";
    self.parent.insertBefore(self.canvas, self.text);
    self.context = self.canvas.getContext("2d", {willReadFrequently: true});


    const containsBack = self.containsBack;
    self.containsBack = (x, y) => {
        if (self.displayBackground) {
            return containsBack.call(self, x, y);
        }
        // 未能命中文字，再进行图形判断
        let left = 0;
        let top = 0;

        let x1 = x + shape.margin - shape.x;
        let y1 = y + shape.margin - shape.y;
        if (shape.width < 0) {
            left += shape.width;
        }
        if (shape.height < 0) {
            top += shape.height;
        }

        // rotate coordinate
        try {
            let imageData = self.context.getImageData(((x1 - left) * self.pixelRate.ratioX), ((y1 - top) * self.pixelRate.ratioY), 1, 1);
            return imageData.data[3] > 0;
        } catch (e) {
            // 没关系，继续，不影响其他错误信息的处理.
        }
      return false;
    };

    let resize = self.resize;
    self.resize = () => {// react to shape.width,shape.height
        let size = resize.apply(self);
        self.resizeCanvas(size);
        self.displayBackground ? compareAndSet(self.parent.style, 'background', shape.getBackColor()) : compareAndSet(self.parent.style, 'background', "transparent");
        return size;
    };

    self.resizeCanvas = size => {
        compareAndSet(self.canvas, 'id', "static:" + shape.id);
        const canvasSize = self.updateCanvas(size.width, size.height, 'canvas');
        const dx = canvasSize.width - size.width;
        const dy = canvasSize.height - size.height;
        self.updateIfChange(self.canvas.style, 'left', (-dx / 2) + "px", 'canvas_left');
        self.updateIfChange(self.canvas.style, 'top', (-dy / 2) + "px", 'canvas_top');
        self.updateIfChange(self.canvas.style, 'opacity', shape.globalAlpha, 'canvas_opacity');
    }

    self.drawStatic = (context, x, y) => {
    };

    self.setVisibility = () => {
        if (!shape.inScreen()) {
            self.parent.style.visibility = "hidden";
            return;
        } else {
            self.parent.style.visibility = "visible";
        }
        self.resize();
    }
    self.getSnapshot = () => {
        const node = self.parent.cloneNode();
        node.style.border = "solid " + shape.borderWidth + " " + shape.borderColor;
        return node;
    };

    self.draw = function () {
        let initialized = false;
        return () => {
            if (!initialized) {
                self.initialize();
                initialized = true;
            }
            if (!shape.getVisibility()) {
                return;
            }
            self.drawBorder();
            // 重新绘制
            let offsetX = shape.width < 0 ? -shape.width : 0;
            let offsetY = shape.height < 0 ? -shape.height : 0;

            let context = self.context;
            context.save();
            self.resize();
            self.clearCanvas(context);
            context.strokeStyle = shape.getBorderColor();
            context.fillStyle = shape.getBackColor();
            context.lineWidth = shape.borderWidth;
            context.globalAlpha = shape.globalAlpha;
            self.drawStatic(context, x + shape.margin + offsetX, y + shape.margin + offsetY);
            self.drawRegions(context);
            context.restore();
        };
    }();

    self.clearCanvas = context => {
        context.clearRect(0, 0, context.canvas.clientWidth, context.canvas.clientHeight);
    }
    return self;
};

const simpleCanvasDrawer = (shape, div, x = 0, y = 0) => {
    let self = canvasDrawer(shape, div);
    self.type = "simple canvas drawer";

    self.draw = () => {
        if (self.canvas !== self.parent) {
            self.canvas.id = self.parent.id;
            self.canvas.style = self.parent.style;
            self.parent.remove();
            self.parent = self.canvas;

        }
    }
    self.animationResize = () => self.animationCanvas === undefined;
    self.drawAnimation = () => {
        if (!shape.visible) {
            return;
        }
        if (!shape.enableAnimation) {
            return;
        }
        self.drawDynamic();
    };

    return self;
};

export {canvasDrawer, simpleCanvasDrawer};
