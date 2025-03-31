/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import PropTypes from 'prop-types';
import {VariableAggregationInputForm} from '@/components/variableAggregation/VariableAggregationInputForm.jsx';
import {VariableAggregationOutputForm} from '@/components/variableAggregation/VariableAggregationOutputForm.jsx';

/**
 * 变量聚合节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} DOM对象.
 */
const _VariableAggregationWrapper = ({data, shapeStatus}) => {
  const variables = data.inputParams.find(ip => ip.name === 'variables')?.value ?? [];

  return (<>
    <VariableAggregationInputForm variables={variables} shapeStatus={shapeStatus}/>
    <VariableAggregationOutputForm outputParams={data.outputParams}/>
  </>);
};

_VariableAggregationWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const VariableAggregationWrapper = React.memo(_VariableAggregationWrapper);