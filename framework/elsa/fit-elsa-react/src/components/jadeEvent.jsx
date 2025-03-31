/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {isPointInRect, line, LINEMODE} from '@fit-elsa/elsa-core';
import {emptyStatusManager} from '@/components/base/emptyStatusManager.js';

/**
 * jade连线对象.
 *
 * @override
 */
const jadeEvent = (id, x, y, width, height, parent, drawer) => {
    let self = line(id, x, y, width, height, parent, drawer);
    self.type = "jadeEvent";
    self.serializedFields.batchAdd("runnable");
    self.borderWidth = 1;
    self.beginArrow = false;
    self.endArrow = true;
    self.lineMode = LINEMODE.AUTO_CURVE;
    self.borderColor = "#B1B1B7";
    self.mouseInBorderColor = "#B1B1B7";
    self.allowSwitchLineMode = false;
    self.runnable = true;
    self.statusManager = emptyStatusManager(self);

    /**
     * 保证曲线的位置在shape的下方
     *
     * @override
     */
    const getIndex = self.getIndex;
    self.getIndex = () => {
        return getIndex.call(self) - 200;
    };

    /**
     * 当页面加载完成之后，如果存在toShape，需要获取toShape的对象，
     * 在release的时候方便调用其offConnect方法.
     */
    self.onPageLoaded = () => {
        if (self.toShape) {
            self.currentToShape = self.page.getShapeById(self.toShape);
        }
    };

    const onShapeOffConnect = () => {
        self.page.onShapeOffConnect && self.page.onShapeOffConnect();
        self.currentToShape && self.currentToShape.offConnect();
    };

    const onShapeOnConnect = () => {
        self.page.onShapeConnect && self.page.onShapeConnect();
        self.currentToShape && self.currentToShape.onConnect();
    };

    /**
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.call(self);

        // 不展示起始连接点.
        self.fromConnector.visible = false;
        // 结束连接点透明
        self.toConnector.direction.color = "transparent";
        self.toConnector.strokeStyle = "transparent";

        /**
         * @override
         */
        const toRelease = self.toConnector.release;
        self.toConnector.release = position => {

            // 正式release之前，先保存release之前的toShape.
            self.isFocused = false;
            toRelease.call(self.toConnector, position);

            if (self.toShape === "") {
                onShapeOffConnect(); // 如果是把存在的线拖走，则会断掉之前的连接
                self.remove();
            } else {
                if (self.currentToShape) {
                    // toShape未发生变化，不触发任何connect事件.
                    if (self.currentToShape.id !== self.toShape) {
                        onShapeOffConnect();
                    } else {
                        return;
                    }
                }
                // 在每一次release时，记录当前正连接的toShape.
                self.currentToShape = self.getToShape();
                onShapeOnConnect();
                if (!self.page.canDragIn(self)) { // connector不允许拖入，或者connector被已经存在的线占用了
                    self.remove();
                }
            }
        };
        self.toConnector.radius = 4;

        /**
         * resize不再通过delta计算，否则会导致线被移出linking图形时，产生不跟手的效果.
         *
         * @override
         */
        self.toConnector.moving = (deltaX, deltaY, x, y) => {
            // moving过程中，不应该产生dirties事件，因此这里需要ignoreChange.
            self.ignoreChange(() => {
                let value = self.page.disableReact;
                self.page.disableReact = true;
                const from = self.from();
                self.resize(x - from.x, y - from.y);
                self.shapeLinking(self.to().x, self.to().y);
                self.toMoving = true;
                self.page.disableReact = value;
                if (self.connectingShape && self.connectingShape.linkingConnector) {
                    self.definedToConnector = self.connectingShape.linkingConnector.direction.key;
                    self.toShape = self.connectingShape.id;
                } else {
                    self.definedToConnector = "";
                    self.toShape = "";
                }
            });
            self.follow();
        };
    };

    /**
     * @override
     */
    const remove = self.remove;
    self.remove = (source) => {
        const toShape = self.getToShape();
        const removed = remove.apply(self, [source]);
        self.page.onShapeOffConnect && self.page.onShapeOffConnect();
        toShape && toShape.offConnect();
        return removed;
    };

    /**
     * 同时连接开始和结束节点.
     *
     * @param fromShapeId 开始节点id.
     * @param fromConnector 开始节点的connector.
     * @param toShapeId 结束节点的id.
     * @param toShapeConnector 结束节点的connector.
     */
    self.connect = (fromShapeId, fromConnector, toShapeId, toShapeConnector) => {
        self.fromShape = fromShapeId;
        self.definedFromConnector = fromConnector;
        self.toShape = toShapeId;
        self.definedToConnector = toShapeConnector;
        self.follow();
    };

    /**
     * 只要鼠标进入到图形中，即视为可连接.
     *
     * @override
     */
    self.validateLinking = (shape, x, y) => {
        return isPointInRect({x, y}, shape.getBound());
    };

    /**
     * @override
     */
    self.shapeLinking = (x, y) => {
        if (!self.linkAble) {
            return;
        }
        const connectingShape = self.page.find(x, y, s => s.allowLink && self.validateLinking(s, x, y));
        self.shapeDelinking(connectingShape);
        self.connectingShape = connectingShape;
        if (self.connectingShape && self.connectingShape !== self.page) {
            self.connectingShape.linking = true;

            // 找到距离最近的可被连接的connector.
            self.connectingShape.linkingConnector = self.connectingShape.getClosestConnector(x, y, c => c.connectable && c.allowToLink);
            self.connectingShape.render();
        }
    };

    /**
     * @override
     */
    self.needGetToConnector = () => {
        return self.getToShape();
    };

    /**
     * @override
     */
    const load = self.load;
    self.load = (ignoreFilter) => {
        load.apply(self, [ignoreFilter]);
        /**
         * jadeEvent的坐标以及宽高变化不触发dirties.
         *
         * @override
         */
        const propertyChanged = self.propertyChanged;
        self.propertyChanged = (property, value, preValue) => {
            const positionNotChanged = property === "x" || property === "y";
            const sizeNotChanged = property === "width" || property === "height";
            if (positionNotChanged || sizeNotChanged) {
                return;
            }
            propertyChanged.apply(self, [property, value, preValue]);
        };
    };

    /**
     * 获取目标图形.
     *
     * @returns {*} 目标图形.
     */
    self.getToNode = () => {
        return self.page.sm.getShapeById(self.toShape);
    };

    return self;
};

export {jadeEvent};
