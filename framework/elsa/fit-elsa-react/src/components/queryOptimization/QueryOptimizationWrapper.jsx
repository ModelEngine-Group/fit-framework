/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {JadeInputForm} from '../common/JadeInputForm.jsx';
import './style.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {Trans} from 'react-i18next';
import {OptimizationConfig} from '@/components/queryOptimization/OptimizationConfig.jsx';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import httpUtil from '@/components/util/httpUtil.jsx';

/**
 * 输入参数国际化描述
 *
 * @type {JSX.Element}
 */
const inputDescription = (<>
  <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='optimizationInputPopover' components={{p: <p/>}}/>
  </div>
</>);

/**
 * 输出参数国际化描述
 *
 * @return {JSX.Element|null} 描述信息div
 */
const getOutputDescription = () => {
  return (<>
    <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <Trans i18nKey='optimizationOutputPopover' components={{p: <p/>}}/>
    </div>
  </>);
};

/**
 * 构造输入组件操作方法
 *
 * @param dispatch react机制，发送事件类型和数据
 * @returns {{addItem: addItem, deleteItem: deleteItem, updateItem: updateItem, initItems: (function(): *)}}
 */
const getInputOperateFunction = (dispatch) => {
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
const EMPTY_STRING = '';

/**
 * 问题改写节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
const _QueryOptimizationWrapper = ({data, shapeStatus}) => {
  const dispatch = useDispatch();
  const shape = useShapeContext();
  let config;
  if (shape?.graph?.configs) {
    config = shape.graph.configs.find(node => node.node === 'queryOptimizationNodeState');
  }
  const [modelOptions, setModelOptions] = useState([]);
  const rewriteParam = data.inputParams.find(item => item.name === 'rewriteParam');
  const temperature = getConfigValue(rewriteParam, ['temperature'], EMPTY_STRING);
  const template = getConfigValue(rewriteParam, ['template'], EMPTY_STRING);
  const strategy = getConfigValue(rewriteParam, ['strategy'], EMPTY_STRING);
  const serviceName = getConfigValue(rewriteParam, ['accessInfo', 'serviceName'], EMPTY_STRING);
  const tag = getConfigValue(rewriteParam, ['accessInfo', 'tag'], EMPTY_STRING);
  const memoryConfig = data.inputParams.find(item => item.name === 'memoryConfig');
  const {addItem, updateItem, deleteItem} = getInputOperateFunction(dispatch);

  /**
   * 初始化数据
   *
   * @return {*}
   */
  const initItems = () => getConfigValue(rewriteParam, ['args']);

  useEffect(() => {
    if (config?.urls?.llmModelEndpoint) {
      // 发起网络请求获取 options 数据
      httpUtil.get(`${config.urls.llmModelEndpoint}`, new Map(), (jsonData) => {
        setModelOptions(jsonData.models.map(item => ({
          value: `${item.serviceName}&&${item.tag}`,
          label: item.serviceName,
        })));
      });
    }
  }, []); // useEffect 依赖数组为空，表示只在组件挂载时执行一次

  return (<>
    <JadeInputForm
      shapeStatus={shapeStatus}
      items={initItems()}
      addItem={addItem}
      updateItem={updateItem}
      deleteItem={deleteItem}
      editable={false}
      content={inputDescription}
      maxInputLength={1000}/>
    <OptimizationConfig modelOptions={modelOptions} disabled={shapeStatus.disabled} template={template}
                        strategy={strategy} memoryConfig={memoryConfig} temperature={temperature}
                        serviceName={serviceName} tag={tag}/>
    <InvokeOutput outputData={data.outputParams} getDescription={getOutputDescription}/>
  </>);
};

_QueryOptimizationWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const QueryOptimizationWrapper = React.memo(_QueryOptimizationWrapper);