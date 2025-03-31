/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {standardRunner} from "@/flow/runners.js";

/**
 * 正常评估页面中节点运行时 runner.
 *
 * @param node 节点.
 * @return {{}} runner 对象.
 */
export const evaluationRunner = (node) => {
    const self = standardRunner(node);

    /**
     * @override
     */
    const stopRun = self.stopRun;
    self.stopRun = (dataList) => {
        stopRun.apply(self, [dataList]);
        node.statusManager.setDisabled(!node.page.isEvaluationNode(node));
        node.statusManager.setReferenceDisabled(false);
    };

    /**
     * @override
     */
    const resetRun = self.resetRun;
    self.resetRun = () => {
        resetRun.apply(self);
        node.statusManager.setDisabled(!node.page.isEvaluationNode(node));
        node.statusManager.setReferenceDisabled(false);
    };

    return self;
};

/**
 * 评估页面中条件节点运行时 runner.
 *
 * @param node 节点.
 * @return {{}} runner 对象.
 */
export const conditionEvaluationRunner = (node) => {
    const self = evaluationRunner(node);

    /**
     * @override
     */
    const testRun = self.testRun;
    self.testRun = () => {
        testRun.apply(self);
        const flowMeta = node.getFlowMeta();
        flowMeta.conditionParams.branches.forEach(b => {
            b.disabled = true;
        });
        node.setFlowMeta(flowMeta, true);
    };

    /**
     * @override
     */
    const resetRun = self.resetRun;
    self.resetRun = () => {
        resetRun.apply(self);
        const flowMeta = node.getFlowMeta();
        flowMeta.conditionParams.branches.forEach(b => {
            b.disabled = !b.runnable;
        });
        node.setFlowMeta(flowMeta, true);
    };

    /**
     * @override
     */
    const stopRun = self.stopRun;
    self.stopRun = (dataList) => {
        stopRun.apply(self, [dataList]);
        const flowMeta = node.getFlowMeta();
        flowMeta.conditionParams.branches.forEach(b => {
            b.disabled = !b.runnable;
        });
        node.setFlowMeta(flowMeta, true);
    };

    return self;
};