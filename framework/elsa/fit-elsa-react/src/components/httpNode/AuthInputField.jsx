/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Col, Form} from 'antd';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';

/**
 * 认证输入框组件
 *
 * @param {string} authMethod 认证方式 ('basic', 'Bearer', 'custom')
 * @param {object} authentication 认证数据
 * @param {function} onChange 回调函数
 * @param {function} getAuthKey 获取API Key函数
 * @param {function} getHeader 获取Header函数
 * @returns {React.JSX.Element}
 */
const _AuthInputField = ({authMethod, authentication, onChange, getAuthKey, getHeader}) => {
  const {t} = useTranslation();

  // 渲染 Header 和 API Key 的表单项
  const renderHeaderField = () => (
    <Col span={24}>
      <Form.Item
        id={`header-custom-${authentication.id}`}
        name={`header-custom-${authentication.id}`}
        rules={[{required: true, message: t('headerCanNotBeEmpty')}]}
        initialValue={getHeader()}
        label={t('Header')}
        labelAlign='left'
        labelCol={{span: 24}}
        wrapperCol={{span: 24}}
        validateTrigger={'onBlur'}
      >
        <JadeInput
          onChange={(e) => onChange(`header-custom-${authentication.id}`, e.target.value)}
          placeholder={t('inputHeader')}
        />
      </Form.Item>
    </Col>
  );

  const renderApiKeyField = () => (
    <Col span={24}>
      <Form.Item
        id={`api-key-${authMethod}-${authentication.id}`}
        name={`api-key-${authMethod}-${authentication.id}`}
        rules={[{required: true, message: t('apiKeyCanNotBeEmpty')}]}
        initialValue={getAuthKey()}
        label={t('apiKey')}
        labelAlign='left'
        labelCol={{span: 24}}
        wrapperCol={{span: 24}}
        validateTrigger={'onBlur'}
      >
        <JadeInput
          onChange={(e) => onChange(`api-key-${authMethod}-${authentication.id}`, e.target.value)}
          placeholder={t('inputApiKey')}
        />
      </Form.Item>
    </Col>
  );

  return (
    <>
      {authMethod === 'custom' && renderHeaderField()}
      {renderApiKeyField()}
    </>
  );
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.authentication === nextProps.authentication &&
    prevProps.authMethod === nextProps.authMethod &&
    prevProps.getAuthKey === nextProps.getAuthKey &&
    prevProps.getHeader === nextProps.getHeader &&
    prevProps.onChange === nextProps.onChange;
};

_AuthInputField.propTypes = {
  authentication: PropTypes.object.isRequired, // 确保 toolOptions 是一个必需的array类型
  authMethod: PropTypes.bool.isRequired,
  onChange: PropTypes.func.isRequired,
  getHeader: PropTypes.func.isRequired,
  getAuthKey: PropTypes.func.isRequired,
};

export const AuthInputField = React.memo(_AuthInputField, areEqual);