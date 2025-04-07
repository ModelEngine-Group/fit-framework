/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDispatch, useFormContext} from '@/components/DefaultRoot.jsx';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import React from 'react';
import {Collapse, Form, Image} from 'antd';
import {EyeOutlined} from '@ant-design/icons';

const {Panel} = Collapse;

/**
 * 开始节点应用配置表单。
 *
 * @param item 应用配置结构体。
 * @param disabled 是否禁用。
 * @param configs 开始节点相关配置。
 * @returns {JSX.Element} 开始节点应用配置表单的DOM。
 */
const _AppConfiguration = ({item, disabled, configs}) => {
  const dispatch = useDispatch();
  const form = useFormContext();
  const {t} = useTranslation();
  const appChatStyle = item?.value?.find(v => v.name === 'appChatStyle') ?? {};

  const renderOption = (config, option) => {
    const isSelected = appChatStyle.value === option.value;
    const optionCount = config.options.length;
    const flexBasis = optionCount >= 3 ? 'calc(33.333% - 11px)' : '1';

    return (
      <div
        key={option.value}
        onClick={() => !disabled && handleOptionClick(config, option)}
        style={{
          flex: `0 0 ${flexBasis}`,
          padding: '12px',
          border: `2px solid ${isSelected ? '#1890ff' : '#d9d9d9'}`,
          borderRadius: '4px',
          cursor: disabled ? 'not-allowed' : 'pointer',
          textAlign: 'center',
          background: disabled ? '#f5f5f5' : 'white',
          opacity: disabled ? 0.6 : 1,
        }}
      >
        {option.image && (
          <div className={'jade-custom-image-container'}>
            <Image
              src={option.image}
              width={111}
              height={62.28}
              style={{borderRadius: '4px'}}
              preview={{
                mask: (
                  <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100%',
                    backgroundColor: 'transparent',
                  }}>
                    <EyeOutlined style={{fontSize: '20px', color: '#000'}}/>
                  </div>
                ),
              }}
            />
          </div>
        )}
        <span className={'jade-font-size jade-font-color'}>
          {t(option.label)}
        </span>
      </div>
    );
  };

  const handleOptionClick = (config, option) => {
    form.setFieldsValue({[`${config.name}`]: option.value});
    dispatch({
      actionType: 'changeAppConfig',
      name: config.name,
      value: option.value,
    });
  };

  return (
    <Collapse bordered={false} className="jade-custom-collapse" defaultActiveKey={['configPanel']}>
      <Panel
        key={'configPanel'}
        header={
          <div className="panel-header" style={{display: 'flex', alignItems: 'center', justifyContent: 'flex-start'}}>
            <span className="jade-panel-header-font">{t('appConfig')}</span>
          </div>
        }
        className="jade-panel"
      >
        <div className={`jade-custom-panel-content`}>
          {configs?.appChatStyle && (() => {
            const appChatStyleConfig = configs.appChatStyle;
            return (
              <Form.Item
                key={appChatStyleConfig.name}
                className="jade-form-item"
                label={t(appChatStyleConfig.label)}
                name={`${appChatStyleConfig.name}`}
                rules={appChatStyleConfig.rules}
                validateTrigger="onBlur"
                initialValue={appChatStyle.value}
              >
                <div style={{display: 'flex', flexDirection: 'row', flexWrap: 'wrap', gap: '16px', width: '100%'}}>
                  {appChatStyleConfig.options.map(option => renderOption(appChatStyleConfig, option))}
                </div>
              </Form.Item>
            );
          })()}
        </div>
      </Panel>
    </Collapse>
  );
};

_AppConfiguration.propTypes = {
  item: PropTypes.shape({
    value: PropTypes.arrayOf(
      PropTypes.shape({
        name: PropTypes.string.isRequired,
        value: PropTypes.any
      })
    ).isRequired
  }).isRequired,
  disabled: PropTypes.bool.isRequired,
  configs: PropTypes.shape({
    appChatStyle: PropTypes.shape({
      name: PropTypes.string,
      options: PropTypes.arrayOf(
        PropTypes.shape({
          value: PropTypes.any.isRequired,
          label: PropTypes.string.isRequired,
          image: PropTypes.string
        })
      )
    })
  })
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.item === nextProps.item &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.config === nextProps.config;
};

export const AppConfiguration = React.memo(_AppConfiguration, areEqual);