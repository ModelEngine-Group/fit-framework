/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {evaluationStartNodeDrawer} from "@/components/evaluation/evaluationStart/evaluationStartNodeDrawer.jsx";
import {DIRECTION} from "@fit-elsa/elsa-core";
import {evaluationNode} from "@/components/evaluation/evaluationNode.jsx";

/**
 * 评估开始节点shape
 *
 * @override
 */
export const evaluationStartNodeStart = (id, x, y, width, height, parent, drawer) => {
    const self = evaluationNode(id, x, y, width, height, parent, drawer ? drawer : evaluationStartNodeDrawer);
    self.type = "evaluationStartNodeStart";
    self.componentName = "evaluationStartComponent";
    self.text = "评估开始"
    self.width = 368;
    self.isUnique = true;
    delete self.flowMeta.jober;
    self.deletable = false;

    /**
     * 获取用户自定义组件.
     *
     * @return {*}
     */
    self.getComponent = () => {
        return self.graph.plugins[self.componentName](self.flowMeta.inputParams);
    };

    /**
     * 设置方向为W方向不出现连接点
     *
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.apply(self);
        self.connectors.remove(c => c.direction.key === DIRECTION.W.key);
    };

    /**
     * @override
     */
    self.serializerJadeConfig = (jadeConfig) => {
        self.flowMeta.inputParams = jadeConfig;
    };

    /**
     * 评估开始节点的测试报告章节，设置为空
     */
    self.getRunReportSections = () => {
        return [{}];
    };

    return self;
}