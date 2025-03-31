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
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {TextExtractionInput} from '@/components/textExtraction/TextExtractionInput.jsx';
import httpUtil from '@/components/util/httpUtil.jsx';
import {HistoryConfig} from '@/components/textExtraction/HistoryConfig.jsx';
import {TextExtractionSchema} from '@/components/textExtraction/TextExtractionSchema.jsx';
import {ModelConfig} from '@/components/common/ModelConfig.jsx';

const EMPTY_STRING = '';

/**
 * 输出参数国际化描述
 *
 * @return {JSX.Element|null} 描述信息div
 */
const getOutputDescription = () => {
  return (<>
    <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <Trans i18nKey='textExtractionOutputPopover' components={{p: <p/>}}/>
    </div>
  </>);
};

/**
 * 问题改写节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
const _TextExtractionWrapper = ({data, shapeStatus}) => {
  const dispatch = useDispatch();
  const shape = useShapeContext();
  let config;
  if (shape?.graph?.configs) {
    config = shape.graph.configs.find(node => node.node === 'textExtractionNodeState');
  }
  const [modelOptions, setModelOptions] = useState([]);
  const extractParam = data.inputParams.find(item => item.name === 'extractParam');
  const temperature = getConfigValue(extractParam, ['temperature'], EMPTY_STRING);
  const description = getConfigValue(extractParam, ['desc'], EMPTY_STRING);
  const serviceName = getConfigValue(extractParam, ['accessInfo', 'serviceName'], EMPTY_STRING);
  const tag = getConfigValue(extractParam, ['accessInfo', 'tag'], EMPTY_STRING);
  const memoryConfig = data.inputParams.find(item => item.name === 'memoryConfig');
  const memorySwitch = data.inputParams.find(item => item.name === 'memorySwitch');
  const treeData = data.outputParams.find(item => item.name === 'output').value.find(v => v.name === 'extractedParams');

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
    <TextExtractionInput shapeStatus={shapeStatus} extractParam={extractParam} dispatch={dispatch}/>
    <TextExtractionSchema disabled={shapeStatus.disabled} output={treeData}/>
    <ModelConfig
      modelOptions={modelOptions} temperature={temperature} serviceName={serviceName} tag={tag}
      description={description} disabled={shapeStatus.disabled} promptPopover={'optimizationPromptPopover'}
      promptTitle={'textExtractionPrompt'}/>
    <HistoryConfig
      disabled={shapeStatus.disabled}
      dispatch={dispatch}
      memoryConfig={memoryConfig}
      memorySwitch={memorySwitch}/>
    <InvokeOutput outputData={data.outputParams} getDescription={getOutputDescription}/>
  </>);
};

_TextExtractionWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const TextExtractionWrapper = React.memo(_TextExtractionWrapper);