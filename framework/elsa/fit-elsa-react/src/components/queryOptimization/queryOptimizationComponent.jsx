/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {QueryOptimizationWrapper} from '@/components/queryOptimization/QueryOptimizationWrapper.jsx';
import {ChangeTemplateTypeReducer, ChangeTemplateValueReducer} from '@/components/queryOptimization/reducers.js';
import {
  AddInputReducer,
  ChangeAccessInfoConfigReducer,
  ChangeConfigReducer,
  ChangeFlowMetaReducer,
  ChangeHistoryTypeReducer,
  ChangeWindowTypeReducer,
  ChangeWindowValueReducer,
  DeleteInputReducer,
  EditInputReducer,
} from '@/components/common/reducers/commonReducers.js';

const defaultJadeConfig = {
  inputParams: [
    {
      id: `rewriteParam_${uuidv4()}`,
      name: 'rewriteParam',
      type: 'Object',
      from: 'Expand',
      value: [{
        id: `strategy_${uuidv4()}`,
        name: 'strategy',
        type: 'String',
        from: 'Input',
        value: 'builtin',
      }, {
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
      type: 'Array',
      from: 'Expand',
      value: [],
    },
  ],
};

/**
 * 问题改写节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const queryOptimizationComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, ChangeTemplateTypeReducer());
  addReducer(builtInReducers, ChangeTemplateValueReducer());
  addReducer(builtInReducers, ChangeWindowValueReducer());
  addReducer(builtInReducers, ChangeWindowTypeReducer());
  addReducer(builtInReducers, ChangeHistoryTypeReducer());
  addReducer(builtInReducers, ChangeConfigReducer('rewriteParam'));
  addReducer(builtInReducers, ChangeAccessInfoConfigReducer('rewriteParam'));
  addReducer(builtInReducers, AddInputReducer('rewriteParam'));
  addReducer(builtInReducers, EditInputReducer('rewriteParam'));
  addReducer(builtInReducers, DeleteInputReducer('rewriteParam'));

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : defaultJadeConfig;
  };

  /**
   * 必须.
   *
   * @param shapeStatus 图形状态集合.
   * @param data 数据.
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<><QueryOptimizationWrapper shapeStatus={shapeStatus} data={data}/></>);
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