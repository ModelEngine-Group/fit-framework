/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Collapse, Form, Input} from 'antd';
import {useDispatch, useFormContext} from '../DefaultRoot.jsx';
import {JadeCollapse} from '../common/JadeCollapse.jsx';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {JadeReferenceTreeSelect} from '@/components/common/JadeReferenceTreeSelect.jsx';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {DATA_TYPES} from '@/common/Consts.js';
import IconTrashBin from '../asserts/icon-delete-trash-bin.svg?react'; // 导入背景图片

const {TextArea} = Input;
const {Panel} = Collapse;

/**
 * 变量更新节点输入表单
 *
 * @param variables 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} DOM对象
 */
const _VariableUpdateInputForm = ({variables, shapeStatus}) => {

  return (<>
    <div className={'jade-variable-input-content'}>
      {
        variables.map(v => {
          return (
            <VariableItem key={v.id} variables={variables} variable={v} shapeStatus={shapeStatus}/>
          );
        })
      }
    </div>
  </>);
};

_VariableUpdateInputForm.propTypes = {
  variables: PropTypes.array.isRequired, shapeStatus: PropTypes.object,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.shapeStatus === nextProps.shapeStatus && isVariablesEqual(prevProps, nextProps);
};

const isVariablesEqual = (prevProps, nextProps) => {
  if (prevProps.variables.length !== nextProps.variables.length) {
    return false;
  }
  for (let i = 0; i < prevProps.variables.length; i++) {
    if (prevProps.variables[i].id === nextProps.variables[i].id) {
      return false;
    }
  }
  return true;
};

export const VariableUpdateInputForm = React.memo(_VariableUpdateInputForm, areEqual);

/**
 * 变量条目.
 *
 * @param variable 变量.
 * @param variables 变量列表.
 * @param shapeStatus 图形状态.
 * @returns {Element} dom元素.
 * @constructor
 */
const VariableItem = ({variable, variables, shapeStatus}) => {
  const {t} = useTranslation();
  const dispatch = useDispatch();
  const form = useFormContext();
  const keyItem = variable?.value?.find(item => item.name === 'key') ?? {};
  const valueItem = variable?.value?.find(item => item.name === 'value') ?? {};

  const deleteVariable = () => {
    if (variables.length === 1) {
      return;
    }
    dispatch({actionType: 'deleteVariable', data: {id: variable.id}});
  };

  const handleItemChange = (key, changes) => {
    dispatch({actionType: 'updateVariable', data: {id: variable.id, key: key, updates: changes}});
  }

  // 过滤类型，变量更新前后类型要保持一致.
  const typeFilter = (o) => {
    // keyItem未被选择，此时选择不到对应的结果.
    if (keyItem.dataType === '') {
      return false;
    }
    const defaultType = DATA_TYPES?.STRING?.toUpperCase() ?? "STRING"; // 防御性兜底
    const oType = o?.type?.toUpperCase() ?? defaultType;
    const refType = keyItem?.dataType?.toUpperCase() ?? defaultType;
    return oType === refType;
  };

  const clearValueItemInfo = () => {
    let changes = [{key: 'referenceKey', value: ''}, {key: 'referenceNode', value: ''}, {key: 'referenceId', value: ''},
      {key: 'type', value: DATA_TYPES.STRING,}, {key: 'value', value: ''}];
    handleItemChange('value', changes);
    form.resetFields([`valueSource-${valueItem.id}`, '']);
  };

  /**
   * 根据值渲染组件
   *
   * @param item 值
   * @return {JSX.Element|null}
   */
  const renderValueComponent = (item) => {
    switch (item.from) {
      case 'Reference':
        return (<>
          <JadeReferenceTreeSelect
            disabled={shapeStatus.referenceDisabled}
            reference={item}
            typeFilter={typeFilter}
            onReferencedValueChange={(referenceKey, value, type) => {
              let changes = [{key: 'referenceKey', value: referenceKey}, {key: 'value', value: value}, {key: 'type', value: type}];
              handleItemChange('value', changes);
            }}
            onReferencedKeyChange={(e) => {
              let changes = [{key: 'referenceNode', value: e.referenceNode},
                {key: 'referenceId', value: e.referenceId},
                {key: 'referenceKey', value: e.referenceKey},
                {key: 'value', value: e.value},
                {key: 'type', value: e.type}];
              handleItemChange('value', changes);
            }}
            style={{fontSize: '12px'}}
            placeholder={t('pleaseSelect')}
            onMouseDown={(e) => e.stopPropagation()}
            showSearch
            className='jade-select'
            dropdownStyle={{
              maxHeight: 400,
              overflow: 'auto',
            }}
            rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
          />
        </>);
      case 'Input':
        return (<>
          <Form.Item
            className='jade-form-item'
            name={`textarea-item-${item.id}`}
            rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
            initialValue={item.value}
            validateTrigger='onBlur'
          >
            <TextArea
              disabled={shapeStatus.disabled}
              maxLength={500}
              showCount
              className='jade-textarea-input jade-font-size'
              onBlur={(e) => {
                if (item.value !== e.target.value) {
                  let changes = [{key: 'value', value: e.target.value}];
                  handleItemChange('value', changes);
                }
              }}
            />
          </Form.Item>
        </>);
      default:
        return null;
    }
  };

  return (<JadeCollapse defaultActiveKey={['inputPanel']}>
    {<Panel
      key={'inputPanel'}
      className="jade-panel"
      header={
        <div style={{display: 'flex', alignItems: 'center'}}>
          {variables.length > 1 && <Button
            disabled={shapeStatus.disabled}
            type="text"
            className="icon-button start-node-delete-icon-button"
            onClick={deleteVariable}>
            <IconTrashBin/>
          </Button>}
        </div>
      }>
      <div className={'jade-custom-panel-content jade-variable-update-input-form'}>
        <span className={'jade-font-size'}>{t('variable')}</span>
        <JadeReferenceTreeSelect
          className='jade-select'
          disabled={shapeStatus.disabled}
          rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
          reference={keyItem}
           // 此节点需要获取对应变量路径用于替换值，此处value直接拼接referenceNode在前面就可以在businessData中找到对应的位置
          onReferencedValueChange={(referenceKey, value, type) => {
            let changes = [{key: 'referenceKey', value: referenceKey}, {key: 'dataType', value: type}, {key: 'value', value: [keyItem.value[0], ...value]}];
            handleItemChange('key', changes);
            if(type !== keyItem.dataType) {
              clearValueItemInfo();
            }
          }}
          onReferencedKeyChange={(e) => {
            let changes = [{key: 'referenceNode', value: e.referenceNode},
              {key: 'referenceId', value: e.referenceId},
              {key: 'referenceKey', value: e.referenceKey},
              {key: 'dataType', value: e.type},
              {key: 'value', value: [e.referenceNode, ...e.value]}];
            handleItemChange('key', changes);
            clearValueItemInfo();
          }}
        />
        <div>
          <span className={'jade-font-size'} style={{paddingRight: '4px'}}>{t('value')}</span>
          <Form.Item id={`valueSource-${valueItem.id}`} initialValue={valueItem.from}>
            <JadeStopPropagationSelect
              disabled={shapeStatus.disabled}
              id={`valueSource-select-${valueItem.id}`}
              className={'jade-select'}
              style={{width: '100%'}}
              onChange={(value) => {
                let changes = [{key: 'from', value: value}, {key: 'value', value: ''}];
                if (value === 'Input') {
                  changes = [{key: 'from', value: value},
                    {key: 'value', value: ''},
                    {key: 'type', value: DATA_TYPES.STRING},
                    {key: 'referenceNode', value: ''},
                    {key: 'referenceId', value: ''},
                    {key: 'referenceKey', value: ''}];
                }
                handleItemChange('value', changes);
                form.resetFields([`valueSource-${valueItem.id}`, value]);
              }}
              options={[{value: 'Reference', label: t('reference')}, {value: 'Input', label: t('input'),}]}
              value={valueItem.from}
            />
          </Form.Item>
        </div>
        {renderValueComponent(valueItem)}
      </div>
    </Panel>}
  </JadeCollapse>);
};

VariableItem.propTypes = {
  variable: PropTypes.object.isRequired,
  variables: PropTypes.array.isRequired,
  shapeStatus: PropTypes.object.isRequired,
};
