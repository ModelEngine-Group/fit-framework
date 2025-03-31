/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {CURSORS, PAGE_MODE} from '../../common/const.js';
import {getGridGrabPosition} from '../../common/util.js';

/**
 * 专画鼠标
 * 辉子 2020-02-26
 */
const cursorDrawer = (() => {
    let drawer = {color: 'steelBlue', fillColor: 'whitesmoke'};
    let images = {};

    let createMoveImage = function (context, x, y, degree, page, shape) {
        if (shape !== null && shape.cursorStyle) {
            return shape.cursorStyle;
        } else {
            return CURSORS.MOVE;
        }
    };
    let createLineImage = function (context, x, y) {
        return CURSORS.POINTER;
    };
    let createRotateImage = function (context, x, y, degree, page, shape) {
        const scale = page.scaleX > 1 ? page.scaleX : 1;
        let r = 5 * scale;
        context.strokeStyle = drawer.color;
        context.fillStyle = drawer.color;
        context.beginPath();
        context.lineWidth = scale;
        context.ellipse(x, y, r, r, 0, 0.9, 2 * Math.PI);
        context.stroke();
        context.beginPath();
        context.ellipse(x, y, 2 * Number(scale), 2 * Number(scale), 0, 0, 2 * Math.PI);
        context.fill();
        context.beginPath();
        context.moveTo(x + r - 2 * Number(scale), y - Number(scale));
        context.lineTo(x + r + 2 * Number(scale), y - Number(scale));
        context.lineTo(x + r, y + 3 * Number(scale));
        context.closePath();
        context.fill();
        return CURSORS.NONE;
    };
    let createDefaultImage = function (context, x, y, rotateDegree, graphPage) {
        displayGridGrab(context, x, y, graphPage);
        return CURSORS.DEFAULT;
    };
    let createNoneImage = function (context, x, y, rotateDegree, graphPage) {
        displayGridGrab(context, x, y, graphPage);
        return CURSORS.NONE;
    };
    let displayGridGrab = function (context, x, y, graphPage) {
        if (!graphPage.enableGrid) {
            return;
        }
        let p = getGridGrabPosition({x, y}, graphPage);
        let scale = (graphPage.scaleX + graphPage.scaleY) / 2
        let step = 2 / scale;
        context.dynamicRect(p.x - step, p.y - step, 2 * step, 2 * step, 0.5 / scale, 'darkorange', '', 0, 0, 0);
    };

    let createCrossImage = function (context, x, y, rotateDegree, graphPage) {
        displayGridGrab(context, x, y, graphPage);
        return CURSORS.CROSSHAIR;
    };

    let createSouthClipImage = function (context, x, y, rotateDegree, graphPage) {
        return createNSClipImage(context, x, y, rotateDegree, graphPage, 'S');
    };
    let createNorthClipImage = function (context, x, y, rotateDegree, graphPage) {
        return 'n-resize';
    };
    let createWestClipImage = function (context, x, y, rotateDegree, graphPage) {
        return createWEClipImage(context, x, y, rotateDegree, graphPage, 'W');
    };
    let createEastClipImage = function (context, x, y, rotateDegree, graphPage) {
        return createWEClipImage(context, x, y, rotateDegree, graphPage, 'E');
    };
    let createNSClipImage = function (context, x, y, rotateDegree, graphPage, direction) {
        let r = 8;
        context.save();
        context.translate(x, y);
        context.rotate((rotateDegree) * Math.PI / 180);
        context.beginPath();
        context.strokeStyle = 'gray';
        context.lineWidth = 4;
        context.moveTo(-r, 0);
        context.lineTo(r, 0);
        context.stroke();
        context.beginPath();
        context.lineWidth = 2;
        context.moveTo(0, 0);
        if (direction === 'N') {
            context.lineTo(0, -10);
        }
        if (direction === 'S') {
            context.lineTo(0, 10);
        }
        context.stroke();
        context.restore();
        return CURSORS.NONE;
    };
    let createWEClipImage = function (context, x, y, rotateDegree, graphPage, direction) {
        let r = 8;
        context.save();
        context.translate(x, y);
        context.rotate((rotateDegree) * Math.PI / 180);
        context.beginPath();
        context.strokeStyle = 'gray';
        context.lineWidth = 4;
        context.moveTo(0, -r);
        context.lineTo(0, r);
        context.stroke();
        context.beginPath();
        context.lineWidth = 2;
        context.moveTo(0, 0);
        if (direction === 'W') {
            context.lineTo(-10, 0);
        }
        if (direction === 'E') {
            context.lineTo(10, 0);
        }
        context.stroke();
        context.restore();
        return CURSORS.NONE;
    };

    let createPresentationImage = function (context, x, y, rotateDegree, graphPage, direction) {
        let r1 = 3;
        let r2 = 10;
        context.beginPath();
        let g = context.createRadialGradient(x, y, r1, x, y, r2);
        g.addColorStop(0, 'red');
        g.addColorStop(1, 'RGBA(255,255,255,0.01)');
        context.fillStyle = g;
        context.arc(x, y, r2, 0, 2 * Math.PI);
        context.fill();
        return CURSORS.NONE;
    };
    let createPenImage = function (context, x, y, rotateDegree, graphPage, direction) {
        context.fillStyle = graphPage.graph.setting.borderColor;
        context.strokeStyle = 'lightgray';
        context.beginPath();
        context.moveTo(x, y);
        context.lineTo(x + 6, y + 2);
        context.lineTo(x + 14, y + 10);
        context.lineTo(x + 10, y + 14);
        context.lineTo(x + 2, y + 6);
        context.closePath();
        context.fill();
        context.stroke();
        return CURSORS.NONE;
    };
    let createEraserImage = function (context, x, y, rotateDegree, graphPage, direction) {
        context.strokeStyle = graphPage.graph.setting.borderColor;
        context.fillStyle = 'white';
        const r = graphPage.mouseInShape.eraser * 2;
        context.beginPath();
        context.rect(x - r, y - r, 2 * r, 2 * r);
        context.fill();
        context.stroke();
        return CURSORS.NONE;
    };

    images['ew-resize'] = () => 'w-resize';
    images['ns-resize'] = () => 'n-resize';
    images['nesw-resize'] = () => 'nesw-resize';
    images['nwse-resize'] = () => 'nwse-resize';
    images['col-resize'] = () => 'col-resize';
    images['row-resize'] = () => 'row-resize';
    images[CURSORS.CROSSHAIR] = createCrossImage;
    images[CURSORS.MOVE] = createMoveImage;
    images[CURSORS.DEFAULT] = createDefaultImage;
    images[CURSORS.NONE] = createNoneImage;
    images[CURSORS.POINTER] = createRotateImage;
    images[CURSORS.HAND] = createLineImage;
    images['s-clip'] = createSouthClipImage;
    images['n-clip'] = createNorthClipImage;
    images['w-clip'] = createWestClipImage;
    images['e-clip'] = createEastClipImage;
    images[CURSORS.PRESENTATION] = createPresentationImage;
    images[CURSORS.PEN] = createPenImage;
    images[CURSORS.ERASER] = createEraserImage;
    images['not-allowed'] = () => {};
    images[CURSORS.TEXT] = () => CURSORS.TEXT;
    images[CURSORS.GRAB] = () => CURSORS.GRAB;
    images[CURSORS.GRABBING] = () => CURSORS.GRABBING;

    drawer.color = 'steelBlue';
    drawer.type = 'cursor drawer';
    drawer.draw = function (context, x, y, cursor, page, shape) {
        let cursorVal = cursor;
        if (!page.showCursor()) {
            return false;
        }
        cursorVal = page.mode === (PAGE_MODE.PRESENTATION || PAGE_MODE.VIEW) ? CURSORS.PRESENTATION : cursorVal;
        // if (position == null) return;
        let degree = 0;
        if (shape) {
            degree = shape.rotateDegree;
            let parent = shape.getContainer();
            while (parent !== undefined && parent !== shape.page) {
                degree += parent.rotateDegree;
                parent = parent.getContainer();
            }
        }
        try {
            return images[cursorVal](context, x, y, degree, page, shape);
        } catch (e) {
            return CURSORS.DEFAULT;
        }
    }
    return drawer;
})();

export {cursorDrawer};