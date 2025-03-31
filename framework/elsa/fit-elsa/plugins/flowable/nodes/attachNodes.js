/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { linkerDrawer } from "../drawers/drawer.js";
import { node } from "./node.js";



/**
 * flow对外的节点，可以是email，message，file，event,外部工作流连接等等
 * 辉子 2022
 */
const attachNode = (id, x, y, width, height, parent) => {
    const WIDTH = 32;
    const self = node(id, x, y, WIDTH, WIDTH, parent, false, linkerDrawer);
    self.type = "attachNode";
    self.ignoreAutoFit = true;
    self.eventType = "line";
    self.initEvent = line=>{
        line.dashWidth = 10;
        line.borderWidth = 1;
        line.ignoreAutoFit = true;
        line.beginArrow = false;
    };

    const drawerMove = self.drawer.move;
    self.drawer.move = () => {
        const container = self.getContainer();
        if (container.isTypeof("flowable")) {
            const borders = [];
            borders.push({ x: container.x + container.groupBorder, d: "left", d1: "right", delta: Math.abs(self.x + self.width / 2 - container.x) });//left border;
            borders.push({ y: container.y + container.groupBorder, d: "top", d1: "bottom", delta: Math.abs(self.y + self.height / 2 - container.y) });//top border;
            borders.push({ x: container.x + container.width - container.groupBorder, d: "right", d1: "left", delta: Math.abs(self.x + self.width / 2 - container.x - container.width) });//right border;
            borders.push({ y: container.y + container.height - container.groupBorder, d: "bottom", d1: "top", delta: Math.abs(self.y + self.height / 2 - container.y - container.height) });//bottom border;
            const border = borders.minBy(b => b.delta);
            border.y === undefined ? (self.x = border.x - self.width / 2) : (self.y = border.y - self.height / 2);
            self.connectors.forEach(c => {
                c.visible = c.type === border.d;
                c.getConnectable = shape => (c.type === border.d || c.type === border.d1);
                //c.connectable = (c.type === border.d || c.type === border.d1);
            });
        }
        drawerMove.call(self);
    };
    self.drawer.drawStatic = (context, x, y) => {
        self.width = self.height = WIDTH;
    };

    return self;
};

/**
 * 外联其他flowable
 * 辉子 2022
 */
const linker = (id, x, y, width, height, parent) => {
    const self = attachNode(id, x, y, width, height, parent);
    self.type = "linker";
    self.text = "";
    self.backColor = "white";
    self.regions.remove(s => s === self.statusRegion || s === self.modeRegion);
    delete self.statusRegion;
    delete self.modeRegion;

    const init = self.initConnectors;
    self.initConnectors = () => {
        init.call(self);
        self.connectors.forEach(c => c.radius = 3);
    };

    const drawStatic = self.drawer.drawStatic;
    self.drawer.drawStatic = (context, x, y) => {
        drawStatic.call(self.drawer, context, x, y);
        const r = 5;
        context.strokeStyle = self.get("borderColor");
        context.lineWidth = 3;
        context.beginPath();
        context.moveTo(x + r, y + r);
        context.lineTo(x + self.width - r, y + self.height - r);
        context.stroke();
        context.fillStyle = self.get("borderColor");
        context.strokeStyle = self.get("backColor");
        context.lineWidth = 1;
        context.beginPath();
        context.arc(x + r + 1, y + r + 1, r, 0, 2 * Math.PI);
        context.fill();
        context.stroke();
        context.beginPath();
        context.arc(x + self.width - r - 1, y + self.height - r - 1, r, 0, 2 * Math.PI);
        context.fill();
        context.stroke();
    };
    return self;
};

export { linker };