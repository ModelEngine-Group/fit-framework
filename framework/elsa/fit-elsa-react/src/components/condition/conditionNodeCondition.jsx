/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import {DIRECTION} from "@fit-elsa/elsa-core";
import {NODE_STATUS, SECTION_TYPE, VIRTUAL_CONTEXT_NODE} from "@/common/Consts.js";
import {conditionNodeDrawer} from "@/components/condition/conditionNodeDrawer.jsx";
import {ConditionNodeConnectorValidator, FormValidator} from '@/components/base/validator.js';

/**
 * jadeStream中的条件节点.
 *
 * @override
 */
export const conditionNodeCondition = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : conditionNodeDrawer);
    self.type = "conditionNodeCondition";
    self.text = "条件";
    self.width = 600;
    self.componentName = "conditionComponent";
    delete self.flowMeta.jober;

    /**
     * 去除方向为E的连接点.
     *
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.apply(self);
        self.connectors.remove(c => c.direction.key === DIRECTION.E.key);
    };

    /**
     * 通过分支id获取线.
     *
     * @param branchId 分支id.
     * @return {*} 符合条件的线的集合.
     */
    self.getEventsByBranchId = (branchId) => {
        return self.page.getEvents()
            .filter(e => e.fromShape === self.id)
            .filter(e => e.definedFromConnector.split("|")[1] === branchId);
    };

    /**
     * 获取用户自定义组件.
     *
     * @return {*}
     */
    self.getComponent = () => {
        return self.graph.plugins[self.componentName](self.flowMeta.conditionParams);
    };

    /**
     * @override
     */
    self.serializerJadeConfig = (jadeConfig) => {
      self.flowMeta.conditionParams = jadeConfig;
      self.flowMeta.enableStageDesc = self.flowMeta.conditionParams.enableStageDesc;
      self.flowMeta.stageDesc = self.flowMeta.conditionParams.stageDesc;
    };

    /**
     * 反序列化.
     *
     * @override
     */
    self.deSerialized = () => {
        const flowMeta = self.flowMeta;
        flowMeta.conditionParams.enableStageDesc = flowMeta.enableStageDesc;
        flowMeta.conditionParams.stageDesc = flowMeta.stageDesc;
    };

    /**
     * 将jadeConfig格式转换为试运行报告识别的格式
     *
     * @param data
     * @return {*}
     */
    const transformData = data => data.map(item => {
        return {
            logic: item.conditionRelation,
            conditions: item.conditions.map(condition => {
                const left = condition.value.find(v => v.name === 'left');
                const right = condition.value.find(v => v.name === 'right');

                const transformedCondition = {
                    left: {
                        key: left.value && left.value.join('.'),
                        type: left.type,
                        value: ""
                    },
                    operator: condition.condition
                };

                if (right && right.from === "Input") {
                    transformedCondition.right = {
                        key: '',
                        type: right.type,
                        value: right.value && right.value
                    };
                } else if (right && right.from === "Reference" && right.value.length > 0) {
                    transformedCondition.right = {
                        key: right.value && right.value.join('.'),
                        type: right.type,
                        value: ""
                    };
                }

                return transformedCondition;
            })
        };
    });

    /**
     * 条件节点默认的测试报告章节
     */
    self.getRunReportSections = () => {
        const branches = self.drawer.getLatestJadeConfig().branches;
        const sectionSource = self.input ? self.input.branches : transformData(branches);
        // 过滤掉else分支
        const conditionSections = sectionSource.filter(branch => !branch.conditions.some(condition => condition.condition === 'true')).map((branch, index) => {
            const no = (index + 1).toString();
            const name = "condition";
            return {no: no, name: name, type: SECTION_TYPE.CONDITION, data: branch}
        });
        // 获取最大的no值
        const maxNo = conditionSections.length > 0 ? Math.max(...conditionSections.map(section => parseInt(section.no))) : 0;

        // 添加新的 object
        conditionSections.push({
            no: (maxNo + 1).toString(),
            name: 'output',
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.output),
        });
        return conditionSections;
    };

    /**
     * @override
     */
    const setRunReportSections = self.setRunReportSections;

    /**
     * 构造输出
     *
     * @param runFromConnectorName 锚点名
     * @param i18n 国际化组件
     */
    self.buildOutput = (runFromConnectorName, i18n) => {
        if (runFromConnectorName.includes('|')) {
            const match = runFromConnectorName.match(/dynamic-(\d+)/);
            if (match) {
                // 解析并加 1
                const newNumber = parseInt(match[1], 10) + 1;
                self.output = {result: `${i18n.t('passConditionPrefix')}${newNumber}${i18n.t('branch')}`};
            }
        } else {
            // 如果没有 '|'，则说明走的是Else分支
            self.output = {result: `${i18n.t('passElseCondition')}`};
        }
    };

    /**
     * 设置运行报告数据
     *
     * @param data 源数据
     */
    self.setRunReportSections = (data) => {
        setRunReportSections.apply(self, [data]);
        // 条件节点输出需要计算所属分支
        const runFromConnectorName = self.page.getShapes().find(s => s.id === data.nextLineId)?.definedFromConnector ?? '';
        // 检查是否包含 '|'
        const i18n = self.graph.i18n;
        self.buildOutput(runFromConnectorName, i18n);
    };

    /**
     * @override
     */
    self.setFlowMeta = (flowMeta, jadeNodeConfigChangeIgnored) => {
        self.drawer.dispatch({
            type: "system_update",
            changes: [
                {key: "branches", value: flowMeta?.conditionParams?.branches},
                {key: "jadeNodeConfigChangeIgnored", value: jadeNodeConfigChangeIgnored || self.page.isRunning}
            ]
        });
    };

    /**
     * @override
     */
    self.getEntity = () => {
        return self.flowMeta.conditionParams.branches;
    };

    /**
     * @override
     */
    self.isReferenceAvailable = (preNodesInfo, observerProxy) => {
        // 确定proxy所在的branch.
        const o = observerProxy;
        const branches = self.drawer.getLatestJadeConfig().branches;
        const branch = branches.find(b => {
            return b.conditions.some(c => {
                return c.value.filter(v => v.from === "Reference").some(v => v.id === o.id);
            });
        });
        if (!branch) {
            return false;
        }
        return preNodesInfo.some(p => p.id === observerProxy.nodeId && p.runnable === branch.runnable);
    };

    /**
     * 校验节点状态是否正常.
     *
     * @param linkNodeSet 链路中的节点列表的Set
     * @return Promise 校验结果
     */
    self.validate = (linkNodeSet) => {
        const validators = [new FormValidator(self)];
        if (linkNodeSet.has(self.id)) {
            validators.push(new ConditionNodeConnectorValidator(self));
        }
        return self.runValidators(validators);
    };

    /**
     * 获取分支数据.
     */
    self.getBranches = () => {
        return self.drawer.getLatestJadeConfig().branches;
    };

    /**
     * override
     */
    self.pasted = () => {
    };

    return self;
};