/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DIRECTION, LINEMODE} from '../common/const.js';
import {closestPoint, isPointInRect} from '../common/util.js';
import {shape} from './shape.js';
import {svgLineDrawer} from './drawers/lineDrawer.js';
import {connector} from './connector.js';
import {lineHelper} from "./lineHelper.js";

/**
 * 线：elsa体系里最重要，也是最难的一个形状
 * 模式有：直线，曲线，折线
 * 可以连接不同形状，连接点必须是connector
 * 如果没有特定connector，line会连接最近的connector，如果有指定connector，则会指定connector连接
 */
const line = (id, x, y, width, height, parent, drawer) => {
    let self = shape(id, x, y, width, height, parent, drawer ? drawer : svgLineDrawer);// drawer === undefined ? lineDrawer : drawer);
    self.type = "line";
    self.margin = self.pad = 0;
    self.backColor = "white";
    self.hideText = true;
    self.lineHelper = lineHelper();
    // ------------序列化属性---------------
    self.beginArrow = false;
    self.beginArrowEmpty = false;
    self.beginArrowMode = "arrow";
    self.beginArrowSize = 4;
    self.endArrow = false;
    self.endArrowEmpty = false;
    self.endArrowSize = 4;
    self.endArrowMode = "arrow";
    self.textX = 0;
    self.textY = 0;
    self.hAlign = ALIGN.MIDDLE;

    self.lineWidth = 2;

    self.width = 100;
    self.height = 80;

    self.fromShape = "";// 序列化时只存储shape id
    self.toShape = "";// 序列化时只存储shape id
    self.definedFromConnector = "";// 指定的连接的对方shape.connector.key
    self.definedToConnector = "";

    self.arrowBeginPoint = {x: 0, y: 0};// the point to draw begin arrow
    self.arrowEndPoint = {x: 0, y: 0};// the point to draw end arrow

    self.curvePoint1 = {x: 0, y: 0};// adjust point1 for curve mode, curve pints are connectors
    self.curvePoint2 = {x: 0, y: 0};// adjust point2 for curve mode

    self.brokenPoints = [];
    self.controlPoints = [];

    self.lineMode = LINEMODE.STRAIGHT;
    self.allowShine = false;
    self.allowSwitchLineMode = true;
    //------------------------------------
    /**
     * 线连接的不同形式，dragTo的方式不一样.
     *
     * @override
     */
    const dragTo = self.dragTo;
    self.dragTo = (position) => {
        const fromShape = self.getFromShape();
        const toShape = self.getToShape();
        if (fromShape && toShape) {
            whenLinkedTwoShapes(fromShape, toShape, position);
        } else if (fromShape) {
            whenLinkedOneShape(fromShape, position);
        } else if (toShape) {
            whenLinkedOneShape(toShape, position);
        } else {
            // 线没有连接图形，只是被选中了，平移
            self.moveTo(self.x + position.deltaX, self.y + position.deltaY);
            dragTo.call(self, position);
        }
    };

    const whenLinkedTwoShapes = (fromShape, toShape, position) => {
        if (fromShape.isFocused && toShape.isFocused) {
            // 两个图形都被选中，平移.
            self.moveTo(self.x + position.deltaX, self.y + position.deltaY);
            dragTo.call(self, position);
        } else if (fromShape.isFocused || toShape.isFocused) {
            // 只有一个图形被选中，follow.
            self.onEffect();
        } else {
            // 两个图形都未被选中，不动.
        }
    };

    const whenLinkedOneShape = (linkedShape, position) => {
        if (linkedShape.isFocused) {
            if (self.isFocused) {
                // 线也被选中，平移
                self.moveTo(self.x + position.deltaX, self.y + position.deltaY);
                dragTo.call(self, position);
            } else {
                // 线未被选中，follow
                self.onEffect();
            }
        } else {
            // 图形未被选中，不动
        }
    };

    self.from = () => {
        return {x: self.x, y: self.y};
    };

    self.to = () => {
        return {x: self.x + self.width, y: self.y + self.height};
    };

    self.beginDrag = () => {
        self.mousedownConnector = self.connectors.find(c => c.isType('to'));
        if (self.mousedownConnector === undefined) {
            self.mousedownConnector = null;
        }
        self.isNew = true;
    };

    self.getFromShape = () => self.page.sm.getShapeById(self.fromShape);

    self.getToShape = () => self.page.sm.getShapeById(self.toShape);

    self.getDefinedFromConnector = () => {
        if (self.definedFromConnector === "" || self.fromShape === "") {
            return null;
        }

        const fromShape = self.getFromShape();
        if (!fromShape) {
            return null;
        }

        return fromShape.getConnectors().find(c => c.direction.key === self.definedFromConnector);
    };

    self.getDefinedToConnector = () => {
        if (self.definedToConnector === "" || self.toShape === "") {
            return null;
        }

        const toShape = self.getToShape();
        if (!toShape) {
            return null;
        }
        return toShape.getConnectors().find(c => c.direction.key === self.definedToConnector);
    };

    self.fromConnector = null;// 起始connector，是指line的connector
    self.toConnector = null;// 结束connector
    self.linkAble = true;// 是否可以连接其他形状
    self.allowLink = false;// 不允许被其他line连接
    self.fromShapeConnector = null;// 实际连接的对方shape.connector,如果有指定连接(definedFromConnector/definedToConnector)，那么实际连接就是指定连接，如果没有指定连接，实际连接是距离最近的connector
    self.toShapeConnector = null;

    /**
     * 做一些反序列化之后的初始化工作.
     *
     * @override
     */
    const deSerialized = self.deSerialized;
    self.deSerialized = () => {
        deSerialized.apply(self);
        !self.arrowBeginPoint && (self.arrowBeginPoint = {x: 0, y: 0});
        !self.arrowEndPoint && (self.arrowEndPoint = {x: 0, y: 0});
        !self.brokenPoints && (self.brokenPoints = []);
    };

    /**
     * 获取实际连接的起始图形连接点.
     *
     * @return {null|*} 连接点.
     */
    self.getFromShapeConnector = () => {
        return self.fromShapeConnector;
    };

    /**
     * 获取实际连接的结束图形连接点.
     *
     * @return {null|*} 连接点.
     */
    self.getToShapeConnector = () => {
        return self.toShapeConnector;
    }

    let fromMoving = false;// from connector is dragging
    self.toMoving = false;// to connector is dragging

    // null object for easy programming
    self.connectingShape = {
        id: "", linking: false, linkingConnector: null, type: null, render: () => {
        }
    };

    self.validateLinking = (target, x, y) => {
        const WIDTH = 10;
        // in border
        if (target.drawer.containsBorder(x, y)) {
            return true;
        }
        // in center
        let x1 = target.x + target.width / 2 - WIDTH;
        x1 < 0 ? x1 = 0 : x1;
        let y1 = target.y + target.height / 2 - WIDTH
        y1 < 0 ? y1 = 0 : y1;
        const width = x1 === 0 ? target.width : 2 * WIDTH;
        const height = y1 === 0 ? target.hight : 2 * WIDTH;
        return isPointInRect({x, y}, {x: x1, y: y1, width, height});
    };

    self.shapeLinking = (x, y) => {
        if (!self.linkAble) {
            return;
        }

        const connectingShape = self.page.find(x, y, s => s.allowLink && self.validateLinking(s, x, y));
        self.shapeDelinking(connectingShape);
        self.connectingShape = connectingShape;
        if (self.connectingShape && self.connectingShape !== self.page) {
            self.connectingShape.linking = true;

            // 这里必须判断connector是否可以连接.
            self.connectingShape.linkingConnector = self.connectingShape.getMouseOnConnector(x, y, c => c.connectable && c.allowToLink);
            self.connectingShape.render();// .drawer.draw();//invalidate();save some performance
        }
    };

    self.shapeDelinking = (connectingShape) => {
        if (self.connectingShape && self.connectingShape !== connectingShape && self.connectingShape !== self.page) {
            self.connectingShape.linking = false;
            self.connectingShape.linkingConnector = null;
            self.connectingShape.render();
        }
    };

    self.containerChanged = (preValue, value) => {
        if (self.container === self.fromShape.id) {
            self.container = preValue;
        }// don't link self container
    };

    self.getCurvePoint1 = () => {
        return {x: self.width / 3 + self.curvePoint1.x, y: self.height / 3 + self.curvePoint1.y};
    };

    self.getCurvePoint2 = () => {
        return {x: 2 * self.width / 3 + self.curvePoint2.x, y: 2 * self.height / 3 + self.curvePoint2.y};
    };

    self.getBezierControlPoint1 = (fromX, fromY, toX, toY, ox, oy) => {
        return {x: (fromX < toX ? (fromX + toX) / 2 : fromX - (toX - fromX) / 2) + ox, y: fromY + oy};
    };

    self.getBezierControlPoint2 = (fromX, fromY, toX, toY, ox, oy) => {
        return {x: (fromX < toX ? (fromX + toX) / 2 : toX + (toX - fromX) / 2) + ox, y: toY + oy};
    };

    self.calculateBezierToPoint = (fromX, fromY, toX, toY, ox, oy) => {
        return {x: toX + ox, y: toY + oy};
    };

    self.switchLineMode = () => {
        self.lineMode = self.lineMode.next();
    };

    self.resized = () => {
        self.effectGroup();
        self.invalidateAlone();
    };

    self.firstRelease = position => {
        if (self.isNew) {
            if (self.width === 1 && self.height === 1) {
                self.resize(100, 100);
                self.invalidateAlone();
            }
            delete self.isNew;
            position.context.command = "addShape";// 正在新增shape
            position.context.shapes = [{shape: self}];// shape added
        }
    };

    /**
     * 初始化直线connector.
     */
    const initStraightConnectors = () => {
        let cFrom = connector(self, (s, l) => 0, (s, l) => 0, s => DIRECTION.L);
        cFrom.type = "from";
        cFrom.moving = (deltaX, deltaY) => {// from point
            let value = self.page.disableReact;
            self.page.disableReact = true;
            self.moveTo(self.x + deltaX, self.y + deltaY);
            self.resize(self.width - deltaX, self.height - deltaY);
            self.shapeLinking(self.from().x, self.from().y);
            fromMoving = true;
            self.page.disableReact = value;
            self.follow();
        };

        cFrom.onReturnDrag = (x, mouseOffsetX, y, mouseOffsetY) => {
            let value = self.page.disableReact;
            self.page.disableReact = true;
            const newXLocation = x - mouseOffsetX;
            const newYLocation = y - mouseOffsetY;
            self.moveTo(newXLocation, newYLocation);
            self.resize(self.to().x - newXLocation, self.to().y - newYLocation);
            self.shapeLinking(self.from().x, self.from().y);
            fromMoving = true;
            self.page.disableReact = value;
            self.follow();
        };

        cFrom.release = (position) => {
            if (!self.linkAble) {
                return;
            }
            if (self.connectingShape === undefined) {
                return;
            }
            if (self.fromShape !== self.connectingShape.id) {
                if (self.connectingShape === self.page) {
                    self.fromShape = "";
                } else {
                    self.fromShape = self.connectingShape.id;
                }
            }
            self.definedFromConnector = self.connectingShape.linkingConnector ? self.connectingShape.linkingConnector.direction.key : "";
            self.shapeDelinking();
            fromMoving = false;
            self.toMoving = false;
            self.follow();

            // for history
            const dirty = position.context.shapes.find(s => s.shape === self);
            if (dirty === undefined) {
                return;
            }
            dirty.fromShape.preValue = self.preFromShape;
            dirty.fromShape.value = self.fromShape;
            dirty.fromConn.preValue = self.preDefinedFromConnector;
            dirty.fromConn.value = self.definedFromConnector;
            dirty.x.value = self.x;
            dirty.y.value = self.y;
            dirty.width.value = self.width;
            dirty.height.value = self.height;
        };
        self.fromConnector = cFrom;

        const cTo = connector(self, (s, l) => s.width, (s, l) => s.height, s => DIRECTION.L, s => s.visible, s => s.resizeable, s => true);
        cTo.type = "to";
        cTo.moving = (deltaX, deltaY) => {// to point
            let value = self.page.disableReact;
            self.page.disableReact = true;
            self.resize(self.width + deltaX, self.height + deltaY);
            self.shapeLinking(self.to().x, self.to().y);
            self.toMoving = true;
            self.page.disableReact = value;
            self.follow();
        };

        cTo.onReturnDrag = (x, mouseOffsetX, y, mouseOffsetY) => {
            let value = self.page.disableReact;
            self.page.disableReact = true;
            self.resize(x - self.from().x, y - self.from().y);
            self.shapeLinking(self.to().x, self.to().y);
            self.toMoving = true;
            self.page.disableReact = value;
            self.follow();
        };

        /**
         * @override
         */
        const toConnectorOnMouseDrag = cTo.onMouseDrag;
        cTo.onMouseDrag = (position) => {
            const focusedShapes = self.page.getFocusedShapes();

            // 单独选中了线，可以拖动
            // 多选，并且线的toShape为空，可以拖动
            const isSelectSelf = focusedShapes.length === 1 && focusedShapes[0] === self;
            if (isSelectSelf || (focusedShapes.length > 1 && !self.toShape)) {
                toConnectorOnMouseDrag.apply(cTo, [position]);
            }
        };

        cTo.release = (position) => {
            if (!self.linkAble) {
                return;
            }
            if (self.connectingShape === undefined) {
                return;
            }
            if (self.toShape !== self.connectingShape.id) {
                if (self.connectingShape === self.page) {
                    self.toShape = "";
                } else {
                    self.toShape = self.connectingShape.id;
                }
            }
            self.definedToConnector = self.connectingShape.linkingConnector ? self.connectingShape.linkingConnector.direction.key : "";
            self.shapeDelinking();
            fromMoving = false;
            self.toMoving = false;
            self.follow();

            // for history
            const dirty = ((position.context.command === "addShape") ? undefined : position.context.shapes.find(s => s.shape === self));
            if (dirty !== undefined) {
                dirty.toShape.preValue = self.preToShape;
                dirty.toShape.value = self.toShape;
                dirty.toConn.preValue = self.preDefinedToConnector;
                dirty.toConn.value = self.definedToConnector;
                dirty.width.value = self.width;
                dirty.height.value = self.height;
            }
            self.firstRelease(position);
        }
        self.toConnector = cTo;
    };

    /**
     * 初始化文本connector.
     */
    const initTextConnector = () => {
        if (self.hideText) {
            return;
        }

        // 移动线条、node之后点击会初始化：每次都计算 刷新页面会初始化：记录上次的值
        const cText = connector(self,
            (s, c) => -10 + s.width / 2 + (self.textX ? self.textX : 0),
            (s, c) => -10 + s.height / 2 + (self.textY ? self.textY : 0),
            s => DIRECTION.D,
            s => s.visible,
            s => s.resizeable,
            s => true);
        cText.type = "text";
        cText.radius = 4;
        cText.moving = (deltaX, deltaY) => {
            !self.textX && (self.textX = 0);
            !self.textY && (self.textY = 0);
            self.textX += deltaX;
            self.textY += deltaY;
            self.manageConnectors();
            self.invalidate();
        };
        self.textConnector = cText;
    };

    /**
     * 初始化曲线connector.
     */
    const initCurveConnectors = () => {
        const c1 = connector(self,
            s => self.getCurvePoint1().x,
            s => self.getCurvePoint1().y,
            s => DIRECTION.L,
            s => s.visible && s.lineMode.type === LINEMODE.CURVE.type,
            s => true,
            s => false
        );
        c1.type = "curve begin";
        c1.moving = (deltaX, deltaY) => {
            // only first level property change will trigger property change react
            self.curvePoint1 = {x: self.curvePoint1.x + deltaX, y: self.curvePoint1.y + deltaY};
            self.invalidateAlone();
        };

        const c2 = connector(self,
            s => self.getCurvePoint2().x,
            s => self.getCurvePoint2().y,
            s => DIRECTION.L,
            s => s.visible && s.lineMode.type === LINEMODE.CURVE.type,
            s => true,
            s => false
        );
        c2.type = "curve end";
        c2.moving = (deltaX, deltaY) => {
            // only first level property change will trigger property change react
            self.curvePoint2 = {x: self.curvePoint2.x + deltaX, y: self.curvePoint2.y + deltaY};
            self.invalidateAlone();
        };
    };

    /**
     * 初始化折线connector.
     */
    const initBrokenConnectors = () => {
        if (self.lineMode !== LINEMODE.BROKEN) {
            return;
        }
        // 初始化时，只有当brokenPoints的长度等于0，才生成broken point.
        if (!self.brokenPoints || self.brokenPoints.length === 0) {
            self.lineHelper.brokenLineHelper.generateBrokenPoints(self);
        }
        generateControlPoints();
    };

    /**
     * 生成控制点.
     */
    const generateControlPoints = () => {
        // 不管是否需要生成controlPoints，都先删除所有的controlPoint.
        const connectors = self.getConnectors();
        self.connectors = connectors.filter(c => !c.type.startsWith("controlPoint"));
        self.controlPoints = [];

        // 只有同时连接两个图形时，才需要生成controlPoint；否则，不生成controlPoint，直接返回.
        if (self.fromShape === "" || self.toShape === "") {
            return;
        }

        // 没有或只有一个转折点，直接返回，不用生成controlPoints.
        if (self.brokenPoints.length <= 1) {
            return;
        }

        // 如果转折点之间距离小于10,不生成controlPoint
        const minGenerateControlPointDistance = 10;
        // 创建controlPoint.
        for (let i = 1; i < self.brokenPoints.length; i++) {
            const controlPointDirection = self.brokenPoints[i - 1].x === self.brokenPoints[i].x ? DIRECTION.H : DIRECTION.V;
            const distanceX = Math.abs(self.brokenPoints[i - 1].x - self.brokenPoints[i].x);
            const distanceY = Math.abs(self.brokenPoints[i - 1].y - self.brokenPoints[i].y);
            const hCondition = controlPointDirection === DIRECTION.H &&
                distanceY < minGenerateControlPointDistance;
            const vCondition = controlPointDirection === DIRECTION.V &&
                distanceX < minGenerateControlPointDistance;
            if (hCondition || vCondition) {
                continue;
            }
            const controlPoint = connector(self,
                () => (self.brokenPoints[i - 1].x + self.brokenPoints[i].x) / 2,
                () => (self.brokenPoints[i - 1].y + self.brokenPoints[i].y) / 2,
                () => controlPointDirection,
                () => self.lineMode === LINEMODE.BROKEN,
                () => true,
                () => false);
            controlPoint.type = "controlPoint-" + i;
            controlPoint.offsetX = 0;
            controlPoint.offsetY = 0;
            controlPoint.moving = (deltaX, deltaY) => {
                controlPoint.direction === DIRECTION.V ? controlPoint.offsetY += deltaY : controlPoint.offsetX += deltaX;
                if (controlPoint.direction === DIRECTION.V) {
                    self.brokenPoints[i - 1].y += deltaY;
                    self.brokenPoints[i].y += deltaY;
                } else {
                    self.brokenPoints[i - 1].x += deltaX;
                    self.brokenPoints[i].x += deltaX;
                }

                self.invalidate();

                // 发生改变时，通知协同方，发生了变化，否则会导致主画布中线发生了变化，但是协同方未变化.
                self.brokenPoints = [...self.brokenPoints];
            };

            controlPoint.onReturnDrag = (x, mouseOffsetX, y, mouseOffsetY) => {
                controlPoint.direction === DIRECTION.V ? controlPoint.offsetY = y - self.y : controlPoint.offsetX = x - self.x;
                if (controlPoint.direction === DIRECTION.V) {
                    self.brokenPoints[i - 1].y = y - self.y;
                    self.brokenPoints[i].y = y - self.y;
                } else {
                    self.brokenPoints[i - 1].x = x - self.x;
                    self.brokenPoints[i].x = x - self.x;
                }
                self.invalidate();
                // 发生改变时，通知协同方，发生了变化，否则会导致主画布中线发生了变化，但是协同方未变化.
                self.brokenPoints = [...self.brokenPoints];
            };

            self.controlPoints.push(controlPoint);
        }
    };

    /**
     * @override
     */
    self.initConnectors = () => {
        self.connectors = [];

        // 初始化connector之前，先初始化fromShapeConnector和toShapeConnector.
        self.fromShapeConnector = self.getDefinedFromConnector();
        self.toShapeConnector = self.getDefinedToConnector();

        initStraightConnectors();
        initCurveConnectors();
        initBrokenConnectors();
        initTextConnector();
    };

    /**
     * @override
     */
    const reset = self.reset;
    self.reset = () => {
        reset.apply(self);
        fromMoving = false;
        self.toMoving = false;
    };

    const checkContainer = () => {
        self.page.moveToContainer(self);
    };

    /**
     * @override
     */
    const moveTo = self.moveTo;
    self.moveTo = (x, y) => {
        if (self.parentMoving) {
            self.x = x;
            self.y = y;
            return;
        }
        checkContainer();
        moveTo.call(self, x, y);
    };

    /**
     * @override
     */
    self.onReturnDrag = (x, mouseOffsetX, y, mouseOffsetY) => {
        if (self.parentMoving) {
            self.x = x - mouseOffsetX;
            self.y = y - mouseOffsetY;
            return;
        }
        if (self.fromShape !== "" || self.toShape !== "") {
            self.follow();
        } else {
            checkContainer();
        }
        if (self.mousedownConnector) {
            self.mousedownConnector.onReturnDrag(x, mouseOffsetX, y, mouseOffsetY);
        }
    };

    /**
     * 跟随图形移动.
     */
    self.follow = () => {
        // 自己连自己，由north connector连接east connector
        const noDefinedConnector = self.definedFromConnector === "" && self.definedToConnector === "";
        if (self.fromShape !== "" && self.fromShape === self.toShape && noDefinedConnector) {
            return;
        }

        // pair不存在可能是line还没有初始化完成.
        const pair = getPair();
        if (!pair) {
            return;
        }

        self.x = pair.from.x;
        self.y = pair.from.y;
        self.fromShapeConnector = pair.from.connector;
        self.toShapeConnector = pair.to.connector;
        self.resize(pair.to.x - pair.from.x, pair.to.y - pair.from.y);
        if (self.lineMode === LINEMODE.BROKEN) {
            self.lineHelper.brokenLineHelper.generateBrokenPoints(self);
            generateControlPoints();
        }
        self.invalidate();
        checkContainer();
    };

    const getPair = () => {
        if (!self.fromConnector || !self.toConnector) {
            return null;
        }

        // 找到fromShape和toShape间距离最短的connector，如果没有连接的shape，那么就是line自己的from，to connector
        const fromPosition = self.fromConnector.getPosition();
        const toPosition = self.toConnector.getPosition();
        let from = [{x: fromPosition.x, y: fromPosition.y, connector: null}];
        let to = [{x: toPosition.x, y: toPosition.y, connector: null}];
        if (self.getFromShape() && !fromMoving) {
            const fc = self.getDefinedFromConnector()
            if (fc !== undefined && fc !== null) {
                const fcPosition = fc.getPosition();
                from = [{x: fcPosition.x, y: fcPosition.y, connector: fc}];
            } else {
                from = self.getFromShape().getConnectors().filter(connector => connector.connectable)
                    .map(c => {
                        const position = c.getPosition();
                        return {x: position.x, y: position.y, connector: c}
                    });
            }
        }

        if (self.needGetToConnector()) {
            const tc = self.getDefinedToConnector();
            if (tc !== undefined && tc !== null) {
                to = [{
                    x: tc.getPosition().x, y: tc.getPosition().y, connector: tc
                }];
            } else {
                to = self.getToShape().getConnectors().filter(connector => connector.connectable)
                    .map(c => {
                        let position = c.getPosition();
                        return {x: position.x, y: position.y, connector: c};
                    });
            }
        }

        return closestPoint(from, to);
    };

    /**
     * 是否需要计算
     *
     * @return {boolean}
     */
    self.needGetToConnector = () => {
        return self.getToShape() && !self.toMoving;
    };

    /**
     * 由于line所连接的图形拖动导致line需要变化时，调用此方法.
     */
    self.onEffect = () => {
        self.controlPoints.forEach(ep => {
            ep.offsetX = 0;
            ep.offsetY = 0;
        });
        self.follow();
    };

    self.getEditRect = () => {
        return self.drawer.getEditRect();
    };

    /**
     * @override
     */
    const manageConnectors = self.manageConnectors;
    self.manageConnectors = () => {
        manageConnectors.apply(self);

        const connectors = self.connectors;
        const m1 = -Math.min(...connectors.map(c => convertY(c, self)));// 上
        const m2 = -Math.min(...connectors.map(c => convertX(c, self)));// 左
        const m3 = Math.max(...connectors.map(c => convertY(c, self))) - Math.abs(self.height);// 下
        const m4 = Math.max(...connectors.map(c => convertX(c, self))) - Math.abs(self.width);// 右
        const max = Math.max(m1, m2, m3, m4);
        self.margin = max + 20;
    };

    self.getFrame = () => {
        const connectors = self.connectors;
        const validConnectors = connectors.filter(c => c.type.startsWith("controlPoint") || c.type === "to" || c.type === "from");
        const minX = Math.min(...validConnectors.map(c => c.x));
        const minY = Math.min(...validConnectors.map(c => c.y));
        const maxX = Math.max(...validConnectors.map(c => c.x));
        const maxY = Math.max(...validConnectors.map(c => c.y));
        return {x: self.x + minX, y: self.y + minY, width: maxX - minX, height: maxY - minY};
    };

    /**
     * @override
     */
    const keyPressed = self.keyPressed;
    self.keyPressed = function (e) {
        if ((e.ctrlKey || e.metaKey) && (e.code === "KeyL")) {
            if (self.allowSwitchLineMode) {
                self.switchLineMode();
            }
            return false;
        }
        return keyPressed.call(self, e);
    };

    /**
     * @override
     */
    const invalidate = self.invalidate;
    self.invalidate = () => {
        if (self.lineMode.next === undefined) {
            self.lineMode = LINEMODE[self.lineMode.type.toUpperCase()];
        }
        invalidate.apply(self);
    };

    // --------------------------serialization & detection------------------------------
    self.addDetection(["lineMode"], (property, value, preValue) => {
        // 保障后续的比较时，同样来自于LINEMODE， 否则反序列等场景，不是同一个指针地址
        self.lineMode = LINEMODE[value.type.toUpperCase()]
        self.invalidate();
    });
    self.addDetection(["fromShape"], (property, value, preValue) => {
        self.preFromShape = preValue;
    });
    self.addDetection(["toShape"], (property, value, preValue) => {
        self.preToShape = preValue;
    });
    self.addDetection(["definedFromConnector"], (property, value, preValue) => {
        self.preDefinedFromConnector = preValue;
    });
    self.addDetection(["definedToConnector"], (property, value, preValue) => {
        self.preDefinedToConnector = preValue;
    });
    self.addDetection(["beginArrow", "endArrow", "borderColor", "lineWidth"], (property, value, preValue) => {
        if (preValue === value) {
            return;
        }
        self.render();
    });
    //----------------------------------------------------------------------------------

    self.containerAllowed = parent => {
        if (parent.isTypeof('page')) {
            return true;
        }

        /*
         * 如果容器是line所连接的图形，那么该容器不能作为图形的容器.
         */
        const fromShape = self.getFromShape();
        const toShape = self.getToShape();
        if (fromShape === parent || toShape === parent) {
            return false;
        }

        let bound = parent.getBound();
        return isPointInRect(self.from(), bound) && isPointInRect(self.to(), bound);
    }

    /**
     * 更改 line 的起始连接点到指定 Shape 和 connector
     */
    self.connectFrom = (shapeId, connector) => {
        self.fromShape = shapeId;
        self.definedFromConnector = connector;
        self.follow();
        return self;
    };

    /**
     * 更改 line 的终止连接点到指定 Shape 和 connector
     */
    self.connectTo = (shapeId, connector) => {
        self.toShape = shapeId;
        self.definedToConnector = connector;
        self.follow();
        return self;
    };

    /**
     * 重写获取配置方法.
     * 1、line不需要corerRadius配置.
     */
    const getConfigurations = self.getConfigurations;
    self.getConfigurations = () => {
        const configurations = getConfigurations.apply(self);
        configurations.remove(c => c.field === "cornerRadius" || c.field === "rotateDegree" || c.field === "backColor");
        return configurations;
    }

    /**
     * 重写render方法.
     * 1、线的width和height有可能是负数，所以当线被删除时，移动鼠标，触发page的mouseInShape改变，会导致调用已被删除的line对象的render方法。
     * 2、由于此时线的width或height是负数，就会触发move方法，导致线被重新绘制到了page的container中.
     * 3、这里重写render方法，当container为空字符串时，不执行render方法.
     */
    const render = self.render;
    self.render = () => {
        if (self.container === "") {
            return;
        }
        render.apply(self);
    };

    /**
     * 当fromShape等属性发生变化时，需要进行follow操作.
     *
     * @override
     */
    const onMessageDataChange = self.onMessageDataChange;
    self.onMessageDataChange = (data) => {
        const hasShape = data.hasOwnProperty("fromShape") || data.hasOwnProperty("toShape");
        const hasConnector = data.hasOwnProperty("definedFromConnector") || data.hasOwnProperty("definedToConnector");
        if (hasShape || hasConnector) {
            self.follow();
        } else {
            if (self.lineMode === LINEMODE.BROKEN) {
            // 协同时，收到消息，需要重新生成controlPoints，否则，会导致消息接收端将margin改为20，从而无法选中超出line矩形区域的部分.
                generateControlPoints();
            }
            onMessageDataChange.apply(self, [data]);
        }
    };

    self.text = "";

    return self;
};

const convertX = (connector, line) => {
    if (line.width < 0) {
        return connector.x - line.width;
    } else {
        return connector.x;
    }
};

const convertY = (connector, line) => {
    if (line.height < 0) {
        return connector.y - line.height;
    } else {
        return connector.y;
    }
};

export {line};
