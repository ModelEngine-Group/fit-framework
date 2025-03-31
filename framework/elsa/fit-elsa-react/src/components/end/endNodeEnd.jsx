/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '@/components/base/jadeNode.jsx';
import './style.css';
import {DIRECTION} from '@fit-elsa/elsa-core';
import {SECTION_TYPE} from '@/common/Consts.js';
import {endNodeDrawer} from '@/components/end/endNodeDrawer.jsx';
import {EndNodeConnectorValidator, FormValidator} from '@/components/base/validator.js';
import {SYSTEM_EXTRA_KEY, VALID_FORM_KEY} from '@/components/end/EndConst.js';

/**
 * 结束节点shape
 *
 @override
 */
export const endNodeEnd = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : endNodeDrawer);
    self.type = 'endNodeEnd';
    self.text = '结束';
    self.componentName = 'endComponent';
    self.flowMeta = {
        triggerMode: 'auto',
        callback: {
            type: 'general_callback',
            name: '通知回调',
            fitables: ['modelengine.fit.jober.aipp.fitable.AippFlowEndCallback'],
            converter: {
                type: 'mapping_converter',
            },
        }
    };

    /**
     * @override
     */
    const remove = self.remove;
    self.remove = (source) => {
        // 保证页面最少一个结束节点
        let beforeCount = self.page.sm.getShapes(s => s.type === 'endNodeEnd').length;
        if (beforeCount <= 1 && self.type === 'endNodeEnd') {
            return [];
        }
        const removed = remove.apply(self, [source]);
        const curCount = self.page.sm.getShapes(s => s.type === 'endNodeEnd').length;
        // 当从两个结束节点删除为一个的时候，需要通知最后一个结束节点刷新
        if (curCount === 1) {
            self.page.triggerEvent({
                type: 'END_NODE_MENU_CHANGE',
                value: [1]
            });
        }
        return removed;
    };

    /**
     * 设置E方向没有连接点
     *
     * @override
     */
    const initConnectors = self.initConnectors;
    self.initConnectors = () => {
        initConnectors.apply(self);
        self.connectors.remove(c => c.direction.key === DIRECTION.E.key);
    };

    /**
     * 序列化组件信息
     *
     * @override
     */
    self.serializerJadeConfig = (jadeConfig) => {
        self.flowMeta.callback.converter.entity = jadeConfig;
    };

    /**
     * 反序列化.
     *
     * @override
     */
    self.deSerialized = () => {
    };

    /**
     * 获取用户自定义组件.
     *
     * @override
     */
    self.getComponent = () => {
        return self.graph.plugins[self.componentName](self.flowMeta.callback.converter.entity, self);
    };

    /**
     * 获取组件自定义entity对象
     *
     * @override
     */
    self.getEntity = () => {
        return self.flowMeta.callback.converter.entity;
    };

    /**
     * 结束节点的测试报告章节
     */
    self.getRunReportSections = () => {
        return [{
            no: '1',
            name: 'output',
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(Object.keys(self.input)
              .filter(key => !VALID_FORM_KEY.has(key))
              .filter(key => !SYSTEM_EXTRA_KEY.has(key))
              .reduce((acc, key) => {
                  acc[key] = self.input[key];
                  return acc;
              }, {})),
        }];
    };

    /**
     * @override
     */
    const created = self.created;
    self.created = () => {
        created.apply(self);
        const endNodes = self.page.sm.getShapes(s => s.type === self.type);
        // 当从一个结束节点变为两个结束节点的时候，需要通知结束节点的header刷新
        if (endNodes.length === 2) {
            self.page.triggerEvent({
                type: 'END_NODE_MENU_CHANGE',
                value: [self.page.sm.getShapes(s => s.type === 'endNodeEnd').length],
            });
        }
    };

    /**
     * 当结束节点中的引用发生了变化，需要重新计算所有大模型节点的日志状态.
     *
     * @param finalOutput 结束节点的输入数据.
     */
    self.onFinalOutputChange = (finalOutput) => {
        if (finalOutput.from !== 'Expand') {
            return;
        }

        const values = finalOutput.value;

        // 第一个引用若不是大模型，则后续的所有大模型节点enableLog都是false.
        // 若第一个引用是对大模型的引用，遍历，后续【连续】的对大模型节点的引用.
        const startNode = self.page.getStartNode();
        const llmNodes = [];
        startNode.getChains(self).forEach((n, i) => {
            if (n.isTypeof('llmNodeState')) {
                llmNodes.push({index: i, node: n, isBlock: false});
            } else {
                if (n.isTypeof('conditionNodeCondition') || n.isTypeof('manualCheckNodeState')) {
                    llmNodes.forEach(llm => llm.isBlock = true);
                }
            }
        });

        let indexes = []; // 记录所有需要输出日志的大模型的下标.
        for (let i = 0; i < values.length; i++) {
            const output = values[i];

            // 不是reference，或referenceKey不存在，退出循环.
            if (output.from !== 'Reference' || !output.referenceKey) {
                break;
            }

            // 引用的不是大模型，退出循环.
            const node = llmNodes.find(n => n.node.id === output.referenceNode);
            if (!node) {
                break;
            }

            if (node.isBlock) {
                break;
            }

            // 当前大模型节点的index小于indexes中的值，跳出循环.
            if (indexes.length > 0 && node.index < indexes[indexes.length - 1]) {
                break;
            }

            indexes.push(node.index);
        }

        // 设置
        llmNodes.forEach((v) => {
            v.node.drawer.dispatch({type: 'updateLogStatus',
                value: indexes.contains(index => index === v.index)});
        });
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
            validators.push(new EndNodeConnectorValidator(self));
        }
        return self.runValidators(validators);
    };

    return self;
}