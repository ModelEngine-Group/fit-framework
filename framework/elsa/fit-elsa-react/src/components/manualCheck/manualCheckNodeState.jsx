/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '@/components/base/jadeNode.jsx';
import './style.css';
import {manualCheckNodeDrawer} from '@/components/manualCheck/manualCheckNodeDrawer.jsx';

/**
 * jadeStream中的人工检查节点.
 *
 * @override
 */
export const manualCheckNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : manualCheckNodeDrawer);
    self.type = 'manualCheckNodeState';
    self.text = '智能表单';
    self.componentName = 'manualCheckComponent';
    self.flowMeta.triggerMode = 'manual';
    delete self.flowMeta.jober;

    /**
     * 获取用户自定义组件.
     *
     * @return {*}
     */
    self.getComponent = () => {
        return self.graph.plugins[self.componentName](self.flowMeta.task);
    };

    /**
     * @override
     */
    self.serializerJadeConfig = (jadeConfig) => {
        self.flowMeta.task = jadeConfig;
        self.flowMeta.enableStageDesc = self.flowMeta.task.enableStageDesc;
        self.flowMeta.stageDesc = self.flowMeta.task.stageDesc;
        delete self.flowMeta.task.enableStageDesc;
        delete self.flowMeta.task.stageDesc;
    };

    /**
     * 反序列化.
     *
     * @override
     */
    self.deSerialized = () => {
        const flowMeta = self.flowMeta;
        flowMeta.task.enableStageDesc = flowMeta.enableStageDesc;
        flowMeta.task.stageDesc = flowMeta.stageDesc;
    };

    /**
     * @override
     */
    self.getEntity = () => {
        return self.flowMeta.task;
    };

    /**
     * @override
     */
    self.setFlowMeta = (flowMeta, jadeNodeConfigChangeIgnored = false) => {
        self.drawer.dispatch({
            type: "system_update",
            changes: [
                {key: "converter", value: flowMeta?.task?.converter},
                {key: "jadeNodeConfigChangeIgnored", value: jadeNodeConfigChangeIgnored || self.page.isRunning}
            ]
        });
    };

    return self;
};