/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {canvasRectangleDrawer} from "./rectangleDrawer.js";

/**
 * canvas绘制集合图形的drawer.
 *
 * @param shape 图形对象.
 * @param div dom元素.
 * @param x 横坐标.
 * @param y 纵坐标.
 * @returns {*} drawer对象.
 */
export const canvasGeometryDrawer = (shape, div, x, y) => {
    const self = canvasRectangleDrawer(shape, div, x, y);

    /**
     * 重写drawStatic方法，表现和rectangle不太一致.
     *
     * @param context canvas的上下文对象.
     * @param x 横坐标.
     * @param y 纵坐标.
     */
    self.drawStatic = (context, x, y) => {
        const borderWidth = shape.borderWidth;
        const dashWidth = shape.dashWidth;
        const backColor = shape.getBackColor();
        const borderColor = shape.getBorderColor()

        context.beginPath();
        context.setLineDash([dashWidth, dashWidth / 1.3]);

        // canvas画边框时会向内缩1/2 border宽度,所以偏移量也需要做相应调整;1为shape有个默认1px的边框，需要做相应的偏移
        const offset = borderWidth / 2 + 1;
        let points = calcPoint(self.getPoints(x, y), borderWidth / 2);
        for (let i = 0; i < points.length; i++) {
            if (self.requireMoveToStart() && i === 0) {
                context.moveTo(points[0][0] - offset, points[0][1] - offset);
            } else {
                context.lineTo(points[i][0] - offset, points[i][1] - offset);
            }
        }
        context.closePath();

        // 填充.
        if (backColor !== "") {
            context.fillStyle = backColor;
            context.fill();
        }

        // 边框.
        if (borderWidth !== 0) {
            context.strokeStyle = borderColor;
            context.lineWidth = borderWidth;
            context.stroke();
        }
    }

    /**
     * 获取坐标点集合.子类需重写该方法.
     *
     * @param x 横坐标.
     * @param y 纵坐标.
     * @returns {*[]} 坐标点集合.
     */
    self.getPoints = (x, y) => [];

    /**
     * 是否移动画笔到起始点.默认为false，子类可重写该方法.
     *
     * @returns {boolean} 默认为false.
     */
    self.requireMoveToStart = () => {
        return false;
    };

    /**
     * 图形等距缩放（计算点坐标）
     * 该算法只适用于【坐标点以逆时针方向绘制】的图形
     *
     * @param data 初始点坐标
     * @param secDis 缩放距离
     */
    const calcPoint = (data, secDis) => {
        const num = data.length;
        const scaleData = [];
        for (let i = 0; i < num; i++) {
            const index = i === 0 ? num : i;
            const x1 = data[i % num][0] - data[(index - 1) % num][0];
            const y1 = data[i % num][1] - data[(index - 1) % num][1];

            const x2 = data[(i + 1) % num][0] - data[i % num][0];
            const y2 = data[(i + 1) % num][1] - data[i % num][1];

            const dA = ((x1 ** 2) + (y1 ** 2)) ** 0.5;
            const dB = ((x2 ** 2) + (y2 ** 2)) ** 0.5;

            let vecCross = (x1 * y2) - (x2 * y1);
            let sinTheta = vecCross / (dA * dB);
            let dv = secDis / sinTheta;

            let v1X = (dv / dA) * x1;
            let v1Y = (dv / dA) * y1;

            let v2X = (dv / dB) * x2;
            let v2Y = (dv / dB) * y2;

            let pqX = v1X - v2X;
            let pqY = v1Y - v2Y;

            let qX = data[i % num][0] + pqX;
            let qY = data[i % num][1] + pqY;
            scaleData.push([qX, qY]);
        }

        return scaleData;
    }

    return self;
}