/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {Form, Popover} from 'antd';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import {QuestionCircleOutlined} from '@ant-design/icons';
import React from 'react';

/**
 * 开始节点关于入参的展示名称
 *
 * @param itemId 名称所属Item的唯一标识
 * @param propValue 展示名称的初始值
 * @param disableModifiable 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @param items 所有表单对象
 * @returns {JSX.Element} 开始节点关于入参展示名称的Dom
 */
const _DisplayName = ({itemId, propValue, disableModifiable, onChange, items}) => {
  const { t } = useTranslation();

  const promptContent = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <span>{t('startNodeDisplayNamePopover')}</span>
  </div>);

  return (<Form.Item
    className="jade-form-item"
    label={<div className={'required-after'} style={{display: 'flex', alignItems: 'center'}}>
      <span>{t('displayName')}</span>
      <Popover
        content={[promptContent]}
        align={{offset: [0, 3]}}
        overlayClassName={'jade-custom-popover'}
      >
        <QuestionCircleOutlined className="jade-panel-header-popover-content"/>
      </Popover>
    </div>}
    name={`displayName-${itemId}`}
    rules={[{required: true, message: t('paramDisplayNameCannotBeEmpty')},
      {pattern: /^[a-zA-Z0-9\u4e00-\u9fa5]+([_-]+[a-zA-Z0-9\u4e00-\u9fa5]+)*$/, message: <Trans i18nKey='displayNameRule' components={{p: <p/>}}/>},
      // 自定义校验函数
      () => ({
        validator(_, value) {
          const otherValues = items.filter(i => i.id !== itemId).map(i => i.displayName);
          if (otherValues.includes(value)) {
            return Promise.reject(new Error(t('attributeDisplayNameMustBeUnique')));
          }
          return Promise.resolve();
        },
      })]}
    validateTrigger="onBlur"
    initialValue={propValue}
  >
    <JadeInput
      className='jade-start-node-input'
      id={itemId}
      value={propValue}
      disabled={disableModifiable}
      placeholder={t('pleaseInsertDisplayName')}
      showCount
      maxLength={20}
      onChange={e => onChange && onChange('displayName', e.target.value)} // 当输入框的值发生变化时调用父组件传递的回调函数
    />
  </Form.Item>);
};

_DisplayName.propTypes = {
  itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
  propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的字符串
  disableModifiable: PropTypes.bool.isRequired, // 确保 disableModifiable 是一个必需的bool值
  onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disableModifiable === nextProps.disableModifiable &&
    prevProps.propValue === nextProps.propValue &&
    prevProps.itemId === nextProps.itemId &&
    prevProps.onChange === nextProps.onChange &&
    prevProps.items === nextProps.items;
};

export const DisplayName = React.memo(_DisplayName, areEqual);