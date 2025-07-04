/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import LoopWrapper from '@/components/loopNode/LoopWrapper.jsx';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import {ChangePluginByMetaDataReducer, DeletePluginReducer, UpdateInputReducer, UpdateRadioInfoReducer} from '@/components/loopNode/reducers/reducers.js';
import {defaultComponent} from '@/components/defaultComponent.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, DEFAULT_ADD_TOOL_NODE_CONTEXT, FROM_TYPE} from '@/common/Consts.js';

export const loopComponent = (jadeConfig, shape) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, ChangePluginByMetaDataReducer(shape, self));
  addReducer(builtInReducers, DeletePluginReducer(shape, self));
  addReducer(builtInReducers, UpdateInputReducer(shape, self));
  addReducer(builtInReducers, UpdateRadioInfoReducer(shape, self));
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
          name: 'args',
          type: DATA_TYPES.OBJECT,
          from: FROM_TYPE.EXPAND,
          value: [],
        },
        {
          id: uuidv4(),
          name: 'config',
          type: DATA_TYPES.OBJECT,
          from: FROM_TYPE.INPUT,
          value: {},
        },
        {
          id: uuidv4(),
          name: 'toolInfo',
          type: DATA_TYPES.OBJECT,
          from: FROM_TYPE.INPUT,
          value: {},
        },
        JSON.parse(JSON.stringify(DEFAULT_ADD_TOOL_NODE_CONTEXT)),
      ],
      outputParams: [],
    };
  };

  /**
   * 必须.
   */
  self.getReactComponents = (shapeStatus) => {
    return (<>
      <LoopWrapper shapeStatus={shapeStatus}/>
    </>);
  };

  /**
   * @override
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    // 等其他节点改造完成，可以将reducers相关逻辑提取到基类中，子类中只需要向builtInReducers中添加reducer即可.
    const reducer = builtInReducers.get(action.type);
    return reducer ? reducer.reduce(config, action) : reducers.apply(self, [config, action]);
  };

  return self;
};