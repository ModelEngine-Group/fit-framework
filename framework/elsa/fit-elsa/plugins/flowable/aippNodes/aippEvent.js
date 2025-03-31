/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {insertNodeRegion} from "../hitregions/hitregion.js";
import {event} from "../nodes/event.js";
import {EVENT_TYPE} from "../../../common/const.js";
import {BASE_EVENT_LENGTH} from "../common/const.js";

/**
 * Aipp专门事件，在flowable里定义为节点之间的“线”
 */
const aippEvent = (id, x, y, width, height, parent) => {
    let self = event(id, x, y, width, height, parent);
    self.type = "aippEvent";
    self.moveable = false;
    self.endArrow = false;
    self.borderWidth = 2;
    self.borderColor = "#e3e5e8";
    self.ignoreDefaultContextMenu = true;

    // Aipp流程编排项目特有热区，点击后会在event当前位置插入一个新的state
    self.insertNodeRegion = insertNodeRegion(self, s => Math.max(s.width / 2, 0) - 11, s => Math.max(s.height / 2, 0) - 12, () => 24, () => 24, 3);
    self.enableNodeInsertion = () => {
        self.insertNodeRegion.visible = true;
        return self;
    }
    self.insertNodeRegion.click = () => {
        self.page.triggerEvent({
            type: EVENT_TYPE.INSERT_NODE_REGION_CLICKED,
            value: {
                region: self.insertNodeRegion
            }
        });
    };

    self.insertNodeRegion.insertNodes = (nodes) => {
        const page = self.page;
        nodes.forEach(node => {
            // 插入一个新的state和一个新的event
            // 当前event的toShape改为指向新state的北接口
            const newState = page.createNew(node.type, self.x, self.y + BASE_EVENT_LENGTH, undefined, page.wantedShape.getProperties());
            newState.setFlowableContext(node).resize();
            newState.moveTo(self.x - newState.width / 2, self.y + BASE_EVENT_LENGTH);

            page.createNew("aippEvent", 0, 0)
                .enableNodeInsertion()
                .connectFrom(newState.id, "S")
                .connectTo(self.toShape, "N");
            self.connectTo(newState.id, "N");

            // 由于插入了新的state，在新state下方的其他state需要向下平移70单位
            self.page.shapes.filter(s => s !== newState && (s.type === "aippEnd" || s.type === "aippState") && s.y > newState.y - 30)
                .map(s => s.moveTo(s.x, s.y + BASE_EVENT_LENGTH + newState.height));
        });
        // 刷新所有event
        self.page.shapes.filter(s => s.type === "aippEvent").map(s => s.follow());
        self.invalidate();
    }

    // 鼠标悬停时变为指针
    self.onMouseMove = () => {
        self.page.cursor = "hand";
    };

    let onMouseUp = self.onMouseUp;
    self.onMouseUp = async position => {
        await onMouseUp.call(self, position);
        self.unSelect();
    }

    return self;
};

export { aippEvent };
