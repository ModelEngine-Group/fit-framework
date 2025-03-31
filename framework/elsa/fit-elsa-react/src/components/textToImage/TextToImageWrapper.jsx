/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import React from 'react';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {JadeInputForm} from '@/components/common/JadeInputForm.jsx';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {AiPromptPanel} from '@/components/common/AiPromptPanel.jsx';
import {TextToImageParamConfig} from '@/components/textToImage/TextToImageParamConfig.jsx';

/**
 * 输入参数国际化描述
 *
 * @type {JSX.Element}
 */
const inputDescription = (<>
  <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='fileExtractionInputTips' components={{p: <p/>}}/>
  </div>
</>);

/**
 * 多模态文件提取节点Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @returns {JSX.Element} 大模型表单Wrapper的DOM
 */
const _FileExtractionWrapper = ({data, shapeStatus}) => {
  const dispatch = useDispatch();
  const generateImageParam = data.inputParams.find(item => item.name === 'imageParam');
  const prompt = getConfigValue(generateImageParam, ['description'], '');
  const imageCount = getConfigValue(generateImageParam, ['imageCount'], '');
  const shapeId = useShapeContext().id;
  const {t} = useTranslation();

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
   * 输出参数国际化描述
   *
   * @return {JSX.Element|null} 描述信息div
   */
  const getOutputDescription = () => {
    return (<>
      <div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='fileExtractionOutputTips' components={{p: <p/>}}/>
      </div>
    </>);
  };

  /**
   * 添加输入的变量
   *
   * @param id id 数据id
   */
  const addItem = (id) => {
    dispatch({actionType: 'addInput', id: id});
  };

  /**
   * 删除input
   *
   * @param id 需要删除的数据id
   */
  const deleteItem = (id) => {
    dispatch({actionType: 'deleteInput', id: id});
  };

  /**
   * 初始化数据
   *
   * @return {*}
   */
  const initItems = () => getConfigValue(generateImageParam, ['args'], 'value');

  return (<>
    <JadeInputForm
      shapeStatus={shapeStatus}
      addItem={addItem}
      deleteItem={deleteItem}
      items={initItems()}
      updateItem={updateItem}
      maxInputLength={1000}
      content={inputDescription}/>
    <AiPromptPanel
      name={`textToImagePromptDrawer-${shapeId}-${prompt.id}`}
      prompt={prompt}
      header={t('textToImageConfigPanelHeader')}
      drawerTitle={t('textToImagePrompt')}
      disabled={shapeStatus.disabled}/>
    <TextToImageParamConfig imageCount={imageCount} disabled={shapeStatus.disabled}/>
    <InvokeOutput outputData={data.outputParams} getDescription={getOutputDescription}/>
  </>);
};

_FileExtractionWrapper.propTypes = {
  data: PropTypes.object.isRequired, shapeStatus: PropTypes.object,
};

export const TextToImageWrapper = React.memo(_FileExtractionWrapper);