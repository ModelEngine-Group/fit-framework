/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import {DIRECTION} from "@fit-elsa/elsa-core";
import "./style.css";
import {SECTION_TYPE} from "@/common/Consts.js";
import {startNodeDrawer} from "@/components/start/startNodeDrawer.jsx";

/**
 * jadeStream中的流程启动节点.
 *
 * @override
 */
export const startNodeStart = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : startNodeDrawer);
    self.type = "startNodeStart";
    self.text = "开始";
    self.componentName = "startComponent";
    delete self.flowMeta.jober;

    /**
     * @override
     */
    const remove = self.remove;
    self.remove = (source) => {
        // 保证页面最少一个开始节点
        let beforeCount = self.page.sm.getShapes(s => s.type === 'startNodeStart').length;
        if (beforeCount <= 1 && self.type === 'startNodeStart') {
            return [];
        }
        const removed = remove.apply(self, [source]);
        const curCount = self.page.sm.getShapes(s => s.type === 'startNodeStart').length;
        // 当从两个结束节点删除为一个的时候，需要通知最后一个开始节点刷新
        if (curCount === 1) {
            self.page.triggerEvent({
                type: 'START_NODE_MENU_CHANGE',
                value: [1],
            });
        }
        return removed;
    };

    /**
     * @override
     */
    const created = self.created;
    self.created = () => {
        created.apply(self);
        const startNodes = self.page.sm.getShapes(s => s.type === self.type);
        // 当从一个开始节点变为两个开始节点的时候，需要通知开始节点的header刷新
        if (startNodes.length === 2) {
            self.page.triggerEvent({
                type: 'START_NODE_MENU_CHANGE',
                value: [self.page.sm.getShapes(s => s.type === 'startNodeStart').length],
            });
        }
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
     * 获取用户自定义组件.
     *
     * @return {*}
     */
    self.getComponent = () => {
        return self.graph.plugins[self.componentName](self.flowMeta.inputParams);
    };

    /**
     * @override
     */
    self.serializerJadeConfig = (jadeConfig) => {
        self.flowMeta.inputParams = jadeConfig;
    };

    /**
     * 反序列化.
     *
     * @override
     */
    self.deSerialized = () => {
    };

    /**
     * 获取试运行入参
     */
    self.getRunInputParams = () => {
        return self.drawer.getLatestJadeConfig().find(config => config.name === "input").value;
    };

    /**
     * 开始节点的测试报告章节
     */
    self.getRunReportSections = () => {
        return [{
            no: "1",
            name: "input",
            type: SECTION_TYPE.DEFAULT,
            data: self.getOutputData(self.input)
        }];
    };

    /**
     * @override
     */
    self.getEntity = () => {
        return self.flowMeta;
    };

    /**
     * 获取对话轮次.
     *
     * @return {number|*} 对话轮次.
     */
    self.getConversationTurn = () => {
        const memories = self.flowMeta.inputParams.find(i => i.name === "memory");
        const type = memories.value.find(m => m.name === "type");
        if (type.value === "ByConversationTurn") {
            const value = memories.value.find(m => m.name === "value")?.value;
            if (value) {
                return parseInt(value);
            }
        }
        return 3;
    };

    return self;
};