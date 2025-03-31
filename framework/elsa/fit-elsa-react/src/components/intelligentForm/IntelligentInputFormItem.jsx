/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import {FormItemName} from '@/components/intelligentForm/FormItemName.jsx';
import Type from '@/components/common/Type.jsx';
import React from 'react';
import {useTranslation} from 'react-i18next';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {FormItemRenderType} from '@/components/intelligentForm/FormItemRenderType.jsx';
import {FormItemSelectValue} from '@/components/intelligentForm/FormItemSelectValue.jsx';
import {RENDER_OPTIONS_TYPE} from '@/components/intelligentForm/Consts.js';
import {FormItemDisplayName} from '@/components/intelligentForm/FormItemDisplayName.jsx';
import {Col, Row} from 'antd';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

/**
 * 智能表单节点入参表单元素
 *
 * @param item 表单对应的对象
 * @param items 所有入参表单对象
 * @param shapeStatus 图形状态
 * @param output 智能表单节点出参
 * @returns {JSX.Element} 开始节点入参表单的DOM
 */
const _IntelligentInputFormItem = ({item, items, shapeStatus, output}) => {
  const dispatch = useDispatch();
  const { t } = useTranslation();

  /**
   * 传递至子组件，用于使用dispatch更新
   *
   * @param id 修改的item唯一标识
   * @param entries 修改的数组
   */
  const handleFormValueChange = (id, entries = []) => {
    const changes = new Map(entries.map(({ key, value }) => [key, value])); // 将 entries 转换为 Map
    // 如果 type 是 'type'，清空 renderType
    if (changes.has('type') && changes.get('type') !== item.type) {
      changes.set('renderType', undefined);
    }
    dispatch({ type: 'updateParam', id: id, changes });
  };

  const handleChange = (value) => {
    handleFormValueChange(item.id, [{key: 'type', value: value}]); // 当选择框的值发生变化时调用父组件传递的回调函数
    document.activeElement.blur();// 在选择后取消焦点
  };

  const handleOptionsChange = (id, entries = []) => {
    const newOptions = {...item.options};
    const hasInputFrom = entries.some(entry => entry.key === 'from' && entry.value === FROM_TYPE.INPUT);
    entries.forEach(({key, value}) => {
      newOptions[key] = value;
    });
    if (hasInputFrom) {
      newOptions['type'] = DATA_TYPES.ARRAY;
    }
    handleFormValueChange(item.id, [{key: 'options', value: newOptions}]);
  };

  return (<>
    <Row>
      <Col flex='0 0 151px'>
        <FormItemName itemId={item.id} propValue={item.name} type={item.type} disabled={shapeStatus.disabled} onChange={handleFormValueChange} items={items} output={output}/>
      </Col>
      <Col flex='0 0 151px'>
        <FormItemDisplayName itemId={item.id} propValue={item.displayName} disabled={shapeStatus.disabled} onChange={handleFormValueChange} items={items}/>
      </Col>
    </Row>
    <Row>
      <Col flex='0 0 151px'>
        <Type itemId={item.id} propValue={item.type} disableModifiable={shapeStatus.disabled} onChange={handleChange} labelName={t('formItemType')} className={'intelligent-form-left-select'}/>
      </Col>
      <Col flex='0 0 151px'>
        <FormItemRenderType itemId={item.id} propValue={item.renderType} disabled={shapeStatus.disabled} onChange={handleFormValueChange} type={item.type}/>
      </Col>
    </Row>
    <FormItemSelectValue item={item} onChange={handleFormValueChange} shapeStatus={shapeStatus} label={t('formItemDefaultValue')}/>
    {RENDER_OPTIONS_TYPE.has(item.renderType) && <FormItemSelectValue item={item.options} onChange={handleOptionsChange} shapeStatus={shapeStatus} label={t('formItemOptionsValue')} inputRequired={true}/>}
  </>);
};

_IntelligentInputFormItem.propTypes = {
  item: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    from: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    options: PropTypes.object.isRequired,
  }).isRequired,
  shapeStatus: PropTypes.object.isRequired,
  output: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.item.id === nextProps.item.id &&
    prevProps.item.name === nextProps.item.name &&
    prevProps.item.type === nextProps.item.type &&
    prevProps.item.from === nextProps.item.from &&
    prevProps.item.value === nextProps.item.value &&
    prevProps.item.options === nextProps.item.options &&
    prevProps.items === nextProps.items;
};

export const IntelligentInputFormItem = React.memo(_IntelligentInputFormItem, areEqual);