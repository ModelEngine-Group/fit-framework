/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from "uuid";
import HuggingFaceFormWrapper from "@/components/huggingFace/HuggingFaceFormWrapper.jsx";
import {updateInput} from "@/components/util/JadeConfigUtils.js";
import {defaultComponent} from "@/components/defaultComponent.js";

/**
 * huggingFace调用节点组件
 *
 * @param jadeConfig
 */
export const huggingFaceComponent = (jadeConfig) => {
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
     * 获取当前节点的所有组件
     *
     * @return {JSX.Element}
     */
    self.getReactComponents = (shapeStatus, data) => {
        return (<>
            <HuggingFaceFormWrapper shapeStatus={shapeStatus} data={data}/>
        </>);
    };

    /**
     * @override
     */
    const reducers = self.reducers;
    self.reducers = (config, action) => {
        let newConfig = {...config};

        function _insertOrUpdateModelParam() {
            const inputParams = config.inputParams.slice(); // 创建一个新数组以避免直接修改原数组
            const modelParam = {
                id: "model_" + uuidv4(),
                name: "model",
                type: "String",
                from: "Input",
                value: action.value
            };
            const secondElement = inputParams[1];
            if (secondElement.id.startsWith("model_")) {
                // 修改第二个对象的 value 属性
                inputParams[1] = {...secondElement, value: action.value};
            } else {
                // 在第二个位置插入新的 modelParam 对象
                inputParams.splice(1, 0, modelParam);
            }
            return inputParams;
        }

        switch (action.type) {
            case "update": {
                newConfig.inputParams = updateInput(config.inputParams, action.id, action.changes);
                return newConfig;
            }
            case "insertOrUpdateModel": {
                newConfig.inputParams = _insertOrUpdateModelParam();
                return newConfig;
            }
            case 'changeFlowMeta': {
                newConfig.enableStageDesc = action.data.enableStageDesc;
                newConfig.stageDesc = action.data.stageDesc;
                return newConfig;
            }
            default: {
                return reducers.apply(self, [config, action]);
            }
        }
    };

    return self;
}