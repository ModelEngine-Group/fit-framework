/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {InputForm} from '@/components/retrieval/InputForm.jsx';
import {KnowledgeForm} from '@/components/retrieval/KnowledgeForm.jsx';
import PropTypes from 'prop-types';
import {OutputForm} from '@/components/common/OutputForm.jsx';

RetrievalWrapper.propTypes = {
  data: PropTypes.object.isRequired,
  shapeStatus: PropTypes.object,
};


/**
 * retrieval组件Wrapper
 *
 * @param data 数据.
 * @param shapeStatus 节点状态
 * @returns {JSX.Element} retrieval组件dom
 */
export default function RetrievalWrapper({data, shapeStatus}) {
  const queryData = data && data.inputParams.find(item => item.name === 'query');
  const knowledge = data?.inputParams.find(item => item.name === 'knowledge')?.value ?? [];
  const maximum = data && data.inputParams.find(item => item.name === 'maximum').value;
  const outputParams = data && data.outputParams;

  return (<>
    <InputForm shapeStatus={shapeStatus} queryData={queryData}/>
    <KnowledgeForm
      knowledge={knowledge}
      maximum={maximum}
      disabled={shapeStatus.disabled}
      enableGroup={false}/>
    <OutputForm outputParams={outputParams} outputPopover={'knowledgeBaseOutputPopover'}/>
  </>);
}