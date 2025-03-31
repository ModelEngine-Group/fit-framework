/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from "./rectangle.js";
import {canvasGeometryDrawer} from "./drawers/canvasGeometryDrawer.js";

/**
 * 菱形绘制器.
 *
 * @param shape 图形对象.
 * @param div dom元素.
 * @param x 横坐标.
 * @param y 纵坐标.
 * @returns {*} drawer对象.
 */
const canvasDiamondDrawer = (shape, div, x, y) => {
    const self = canvasGeometryDrawer(shape, div, x, y);

    self.drawBorder = () => {
    };

    self.backgroundRefresh = () => {
    };

    /**
     * 绘制方式：菱形计算以逆时针方向的四个顶点坐标
     *
     * @param px drawer的横坐标
     * @param py drawer的纵坐标
     */
    self.getPoints = (px, py) => {
        return [[px + shape.width / 2, py], [px, py + shape.height / 2], [px + shape.width / 2, py + shape.height],
            [px + shape.width, py + shape.height / 2]];
    }

    self.requireMoveToStart = () => true;

    return self;
}

/**
 * 菱形
 */
const diamond = (id, x, y, width, height, parent) => {
    let self = rectangle(id, x, y, width, height, parent, canvasDiamondDrawer);
    self.type = "diamond";
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

export {diamond};