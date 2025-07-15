/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ParallelWrapper from '@/components/parallelNode/ParallelWrapper.jsx';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';

import {defaultComponent} from '@/components/defaultComponent.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, DEFAULT_ADD_TOOL_NODE_CONTEXT, FROM_TYPE} from '@/common/Consts.js';
import {AddPluginByMetaDataReducer, DeletePluginReducer, UpdateInputReducer} from '@/components/parallelNode/reducers/reducers.js';
import {OUTPUT, TOOL_CALLS} from '@/components/parallelNode/consts.js';

export const parallelComponent = (jadeConfig, shape) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, AddPluginByMetaDataReducer(shape, self));
  addReducer(builtInReducers, DeletePluginReducer(shape, self));
  addReducer(builtInReducers, UpdateInputReducer(shape, self));
  addReducer(builtInReducers, ChangeFlowMetaReducer(shape, self));

  /**
   * 必填
   *
   * @return 组件信息
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: uuidv4(),
          name: TOOL_CALLS,
          type: DATA_TYPES.ARRAY,
          from: FROM_TYPE.EXPAND,
          value: [],
        },
        JSON.parse(JSON.stringify(DEFAULT_ADD_TOOL_NODE_CONTEXT))],
      outputParams: [{
        id: uuidv4(),
        name: OUTPUT,
        type: DATA_TYPES.OBJECT,
        from: FROM_TYPE.EXPAND,
        value: [],
      }],
    };
  };

  /**
   * 必须.
   */
  self.getReactComponents = (shapeStatus) => {
    return (<>
      <ParallelWrapper shapeStatus={shapeStatus}/>
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

  return self;
};