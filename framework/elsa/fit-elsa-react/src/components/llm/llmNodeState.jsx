/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import "./style.css";
import {SECTION_TYPE} from "@/common/Consts.js";
import {llmNodeDrawer} from "@/components/llm/llmNodeDrawer.jsx";

/**
 * jadeStream中的大模型节点.
 *
 * @override
 */
export const llmNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : llmNodeDrawer);
    self.type = "llmNodeState";
    self.text = "大模型";
    self.componentName = "llmComponent";
    self.flowMeta.jober.fitables.push("modelengine.fit.jober.aipp.fitable.LLMComponent");
    self.flowMeta.jober.isAsync = "true";

    /**
     * 获取大模型节点测试报告章节
     */
    self.getRunReportSections = () => {
        // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
        return [{
            no: "1",
            name: "input",
            type: SECTION_TYPE.DEFAULT,
            data: self.input.prompt ? self.input.prompt.variables : {}
        }, {
            no: "2",
            name: "output",
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.output)
        }];
    };

    return self;
};