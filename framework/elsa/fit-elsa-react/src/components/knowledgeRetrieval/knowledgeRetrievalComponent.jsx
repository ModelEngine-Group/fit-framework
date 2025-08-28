/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {KnowledgeRetrievalWrapper} from '@/components/knowledgeRetrieval/KnowledgeRetrievalWrapper.jsx';
import {retrievalComponent} from '@/components/retrieval/retrievalComponent.jsx';
import {
  DATA_TYPES,
  DEFAULT_KNOWLEDGE_NODE_ACCESS_INFO,
  DEFAULT_KNOWLEDGE_NODE_RERANK_TOP_N,
  DEFAULT_KNOWLEDGE_REPO_GROUP,
  DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_KNOWLEDGE_CONFIG_ID,
  FROM_TYPE,
  DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_EXTENSIONS,
} from '@/common/Consts.js';
import {
  ChangeAccessInfoReducer,
  ChangeRerankParamReducer,
  UpdateGroupIdAndConfigIdReducer,
  UpdateInputParamReducer,
  UpdateKnowledgeReducer,
  UpdateOptionReducer,
} from '@/components/knowledgeRetrieval/reducers.js';

/**
 * retrieval节点组件
 *
 * @param jadeConfig 配置
 * @param shape 对应图形
 */
export const knowledgeRetrievalComponent = (jadeConfig, shape) => {
  const self = retrievalComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, UpdateInputParamReducer());
  addReducer(builtInReducers, UpdateOptionReducer());
  addReducer(builtInReducers, UpdateKnowledgeReducer());
  addReducer(builtInReducers, UpdateGroupIdAndConfigIdReducer());
  addReducer(builtInReducers, ChangeRerankParamReducer());
  addReducer(builtInReducers, ChangeAccessInfoReducer());

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
        type: 'String',
        from: 'Reference',
        referenceNode: '',
        referenceId: '',
        referenceKey: '',
        editable: false,
        value: [],
      }, {
        id: `knowledge_${uuidv4()}`,
        name: 'knowledgeRepos',
        type: DATA_TYPES.ARRAY,
        from: FROM_TYPE.EXPAND,
        value: [],
      }, {
        id: `retriever_option_${uuidv4()}`,
        name: 'option',
        type: DATA_TYPES.OBJECT,
        from: FROM_TYPE.EXPAND,
        value: [{
          id: '03ce03b6-8d00-4fb0-bf32-85b2b40aaaee',
          from: FROM_TYPE.EXPAND,
          name: 'indexType',
          type: DATA_TYPES.OBJECT,
          value: [
            {
              id: '543ff920-9927-48c6-bb65-cb1b97944b65',
              from: FROM_TYPE.INPUT,
              name: 'type',
              type: DATA_TYPES.STRING,
              value: 'semantic',
            },
            {
              id: '03d471a3-d4da-48a3-bbf8-d05bf06374e1',
              from: FROM_TYPE.INPUT,
              name: 'name',
              type: DATA_TYPES.STRING,
              value: `${shape?.graph?.i18n?.t('semanticRetrieve') ?? 'semanticRetrieve'}`,
            },
            {
              id: '647d0884-5539-4618-922e-af12b08d1d34',
              from: FROM_TYPE.INPUT,
              name: 'description',
              type: DATA_TYPES.STRING,
              value: '基于文本的含义检索出最相关的内容',
            },
          ],
        },
          {
            id: 'a6a619c8-eef0-4bfa-9e12-a8994edfb83f',
            name: 'similarityThreshold',
            type: DATA_TYPES.NUMBER,
            from: FROM_TYPE.INPUT,
            value: 0.5,
          },
          {
            id: 'c809934a-9023-48dc-a2c8-e33274ab7101',
            name: 'referenceLimit',
            type: DATA_TYPES.OBJECT,
            from: FROM_TYPE.EXPAND,
            value: [
              {
                id: '369ad79e-397f-417c-b671-c4f714734693',
                name: 'type',
                type: DATA_TYPES.STRING,
                from: FROM_TYPE.INPUT,
                value: 'topK',
              },
              {
                id: '31071b92-7d9f-443b-930c-3329d05671f5',
                name: 'value',
                type: DATA_TYPES.INTEGER,
                from: FROM_TYPE.INPUT,
                value: 3,
              },
            ],
          },
          {
            id: 'e45abef0-e276-42ea-832a-87e4a2aeb2be',
            name: 'rerankParam',
            type: DATA_TYPES.OBJECT,
            from: FROM_TYPE.EXPAND,
            value: [
              {
                id: '5b737124-7de9-45b9-bff3-87c6b4d817e8',
                name: 'enableRerank',
                type: DATA_TYPES.BOOLEAN,
                from: FROM_TYPE.INPUT,
                value: false,
              },
              DEFAULT_KNOWLEDGE_NODE_ACCESS_INFO,
              DEFAULT_KNOWLEDGE_NODE_RERANK_TOP_N,
            ],
          },
          {
            id: uuidv4(),
            name: 'groupId',
            type: DATA_TYPES.STRING,
            from: FROM_TYPE.INPUT,
            value: DEFAULT_KNOWLEDGE_REPO_GROUP,
          },
          JSON.parse(JSON.stringify(DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_KNOWLEDGE_CONFIG_ID)),
          JSON.parse(JSON.stringify(DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_EXTENSIONS)),
        ],
      }],
      outputParams: [{
        id: `output_${uuidv4()}`,
        name: 'output',
        type: DATA_TYPES.ARRAY,
        from: FROM_TYPE.EXPAND,
        value: [],
      }],
    };
  };

  /**
   * @override
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <KnowledgeRetrievalWrapper shapeStatus={shapeStatus} data={data}/>
    </>);
  };

  /**
   * @override
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    let reducer = builtInReducers.get(action.actionType);
    if (!reducer) {
      reducer = builtInReducers.get(action.type);
    }
    return reducer ? reducer.reduce(config, action) : reducers.apply(self, [config, action]);
  };

  /**
   * @override
   */
  self.getKnowledgeRepos = (newConfig) => {
    return newConfig.inputParams.find(newTask => newTask.name === 'knowledgeRepos');
  };

  return self;
};
