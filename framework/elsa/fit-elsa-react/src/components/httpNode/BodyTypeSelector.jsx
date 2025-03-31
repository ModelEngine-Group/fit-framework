/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Radio} from 'antd';
import '../common/style.css';
import React from 'react';
import PropTypes from 'prop-types';

/**
 * 请求体选择组件
 *
 * @param onBodyTypeChange 请求体类型修改的回调
 * @param type body类型
 * @param disabled 是否禁用
 * @returns {React.JSX.Element}
 * @constructor
 */
const _BodyTypeSelector = ({onBodyTypeChange, type, disabled}) => {
  return (<>
    <Radio.Group onChange={onBodyTypeChange} value={type} disabled={disabled}>
      <Radio value='none'>none</Radio>
      <Radio value='x-www-form-urlencoded'>x-www-form-urlencoded</Radio>
      <Radio value='json'>JSON</Radio>
      <Radio value='text'>raw-text</Radio>
    </Radio.Group>
  </>);
};

_BodyTypeSelector.propTypes = {
  onBodyTypeChange: PropTypes.func.isRequired,
  type: PropTypes.string.isRequired,
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.type === nextProps.type &&
    prevProps.onBodyTypeChange === nextProps.onBodyTypeChange &&
    prevProps.disabled === nextProps.disabled;
};

export const BodyTypeSelector = React.memo(_BodyTypeSelector, areEqual);