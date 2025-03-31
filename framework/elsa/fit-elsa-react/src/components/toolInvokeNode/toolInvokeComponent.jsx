/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ToolInvokeFormWrapper from "@/components/toolInvokeNode/ToolInvokeFormWrapper.jsx";
import {updateInput} from "@/components/util/JadeConfigUtils.js";
import {defaultComponent} from "@/components/defaultComponent.js";

/**
 * 工具调用节点组件
 *
 * @param jadeConfig
 */
export const toolInvokeComponent = (jadeConfig) => {
    const self = defaultComponent(jadeConfig);

    /**
     * 必填
     *
     * @return 组件信息
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            inputParams: [],
            outputParams: []
        };
    };

    /**
     * @override
     */
    self.getReactComponents = (shapeStatus, data) => {
        return (<>
            <ToolInvokeFormWrapper shapeStatus={shapeStatus} data={data}/>
        </>);
    };

    /**
     * @override
     */
    const reducers = self.reducers;
    self.reducers = (config, action) => {
        let newConfig = {...config};
        switch (action.type) {
            case "update":
                newConfig.inputParams = updateInput(config.inputParams, action.id, action.changes);
                return newConfig;
            case 'changeFlowMeta':
                newConfig.enableStageDesc = action.data.enableStageDesc;
                newConfig.stageDesc = action.data.stageDesc;
                return newConfig;
            default: {
                return reducers.apply(self, [config, action]);
            }
        }
    };

    return self;
}