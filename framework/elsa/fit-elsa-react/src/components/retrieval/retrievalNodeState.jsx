/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import "./style.css";
import {SECTION_TYPE} from "@/common/Consts.js";
import {retrievalNodeDrawer} from "@/components/retrieval/retrievalNodeDrawer.jsx";

/**
 * 知识检索shape
 *
 * @override
 */
export const retrievalNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : retrievalNodeDrawer);
    self.type = "retrievalNodeState";
    self.text = "普通检索";
    self.componentName = "retrievalComponent";
    self.flowMeta.jober.fitables.push("modelengine.fit.jober.aipp.fitable.NaiveRAGComponent");

    /**
     * 获取知识检索节点测试报告章节
     */
    self.getRunReportSections = () => {
        // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
        let data = {};
        if (self.input) {
            data = {query: self.input.query};
        }
        return [{
            no: "1",
            name: "input",
            type: SECTION_TYPE.DEFAULT,
            data: data
        }, {
            no: "2",
            name: "output",
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.output)
        }];
    };

    return self;
}