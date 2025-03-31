/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from "./rectangle.js";
import {canvasGeometryDrawer} from "./drawers/canvasGeometryDrawer.js";

/**
 * 正五边形绘制器.
 *
 * @param shape 图形对象.
 * @param div dom元素.
 * @param x 横坐标.
 * @param y 纵坐标.
 * @returns {*} drawer对象.
 */
const canvasRegularPentagonalDrawer = (shape, div, x, y) => {
    const self = canvasGeometryDrawer(shape, div, x, y);

    self.drawBorder = () => {
    };

    self.backgroundRefresh = () => {
    };

    /**
     * 绘制方式：正五边形根据外接圆计算外圆顶点坐标（逆时针）方式绘制
     *
     * @param px drawer的横坐标
     * @param py drawer的纵坐标
     */
    self.getPoints = (px, py) => {
        const num = 5; // 几边型
        const centerX = px + shape.width / 2; // 中心坐标点
        const centerY = py + shape.height / 2;
        let points = [];
        //画正多边形
        for (let i = 0; i < num; i++) {
            const posX = (shape.width / 2) * Math.cos(Math.PI / 180 * 360 / num * i + 60) + centerX;
            const posY = (shape.height / 2) * Math.sin(Math.PI / 180 * 360 / num * i + 60) + centerY;
            points.push([posX, posY]);
        }
        return points.reverse();
    }

    return self;
}

/**
 * 正五边形
 */
const regularPentagonal = (id, x, y, width, height, parent) => {
    let self = rectangle(id, x, y, width, height, parent, canvasRegularPentagonalDrawer);
    self.type = "regularPentagonal";
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

export {regularPentagonal};