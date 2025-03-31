/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {customizedDrawer} from './htmlDrawer.js';
import {LINEMODE} from '../../common/const.js';
import {isPointInRect} from '../../common/util.js';

/**
 * svg格式的线绘制器.
 *
 * @override
 */
const svgLineDrawer = (shape, div, x, y) => {
    let self = customizedDrawer("svg")(shape, div, x, y);
    self.offset = 0;
    self.element.style.overflow = "visible";
    self.markerBuilder = markerBuilder(self);

    self.getEditRect = () => {
        let x = shape.x + shape.textConnector.x - self.text.clientWidth / 2;
        let y = shape.y + shape.textConnector.y + self.text.clientHeight / 2;
        return {x, y, width: (self.text.clientWidth + 10), height: self.text.clientHeight};
    };

    const resize = self.resize;
    self.resize = () => {
        const lineWidth = shape.lineWidth * 2;
        resize.apply(self);
        self.parent.style.border = "";
        self.parent.style.boderStyle = "none";
        self.parent.style.background = "transparent";
        self.parent.style.width = (Math.abs(shape.width) < lineWidth ? lineWidth : shape.width) + "px";
        self.parent.style.height = (Math.abs(shape.height) < lineWidth ? lineWidth : shape.height) + "px";
        self.element.style.left = "0px";// (Math.abs(shape.width) < lineWidth ? (Math.abs(shape.width) - lineWidth) / 2 : 0) + "px";
        self.element.style.top = "0px";//(Math.abs(shape.height) < lineWidth ? (Math.abs(shape.height) - lineWidth) / 2 : 0) + "px";
        self.element.style.width = self.parent.style.width;
        self.element.style.height = self.parent.style.height;
    };

    /**
     * 重写textResize方法.
     */
    const textResize = self.textResize;
    self.textResize = () => {
        if (shape.hideText) {
            return;
        }
        textResize.apply(self);
        self.text.style.padding = "1px";
        self.text.style.whiteSpace = "nowrap";

        const editor = self.getEditor();
        editor.editable.style.whiteSpace = null; // 不设置为null会导致中文情况下，移动textConnector，文本变成长条形.
        const textString = editor.getTextString();
        // 当输入框中没有文字时，让其宽度父级元素（line宽度）保持一致
        if (textString === "") {
            self.text.style.width = "100%";
        } else {
            self.text.style.width = "fit-content"; // 如果输入框有文字，输入框的宽度需要在resize中保持一致，否则会导致文字换行
        }
        self.text.style.left = (shape.width > 0 ? shape.textConnector.x : (-shape.width + shape.textConnector.x)) - self.text.clientWidth / 2 + "px";
        self.text.style.top = (shape.height > 0 ? shape.textConnector.y - self.text.clientHeight : (-shape.height + shape.textConnector.y) - self.text.clientHeight) - self.text.clientHeight / 2 + "px";
    };

    self.getTextPaddingLeft = () => "1px";
    self.getTextPaddingRight = () => "1px";
    self.getTextLeft = () => (shape.width > 0 ? shape.textConnector.x : (-shape.width + shape.textConnector.x)) - self.text.clientWidth / 2 + "px";
    self.getTextTop = () => (shape.height > 0 ? shape.textConnector.y : (-shape.height + shape.textConnector.y)) - self.text.clientHeight / 2 + "px";
    self.getTextWhiteSpace = () => "nowrap";
    self.getTextPosition = () => "absolute";
    self.getTextPadding = () => "1px";
    self.getParentBorder = () => "";

    /**
     * 坐标是否包含在border中.
     *
     * @override
     */
    self.containsBorder = (x, y) => {
        const point = self.element.createSVGPoint();
        point.x = x - shape.x + (shape.width > 0 ? 0 : -shape.width);
        point.y = y - shape.y + (shape.height > 0 ? 0 : -shape.height);
        return self.svgLine.isPointInStroke(point);
    };

    self.containsBack = () => false;

    /**
     * 坐标是否在text中.
     *
     * @override
     */
    self.containsText = (x, y) => {
        let tArea = {
            x: parseInt(self.parent.style.left.slice(0, -2)) + parseInt(self.text.style.left.slice(0, -2)),
            y: parseInt(self.parent.style.top.slice(0, -2)) + parseInt(self.text.style.top.slice(0, -2)),
            width: self.text.clientWidth,
            height: self.text.clientHeight
        };
        return isPointInRect({x, y}, tArea)
    };

    /**
     * 绘制连接点.
     *
     * @override
     */
    self.drawConnectors = context => {
        let x1 = -shape.width / 2;
        let y1 = -shape.height / 2;
        shape.getConnectors().filter(c => c.getVisibility()).forEach(connector => connector.draw(context, x1, y1));
    };

    /**
     * 空实现，不做任何处理.
     *
     * @override
     */
    self.drawBorder = () => {
    };

    /**
     * 绘制线.
     *
     * @override
     */
    self.drawStatic = () => {
        const varX = 0;
        const varY = 0;
        const toX = varX + shape.width;
        const toY = varY + shape.height;
        const ox = shape.width > 0 ? 0 : -shape.width;
        const oy = shape.height > 0 ? 0 : -shape.height;
        self.svgLine = SvgLine(shape, self);
        self.svgLine.create(self.element);
        self.svgLine.drawSvgLine(varX, varY, toX, toY, varX, varY, ox, oy);
    };

    self.drawDraft = () => {
        self.move();
        self.draw();
    };

    const drawShiningSpot = (context, centerX, centerY, prevX, prevY) => {
        const r = shape.lineWidth + 1;
        context.beginPath();
        context.arc(centerX + 1, centerY + 1, r, 0, 2 * Math.PI);
        context.fillStyle = shape.backColor;
        context.fill();
        context.closePath();
        return {x: centerX, y: centerY};
    };

    let progress = 0;
    let prePoint = {x: 0, y: 0};
    let step = 0.5;
    self.drawDynamic = function (context, x, y) {
        if (self.svgLine === undefined || self.svgLine === null) {
            return;
        }
        const path = self.svgLine.path;
        const len = path.getTotalLength();
        if (step >= 0) {
            let point = path.getPointAtLength(progress);
            prePoint = drawShiningSpot(context,
                point.x + x - shape.width / 2 - shape.margin,
                point.y + y - shape.height / 2 - shape.margin,
                prePoint.x,
                prePoint.y
            );
        }
        progress += step;
        if (progress > len) {
            progress = -5 * step;
        }
    };

    return self;
};

/**
 * 用于绘制一条svg格式的线.
 *
 * @param shape 图形.
 * @param drawer 绘制器.
 * @return {{}}
 * @constructor
 */
const SvgLine = (shape, drawer) => {
    const self = {};

    /**
     * 创建元素.
     *
     * @param parentElement 父元素.
     */
    self.create = (parentElement) => {
        self.path = drawer.createElement('path', "path:" + shape.id);
        self.shadowPath = drawer.createElement('path', "shadow-path:" + shape.id);
        self.path.setAttribute("fill", "none");
        self.shadowPath.setAttribute("fill", "none");
        parentElement.appendChild(self.path);
        parentElement.appendChild(self.shadowPath);
        self.defs = drawer.createElement("defs", "defs:" + shape.id);
        parentElement.appendChild(self.defs);
    };

    /**
     * 绘制svg线.
     *
     * @param fromX 起始x.
     * @param fromY 起始y.
     * @param toX 目标x.
     * @param toY 目标y.
     * @param x 横坐标.
     * @param y 纵坐标.
     * @param ox 暂时不知道意思.
     * @param oy 暂时不知道意思.
     */
    self.drawSvgLine = (fromX, fromY, toX, toY, x, y, ox, oy) => {
        drawBeginArrow();
        drawEndArrow();
        setAttributes();
        setDescription(fromX, fromY, toX, toY, x, y, ox, oy);
    };

    const drawBeginArrow = () => {
        if (shape.beginArrow) {
            self.markerStart = drawer.markerBuilder.build(shape, "marker-start", "beginArrow", shape.getBorderColor());
            self.defs.appendChild(self.markerStart);
            self.path.setAttribute("marker-start", "url(#" + self.markerStart.id + ")");
        } else {
            self.markerStart && self.markerStart.remove();
        }
    };

    const drawEndArrow = () => {
        if (shape.endArrow) {
            self.markerEnd = drawer.markerBuilder.build(shape, "marker-end", "endArrow", shape.getBorderColor());
            self.defs.appendChild(self.markerEnd);
            self.path.setAttribute("marker-end", "url(#" + self.markerEnd.id + ")");
        } else {
            self.markerEnd && self.markerEnd.remove();
        }
    };

    const setAttributes = () => {
        self.path.setAttribute("stroke", shape.getBorderColor());
        self.shadowPath.setAttribute("stroke", "rgba(222,222,222,0)");
        self.path.setAttribute("stroke-width", shape.lineWidth);
        self.shadowPath.setAttribute("stroke-width", shape.lineWidth + 8);
        self.path.setAttribute("stroke-linecap", "butt");
        self.path.setAttribute("stroke-linejoin", "miter");
        self.path.setAttribute("stroke-dasharray", getDashArray(shape.dashWidth, shape.lineWidth));
    };

    const setDescription = (fromX, fromY, toX, toY, x, y, ox, oy) => {
        self.pathDescription = "M" + (fromX + ox) + "," + (fromY + oy) + " ";
        switch (shape.lineMode.type) {
            case LINEMODE.CURVE.type:
                setCurve(fromX, fromY, toX, toY, x, y, ox, oy);
                break;
            case LINEMODE.BROKEN.type:
                setBroken(toX, toY, x, y, ox, oy);
                break;
            case LINEMODE.STRAIGHT.type:
                self.pathDescription += "L" + (toX + ox) + "," + (toY + oy) + " ";
                break;
            case LINEMODE.AUTO_CURVE.type:
                setAutoCurve(fromX, fromY, toX, toY, ox, oy);
                break;
            default:
                throw new Error("Line type[" + shape.lineMode.type + "] not support.");
        }

        self.path.setAttribute("d", self.pathDescription);
        self.shadowPath.setAttribute("d", self.pathDescription);
    };

    const setCurve = (fromX, fromY, toX, toY, x, y, ox, oy) => {
        let point1 = shape.getCurvePoint1();
        let point2 = shape.getCurvePoint2();
        let points = [];
        points.push({x: fromX, y: fromY});
        points.push({x: x + point1.x, y: y + point1.y});
        points.push({x: x + point2.x, y: y + point2.y});
        points.push({x: toX, y: toY});
        let i = 1;
        for (; i < points.length - 2; i++) {
            let xc = (points[i].x + points[i + 1].x) / 2;
            let yc = (points[i].y + points[i + 1].y) / 2;
            self.pathDescription += "Q" + (points[i].x + ox) + "," + (points[i].y + oy) + "," + (xc + ox) + "," + (yc + oy) + " ";
        }
        self.pathDescription += "Q" + (points[i].x + ox) + "," + (points[i].y + oy) + "," + (points[i + 1].x + ox) + "," + (points[i + 1].y + oy) + " ";
    };

    const setAutoCurve = (fromX, fromY, toX, toY, ox, oy) => {
        let bezierControlPoint1 = shape.getBezierControlPoint1(fromX, fromY, toX, toY, ox, oy);
        let bezierControlPoint2 = shape.getBezierControlPoint2(fromX, fromY, toX, toY, ox, oy);
        let toPoint = shape.calculateBezierToPoint(fromX, fromY, toX, toY, ox, oy);
        self.pathDescription += " C " + bezierControlPoint1.x + "," + bezierControlPoint1.y + "," + bezierControlPoint2.x + "," + bezierControlPoint2.y + "," + toPoint.x + "," + toPoint.y + " ";
    };

    const setBroken = (toX, toY, x, y, ox, oy) => {
        self.pathDescription += "L" + (x + shape.arrowBeginPoint.x + ox) + "," + (y + shape.arrowBeginPoint.y + oy) + " ";
        if (shape.brokenPoints.length > 0) {
            shape.brokenPoints.forEach(bp => {
                self.pathDescription += "L" + (x + bp.x + ox) + "," + (y + bp.y + oy) + " ";
            });
        }
        self.pathDescription += "L" + (x + shape.arrowEndPoint.x + ox) + "," + (y + shape.arrowEndPoint.y + oy) + " ";
        self.pathDescription += "L" + (toX + ox) + "," + (toY + oy) + " ";
    };

    const getDashArray = (dash, lineWidth) => {
        if (dash === 3) {
            // 方点虚线
            return `${lineWidth}`;
        } else if (dash === 5) {
            // 更宽的方点虚线
            return `${lineWidth * 2},${lineWidth}`;
        } else {
            // 实线
            return "0";
        }
    };

    /**
     * 点是否在线上.
     *
     * @param point 点.
     * @return {*} true/false.
     */
    self.isPointInStroke = (point) => {
        // 这里不能使用isPointInFill方法，会导致误判，会导致在线覆盖了部分图形的选中区域.
        return self.shadowPath.isPointInStroke(point);
    };

    return self;
};

/**
 * marker构建器.
 *
 * @param drawer 绘制器对象.
 * @returns {{}}
 */
const markerBuilder = (drawer) => {
    const self = {};

    self.build = (shape, name, type, color) => {
        const arrowSize = shape[type + "Size"];
        const isEmpty = shape[type + "Empty"];
        const mode = shape[type + "Mode"];
        const marker = drawer.createElement('marker', name + ":" + shape.id);
        marker.setAttribute("viewBox", String("0 0 " + (arrowSize * 2 + 2) + " " + (arrowSize * 2 + 2)));
        marker.setAttribute("refX", arrowSize * 2);
        marker.setAttribute("refY", arrowSize + 1);
        marker.setAttribute("markerWidth", arrowSize);
        marker.setAttribute("markerHeight", arrowSize);
        marker.setAttribute("orient", "auto-start-reverse");
        switch (mode) {
            case "arrow":
                marker.innerHTML = createArrow(shape, arrowSize, isEmpty, color)
                break;
            case "diamond":
                marker.innerHTML = "<path d='M 1 " + (arrowSize + 1) + " L " + (arrowSize + 1) + " 1 L " + (arrowSize * 2 + 1) + " " + (arrowSize + 1) + " L " + (arrowSize + 1) + " " + (arrowSize * 2 + 1) + " z' fill='" + (isEmpty ? "white" : color) + "' stroke='" + color + "' stroke-linecap='round' stroke-width='2' />";
                break;
            case "circle":
                marker.innerHTML = "<circle cx='" + (arrowSize + 1) + "' cy='" + (arrowSize + 1) + "' r='" + arrowSize + "' fill='" + (isEmpty ? "white" : color) + "'" + "' stroke='" + color + "' stroke-linecap='round' stroke-width='2' />";
                break;
            default:
                throw new Error("mode[" + mode + "] not support.");
        }

        return marker;
    };

    const createArrow = (shape, arrowSize, isEmpty, color) => {
        const points = [];
        points.push([1, 1]);
        points.push([arrowSize * 2 + 1, arrowSize + 1]);
        points.push([1, arrowSize * 2 + 1]);
        const d = buildSvg(points);
        return "<path d='" + d + "' " +
            "fill='" + (isEmpty ? "white" : color) + "' " +
            "stroke='" + color + "' " +
            "stroke-linecap='round' " +
            "stroke-width='2' />";
    };

    const buildSvg = (points) => {
        let result = "";
        points.forEach((p, index) => {
            if (index === 0) {
                result += "M " + p[0] + " " + p[1];
            } else {
                result += " L " + p[0] + " " + p[1];
            }
        });
        result += " z";
        return result;
    };

    return self;
};

export {svgLineDrawer};