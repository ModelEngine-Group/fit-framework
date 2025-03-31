/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {CONNECTION_RADIUS, DOCK_MODE} from '../common/const.js';

/**
 * 图像四周的resize小方块
 * 辉子 2020-02-27
 */
let connector = (shape,
                 getX,
                 getY,
                 getDirection,
                 getVisibility = (s) => true,
                 getEnable = (s) => true,
                 getConnectable = () => true,
                 release = () => shape.invalidate(),
                 equiproportional,
                 getDelta) => {
    let self = {};
    let r = CONNECTION_RADIUS / 2;
    shape.connectors.push(self);
    self.getX = getX;
    self.getY = getY;
    self.isSolid = false;
    self.visible = true;
    // 是否进行等比缩放  在rectangle中，顶点且按住shift键则为进行等比缩放操作
    self.equiproportional = equiproportional;
    self.getDelta = getDelta;
    self.dragable = true;
    self.getDirection = () => getDirection(shape, self);
    self.getVisibility = () => {
        let container = shape.getContainer();
        let notFixed = container.dockMode === DOCK_MODE.NONE;
        return getVisibility(shape) && notFixed && self.visible;
    }
    self.getEnable = getEnable;
    self.getConnectable = getConnectable;
    self.release = release;

    self.allowFromLink = true; // 允许拖出去
    self.allowToLink = true; // 允许连进来
    self.shape = shape;
    self.type = "default";
    self.radius = r;
    self.shapeStyle = "round";// "round","rect"
    self.strokeStyle = shape.page.graph.setting.focusBorderColor;
    self.ox = self.oy = 0;// 相对计算出来的x的偏移位置

    self.isType = type => self.type === type;
    self.moving = (deltaX, deltaY, x, y) => {
    };
    self.move = (deltaX, deltaY, x, y) => {
        if (!self.visible || !self.enable) {
            return;
        }
        self.beforeMove && self.beforeMove(deltaX, deltaY, x, y);
        self.moving(deltaX, deltaY, x, y);
        self.moved && self.moved(deltaX, deltaY, x, y);
    };
    self.getPosition = () => {
        let point = {x: self.x + shape.x, y: self.y + shape.y};
        if (shape.rotateDegree !== 0) {
            let cx = shape.width / 2 + shape.x;
            let cy = shape.height / 2 + shape.y;
            point = getRotatedCoordinate(point.x, point.y, cx, cy, shape.rotateDegree * Math.PI / 180);
        }
        return point;
    };
    /**
     * 外部擴展在shape.connectors里重寫這個方法
     */
    self.refresh = () => {
        self.frame = shape.getFrame();
        self.x = self.getX(shape, self);
        self.y = self.getY(shape, self);
        self.connectable = self.getConnectable(shape);
        self.enable = self.getEnable(shape);
        self.direction = self.getDirection(shape);
    };

    self.getHitRegion = () => {
        const r = self.radius;
        return {
            x: self.x - r, y: self.y - r,
            width: 2 * r, height: 2 * r
        };
    };
    self.onMouseDown = position => { };
    self.onMouseDrag = (position) => {
        if (!self.dragable) {
            return;
        }
        // history
        position.context.command = "resize";
        let dirty = position.context.shapes.find(s => s.shape === shape);
        if (!dirty) {
            dirty = {
                shape, x: {}, y: {}, width: {}, height: {}, rotateDegree: {}, ox: {}, oy: {}, fromShape: {},
                toShape: {}, fromConn: {}, toConn: {}
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
                dirty.rotateDegree.preValue = shape.rotateDegree;
            }
            position.context.shapes.push(dirty);
        }

        //------------------------------------------------
        let deltaX = position.deltaX;
        let deltaY = position.deltaY;
        let x = position.x;
        let y = position.y;
        if (self.equiproportional && self.getDelta && self.equiproportional()) {
            // 这里使用deltaX和deltaY进行计算时，计算出来的数据不是很准确.
            // 使用movementX和movementY之后相对要准确一些.
            const movementX = position.e.movementX / shape.page.scaleX;
            const movementY = position.e.movementY / shape.page.scaleY;
            const delta = self.getDelta(movementX, movementY);
            deltaX = delta.deltaX;
            deltaY = delta.deltaY;
        }
        self.move(deltaX, deltaY, x, y);
        //------------------------------------------------

        // history
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
        shape.onConnectorDragged(self);
    };
    self.onReturnDrag = (x, mouseOffsetX, y, mouseOffsetY) => {
    };
    self.draw = (context, x, y) => {
        let region = self.getHitRegion();
        if (self.dragable) {
            self.refresh();
            if (!shape.resizeable && self.type !== "rotate") {
                return;
            }
            if (shape.linking && self === shape.linkingConnector) {
                context.fillStyle = "orange";
            } else {
                context.fillStyle = self.direction.color;
            }
            context.lineWidth = 1 / shape.page.scaleX;
            context.beginPath();
            context.strokeStyle = self.strokeStyle === undefined ? shape.getBorderColor() : self.strokeStyle;
            if (self.shapeStyle === "rect") {
                context.rect(x + region.x + 1, y + region.y + 1, region.width, region.height);
                context.closePath();
            }
            if (self.shapeStyle === "round") {
                // 减去一个shape.borderWidth / 2,使得视觉效果更好
                context.arc(x + region.x + region.width / 2,
                        y + region.y + region.height / 2,
                        self.radius,
                        0,
                        2 * Math.PI);
            }

            // 如果是solid，那么填充样式和stroke样式保持一致.
            if (self.isSolid) {
                context.fillStyle = context.strokeStyle;
            }
            context.fill();
            context.stroke();
        } else {
            let r = 4;
            if (shape.linking && self === shape.linkingConnector) {
                context.fillStyle = "orange";
                r = 1;
            } else {
                context.fillStyle = "darkred";
            }
            // 绘制调整 例如宽高为100x100的矩形,其中一个分割点为(50,25),但是在绘制矩形连接点的时候,需要提供左其上角的的坐标点,所以需要减去(region.width - 2 * r)/2,变成(48,23)
            context.fillRect(x + region.x + r - shape.borderWidth / 2, y + region.y + r - shape.borderWidth / 2, region.width - 2 * r, region.height - 2 * r);
        }
    };
    self.refresh();
    return self;
};

export {connector};