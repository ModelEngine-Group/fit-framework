/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {deleteCommand} from "../../../core/commands.js";
import {BASE_EVENT_LENGTH} from "../common/const.js";

/**
 * 删除AippFlow图形。此删除命令会删除所有从当前状态流出的事件，并将流入它的事件指向下一个状态
 * 王成 2023/12/20
 */
const deleteFlowableNodeCommand = (page, shapes) => {
    const self = deleteCommand(page, shapes);

    self.execute = host => {
        self.all = [];
        self.lines = [];
        self.shapes.forEach(s => {
            const shape = host.shapes.find(s1 => s1.id === s.shape);
            const fromLine = host.shapes.find(s1 => s1.fromShape === s.shape);
            const toLine = host.shapes.find(s1 => s1.toShape === s.shape);
            // 重定向指向当前状态的节点
            toLine.connectTo(fromLine.toShape, "N");
            // 删除当前状态流出的事件
            self.lines.push.apply(self.lines, fromLine.remove(fromLine).map(s => s.serialize()));

            // 由于删除了状态，其他下方状态需要向上平移140单位
            host.shapes.filter(s => s !== shape && (s.type === "aippEnd" || s.type === "aippState") && s.y > shape.y - 30)
                .map(s => s.moveTo(s.x, s.y - BASE_EVENT_LENGTH - shape.height));

            // 删除当前状态
            self.all.push.apply(self.all, shape.remove(shape).map(s => s.serialize()));
        });
    }

    return self;
};

export {
    deleteFlowableNodeCommand
}