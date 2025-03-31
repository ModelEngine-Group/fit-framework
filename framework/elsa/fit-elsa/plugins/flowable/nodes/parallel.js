/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ALIGN, DOCK_MODE, FLOWABLE_PARALLEL_MODE} from '../../../common/const.js'
import {node} from './node.js';

/**
 * 平行节点
 * 一个平行节点中可以有多个state节点
 * state节点的运行可以是all：所有的state执行后才结束parallel；either：任意一个节点执行结束paralle执行结束
 * 辉子 2020
 */
const parallel = (id, x, y, width, height, parent) => {
    const HEIGHT = 90;
    const WIDTH = 120;
    let self = node(id, x, y, WIDTH, HEIGHT, parent, true);
    self.type = "parallel";
    self.text = "parallel";
    self.borderColor = "darkgreen";
    self.headColor = "transparent";
    self.fontColor = "darkgreen";
    self.dashWidth = 6;
    self.cornerRadius = 6;
    self.autoWidth = false;
    self.itemPad = [6, 6, 30, 6];
    self.mode = FLOWABLE_PARALLEL_MODE.ALL;
    self.dockMode = DOCK_MODE.VERTICAL;
    self.dockAlign = ALIGN.TOP;
    self.ifMaskItems = false;
    self.hideText = false;
    self.itemScroll = { x: 0, y: 30 };

    let initConnectors = self.initConnectors
    self.initConnectors = () => {
        initConnectors.call(self);
        self.connectors.push(self.resizeConnector);
    };

    self.childAllowed = child => (child.isTypeof('state')) || (child.isTypeof('parallel'));

    let arrangeShapes = self.arrangeShapes;
    self.arrangeShapes = () => {
        arrangeShapes.call(self);
        let shapes = self.getShapes();
        if (shapes.length === 0) {
            return;
        }
        let height = shapes.max(s => s.y + s.height) - self.y + 10;
        height = height > HEIGHT ? height : HEIGHT;
        self.resize(self.width, height);
    };
    self.shapeRemoved = (shape, nextContainer) => {
        self.invalidate();
    };

    let initialize = self.initialize;
    self.initialize = () => {
        let state1 = self.page.createNew("state", self.x, self.y);
        state1.container = self.id;
        state1.text = "state 1";

        let state2 = self.page.createNew("state", self.x, self.y);
        state2.container = self.id;
        state2.text = "state 2";

        initialize.call(self);
    };

    const keyPressed = self.keyPressed;
    self.keyPressed = e => {
        if ((e.shiftKey || e.metaKey) && (e.code === "KeyN")) {
            const s = self.page.createNew("state", self.x, self.y);
            s.container = self.id;
            s.text = "state " + self.getShapes().length;
            self.y -= s.height / 2;
            self.invalidate();
            return false;
        }
        return keyPressed.call(self, e);
    };


    return self;
};

export {parallel};