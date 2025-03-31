/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from "uuid";
import {convertReturnFormat} from "@/components/util/MethodMetaDataParser.js";
import EvaluationTestSetWrapper from "@/components/evaluation/evaluationTestset/EvaluationTestSetWrapper.jsx";
import {defaultComponent} from "@/components/defaultComponent.js";

/**
 * 评估测试集节点组件
 *
 * @param jadeConfig
 */
export const evaluationTestSetComponent = (jadeConfig) => {
    const self = defaultComponent(jadeConfig);

    /**
     * 必须.
     */
    self.getJadeConfig = () => {
        return jadeConfig ? jadeConfig : {
            inputParams: [{
                id: "testSet_" + uuidv4(),
                name: "testSet",
                type: "Object",
                from: "Expand",
                value: [
                    {id: uuidv4(), name: 'name', type: 'String', from: 'Input', value: ""},
                    {id: uuidv4(), name: 'id', type: 'String', from: 'Input', value: ""},
                    {id: uuidv4(), name: 'version', type: 'String', from: 'Input', value: ""},
                    {
                        id: "quantity_" + uuidv4(),
                        name: "quantity",
                        type: "Integer",
                        from: "Input",
                        value: 1
                    }]
            }],
            outputParams: []
        };
    };

    /**
     * @override
     */
    self.getReactComponents = (shapeStatus, data) => {
        return (<>
            <EvaluationTestSetWrapper shapeStatus={shapeStatus} data={data}/>
        </>);
    };

    /**
     * @override
     */
    const reducers = self.reducers;
    self.reducers = (config, action) => {
        /**
         * 获取testSet值
         *
         * @private
         */
        const _getTestSetValue = () => newConfig.inputParams[0].value;

        /**
         * 切换测试集
         *
         * @private
         */
        const _changeTestSet = () => {
            // 这里action包含了整个接口返回的数据
            if (action.value) {
                let testSetValue = _getTestSetValue();
                testSetValue.find(item => item.name === "id").value = action.value.id;
                testSetValue.find(item => item.name === "name").value = action.value.name;
                testSetValue.find(item => item.name === "version").value = Math.max(...action.value.versions.map(item => item.version));
            }
        };

        /**
         * 清除选择的测试集
         *
         * @private
         */
        const _clearTestSet = () => {
            _getTestSetValue().forEach(item => item.value = "");
        };

        /**
         * 转换输出用于展示
         *
         * @private
         */
        const _generateOutput = () => {
            if (!action.value) {
                return;
            }
            const inputJson = action.value;
            const newOutputParams = convertReturnFormat(JSON.parse(inputJson.schema));
            newConfig.outputParams = [newOutputParams];
        };

        /**
         * 清除输出
         *
         * @private
         */
        const _clearSchema = () => {
            newConfig.outputParams = [];
        };

        let newConfig = {...config};
        switch (action.type) {
            case 'changeTestSet':
                _changeTestSet();
                _generateOutput();
                return newConfig;
            case 'clearTestSet':
                _clearSchema();
                _clearTestSet();
                return newConfig;
            default: {
                return reducers.apply(self, [config, action]);
            }
        }
    };

    return self;
}
