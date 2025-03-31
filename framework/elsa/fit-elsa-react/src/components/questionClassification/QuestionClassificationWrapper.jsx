/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {Trans} from 'react-i18next';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import httpUtil from '@/components/util/httpUtil.jsx';
import {HistoryConfig} from '@/components/textExtraction/HistoryConfig.jsx';
import {JadeInputForm} from '@/components/common/JadeInputForm.jsx';
import {ModelConfig} from '@/components/common/ModelConfig.jsx';
import {QuestionClassificationPanel} from '@/components/questionClassification/QuestionClassificationPanel.jsx';

const EMPTY_STRING = '';

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

/**
 * 输入参数国际化描述
 *
 * @type {JSX.Element}
 */
const inputDescription = (<>
  <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='classificationInputPopover' components={{p: <p/>}}/>
  </div>
</>);

/**
 * 问题改写节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
const _QuestionClassificationWrapper = ({data, shapeStatus}) => {
  const dispatch = useDispatch();
  const shape = useShapeContext();
  let config;
  if (shape?.graph?.configs) {
    config = shape.graph.configs.find(node => node.node === 'questionClassificationNodeCondition');
  }
  const [modelOptions, setModelOptions] = useState([]);
  const classifyQuestionParam = data.inputParams.find(item => item.name === 'classifyQuestionParam');
  const temperature = getConfigValue(classifyQuestionParam, ['temperature'], EMPTY_STRING);
  const serviceName = getConfigValue(classifyQuestionParam, ['accessInfo', 'serviceName'], EMPTY_STRING);
  const tag = getConfigValue(classifyQuestionParam, ['accessInfo', 'tag'], EMPTY_STRING);
  const memoryConfig = data.inputParams.find(item => item.name === 'memoryConfig');
  const memorySwitch = data.inputParams.find(item => item.name === 'memorySwitch');
  const questionTypeList = getConfigValue(classifyQuestionParam, ['questionTypeList'], 'value');
  const template = getConfigValue(classifyQuestionParam, ['template'], EMPTY_STRING);
  const {addItem, updateItem, deleteItem} = getInputOperateFunction(dispatch);

  /**
   * 初始化数据
   *
   * @return {*}
   */
  const initItems = () => getConfigValue(classifyQuestionParam, ['args']);

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
      content={inputDescription}
      maxInputLength={1000}/>
    <ModelConfig
      modelOptions={modelOptions} temperature={temperature} serviceName={serviceName} tag={tag}
      description={template} disabled={shapeStatus.disabled} promptTitle={'userPromptTemplate'}
      promptPopover={'questionClassificationPromptPopover'}/>
    <HistoryConfig
      disabled={shapeStatus.disabled}
      dispatch={dispatch}
      memoryConfig={memoryConfig}
      memorySwitch={memorySwitch}/>
    <QuestionClassificationPanel
      shapeId={shape.id} disabled={shapeStatus.disabled}
      questionTypeList={questionTypeList}/>
  </>);
};

_QuestionClassificationWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const QuestionClassificationWrapper = React.memo(_QuestionClassificationWrapper);