/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import "./style.css";
import {SECTION_TYPE} from "@/common/Consts.js";
import {replyNodeDrawer} from "@/components/replyNode/replyNodeDrawer.jsx";

/**
 * jadeStream中的直接回复节点.
 *
 * @override
 */
export const replyNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : replyNodeDrawer);
    self.type = "replyNodeState";
    self.text = "直接回复";
    self.componentName = "replyNodeComponent";
    self.flowMeta.jober.fitables.push("modelengine.fit.jober.aipp.fitable.ReplyNodeComponent");

    /**
     * 获取直接回复节点测试报告章节
     */
    self.getRunReportSections = () => {
        const _getInputData = () => {
            if (self.input && self.input.variables) {
                return self.input.variables;
            } else {
                return {};
            }
        };

        // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
        return [{
            no: "1",
            name: "input",
            type: SECTION_TYPE.DEFAULT,
            data: _getInputData(),
        }];
    };

    return self;
};