/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {InvokeInput} from "@/components/common/InvokeInput.jsx";
import {CustomizedModelSelect} from "@/components/common/CustomizedModelSelect.jsx";
import {InvokeOutput} from "@/components/common/InvokeOutput.jsx";
import PropTypes from "prop-types";

HuggingFaceFormWrapper.propTypes = {
    shapeStatus: PropTypes.object,
    data: PropTypes.object
};

/**
 * HuggingFace表单Wrapper
 *
 * @param shapeStatus 节点状态.
 * @param data 数据
 * @returns {JSX.Element} HuggingFace表单Wrapper的DOM
 */
export default function HuggingFaceFormWrapper({shapeStatus, data}) {
    const inputData = data && data.inputParams;
    const outputData = data && data.outputParams;
    const filteredInputData = inputData ? inputData.slice(2) : [];
    const modelDefaultValue = inputData ? inputData[1].value : undefined;

    return (<>
        <InvokeInput inputData={filteredInputData} shapeStatus={shapeStatus}/>
        <div style={{marginTop: "16px", marginBottom: "16px"}}>
            <CustomizedModelSelect defaultValue={modelDefaultValue} disabled={shapeStatus.disabled}/>
        </div>
        <InvokeOutput outputData={outputData}/>
    </>);
}