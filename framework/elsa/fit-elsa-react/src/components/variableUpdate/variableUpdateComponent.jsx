/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';

import {defaultComponent} from '../defaultComponent.js';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {VariableUpdateWrapper} from '@/components/variableUpdate/VariableUpdateWrapper.jsx';
import {AddVariableReducer, DeleteVariableReducer, UpdateVariableReducer} from '@/components/variableUpdate/reducers/reducers.js';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import {createDefaultVariable} from '@/components/variableUpdate/Constant.js';


/**
 * 变量更新节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const variableUpdateComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, AddVariableReducer());
  addReducer(builtInReducers, UpdateVariableReducer());
  addReducer(builtInReducers, DeleteVariableReducer());
  addReducer(builtInReducers, ChangeFlowMetaReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    const newVariable = createDefaultVariable(); // 调用工厂函数
    newVariable.id = newVariable.id = `updateVariable_${newVariable.id}`;
    return jadeConfig ? jadeConfig : {
      inputParams: [{
        id: `variables_${uuidv4()}`, name: 'updateVariables', type: DATA_TYPES.ARRAY, from: FROM_TYPE.EXPAND, value: [
          newVariable,
        ],
      }],
      outputParams: [],
    };
  };

  /**
   * 必须.
   *
   * @param shapeStatus 图形状态集合.
   * @param data 数据.
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<><VariableUpdateWrapper shapeStatus={shapeStatus} data={data}/></>);
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