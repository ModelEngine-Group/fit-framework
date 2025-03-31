/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form, Input} from 'antd';
import '../common/style.css';
import React from 'react';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';

/**
 * 超时时长输入组件
 *
 * @param timeout 超时时常对象
 * @param disabled 是否禁用
 * @param onBlur 失焦后的回调
 * @returns {React.JSX.Element}
 * @constructor
 */
const _TimeoutInput = ({timeout, disabled, onBlur}) => {
  const {t} = useTranslation();
  return (
    <div className={'request-config-timeout-wrapper'}>
      <span className={'request-config-timeout-text'}>{t('timeout')}</span>
      <Form.Item
        className='jade-form-item' name={`timeout-${timeout}`} initialValue={timeout.value / 1000}
        validateTrigger='onBlur'>
        <Input
          onClick={(e) => e.stopPropagation()}
          onBlur={(e) => onBlur(e, 'changeRequestConfig', timeout.id)}
          disabled={disabled}
          style={{marginRight: '8px', paddingRight: '10px'}}
          className='jade-input'
        />
      </Form.Item>
      <span
        style={{
          position: 'absolute',
          right: '10px',
          color: 'rgb(26, 26, 26)',
          fontFamily: 'Huawei Sans',
          fontSize: '12px',
          fontWeight: 400,
          lineHeight: '14px',
          letterSpacing: '0px',
          textAlign: 'left',
        }}
      >
        s
      </span>
    </div>
  );
};

_TimeoutInput.propTypes = {
  timeout: PropTypes.object.isRequired,
  onBlur: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.timeout === nextProps.timeout &&
    prevProps.onBlur === nextProps.onBlur &&
    prevProps.disabled === nextProps.disabled;
};

export const TimeoutInput = React.memo(_TimeoutInput, areEqual);
