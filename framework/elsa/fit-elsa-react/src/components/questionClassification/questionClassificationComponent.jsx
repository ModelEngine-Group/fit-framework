/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {
  AddInputReducer,
  ChangeAccessInfoConfigReducer,
  ChangeConfigReducer,
  ChangeFlowMetaReducer,
  ChangeHistoryTypeReducer,
  ChangeMemorySwitchReducer,
  ChangePromptValueReducer,
  ChangeWindowTypeReducer,
  ChangeWindowValueReducer,
  DeleteInputReducer,
  EditInputReducer,
} from '@/components/common/reducers/commonReducers.js';
import {QuestionClassificationWrapper} from '@/components/questionClassification/QuestionClassificationWrapper.jsx';
import {
  AddQuestionClassificationReducer,
  ChangeBranchesStatusReducer,
  ChangeQuestionClassificationDescReducer,
  DeleteQuestionClassificationReducer,
} from '@/components/questionClassification/reducers.js';

/**
 * 问题改写节点组件
 *
 * @param jadeConfig 组件配置信息
 * @param shape 图形
 * @return {{}} 组件
 */
export const questionClassificationComponent = (jadeConfig, shape) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, ChangePromptValueReducer('classifyQuestionParam'));
  addReducer(builtInReducers, ChangeMemorySwitchReducer());
  addReducer(builtInReducers, ChangeWindowValueReducer());
  addReducer(builtInReducers, ChangeWindowTypeReducer());
  addReducer(builtInReducers, ChangeHistoryTypeReducer());
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, ChangeBranchesStatusReducer(shape));
  addReducer(builtInReducers, ChangeConfigReducer('classifyQuestionParam'));
  addReducer(builtInReducers, ChangeAccessInfoConfigReducer('classifyQuestionParam'));
  addReducer(builtInReducers, AddInputReducer('classifyQuestionParam'));
  addReducer(builtInReducers, EditInputReducer('classifyQuestionParam'));
  addReducer(builtInReducers, DeleteInputReducer('classifyQuestionParam'));
  addReducer(builtInReducers, ChangeQuestionClassificationDescReducer('classifyQuestionParam'));
  addReducer(builtInReducers, AddQuestionClassificationReducer('classifyQuestionParam'));
  addReducer(builtInReducers, DeleteQuestionClassificationReducer('classifyQuestionParam'));

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: `classifyQuestionParam_${uuidv4()}`,
          name: 'classifyQuestionParam',
          type: 'Object',
          from: 'Expand',
          value: [{
            id: `args_${uuidv4()}`,
            name: 'args',
            type: 'Object',
            from: 'Expand',
            value: [
              {
                id: uuidv4(),
                name: 'query',
                type: 'String',
                from: 'Reference',
                value: '',
                referenceNode: '',
                referenceId: '',
                referenceKey: '',
                editable: false,
              },
            ],
          }, {
            id: `template_${uuidv4()}`,
            name: 'template',
            type: 'String',
            from: 'Input',
            value: '',
          }, {
            id: uuidv4(),
            name: 'accessInfo',
            type: 'Object',
            from: 'Expand',
            value: [
              {id: uuidv4(), name: 'serviceName', type: 'String', from: 'Input', value: ''},
              {id: uuidv4(), name: 'tag', type: 'String', from: 'Input', value: ''},
            ],
          }, {
            id: `temperature_${uuidv4()}`,
            name: 'temperature',
            type: 'Number',
            from: 'Input',
            value: '0.3',
          }, {
            id: `questionTypeList_${uuidv4()}`,
            name: 'questionTypeList',
            type: 'Array',
            from: 'Expand',
            value: [
              {
                id: uuidv4(),
                type: 'Object',
                from: 'Expand',
                conditionType: 'if',
                runnable: true,
                value: [{
                  id: `questionTypeId_${uuidv4()}`,
                  name: 'id',
                  type: 'String',
                  from: 'Input',
                  value: uuidv4(),
                }, {
                  id: `questionTypeDesc_${uuidv4()}`,
                  name: 'questionTypeDesc',
                  type: 'String',
                  from: 'Input',
                  value: '',
                }],
              }, {
                id: uuidv4(),
                type: 'Object',
                from: 'Expand',
                conditionType: 'else',
                runnable: true,
                value: [{
                  id: `questionTypeId_${uuidv4()}`,
                  name: 'id',
                  type: 'String',
                  from: 'Input',
                  value: uuidv4(),
                }, {
                  id: `questionTypeDesc_${uuidv4()}`,
                  name: 'questionTypeDesc',
                  type: 'String',
                  from: 'Input',
                  value: '其他问题分类',
                }],
              }],
          }],
        }, {
          id: `memoryConfig_${uuidv4()}`,
          name: 'memoryConfig',
          type: 'Object',
          from: 'Expand',
          value: [{
            id: `windowAlg_${uuidv4()}`,
            name: 'windowAlg',
            type: 'String',
            from: 'Input',
            value: 'buffer_window',
          }, {
            id: `serializeAlg_${uuidv4()}`,
            name: 'serializeAlg',
            type: 'String',
            from: 'Input',
            value: 'full',
          }, {
            id: `property_${uuidv4()}`,
            name: 'property',
            type: 'Integer',
            from: 'Input',
            value: '6',
          }],
        }, {
          id: `memorySwitch_${uuidv4()}`,
          name: 'memorySwitch',
          type: 'Boolean',
          from: 'Input',
          value: false,
        }, {
          id: `histories_${uuidv4()}`,
          name: 'histories',
          type: 'Array',
          from: 'Reference',
          referenceNode: '_systemEnv',
          referenceId: 'memories',
          referenceKey: 'memories',
          value: [
            'memories',
          ],
        },
      ],
      outputParams: [
        {
          id: `output_${uuidv4()}`,
          name: 'output',
          type: 'String',
          from: 'Input',
          value: '',
        },
      ],
    };
  };

  /**
   * 必须.
   *
   * @param shapeStatus 图形状态集合.
   * @param data 数据.
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<><QuestionClassificationWrapper shapeStatus={shapeStatus} data={data}/></>);
  };

  /**
   * 必须.
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    const reducer = builtInReducers.get(action.actionType) ?? builtInReducers.get(action.type);
    return reducer ? reducer.reduce(config, action) : reducers.apply(self, [config, action]);
  };

  return self;
};