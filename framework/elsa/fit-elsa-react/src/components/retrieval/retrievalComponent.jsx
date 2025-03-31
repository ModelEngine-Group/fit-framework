/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import RetrievalWrapper from '@/components/retrieval/RetrievalWrapper.jsx';
import {defaultComponent} from '@/components/defaultComponent.js';
import {UpdateInputParamReducer} from '@/components/knowledgeRetrieval/reducers.js';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import {
  ChangeMaximumReducer,
  DeleteKnowledgeReducer,
  EditInputReducer,
  UpdateKnowledgeReducer,
} from '@/components/retrieval/reducers.js';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

/**
 * retrieval节点组件
 *
 * @param jadeConfig
 */
export const retrievalComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, EditInputReducer());
  addReducer(builtInReducers, UpdateKnowledgeReducer(self));
  addReducer(builtInReducers, DeleteKnowledgeReducer(self));
  addReducer(builtInReducers, ChangeMaximumReducer());
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, UpdateInputParamReducer());

  /**
   * 必填
   *
   * @return 组件信息
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [{
        id: `query_${uuidv4()}`,
        name: 'query',
        type: DATA_TYPES.STRING,
        from: FROM_TYPE.REFERENCE,
        referenceNode: '',
        referenceId: '',
        referenceKey: '',
        value: [],
      }, {
        id: `knowledge_${uuidv4()}`,
        name: 'knowledge',
        type: DATA_TYPES.ARRAY,
        from: FROM_TYPE.EXPAND,
        value: [],
      }, {
        id: `maximum_${uuidv4()}`,
        name: 'maximum',
        type: DATA_TYPES.INTEGER,
        from: FROM_TYPE.INPUT,
        value: 3,
      }],
      outputParams: [{
        id: `output_${uuidv4()}`,
        name: 'output',
        type: DATA_TYPES.OBJECT,
        from: FROM_TYPE.EXPAND,
        value: [{
          id: uuidv4(),
          name: 'retrievalOutput',
          type: DATA_TYPES.STRING,
          from: FROM_TYPE.INPUT,
          value: 'String',
        }],
      }],
    };
  };

  /**
   * @override
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <RetrievalWrapper shapeStatus={shapeStatus} data={data}/>
    </>);
  };

  /**
   * @override
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    const reducer = builtInReducers.get(action.type);
    return reducer ? reducer.reduce(config, action) : reducers.apply(self, [config, action]);
  };

  /**
   * 获取知识库.
   *
   * @param newConfig 配置.
   * @return {*[]} 知识库配置.
   */
  self.getKnowledgeRepos = (newConfig) => {
    return newConfig.inputParams.find(newTask => newTask.name === 'knowledge');
  };

  return self;
};
