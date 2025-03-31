/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {Col, Form, Row} from 'antd';
import {useTranslation} from 'react-i18next';
import {JadeInput} from '@/components/common/JadeInput.jsx';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {JadeReferenceTreeSelect} from '@/components/common/JadeReferenceTreeSelect.jsx';
import React from 'react';
import {JadeInputTreeSelect} from '@/components/common/JadeInputTree.jsx';
import {useFormContext} from '@/components/DefaultRoot.jsx';

/**
 * 智能表单选择项
 *
 * @param item 默认值所属Item对象
 * @param shapeStatus 图形状态.
 * @param onChange 值被修改时调用的函数
 * @param label 该项的标题
 * @param inputRequired 当为输入时是否必填
 * @returns {JSX.Element} 开始节点关于入参名称的Dom
 */
export const FormItemSelectValue = ({item, shapeStatus, onChange, label, inputRequired = false}) => {
  const form = useFormContext();
  const {t} = useTranslation();

  /* input变化时的回调. */
  const onInputChange = (key, e) => {
    let value = e.target.value.trim() === '' ? null : e.target.value;
    if (value === null) {
      form.setFieldsValue({[`value-${item.id}`]: null});
    }
    const type = item.type;
    if (type === 'Object' || type === 'Array') {
      try {
        value = JSON.parse(value);
      } catch (error) {
        // 不影响，报错可以继续执行.
      }
    }
    onChange(item.id, [{key, value}]);
  };

  /* input失去焦点时的回调. */
  const onInputBlur = () => {
    let value = item.value;
    const type = item.type;
    if (type === 'Object' || type === 'Array') {
      try {
        value = JSON.parse(value);
      } catch (error) {
        // 不影响，报错可以继续执行.
      }
    }
    const key = 'value';
    onChange(item.id, [{key, value}]);
  };

  /**
   * 当reference的value变化时的处理方法.
   *
   * @param referenceKey 键值.
   * @param value 值.
   * @param type 类型.
   */
  const onReferenceValueChange = (referenceKey, value, type) => {
    onChange(item.id, [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {key: 'type', value: type}]);
  };

  /* 当reference的key变化时的处理方法. */
  const onReferenceKeyChange = (e) => {
    onChange(item.id, [{key: 'referenceNode', value: e.referenceNode}, {key: 'referenceId', value: e.referenceId}, {
      key: 'referenceKey',
      value: e.referenceKey,
    }, {key: 'value', value: e.value}, {key: 'type', value: e.type}]);
  };

  /* 获取值field. */
  const getValueField = () => {
    if (item.from === FROM_TYPE.INPUT) {
      return <Form.Item
        id={`value-${item.id}`}
        name={`value-${item.id}`}
        rules={inputRequired ? [{
          required: true, message: t('fieldValueCannotBeEmpty'),
        }] : []}
        initialValue={item.type === DATA_TYPES.ARRAY || item.type === DATA_TYPES.OBJECT ? JSON.stringify(item.value) : item.value}
        validateTrigger='onBlur'
      >
        <JadeInput
          disabled={shapeStatus.disabled}
          className='jade-input'
          style={{borderRadius: '0px 8px 8px 0px'}}
          placeholder={t('plsEnter')}
          value={item.type === DATA_TYPES.ARRAY || item.type === DATA_TYPES.OBJECT ? JSON.stringify(item.value) : item.value}
          onChange={(e) => onInputChange('value', e)}
          onBlur={() => onInputBlur()}
        />
      </Form.Item>;
    } else if (item.from === FROM_TYPE.REFERENCE) {
      return <JadeReferenceTreeSelect
        className='jade-input-tree-title-tree-select jade-select'
        disabled={shapeStatus.referenceDisabled}
        rules={[{
          required: true, message: t('fieldValueCannotBeEmpty'),
        }]}
        reference={item}
        onReferencedKeyChange={(e) => onReferenceKeyChange(e)}
        onReferencedValueChange={(referenceKey, value, type) => onReferenceValueChange(referenceKey, value, type)}
        width={100}
      />;
    } else {
      return null;
    }
  };

  return (<>
    <span className={'jade-font-size jade-font-color'}>{label}</span>
    <div className='jade-input-tree-title'>
      <Row wrap={false}>
        <Col flex="0 0 70px" style={{paddingRight: 0}}>
          <div className='jade-input-tree-title-child'>
            <Form.Item name={`value-select-${item.id}`}>
              <JadeInputTreeSelect node={item}
                                   options={[{value: FROM_TYPE.REFERENCE, label: t('reference')}, {value: FROM_TYPE.INPUT, label: t('input')}]}
                                   updateItem={onChange}
                                   disabled={shapeStatus.disabled}/>
            </Form.Item>
          </div>
        </Col>
        <Col flex="0 0 233px" style={{width: 100}}>
          <div className='jade-input-tree-title-child'>
            {getValueField(item)}
          </div>
        </Col>
      </Row>
    </div>
  </>);
};

FormItemSelectValue.propTypes = {
  item: PropTypes.object.isRequired, // 确保 item 是一个必需的对象
  shapeStatus: PropTypes.object.isRequired, // 确保 shapeStatus 是一个必需的对象
  onChange: PropTypes.func.isRequired, // 确保 onChange 是一个必需的函数
  label: PropTypes.string.isRequired, // 确保 label 是一个必需的字符串
  inputRequired: PropTypes.bool, // 确保 inputRequired 是一个bool
};