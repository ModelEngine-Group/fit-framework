/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ModelForm} from './ModelForm.jsx';
import {JadeInputForm} from '../common/JadeInputForm.jsx';
import {LlmOutput} from './LlmOutput.jsx';
import './style.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useEffect, useState} from 'react';
import httpUtil from '../util/httpUtil.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {v4 as uuidv4} from 'uuid';
import {SkillForm} from '@/components/llm/SkillForm.jsx';
import {KnowledgeForm} from './KnowledgeForm.jsx';

LlmFormWrapper.propTypes = {
  data: PropTypes.object.isRequired,
  shapeStatus: PropTypes.object,
};

/**
 * 大模型表单Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
export default function LlmFormWrapper({data, shapeStatus}) {
  const dispatch = useDispatch();
  const shape = useShapeContext();
  const {t} = useTranslation();
  let config;
  if (!shape || !shape.graph || !shape.graph.configs) {
    // 没关系，继续.
  } else {
    config = shape.graph.configs.find(node => node.node === 'llmNodeState');
  }
  const tool = data.inputParams.find(item => item.name === 'tools');
  const [modelOptions, setModelOptions] = useState([]);
  const [toolOptions, setToolOptions] = useState([]);
  const [previousToolValue, setPreviousToolValue] = useState(null);
  const modelData = {
    model: data.inputParams.find(item => item.name === 'model'),
    serviceName: data.inputParams.find(item => item.name === 'accessInfo')?.value?.find(subItem => subItem.name === 'serviceName') ?? undefined,
    tag: data.inputParams.find(item => item.name === 'accessInfo')?.value?.find(subItem => subItem.name === 'tag') ?? undefined,
    temperature: data.inputParams.find(item => item.name === 'temperature'),
    systemPrompt: data.inputParams.find(item => item.name === 'systemPrompt'),
    maxMemoryRounds: data.inputParams.find(item => item.name === 'maxMemoryRounds'),
    prompt: data.inputParams.filter(item => item.name === 'prompt')
      .flatMap(item => item.value)
      .find(item => item.name === 'template'),
  };
  const knowledgeData = data.inputParams.find(item => item.name === 'knowledgeBases');
  const enableLogData = data.inputParams.find(item => item.name === 'enableLog');

  const initItems = () => {
    return data.inputParams
      .filter(item => item.name === 'prompt') // 找出 name 为 "input" 的项
      .flatMap(item => item.value) // 将每个符合条件的项的 value 属性展开成一个数组
      .filter(item => item.name === 'variables') // 找出 name 为 "variable" 的项
      .flatMap(item => item.value); // 将每个符合条件的项的 value 属性展开成一个数组
  };

  const addItem = (id) => {
    // 大模型节点入参最大数量为20
    if (data.inputParams
      .filter(item => item.name === 'prompt') // 找出 name 为 "input" 的项
      .flatMap(item => item.value) // 将每个符合条件的项的 value 属性展开成一个数组
      .filter(item => item.name === 'variables') // 找出 name 为 "variable" 的项
      .flatMap(item => item.value).length < 20) {
      dispatch({type: 'addInputParam', id: id});
    }
  };

  const updateItem = (id, value) => {
    dispatch({type: 'changeInputParams', id: id, updateParams: value});
  };

  const deleteItem = (id) => {
    dispatch({type: 'deleteInputParam', id: id});
  };

  const content = (<>
    <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <Trans i18nKey='llmInputPopover' components={{p: <p/>}}/>
    </div>
  </>);

  /**
   * 获取技能详情信息
   */
  const getSkillInfo = () => {
    const uniqueNameList = [];
    if (tool && tool.value.length > 0) {
      uniqueNameList.push(...tool.value.map(toolItem => toolItem.value));
    }
    if (!uniqueNameList || uniqueNameList.length === 0) {
      setToolOptions([]);
      return;
    }
    const urlSuffix = uniqueNameList.map(uniqueName => `uniqueNames=${uniqueName}`).join('&');
    httpUtil.get(`${config.urls.toolListEndpoint}?${urlSuffix}`, new Map(), (jsonData) => {
        processToolData(jsonData.data);
      },
      () => {
        processToolData([]);
      },
    );

    const processToolData = (responseData) => {
      const responseMap = new Map(responseData.map(item => [item.uniqueName, item]));

      const toolOptions = uniqueNameList.map(uniqueName => {
        if (responseMap.has(uniqueName)) {
          const item = responseMap.get(uniqueName);
          return {
            id: uuidv4(),
            name: item.name,
            tags: item.tags,
            version: item.version,
            value: item.uniqueName,
          };
        } else {
          // 若请求返回体中没有该 uniqueName 或请求失败，则从 toolItem 中获取信息
          const fallbackItem = tool.value.find(toolItem => toolItem.value === uniqueName);
          return fallbackItem ? {
            id: fallbackItem.id,
            name: fallbackItem.name || 'Unknown',
            tags: fallbackItem.tags || [],
            version: fallbackItem.version || 'Unknown',
            value: fallbackItem.value,
          } : null;
        }
      }).filter(Boolean); // 过滤掉 null 值

      setToolOptions(toolOptions);
    };
  };

  useEffect(() => {
    if (toolOptions.length > 0) {
      dispatch({type: 'updateTools', value: toolOptions});
    }
  }, [toolOptions]);

  useEffect(() => {
    if (!config || !config.urls) {
      // 没关系，继续.
    } else {
      if (!config.urls.llmModelEndpoint) {
        // 没关系，继续.
      } else {
        // 发起网络请求获取 options 数据
        httpUtil.get(`${config.urls.llmModelEndpoint}/fetch/model-list`, new Map(), (jsonData) => setModelOptions(jsonData.models.map(item => {
          return {
            value: `${item.serviceName}&&${item.tag}`,
            label: item.serviceName,
            title: t(item.tag),
          };
        })));
      }
      if (!config.urls.toolListEndpoint) {
        // 没关系，继续.
      } else {
        getSkillInfo();
      }
    }
  }, []); // useEffect 依赖数组为空，表示只在组件挂载时执行一次

  useEffect(() => {
    const newToolValue = tool?.value?.map(item => item.value);
    if (JSON.stringify(newToolValue) !== JSON.stringify(previousToolValue)) {
      getSkillInfo();
      setPreviousToolValue(newToolValue);
    }
  }, [tool?.value]);

  return (
    <div>
      <JadeInputForm
        shapeStatus={shapeStatus}
        items={initItems()}
        addItem={addItem}
        updateItem={updateItem}
        deleteItem={deleteItem}
        content={content}
        maxInputLength={1000}/>
      <KnowledgeForm disabled={shapeStatus.disabled} knowledgeData={knowledgeData}/>
      <ModelForm
        disabled={shapeStatus.disabled}
        modelData={modelData} shapeId={shape.id}
        modelOptions={modelOptions}/>
      <SkillForm disabled={shapeStatus.disabled} toolOptions={toolOptions}/>
      <LlmOutput outputItems={data.outputParams} enableLogData={enableLogData} disabled={shapeStatus.disabled}/>
    </div>
  );
}