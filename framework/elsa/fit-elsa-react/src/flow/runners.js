/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {NODE_STATUS} from '@';
import {setBranchDisabled} from '@/components/util/BranchUtil.js';

/**
 * 正常页面中节点运行时 runner.
 *
 * @param node 节点.
 * @return {{}} runner 对象.
 */
export const standardRunner = (node) => {
    const self = {};

    /**
     * 开始调试.
     */
    self.testRun = () => {
        node.statusManager.setRunStatus(NODE_STATUS.UN_RUNNING);
        node.statusManager.setDisabled(true);
        node.statusManager.setReferenceDisabled(true);
        node.moveable = false;
    };

    /**
     * 停止调试.
     *
     * @param dataList 数据列表.
     */
    self.stopRun = (dataList) => {
        node.moveable = true;
        node.emphasized = false;
        self.refreshRun(dataList);
        node.statusManager.setDisabled(false);
        node.statusManager.setReferenceDisabled(false);
    };

    /**
     * 刷新调试.
     *
     * @param dataList 数据列表.
     */
    self.refreshRun = (dataList) => {
        const data = dataList.find(d => d.nodeId === node.id);
        if (data) {
            node.statusManager.setRunStatus(data.status);
            node.setRunReportSections(data);
        } else {
            const preNodes = node.getDirectPreNodeIds();
            if (preNodes.every(preNode => _isPreNodeFinished(preNode, dataList))) {
                node.statusManager.setRunStatus(NODE_STATUS.RUNNING);
            }
        }
    };

    /**
     * 重置调试.
     */
    self.resetRun = () => {
        node.statusManager.setRunStatus(NODE_STATUS.DEFAULT)
        node.moveable = true;
        delete node.output;
        delete node.input;
        delete node.cost;
        node.statusManager.setDisabled(false);
        node.statusManager.setReferenceDisabled(false);
    };

    const _isPreNodeFinished = (preNode, dataList) => {
        if (preNode.type.endsWith('Condition')) {
            return false;
        }
        const data = dataList.find(d => d.nodeId === preNode.id);
        return data && data.status === NODE_STATUS.SUCCESS;
    };

    return self;
};

/**
 * 正常页面中条件节点运行时 runner.
 *
 * @param node 节点.
 * @return {{}} runner 对象.
 */
export const conditionRunner = (node) => {
    const self = standardRunner(node);

    /**
     * @override
     */
    const testRun = self.testRun;
    self.testRun = () => {
        testRun.apply(self);
        _setBranchDisable(true);
    };

    /**
     * @override
     */
    const resetRun = self.resetRun;
    self.resetRun = () => {
        resetRun.apply(self);
        _setBranchDisable(false);
    };

    /**
     * @override
     */
    const stopRun = self.stopRun;
    self.stopRun = (dataList) => {
        stopRun.apply(self, [dataList]);
        _setBranchDisable(false);
    };

    const _setBranchDisable = (disabled) => {
        setBranchDisabled(node, disabled);
    };

    return self;
};

/**
 * 正常页面中游离节点运行时 runner.
 *
 * @param node 节点.
 * @return {{}} runner对象.
 */
export const inactiveNodeRunner = (node) => {
    const self = {};

    /**
     * 开始调试.
     */
    self.testRun = () => {
        node.ignoreChange(() => {
            node.statusManager.setRunStatus(NODE_STATUS.DEFAULT);
            node.statusManager.setDisabled(true);
            node.moveable = false;
        });
    };

    /**
     * 停止调试.
     */
    self.stopRun = () => {
        node.ignoreChange(() => {
            node.moveable = true;
            node.emphasized = false;
            node.statusManager.setDisabled(false);
        });
    };

    /**
     * 刷新调试.
     *
     */
    self.refreshRun = () => {
        // 无需实现
    };

    /**
     * 重置调试.
     */
    self.resetRun = () => {
        node.ignoreChange(() => {
            node.moveable = true;
            node.statusManager.setDisabled(false);
        });
    };

    return self;
};