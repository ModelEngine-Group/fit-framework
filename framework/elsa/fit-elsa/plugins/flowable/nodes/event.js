/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {LINEMODE} from '../../../common/const.js';
import {line} from '../../../core/line.js';
import {popupMenu} from '../../../core/popupMenu.js';

/**
 * 事件，在flowable里定义为节点之间的“线”
 * 事件名称的原因：A节点发生什么事情到达B节点
 * 辉子 2020
 */
const event = (id, x, y, width, height, parent, drawer) => {
    let self = line(id, x, y, 5, 5, parent, drawer);
    self.type = "event";
    self.borderWidth = 1;
    self.beginArrow = false;
    self.endArrow = true;
    self.text = "";
    self.namespace = "flowable";
    self.autoGenerateShape = "state";
    self.allowShine = true;
    self.lineMode = LINEMODE.BROKEN;
    self.ignoreDefaultContextMenu = true;
    self.ignoreAutoFit = true;
    self.hideText = false;

    /**
     * 调整下一个图形的位置，使其处于正确的位置.
     *
     * @param next 下一个图形.
     */
    self.getNextPosition = (next) => {
        if (Math.abs(self.width) > Math.abs(self.height)) {
            // 如果event的宽度是正数，则只需要将y坐标向上提next.height / 2
            // 如果event的宽度是负数，那么在提y的基础上，还需要将x向前提next.width的距离.
            const x = self.x + self.toConnector.x + (self.width > 0 ? 0 : -next.width);
            const y = self.y + self.toConnector.y - (next.height / 2);
            return {x, y}
        } else {
            // 原理同上.
            const x = self.x + self.toConnector.x - (next.width / 2);
            const y = self.y + self.toConnector.y + (self.height > 0 ? 0 : -next.height);
            return {x, y};
        }
    };

    /**
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.call(self);
        const toRelease = self.toConnector.release;
        self.toConnector.release = async position => {
            // 当toShape不存在时，connectingShape为undefined，因此这里需要判断connectingShape是否存在.
            if (self.connectingShape.namespace !== self.namespace && self.connectingShape.allowLink) {
                // 当toShape不存在，且fromShape存在时，才需要创建菜单及节点.
                // 选中未连接的线，拖动toConnector，释放时，不产生菜单.
                if (self.toShape === "" && self.fromShape !== "") {
                    const createNode = type => {
                        const next = self.page.createNew(type, self.x + self.width, self.y + self.height);
                        const nextPosition = self.getNextPosition(next);
                        next.moveTo(nextPosition.x, nextPosition.y);

                        self.toShape = next.id;
                        self.connectingShape = self.getToShape();
                        delete self.isNew;
                        position.context.command = "addShape";//正在新增shape
                        position.context.shapes = [{ shape: self }, { shape: next }];//shape added
                        if (self.getToShape().isTypeof("attachNode")) {
                            self.dashWidth = 10;
                            self.borderWidth = 1;
                            self.lineMode = LINEMODE.STRAIGHT;
                            self.render();
                        }
                    };

                    const menus = [];
                    menus.push({
                        text: "状态节点", action: (shape, x, y) => {
                            createNode("state");
                        }, draw: (context) => {
                            // context.rotate(Math.PI/4);
                            context.strokeStyle = "dimgray";
                            context.strokeRect(-4, -3, 8, 6);
                            context.fillStyle = "red";
                            context.arc(3, 2, 2, 0, 2 * Math.PI);
                            context.fill();

                        }
                    });
                    menus.push({
                        text: "条件节点", action: (shape, x, y) => {
                            createNode("condition");
                        }, draw: (context) => {
                            context.strokeStyle = "dimgray";
                            context.save();
                            context.rotate(Math.PI / 4);
                            context.strokeRect(-4, -4, 8, 8);
                            context.restore();
                            context.fillStyle = "red";
                            context.fillText("+", -4, 3);

                        }
                    });
                    menus.push({
                        text: "结束节点", action: (shape, x, y) => {
                            createNode("end");
                        }, draw: (context) => {
                            // context.rotate(Math.PI/4);
                            context.strokeStyle = "red";
                            context.fillStyle = "dimgray";
                            context.beginPath();
                            context.arc(0, 0, 4.5, 0, 2 * Math.PI);
                            context.stroke();
                            context.beginPath();
                            context.arc(0, 0, 3, 0, 2 * Math.PI);
                            context.fill();

                        }
                    });
                    const pop = popupMenu(self.x + self.width, self.y + self.height, self, menus);
                    const remove = pop.remove;
                    pop.remove = () => {
                        remove.call(pop);
                        if (self.toShape === "" && self.fromShape !== "") {
                            self.remove();
                        } else {
                            self.unSelect();
                        }
                    }
                }
            }
            await toRelease.call(self.toConnector, position);
        };
    };

    /**
     * @override
     */
    let nowDash = -1;
    const invalidate = self.invalidate;
    self.invalidate = () => {
        let from = self.getFromShape();
        if (from !== undefined && from.indirect) {
            nowDash = self.dashWidth;
            self.dashWidth = 6;
        } else {
            self.dashWidth = nowDash === -1 ? self.dashWidth : nowDash;
        }
        invalidate.call(self);
    };

    return self;
};

export { event };
