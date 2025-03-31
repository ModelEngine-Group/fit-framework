/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import PropTypes from 'prop-types';
import {OutputForm} from '@/components/common/OutputForm.jsx';

/**
 * 变量聚合节点输出表单
 *
 * @param outputParams 数据.
 * @returns {JSX.Element} DOM对象
 */
const _VariableAggregationOutputForm = ({outputParams}) => {
  return (<>
    <OutputForm outputParams={outputParams} outputPopover={'variableAggregationOutputPopover'}/>
  </>);
};

_VariableAggregationOutputForm.propTypes = {
  outputParams: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.outputParams === nextProps.outputParams;
};

export const VariableAggregationOutputForm = React.memo(_VariableAggregationOutputForm, areEqual);