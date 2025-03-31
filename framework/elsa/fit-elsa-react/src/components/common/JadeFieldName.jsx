/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form} from 'antd';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import React from 'react';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';

/**
 * form表单中的名称字段.
 *
 * @param id 唯一标识.
 * @param name 名称.
 * @param shapeStatus 图形状态.
 * @param onNameChange 名称变化时的回调.
 * @param style 样式.
 * @param validator 校验器.
 * @param className 输入框样式类名
 * @return {JSX.Element}
 * @constructor
 */
export const JadeFieldName = ({
                                id,
                                name,
                                shapeStatus,
                                onNameChange,
                                style = {},
                                validator = null,
                                className,
                              }) => {
  const {t} = useTranslation();
  const rules = [
    {required: true, message: t('fieldValueCannotBeEmpty')},
    {
      pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
      message: t('fieldNameRule'),
    },
  ];

  if (validator) {
    // 自定义校验函数
    rules.push(
      () => ({
        validator(_, value) {
          return validator(value);
        },
      }));
  }

  const getClassName = () => {
    if (className) {
      return `jade-input ${className}`;
    } else {
      return 'jade-input';
    }
  };

  return (<>
    <Form.Item
      id={`name-${id}`}
      name={`name-${id}`}
      rules={rules}
      initialValue={name}
    >
      <JadeInput
        disabled={shapeStatus.disabled}
        className={getClassName()}
        placeholder={t('pleaseInsertFieldName')}
        style={style}
        value={name}
        onChange={(e) => onNameChange(e.target.value)}
      />
    </Form.Item>
  </>);
};

JadeFieldName.propTypes = {
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  shapeStatus: PropTypes.object.isRequired,
  onNameChange: PropTypes.func.isRequired,
  style: PropTypes.object,
  validator: PropTypes.func,
};
