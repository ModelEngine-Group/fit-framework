/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from "./rectangle.js";
import {canvasGeometryDrawer} from "./drawers/canvasGeometryDrawer.js";

/**
 * 五角星绘制器.
 *
 * @param shape 图形对象.
 * @param div dom元素.
 * @param x 横坐标.
 * @param y 纵坐标.
 * @returns {*} drawer对象.
 */
const canvasPentagramDrawer = (shape, div, x, y) => {
    const self = canvasGeometryDrawer(shape, div, x, y);

    self.drawBorder = () => {
    };

    self.backgroundRefresh = () => {
    };

    /**
     * 绘制方式：五角星根据内切圆和外接圆计算内圆顶点坐标（逆时针）和外圆顶点坐标（逆时针）方式绘制
     *
     * @param px drawer的横坐标
     * @param py drawer的纵坐标
     */
    self.getPoints = (px, py) => {
        const horn = 5;
        const angle = 360 / horn;
        const wR = (shape.width) / 2; // 外接圆半径（计算横坐标）
        const wr = shape.width / 5; // 内切圆半径，除的值越大五角星越锋锐（计算横坐标）
        const hR = shape.height / 2;// 外接圆半径（计算纵坐标）
        const hr = shape.height / 5;// 内切圆半径（计算纵坐标）
        const rotate = 0;
        const points = [];
        for (let i = 0; i < horn; i++) {
            // 角度转弧度：角度/180*Math.PI
            const roateAngle = i * angle - rotate; // 旋转动起来
            // 外圆顶点坐标
            const X1 = Math.cos((18 + roateAngle) / 180 * Math.PI) * wR + px + wR;
            const Y1 = -Math.sin((18 + roateAngle) / 180 * Math.PI) * hR + py + hR;
            points.push([X1, Y1]);
            // 內圆顶点坐标
            const x1 = Math.cos((54 + roateAngle) / 180 * Math.PI) * wr + px + wR;
            const y1 = -Math.sin((54 + roateAngle) / 180 * Math.PI) * hr + py + hR;
            points.push([x1, y1]);
        }
        return points;
    }

    return self;
}

/**
 * 五角星
 */
const pentagram = (id, x, y, width, height, parent) => {
    let self = rectangle(id, x, y, width, height, parent, canvasPentagramDrawer);
    self.type = "pentagram";
    self.width = 100;
    self.height = 100;
    self.text = "";

    /**
     * 重写获取配置方法.
     * 1、该图形不需要corerRadius配置.
     */
    const getConfigurations = self.getConfigurations;
    self.getConfigurations = () => {
        const configurations = getConfigurations.apply(self);
        configurations.remove(c => c.field === "cornerRadius");
        return configurations;
    }

    return self;
}

export {pentagram};