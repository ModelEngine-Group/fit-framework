/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DIRECTION, FLOWABLE_STATE_STATUS, FLOWABLE_TRIGGER_MODE} from '../../../common/const.js';

import {rectangle} from '../../../core/rectangle.js';
import {container} from '../../../core/container.js';

import {modeRegion, taskStatRegion} from '../hitregions/hitregion.js';
import {group} from '../../../core/group.js';
import {pluginMeta} from '../../../core/configuration/pluginMeta.js';
import {addCommand} from '../../../core/commands.js';

/**
 * flowable中所有节点的父节点，该节点为抽象节点，不能实例化
 * 辉子 2020
 */
const node = (id, x, y, width, height, parent, isContainer, drawer) => {
    let self = null;
    if (isContainer) {
        self = container(id, x, y, width, height, parent, drawer);
    } else {
        self = rectangle(id, x, y, width, height, parent, drawer);
    }
    self.type = "node";
    self.namespace = "flowable";
    //self.borderWidth = 1;
    self.pad = 6;
    self.status = FLOWABLE_STATE_STATUS.RUNNING;
    self.allowFromLink = true;
    self.allowToLink = true;
    self.rotateAble = false;
    self.triggerMode = FLOWABLE_TRIGGER_MODE.AUTO;
    self.ignoreDefaultContextMenu = true;

    self.enableAnimation = true;
    self.eventType = "event";
    self.warningTask = 0;
    self.runningTask = 0;
    self.completedTask = 0;
    self.textEditable = true;
    self.initEvent = line => {
    };

    /**
     * 重写afterRemoved方法，当图形都删除之后，需要删除对应的连线.
     *
     * @override
     */
    const afterRemoved = self.afterRemoved;
    self.afterRemoved = (source) => {
        afterRemoved.apply(self, [source]);
        self.page.sm.getShapes(s => s.fromShape === self.id || s.toShape === self.id).forEach(s => s.remove());
    };

    /**
     * 在取消选中后，线条重新刷新
     */
    const unSelect = self.unSelect;
    self.unSelect = () => {
        unSelect.apply(self);
        self.effectLines();
    }

    /**
     * @override
     */
    const resize = self.resize;
    self.resize = (width, height) => {
        if (width === 1 && height === 1) {
            return;
        }
        resize.apply(self, [width, height]);
    };

    /**
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.apply(self);
        self.resizeConnector = self.connectors.find(c => c.isType('rightBottom'));
        self.connectors.remove(c => c.type === "connection");
        self.connectors.remove(c => c.direction.key === DIRECTION.NE.key ||
            c.direction.key === DIRECTION.NW.key ||
            c.direction.key === DIRECTION.SW.key ||
            c.direction.key === DIRECTION.SE.key);
        self.connectors.forEach(c => {
            self.activeConnector(c);
        });
    };

    /**
     * 将connector变为主动可拖动出线的connector.
     *
     * @param conn connector对象.
     */
    self.activeConnector = (conn) => {
        conn.radius = 4;

        // 重写getDirection方法.
        const getDirection = conn.getDirection;
        conn.getDirection = () => {
            let d = getDirection.apply(conn);
            let nd = {};
            for (let f in d) {
                nd[f] = d[f];
            }
            nd.cursor = "crosshair";
            return nd;
        };

        conn.getEnable = () => true;

        // 重写getVisibility方法.
        const getVisibility = conn.getVisibility;
        conn.getVisibility = () => getVisibility.apply(conn) && self.getContainer().type !== "parallel";

        // 拖动时，能拖出线.
        conn.moving = (deltaX, deltaY, x, y) => {
            if (!conn.allowFromLink) {
                return;
            }
            let e = self.page.createNew(self.eventType, x, y);
            e.fromShape = self.id;
            e.definedFromConnector = conn.direction.key;
            self.unSelect();
            self.initEvent(e);
            e.container = self.container;
            e.isFocused = true;
            e.mousedownConnector = e.toConnector;
            self.page.mousedownShape = e;
            e.reset();
        };
    };

    /**
     * @override
     */
    self.resizeOrRotate = (position) => {
        self.page.cursor = self.mousedownConnector.direction.cursor;
        const originalX = position.x;
        const originalY = position.y;
        const xDiff = originalX - self.x - self.width / 2;
        const yDiff = originalY - self.y - self.height / 2;
        position.x = xDiff + self.width / 2 + self.x;
        position.y = yDiff + self.height / 2 + self.y;
        self.mousedownConnector.onMouseDrag(position);
        position.x = originalX;
        position.y = originalY;
    };

    const onMouseUp = self.onMouseUp;
    self.onMouseUp = position => {
        onMouseUp.apply(self, [position]);
        if (self.getContainer().isType('parallel')) {
            self.getContainer().invalidate();
        }
    };

    const positionEnd = self => {
        const OFFSET = 50;
        const END_WIDTH = 30;
        let pe = self.page.shapes.find(s => s.toShape === self.id);
        if (pe === undefined) {
            return {x: self.x + self.width + OFFSET, y: self.y + 1};
        } else {
            if (pe.toShapeConnector.direction.key === "W") {
                return {x: self.x + self.width + OFFSET, y: self.y + 1};
            }
            if (pe.toShapeConnector.direction.key === "E") {
                return {x: self.x - OFFSET - END_WIDTH, y: self.y + 1};
            }
            if (pe.toShapeConnector.direction.key === "N") {
                return {
                    x: self.x + self.width / 2 - END_WIDTH / 2 + 1, y: self.y + self.height + OFFSET
                };
            }
            if (pe.toShapeConnector.direction.key === "S") {
                return {
                    x: self.x + self.width / 2 - END_WIDTH / 2 + 1, y: self.y - OFFSET - 30
                };
            }
          return undefined;
        }
    };

    const keyPressed = self.keyPressed;
    self.keyPressed = function (e) {
        if (keyPressed.apply(self, [e]) === false) {
            return false;
        }

        if ((e.ctrlKey || e.metaKey) && (e.code === "KeyQ")) {
            const shapes = [];
            let end = self.page.shapes.find(s => s.isType('end'));
            if (end === undefined) {
                let pos = positionEnd(self);
                end = self.page.createNew("end", pos.x, pos.y);
                end.manageConnectors();
                end.invalidate();
                shapes.push({shape: end});
            }
            let event = self.page.createNew("event", self.x + self.width / 2, self.y + self.height / 2);
            event.fromShape = self.id;
            event.toShape = end.id;
            event.reset();
            shapes.push({shape: event});
            addCommand(self.page, shapes);
            return false;
        }
        return true;
    };

    const updateRegionVisibility = () => {
        self.warningRegion.visible = self.warningTask > 0;
        self.runningRegion.visible = self.runningTask > 0;
        self.completedRegion.visible = self.completedTask > 0;
    };

    self.serializedFields.batchAdd("triggerMode");
    // self.statusRegion = statusRegion(self, (s, r) => s.width - STEP, (s, r) => -6, () => self.height-14, () => 12);
    self.modeRegion = modeRegion(self, (s, r) => s.width - 10, (s, r) => s.height - 6, () => 12, () => 12, 0);
    self.warningRegion = taskStatRegion(self, 'warningTask', (s, r) => s.width - 23, (s, r) => s.height - 6, () => 12, () => 12, 1);
    self.runningRegion = taskStatRegion(self, 'runningTask', (shape) => shape.warningRegion.getx(shape) - (self.warningRegion.visible ? 13 : 0), (s, r) => s.height - 6, () => 12, () => 12, 2);
    self.completedRegion = taskStatRegion(self, 'completedTask', (shape) => shape.runningRegion.getx(shape) - (self.runningRegion.visible ? 13 : 0), (s, r) => s.height - 6, () => 12, () => 12, 2);
    updateRegionVisibility();

    self.addDetection(["warningTask", "runningTask", "completedTask"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        updateRegionVisibility();
        self.render();
    });

    const invalidateAlone = self.invalidateAlone;
    self.invalidateAlone = () => {
        updateRegionVisibility();
        invalidateAlone.apply(self);
    };

    // 重写indexCoordinate方法，因为node节点创建时是固定尺寸，在mousedown的过程中就绘制了，图形没有加入page的areas
    self.indexCoordinate = () => {
        self.clearCoordinateIndex();
        if (self.container === "") {
            return;
        }
        if (!self.getVisibility()) {
            return;
        }
        self.createCoordinateIndex();
    };

    /**
     * 空实现，不在选中后进行编辑.
     */
    self.selected = () => {
    };

    /**
     * 双击后进行编辑.
     *
     * @param x 横坐标.
     * @param y 纵坐标.
     */
    const dbClick = self.dbClick;
    self.dbClick = (x, y) => {
        if (!self.textEditable) {
            return;
        }
        dbClick.apply(self, [x, y]);
        self.beginEdit();
    };

    return self;
};

const flowable = (id, x, y, width, height, parent) => {
    const self = group(id, x, y, width, height, parent);
    self.type = "flowable";
    self.groupBorder = 2;
    self.allowLink = false;
    self.borderWidth = 1;
    delete self.borderColor;
    self.width = 500;
    self.height = 200;
    self.keyPressed = e => {
    };
    // self.autoFit = false;
    const manageConnectors = self.manageConnectors;
    self.manageConnectors = () => {
        manageConnectors.call(self);
        self.connectors.forEach(c => c.visible = false);// !self.autoFit);

    };

    self.resize = (width, height) => {
        self.preResize.apply(self, [width, height]);
    };


    const invalidateAlone = self.invalidateAlone;
    self.invalidateAlone = () => {
        invalidateAlone.call(self);
        const shapes = self.getShapes();
        shapes.filter(s => s.isTypeof("attachNode")).forEach(s => {
            s.drawer.move();
        });
        const t1 = shapes.find(s => s.tag === "title");
        if (t1) {
            t1.y = self.y + self.groupBorder;
            t1.drawer.move();
        }

    };

    self.initialize = () => {
        pluginMeta.importBatch(["start", "end", "event", "crossSender", "parallel", "state", "aippStart", "aippEnd", "aippEvent", "aippState"], self.page.graph).then(() => {
            const s = self.page.createNew("start", self.x + self.groupBorder + 15, self.y + self.height / 2 - 15);
            const e = self.page.createNew("end", self.x + self.width - self.groupBorder - 30, self.y + self.height / 2 - 15);
            const t1 = self.page.createNew("text", self.x + self.groupBorder, self.y + self.groupBorder);
            t1.width = self.width - 2 * self.groupBorder;
            t1.height = 30;
            t1.text = "flowable - " + self.page.shapes.filter(s => s.isTypeof("flowable")).length;
            t1.tag = "title";
            t1.manageConnectors = () => t1.connectors.forEach(c => c.getConnectable = () => false);

            const t2 = self.page.createNew("text", (self.x + self.width - 90), (self.y + self.height - 30));
            t2.width = 80 - self.groupBorder;
            t2.height = 30 - self.groupBorder;
            t2.text = self.page.graph.session.name;
            t2.tag = "tail";
            t2.manageConnectors = () => t2.connectors.forEach(c => c.getConnectable = () => false);
            self.invalidate();

        })
    };

    return self;
};

export {node, flowable};
