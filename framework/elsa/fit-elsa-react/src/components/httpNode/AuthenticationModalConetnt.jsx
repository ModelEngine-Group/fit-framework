/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Radio, Row} from 'antd';
import React from 'react';
import {AuthInputField} from '@/components/httpNode/AuthInputField.jsx';
import {useFormContext} from '@/components/DefaultRoot.jsx';
import {useTranslation} from 'react-i18next';
import EmptyCircleIcon from '../asserts/icon-empty-circle.svg?react';
import ApiKeyIcon from '@/components/asserts/icon-api-key.svg?react';
import PropTypes from 'prop-types';

/**
 * 认证弹窗内容组件
 *
 * @param authType 认证类型
 * @param authMethod 认证方法
 * @param setAuthMethod 设置认证方法函数
 * @param setAuthType 设置认证类型函数
 * @param authentication 认证信息
 * @param getAuthKey 获取authKey函数
 * @param getHeader 获取header函数
 * @returns {React.JSX.Element}
 * @private
 */
const _AuthenticationModalContent = ({
  authType,
  authMethod,
  setAuthMethod,
  setAuthType,
  authentication,
  getAuthKey,
  getHeader,
  }) => {
  const form = useFormContext(); // Antd的Form实例
  const {t} = useTranslation();
  /**
   * 认证方式修改：basic、bearer、custom
   *
   * @param e 动作事件
   */
  const handleAuthMethodChange = e => {
    setAuthMethod(e.target.value);
  };

  /**
   * 认证类型修改回调，
   *
   * @param e 动作事件
   */
  const handleAuthTypeChange = e => {
    setAuthType(e.target.value);
    setAuthMethod('basic'); // 重置API Key认证方式
  };

  /**
   * 回调
   *
   * @param id id
   * @param e 事件
   */
  const onChange = (id, e) => {
    form.setFieldsValue({[id]: e});
  };

  return (<>
    <span className={'authentication-modal-radio-group-title'}> {t('authType')} </span>
    <Radio.Group
      value={authType}
      onChange={handleAuthTypeChange}
      className='authentication-radio-group-wrapper'
    >
      <Radio.Button value='none' style={{marginRight: '4px', width: '200px'}} className='auth-type-button'>
        <EmptyCircleIcon className='empty-icon'/>
        <span className={'radio-button-text'}>{t('none')}</span>
      </Radio.Button>
      <Radio.Button value='apiKey' style={{marginLeft: '4px', width: '200px'}} className='auth-type-button'>
        < ApiKeyIcon className='api-key-icon'/>
        <span className={'radio-button-text'}>API-Key</span>
      </Radio.Button>
    </Radio.Group>

    {authType === 'apiKey' && (
      <>
        <Radio.Group
          value={authMethod}
          className='authentication-api-key-radio-group-wrapper'
          onChange={handleAuthMethodChange}
        >
          <Radio.Button value='basic' style={{marginRight: '4px', width: '132px'}} className='auth-type-button'>
            <span className={'radio-button-text'}>{t('basic')}</span>
          </Radio.Button>
          <Radio.Button value='Bearer' style={{marginLeft: '4px', width: '132px'}} className='auth-type-button'>
            <span className={'radio-button-text'}>Bearer</span>
          </Radio.Button>
          <Radio.Button value='custom' style={{marginLeft: '4px', width: '132px'}} className='auth-type-button'>
            <span className={'radio-button-text'}>{t('custom')}</span>
          </Radio.Button>
        </Radio.Group>

        <Row gutter={16}>
          {/* 使用 AuthInputField 组件，根据认证方式渲染不同字段 */}
          {authMethod && (
            <AuthInputField
              authMethod={authMethod}
              authentication={authentication}
              onChange={onChange}
              getAuthKey={getAuthKey}
              getHeader={getHeader}
            />
          )}
        </Row>
      </>
    )}
  </>);
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.authType === nextProps.authType &&
    prevProps.authMethod === nextProps.authMethod &&
    prevProps.getAuthKey === nextProps.getAuthKey &&
    prevProps.getHeader === nextProps.getHeader &&
    prevProps.setAuthType === nextProps.setAuthType &&
    prevProps.setAuthMethod === nextProps.setAuthMethod &&
    prevProps.authentication === nextProps.authentication;
};

_AuthenticationModalContent.propTypes = {
  authType: PropTypes.string.isRequired, // 确保 toolOptions 是一个必需的array类型
  authMethod: PropTypes.string.isRequired,
  setAuthMethod: PropTypes.func.isRequired,
  setAuthType: PropTypes.func.isRequired,
  authentication: PropTypes.object.isRequired,
  getAuthKey: PropTypes.func.isRequired,
  getHeader: PropTypes.func.isRequired,
};

export const AuthenticationModalContent = React.memo(_AuthenticationModalContent, areEqual);