/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {InvokeInput} from '@/components/common/InvokeInput.jsx';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import PropTypes from 'prop-types';
import {SYSTEM_INPUT_KEY} from '@/components/appNode/AppNodeConst.js';

/**
 * 应用和工具流Wrapper
 *
 * @param shapeStatus 节点状态.
 * @param data 数据
 * @returns {JSX.Element} HuggingFace表单Wrapper的DOM
 */
export const AppWrapper = ({shapeStatus, data}) => {
  const inputData = data && data.inputParams;
  const outputData = data && data.outputParams;
  const filteredInputData = inputData ? inputData.find(item => item.name === 'inputParams').value.filter(item => !SYSTEM_INPUT_KEY.has(item.name)) : [];

  return (<>
    <InvokeInput inputData={filteredInputData} shapeStatus={shapeStatus}/>
    <InvokeOutput outputData={outputData}/>
  </>);
};

AppWrapper.propTypes = {
  shapeStatus: PropTypes.object,
  data: PropTypes.object,
};