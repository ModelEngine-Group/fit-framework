/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import React, {useEffect} from 'react';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import {Form} from 'antd';
import {useFormContext} from '@/components/DefaultRoot.jsx';
import {DATA_TYPES, RENDER_TYPE} from '@/common/Consts.js';

/**
 * 智能表单项的渲染方式
 *
 * @param itemId 名称所属Item的唯一标识
 * @param propValue 名称的初始值
 * @param disabled 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @param type 该表单对象的类型
 * @returns {JSX.Element} 开始节点关于入参名称的Dom
 */
export const FormItemRenderType = ({itemId, propValue, disabled, onChange, type}) => {
  const { t } = useTranslation();
  const form = useFormContext();

  // 根据 type 渲染不同的组件
  const renderOptions = () => {
    switch (type) {
      case DATA_TYPES.BOOLEAN:
        return [
          {value: RENDER_TYPE.SWITCH, label: RENDER_TYPE.SWITCH},
        ];
      case DATA_TYPES.NUMBER:
      case DATA_TYPES.INTEGER:
        return [
          {value: RENDER_TYPE.INPUT, label: RENDER_TYPE.INPUT},
        ];
      case DATA_TYPES.STRING:
        return [
          {value: RENDER_TYPE.RADIO, label: RENDER_TYPE.RADIO},
          {value: RENDER_TYPE.INPUT, label: RENDER_TYPE.INPUT},
        ];
      default:
        return [];
    }
  };

  useEffect(() => {
    form.setFieldsValue({ [`renderType-${itemId}`]: propValue });
  }, [propValue, form]);

  return (<Form.Item
    className="jade-form-item"
    label={<div className={'required-after'} style={{display: 'flex', alignItems: 'center'}}>
      <span>{t('formItemRenderType')}</span>
    </div>}
    name={`renderType-${itemId}`}
    rules={[{required: true, message: t('formItemRenderTypeCannotBeEmpty')}]}
    initialValue={propValue}
  >
    <JadeStopPropagationSelect
      className="jade-select intelligent-form-right-select"
      disabled={disabled}
      style={{width: '100%'}}
      onChange={(value) => {
        onChange(itemId, [{key: 'renderType', value:value}]); // 当选择框的值发生变化时调用父组件传递的回调函数
        document.activeElement.blur();// 在选择后取消焦点
      }}
      options={renderOptions()}
    />
  </Form.Item>);
};

FormItemRenderType.propTypes = {
  itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
  propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的字符串
  disabled: PropTypes.bool.isRequired, // 确保 disabled 是一个必须的布尔值
  onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
  type: PropTypes.string.isRequired, // 确保 items 是一个必须的列表
};