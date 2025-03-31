/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {defaultComponent} from '@/components/defaultComponent.js';
import {VariableAggregationWrapper} from '@/components/variableAggregation/VariableAggregationWrapper.jsx';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {
  AddVariableReducer, DeleteVariableReducer,
  UpdateVariableReducer,
} from '@/components/variableAggregation/variableAggregationReducers.js';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';

/**
 * 变量聚合节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const variableAggregationComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, AddVariableReducer());
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, UpdateVariableReducer());
  addReducer(builtInReducers, DeleteVariableReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [{
        id: `variables_${uuidv4()}`, name: 'variables', type: DATA_TYPES.ARRAY, from: FROM_TYPE.EXPAND, value: [
          {
            id: uuidv4(),
            name: null,
            type: null,
            from: FROM_TYPE.REFERENCE,
            value: [],
          },
        ],
      }],
      outputParams: [
        {
          id: `output_${uuidv4()}`, name: 'output', type: DATA_TYPES.STRING, from: FROM_TYPE.INPUT, value: '',
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
    return (<><VariableAggregationWrapper shapeStatus={shapeStatus} data={data}/></>);
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