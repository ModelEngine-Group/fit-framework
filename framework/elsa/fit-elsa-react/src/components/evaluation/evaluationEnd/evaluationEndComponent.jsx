/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import EvaluationEndWrapper from "@/components/evaluation/evaluationEnd/EvaluationEndWrapper.jsx";
import {v4 as uuidv4} from "uuid";
import {defaultComponent} from "@/components/defaultComponent.js";

/**
 * 评估结束节点组件
 *
 * @param jadeConfig
 */
export const evaluationEndComponent = (jadeConfig) => {
    const self = defaultComponent(jadeConfig);

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            inputParams: [
                {
                    id: uuidv4(),
                    name: "evalOutput",
                    type: "Object",
                    from: "Expand",
                    value: [
                        {
                            id: uuidv4(),
                            name: "key0",
                            type: "Object",
                            from: "Reference",
                            description: "",
                            referenceNode: "",
                            referenceId: "",
                            referenceKey: "",
                            value: ""
                        }
                    ]
                }
            ],
            outputParams: [{}],
        }
    };

    /**
     * @override
     */
    self.getReactComponents = (shapeStatus, data) => {
        return (<>
            <EvaluationEndWrapper shapeStatus={shapeStatus} data={data}/>
        </>);
    };

    /**
     * @override
     */
    const reducers = self.reducers;
    self.reducers = (config, action) => {
        /**
         * 添加一个子项
         *
         * @private
         */
        const _addSubItem = () => {
            const newOutput = {...newConfig.inputParams[0]};
            newOutput.value.push({
                id: uuidv4(),
                name: "",
                type: "Object",
                from: "Reference",
                description: "",
                value: ""
            });
            newConfig.inputParams = [newOutput];
        };

        /**
         * 重新构造一个不包含需要删除id的数组
         *
         * @param arr 原始数组
         * @param idToRemove 需要删除的id对应的数据
         * @return {*} 不包含需要删除id的数组
         */
        const removeItemById = (arr, idToRemove) => arr.filter(item => item.id !== idToRemove);

        /**
         * 删除一行(output不支持删除)
         *
         * @private
         */
        const _deleteRow = () => {
            const newOutput = {...newConfig.inputParams[0]};
            newOutput.value = removeItemById(newOutput.value, action.id);
            newConfig.inputParams = [newOutput];
        };

        /**
         * 编辑每一行输出的属性名或者引用字段
         *
         * @private
         */
        const _editOutputProperty = () => {
            const newOutput = {...newConfig.inputParams[0]};
            const target = newOutput.value.find(item => item.id === action.id);
            action.changes.forEach(change => {
                target[change.key] = change.value;
            });
            newConfig.inputParams = [newOutput];
        };

        let newConfig = {...config};
        switch (action.type) {
            case 'editOutputProperty':
                _editOutputProperty();
                return newConfig;
            case 'addSubItem':
                _addSubItem();
                return newConfig;
            case 'deleteRow':
                _deleteRow();
                return newConfig;
            default: {
                return reducers.apply(self, [config, action]);
            }
        }
    };

    return self;
}
