/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {Form} from 'antd';
import {useTranslation} from 'react-i18next';
import {JadeObservableInput} from '@/components/common/JadeObservableInput.jsx';

/**
 * 智能表单项的名称
 *
 * @param itemId 名称所属Item的唯一标识
 * @param propValue 名称的初始值
 * @param type 名称对应的字段类型
 * @param disabled 该字段是否禁止修改
 * @param onChange 值被修改时调用的函数
 * @param items 所有表单对象
 * @param output 表单输出
 * @returns {JSX.Element} 开始节点关于入参名称的Dom
 */
export const FormItemName = ({itemId, propValue, type, disabled, onChange, items, output}) => {
  const {t} = useTranslation();

  return (<Form.Item
    className="jade-form-item"
    label={<div className={'required-after'} style={{display: 'flex', alignItems: 'center'}}>
      <span>{t('formItemName')}</span>
    </div>}
    name={`name-${itemId}`}
    rules={[{required: true, message: t('formItemNameCannotBeEmpty')},
      {pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: t('formItemNameRule')},
      // 自定义校验函数
      () => ({
        validator(_, value) {
          const otherValues = items.filter(i => i.id !== itemId).map(i => i.name);
          if (otherValues.includes(value)) {
            return Promise.reject(new Error(t('formItemNameMustBeUnique')));
          }
          return Promise.resolve();
        },
      })]}
    validateTrigger="onBlur"
    initialValue={propValue}
  >
    <JadeObservableInput
      className="jade-start-node-input"
      style={{borderRadius: '8px 0 0 8px'}}
      id={itemId}
      value={propValue}
      type={type}
      disabled={disabled}
      placeholder={t('pleaseInsertFormItemName')}
      showCount
      maxLength={20}
      parent={output.id}
      onChange={e => onChange && onChange(itemId, [{key:'name', value:e.target.value}])} // 当输入框的值发生变化时调用父组件传递的回调函数
    />
  </Form.Item>);
};

FormItemName.propTypes = {
  itemId: PropTypes.string.isRequired, // 确保 itemId 是一个必需的字符串
  propValue: PropTypes.string.isRequired, // 确保 propValue 是一个必需的字符串
  disabled: PropTypes.bool.isRequired, // 确保 disabled 是一个必须的布尔值
  onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
  items: PropTypes.array.isRequired, // 确保 items 是一个必须的列表
};