/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import React from 'react';
import PropTypes from 'prop-types';
import {Trans} from 'react-i18next';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {JadeInputForm} from '@/components/common/JadeInputForm.jsx';
import {RequestConfig} from '@/components/httpNode/RequestConfig.jsx';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {RequestParams} from '@/components/httpNode/RequestParams.jsx';

/**
 * 构造输入组件操作方法
 *
 * @param data jadeConfig
 * @param dispatch react机制，发送事件类型和数据
 * @returns {{addItem: addItem, deleteItem: deleteItem, updateItem: updateItem, initItems: (function(): *)}}
 */
const getInputOperateFunction = (data, dispatch) => {
  /**
   * 添加输入的变量
   *
   * @param id id 数据id
   */
  const addItem = (id) => {
    dispatch({actionType: 'addInput', id: id});
  };

  /**
   * 更新入参变量属性名或者类型
   *
   * @param id 数据id
   * @param value 新值
   */
  const updateItem = (id, value) => {
    dispatch({actionType: 'editInput', id: id, changes: value});
  };

  /**
   * 删除input
   *
   * @param id 需要删除的数据id
   */
  const deleteItem = (id) => {
    dispatch({actionType: 'deleteInput', id: id});
  };

  return {addItem, updateItem, deleteItem};
};

/**
 * 输入参数国际化描述
 *
 * @type {JSX.Element}
 */
const inputDescription = (<>
  <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='httpInputTips' components={{p: <p/>}}/>
  </div>
</>);

/**
 * 问题改写节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
const _HttpWrapper = ({data, shapeStatus}) => {
  const dispatch = useDispatch();
  const allBodyData = data.inputParams.find(item => item.name === 'allBodyData');
  const httpRequest = data.inputParams.find(item => item.name === 'httpRequest');
  const httpMethod = getConfigValue(httpRequest, ['httpMethod'], '');
  const activeKey = data.inputParams.find(item => item.name === 'activeKey').value;
  const url = getConfigValue(httpRequest, ['url'], '');
  const timeout = getConfigValue(httpRequest, ['timeout'], '');
  const params = getConfigValue(httpRequest, ['params'], '');
  const headers = getConfigValue(httpRequest, ['headers'], '');
  const authentication = getConfigValue(httpRequest, ['authentication'], '');
  const {addItem, updateItem, deleteItem} = getInputOperateFunction(data, dispatch);

  /**
   * 输出参数国际化描述
   *
   * @return {JSX.Element|null} 描述信息div
   */
  const getOutputDescription = () => {
    return (<>
      <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='httpOutputTips' components={{p: <p/>}}/>
      </div>
    </>);
  };

  /**
   * 初始化数据
   *
   * @return {*}
   */
  const initItems = () => getConfigValue(httpRequest, ['args']);

  return (<>
    <JadeInputForm
      typeSelectClassName={'http-field-type-select'}
      fieldValueClassName={'http-field-value'}
      deleteBtnClassName={'delete-btn'}
      shapeStatus={shapeStatus}
      items={initItems()}
      addItem={addItem}
      updateItem={updateItem}
      deleteItem={deleteItem}
      content={inputDescription}/>
    <RequestConfig
      disabled={shapeStatus.disabled} authentication={authentication} httpMethod={httpMethod} timeout={timeout}
      url={url}/>
    <RequestParams
      disabled={shapeStatus.disabled} params={params} headers={headers} requestBody={allBodyData}
      activeKey={activeKey} httpMethod={httpMethod}/>
    <InvokeOutput outputData={data.outputParams} getDescription={getOutputDescription}/>
  </>);
};

_HttpWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const HttpWrapper = React.memo(_HttpWrapper);