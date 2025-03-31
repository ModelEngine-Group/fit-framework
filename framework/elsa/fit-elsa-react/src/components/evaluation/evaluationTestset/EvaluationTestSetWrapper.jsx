/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from "react";
import {InvokeOutput} from "@/components/common/InvokeOutput.jsx";
import {useShapeContext} from "@/components/DefaultRoot.jsx";
import {EvaluationTestSetSelect} from "@/components/evaluation/evaluationTestset/EvaluationTestSetSelect.jsx";
import PropTypes from "prop-types";

EvaluationTestSetWrapper.propTypes = {
    data: PropTypes.object,
    disabled: PropTypes.bool,
};

/**
 * 获取测试集节点配置数据
 *
 * @param shape 评估算法节点shape
 * @return {*} 配置数据
 */
const getEvaluationTestSetConfig = shape => {
    if (!shape || !shape.graph || !shape.graph.configs) {
        throw new Error('Cannot get shape.graph.configs.');
    } else {
        return shape.graph.configs.find(node => node.node === "evaluationTestSetNodeState");
    }
};

/**
 * 评估算法节点组件.
 *
 * @param data 节点数据.
 * @param shapeStatus 图形状态集合.
 * @constructor
 */
export default function EvaluationTestSetWrapper({data, shapeStatus}) {
    const shape = useShapeContext();
    const selectedTestSet = data && (data.inputParams[0].value.find(item => item.name === "name")?.value ?? '');
    // const testQuantity = data && (data.inputParams[0].value.find(item => item.name === "quantity")?.value ?? 0); 暂时注释，后续放开
    const config = getEvaluationTestSetConfig(shape);

    return (<>
        <EvaluationTestSetSelect shapeStatus={shapeStatus}
                                 selectedTestSet={selectedTestSet}
                                 config={config}/>
        {/*<TestQuantity disabled={disabled} quantity={testQuantity}/>*/}
        <InvokeOutput outputData={data.outputParams}/>
    </>);
}

EvaluationTestSetWrapper.propTypes = {
    data: PropTypes.object,
    shapeStatus: PropTypes.object
};