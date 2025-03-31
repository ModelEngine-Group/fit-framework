/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from "@/components/base/jadeNode.jsx";
import "./style.css";
import {fitInvokeNodeDrawer} from "@/components/fitInvokeNode/fitInvokeNodeDrawer.jsx";

/**
 * FIT调用节点shape
 *
 * @override
 */
export const fitInvokeNodeState = (id, x, y, width, height, parent, drawer) => {
    const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : fitInvokeNodeDrawer);
    self.type = "fitInvokeNodeState";
    self.width = 360;
    self.backColor = 'white';
    self.pointerEvents = "auto";
    self.text = "FIT调用";
    self.componentName = "fitInvokeComponent";
    self.flowMeta.triggerMode = 'auto';
    self.flowMeta.jober.type = 'GENERICABLE_JOBER';
    const fitEntity = {
        genericable: {
            id: "",
            params: []
        }
    };

    /**
     * @override
     */
    const serializerJadeConfig = self.serializerJadeConfig;
    self.serializerJadeConfig = () => {
        serializerJadeConfig.apply(self);
        const fitableId = self.flowMeta
                .jober
                .converter
                .entity
                .fitable
                .value
                .find(item => item.name === 'id')
                .value;
        if (fitableId) {
            self.flowMeta.jober.fitables = [fitableId];
        }
        fitEntity.genericable.params = self.flowMeta.jober.converter.entity.inputParams.map(property => {
            return {name: property.name}
        });
        fitEntity.genericable.id = self.flowMeta
                .jober
                .converter
                .entity
                .genericable
                .value
                .find(item => item.name === 'id')
                .value;
        self.flowMeta.jober.entity = fitEntity;
    };

    return self;
}