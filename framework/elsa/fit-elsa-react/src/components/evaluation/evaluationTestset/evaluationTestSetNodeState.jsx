/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {SECTION_TYPE} from "@/common/Consts.js";
import "./style.css";
import {evaluationNode} from "@/components/evaluation/evaluationNode.jsx";
import {evaluationTestSetNodeDrawer} from "@/components/evaluation/evaluationTestset/evaluationTestSetNodeDrawer.jsx";

/**
 * 评估算法节点shape
 *
 * @override
 */
export const evaluationTestSetNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = evaluationNode(id, x, y, width, height, parent, drawer ? drawer : evaluationTestSetNodeDrawer);
    self.type = "evaluationTestSetNodeState";
    self.componentName = "evaluationTestSetComponent";
    self.text = "测试集"
    self.width = 368;
    self.flowMeta.jober.fitables.push("modelengine.jade.app.engine.task.EvalDatasetComponent");

    /**
     * 测试集节点的测试报告章节
     */
    self.getRunReportSections = () => {
        return [{
            no: "1",
            name: "输出",
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.output)
        }];
    };

    return self;
}