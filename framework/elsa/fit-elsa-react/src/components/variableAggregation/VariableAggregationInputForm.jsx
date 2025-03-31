/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import PropTypes from 'prop-types';
import {Button, Collapse} from 'antd';
import {JadePanelHeader} from '@/components/common/JadePanelHeader.jsx';
import {Trans, useTranslation} from 'react-i18next';
import {JadeReferenceTreeSelect} from '@/components/common/JadeReferenceTreeSelect.jsx';
import {MinusCircleOutlined} from '@ant-design/icons';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {DATA_TYPES} from '@/common/Consts.js';
import './variable.css';

const {Panel} = Collapse;

/**
 * 变量聚合节点输入表单
 *
 * @param variables 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} DOM对象
 */
const _VariableAggregationInputForm = ({variables, shapeStatus}) => {
  const dispatch = useDispatch();

  const content = (<>
    <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <Trans i18nKey='variableAggregationInputPopover' components={{p: <p/>}}/>
    </div>
  </>);

  // 添加变量.
  const addVariable = () => {
    dispatch({actionType: 'addVariable'});
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={['inputPanel']}>
      {<Panel
        key={'inputPanel'}
        className='jade-panel'
        header={<>
          <JadePanelHeader
            text={'input'}
            tips={content}
            shapeStatus={shapeStatus}
            onClick={addVariable}/>
        </>}>
        <div className={'jade-custom-panel-content'}>
          <VariableHeader variables={variables}/>
          <VariableContent variables={variables} shapeStatus={shapeStatus}/>
        </div>
      </Panel>}
    </Collapse>
  </>);
};

_VariableAggregationInputForm.propTypes = {
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

export const VariableAggregationInputForm = React.memo(_VariableAggregationInputForm, areEqual);

/**
 * 输入变量header.
 *
 * @param variables 变量参数.
 * @returns {Element} dom元素.
 * @constructor
 */
const VariableHeader = ({variables}) => {
  const {t} = useTranslation();

  return (<>
    <div className={'jade-variable-input-header'} style={{display: 'flex'}}>
      <div className={'jade-variable-input-header-label'}><span>{t('assignVariable')}</span></div>
      {variables.length > 0 ? (<>
        <div className={'jade-observable-tree-node-type-div'}>
          <span
            className={'jade-observable-tree-node-type-name'}>{variables[0].type?.capitalize() ?? DATA_TYPES.STRING}</span>
        </div>
      </>) : <></>}
    </div>
  </>);
};

VariableHeader.propTypes = {
  variables: PropTypes.array.isRequired,
};

/**
 * 输入变量content.
 *
 * @param variables 变量参数.
 * @param shapeStatus 图形状态.
 * @returns {Element} dom元素.
 * @constructor
 */
const VariableContent = ({variables, shapeStatus}) => {
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

VariableContent.propTypes = {
  variables: PropTypes.array.isRequired,
  shapeStatus: PropTypes.object.isRequired,
};

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

  // 过滤类型，variables中的类型要保持一致.
  const typeFilter = (o) => {
    // 所有变量都未引用，可以换类型.
    const referencedVariables = variables.filter(v => v.referenceKey !== null);
    if (referencedVariables.length === 0) {
      return true;
    }

    // 只有一个已引用变量，并且变量是自身，也可以换类型.
    if (referencedVariables.length === 1) {
      if (referencedVariables[0].id === variable.id) {
        return true;
      }
    }

    // o所代表的observable已被引用了，不可再次引用.
    if (referencedVariables.some(r => r.referenceId === o.observableId)) {
      return false;
    }

    return o.type.toUpperCase() === referencedVariables[0]?.type?.toUpperCase() ?? DATA_TYPES.STRING.toUpperCase();
  };

  // 删除变量.
  const deleteVariable = (id) => {
    if (variables.length === 1) {
      return;
    }
    dispatch({actionType: 'deleteVariable', data: {id: id}});
  };

  return (<>
    <div key={variable.id} style={{display: 'flex'}}>
      <div className={'jade-variable-input-content-item'} style={{width: '90%'}}>
        <JadeReferenceTreeSelect
          disabled={shapeStatus.referenceDisabled}
          rules={[{required: true, message: t('fieldValueCannotBeEmpty')}]}
          className='jade-variable-input-jade-tree-select jade-select'
          reference={variable}
          typeFilter={typeFilter}
          onReferencedValueChange={(referenceKey, value, type) => {
            dispatch({actionType: 'updateVariable', data: {id: variable.id, updates: {referenceKey, value, type}}});
          }}
          onReferencedKeyChange={(e) => {
            dispatch({actionType: 'updateVariable', data: {id: variable.id, updates: e}});
          }}/>
      </div>
      {
        variables.length > 1 && (<>
          <div style={{marginLeft: 10}}>
            <Button
              disabled={shapeStatus.disabled}
              type='text'
              className='icon-button'
              style={{height: '100%'}}
              onClick={() => deleteVariable(variable.id)}>
              <MinusCircleOutlined/>
            </Button>
          </div>
        </>)
      }
    </div>
  </>);
};

VariableItem.propTypes = {
  variable: PropTypes.object.isRequired,
  variables: PropTypes.array.isRequired,
  shapeStatus: PropTypes.object.isRequired,
};
