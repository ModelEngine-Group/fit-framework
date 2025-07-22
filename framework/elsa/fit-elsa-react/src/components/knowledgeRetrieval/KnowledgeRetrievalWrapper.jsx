/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {KnowledgeForm} from '@/components/retrieval/KnowledgeForm.jsx';
import {OutputForm} from '@/components/common/OutputForm.jsx';
import PropTypes from 'prop-types';
import {JadeInputForm} from '@/components/common/JadeInputForm.jsx';
import React from 'react';
import {useTranslation} from 'react-i18next';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {SearchForm} from '@/components/knowledgeRetrieval/SearchForm.jsx';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';
import {OutsideRerankForm} from '@/components/knowledgeRetrieval/OutsideRerankForm.jsx';

/**
 * retrieval组件Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 节点状态
 * @returns {JSX.Element} retrieval组件dom
 */
export const KnowledgeRetrievalWrapper = ({data, shapeStatus}) => {
  const knowledge = data?.inputParams.find(item => item.name === 'knowledgeRepos')?.value ?? [];
  const outputParams = data && data.outputParams;
  const option = data.inputParams.find(item => item.name === 'option');
  const groupId = getConfigValue(option, ['groupId'], 'value');
  const knowledgeConfigId = getConfigValue(option, ['knowledgeConfigId'], 'value');
  const {t} = useTranslation();
  const dispatch = useDispatch();

  const tips = <div className={'jade-font-size'}><p>{t('knowledgeBaseInputPopover')}</p></div>;

  const updateItem = (id, changes) => {
    dispatch({actionType: 'updateInputParams', id, changes});
  };

  return (<>
    <JadeInputForm
      shapeStatus={shapeStatus}
      items={data.inputParams.filter(ip => ip.name === 'query')}
      updateItem={updateItem}
      content={tips}
      maxInputLength={1000}
      editable={false}/>
    <KnowledgeForm knowledge={knowledge} groupId={groupId} knowledgeConfigId={knowledgeConfigId} disabled={shapeStatus.disabled}/>
    <SearchForm option={option} groupId={groupId} shapeStatus={shapeStatus}/>
    <OutsideRerankForm option={option} shapeStatus={shapeStatus}/>
    <OutputForm outputParams={outputParams} outputPopover={'knowledgeBaseOutputPopover'}/>
  </>);
};

KnowledgeRetrievalWrapper.propTypes = {
  data: PropTypes.object.isRequired,
  shapeStatus: PropTypes.object,
};