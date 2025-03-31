/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from "./rectangle.js";
import {canvasGeometryDrawer} from "./drawers/canvasGeometryDrawer.js";

/**
 * 平行四边形绘制器.
 *
 * @param shape 图形对象.
 * @param div dom元素.
 * @param x 横坐标.
 * @param y 纵坐标.
 * @returns {*} drawer对象.
 */
const canvasParallelogramDrawer = (shape, div, x, y) => {
    const self = canvasGeometryDrawer(shape, div, x, y);

    self.drawBorder = () => {
    };

    self.backgroundRefresh = () => {
    };

    /**
     * 绘制方式：平行四边形计算逆时针方向的四个顶点坐标
     *
     * @param px drawer的横坐标
     * @param py drawer的纵坐标
     */
    self.getPoints = (px, py) => {
        return [[px + shape.width / 4, py], [px, py + shape.height],
            [px + shape.width - shape.width / 4, py + shape.height], [px + shape.width, py]];
    }

    self.requireMoveToStart = () => true;

    return self;
}

/**
 * 平行四边形
 */
const parallelogram = (id, x, y, width, height, parent) => {
    let self = rectangle(id, x, y, width, height, parent, canvasParallelogramDrawer);
    self.type = "parallelogram";
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

export {parallelogram};