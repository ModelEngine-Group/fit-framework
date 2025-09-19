/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DEFAULT_FLOW_META, SECTION_TYPE} from "@/common/Consts.js";
import {textConcatenateNodeDrawer} from "@/components/textConcatenationNode/textConcatenateNodeDrawer.jsx";
import {baseToolNodeState} from "@/components/base/baseToolNodeState.js";

/**
 * jadeStream中的文本拼接节点.
 *
 * @override
 */
export const textConcatenateNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = baseToolNodeState(id, x, y, width, height, parent, drawer ? drawer : textConcatenateNodeDrawer);
    self.type = "textConcatenateNodeState";
    self.text = "文本拼接";
    self.componentName = "textConcatenateNodeComponent";
    self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

    /**
     * 获取文本拼接节点测试报告章节
     */
    self.getRunReportSections = () => {
        const _getInputData = () => {
            if (self.input && self.input.args) {
                return self.input.args;
            } else {
                return {};
            }
        };

        // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
        return [
            {
            no: "1",
            name: "input",
            type: SECTION_TYPE.DEFAULT,
            data: _getInputData(),
            },
            {
                no: "2",
                name: "output",
                type: SECTION_TYPE.DEFAULT,
                data: self.getOutputData(self.output)
            }];
    };

    return self;
};