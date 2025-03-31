/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useState} from 'react';
import {Button, Drawer} from 'antd'; // 引入Drawer组件
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {useFormContext} from '@/components/DefaultRoot.jsx';
import {AuthenticationModalContent} from '@/components/httpNode/AuthenticationModalConetnt.jsx';
import CloseIcon from '@/components/asserts/icon-close.svg?react'; // 自定义内容

const EMPTY_STRING = '';

const _AuthenticationModal = ({open, onCancel, onConfirm, authentication}) => {
  const form = useFormContext();
  const {t} = useTranslation();
  const header = getConfigValue(authentication, ['header'], EMPTY_STRING);
  const key = getConfigValue(authentication, ['authKey'], EMPTY_STRING);
  const type = getConfigValue(authentication, ['type'], EMPTY_STRING);
  const [authType, setAuthType] = useState('none');
  const [authMethod, setAuthMethod] = useState('basic');

  const getHeader = () => {
    if (type.value === authMethod) {
      return header.value;
    } else {
      return '';
    }
  };

  const getAuthKey = () => {
    if (type.value === authMethod) {
      return key.value;
    } else {
      return '';
    }
  };

  const onConfirmClick = async () => {
    const values = await form.getFieldsValue();
    if (authType === 'none') {
      onConfirm({actionType: 'confirm', changes: {header: '', authKey: '', type: ''}});
    } else {
      if (authMethod === 'custom') {
        onConfirm({
          actionType: 'confirm',
          changes: {
            header: values[`header-custom-${authentication.id}`] || '',
            authKey: values[`api-key-custom-${authentication.id}`] || '',
            type: authMethod,
          },
        });
      } else {
        onConfirm({
          actionType: 'confirm',
          changes: {header: '', authKey: values[`api-key-${authMethod}-${authentication.id}`] || '', type: authMethod},
        });
      }
    }
  };

  useEffect(() => {
    if (open) {
      const fieldValues = {};
      if (authType === 'none') {
        fieldValues[`api-key-basic-${authentication.id}`] = '';
        fieldValues[`api-key-Bearer-${authentication.id}`] = '';
        fieldValues[`header-custom-${authentication.id}`] = '';
        fieldValues[`api-key-custom-${authentication.id}`] = '';
      } else {
        if (authMethod === 'custom') {
          fieldValues[`header-custom-${authentication.id}`] = getHeader();
          fieldValues[`api-key-custom-${authentication.id}`] = getAuthKey();
        } else {
          fieldValues[`api-key-${authMethod}-${authentication.id}`] = getAuthKey();
        }
      }
      form.setFieldsValue(fieldValues);
    }
  }, [open, authentication, authMethod, authType]);

  return (
    <Drawer
      className={'authentication-drawer'}
      title={
        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
          <span className={'authentication-modal-title'}>{t('authentication')}</span>
          <Button type='text'
                  className={'authentication-close-button'}
                  onClick={onCancel}
                  icon={<CloseIcon/>}
          />
        </div>
      }
      placement='right'
      width={456}
      visible={open}
      bodyStyle={{paddingTop: '16px', paddingBottom: '0'}}
      closable={false}
      headerStyle={{
        padding: '24px 24px 0 24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottom: 'none',
      }}
      footer={
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end', // 将按钮靠右对齐
            gap: '16px', // 在两个按钮之间添加 16px 的间距
          }}
        >
          <Button key='cancel' onClick={onCancel} style={{width: 96, height: 32}}>
            {t('cancel')}
          </Button>
          <Button key='confirm' type='primary' onClick={onConfirmClick} style={{width: 96, height: 32}}>
            {t('confirm')}
          </Button>
        </div>
      }
      style={{
        borderTop: 'none',
        boxShadow: 'none',
        border: 'none',
      }}
    >
      <AuthenticationModalContent
        authentication={authentication}
        getHeader={getHeader}
        authMethod={authMethod}
        authType={authType}
        getAuthKey={getAuthKey}
        setAuthMethod={setAuthMethod}
        setAuthType={setAuthType}
      />
    </Drawer>
  );
};

_AuthenticationModal.propTypes = {
  authentication: PropTypes.object.isRequired,
  open: PropTypes.bool.isRequired,
  onCancel: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
};

export const AuthenticationModal = React.memo(_AuthenticationModal);
