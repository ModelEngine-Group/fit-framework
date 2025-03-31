/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Col, Form, Row} from 'antd';
import '../common/style.css';
import React from 'react';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import DeleteItem from '@/components/asserts/icon-delete.svg?react';
import PropTypes from 'prop-types';

/**
 * 变量组件，包含变量名和变量值
 *
 * @param item 条目数据
 * @param t 国际化组件
 * @param disabled 是否禁用
 * @param onParamChange 属性修改
 * @param handleDelete 删除数据
 * @returns {React.JSX.Element} 变量组件
 * @private
 */
const _ParamRow = ({item, t, disabled, onParamChange, handleDelete}) => {
  const paramName = item.name;
  const paramValue = item.value;

  return (
    <Row key={item.id} gutter={16}>
      <Col span={11} style={{paddingRight: 0}}>
        <Form.Item
          id={`name-${item.id}`}
          name={`name-${item.id}`}
          rules={[
            {required: true, message: t('fieldValueCannotBeEmpty')},
          ]}
          initialValue={paramName}
        >
          <JadeInput
            disabled={disabled}
            className='jade-input'
            placeholder={t('pleaseInsertFieldName')}
            onChange={(e) => onParamChange(item.id, 'name', e.target.value)}
          />
        </Form.Item>
      </Col>
      <Col span={11} style={{paddingLeft: 0, paddingRight: 0}}>
        <Form.Item
          id={`value-${item.id}`}
          name={`value-${item.id}`}
          rules={[
            {required: true, message: t('fieldValueCannotBeEmpty')},
          ]}
          initialValue={paramValue}
          validateTrigger='onBlur'
        >
          <JadeInput
            disabled={disabled}
            className='jade-input'
            value={paramValue}
            onChange={(e) => onParamChange(item.id, 'value', e.target.value)}
          />
        </Form.Item>
      </Col>
      <Col span={2} style={{paddingLeft: 0}}>
        <Button
          type='text'
          icon={<DeleteItem/>}
          disabled={disabled}
          onClick={() => handleDelete(item.id)}
        />
      </Col>
    </Row>
  );
};

_ParamRow.propTypes = {
  item: PropTypes.object.isRequired,
  disabled: PropTypes.bool.isRequired,
  handleDelete: PropTypes.func.isRequired,
  onParamChange: PropTypes.func.isRequired,
  t: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.item === nextProps.item &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.handleDelete === nextProps.handleDelete &&
    prevProps.onParamChange === nextProps.onParamChange &&
    prevProps.t === nextProps.t;
};

export const ParamRow = React.memo(_ParamRow, areEqual);