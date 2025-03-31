/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button} from 'antd';
import '../common/style.css';
import React from 'react';
import {useTranslation} from 'react-i18next';
import RequestConfigIcon from '../asserts/icon-config.svg?react';
import PropTypes from 'prop-types';

/**
 * 鉴权信息弹窗按钮
 *
 * @param disabled 是否禁用
 * @param onClick 点击回调函数
 * @returns {React.JSX.Element}
 * @constructor
 */
const _AuthenticationButton = ({disabled, onClick}) => {
  const {t} = useTranslation();

  return (
    <Button disabled={disabled} type='text' className='icon-button request-config-authentication-btn-wrapper'
            onClick={onClick}>
      <div style={{display: 'flex'}}>
        <RequestConfigIcon/>
        <div className={'http-config-authentication-text'}>{t('authentication')}</div>
      </div>
    </Button>
  );
};

_AuthenticationButton.propTypes = {
  onClick: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.onClick === nextProps.onClick &&
    prevProps.disabled === nextProps.disabled;
};

export const AuthenticationButton = React.memo(_AuthenticationButton, areEqual);