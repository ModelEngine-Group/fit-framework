/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ParallelWrapper from '@/components/parallelNode/ParallelWrapper.jsx';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';

import {defaultComponent} from '@/components/defaultComponent.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

export const parallelComponent = (jadeConfig, shape) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();

  addReducer(builtInReducers, ChangeFlowMetaReducer(shape, self));

  /**
   * 必填
   *
   * @return 组件信息
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [{
        id: uuidv4(),
        name: 'tools',
        type: DATA_TYPES.OBJECT,
        from: FROM_TYPE.EXPAND,
        value: [],
      },],
      outputParams: [],
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
    // 等其他节点改造完成，可以将reducers相关逻辑提取到基类中，子类中只需要向builtInReducers中添加reducer即可.
    const reducer = builtInReducers.get(action.type);
    return reducer ? reducer.reduce(config, action) : reducers.apply(self, [config, action]);
  };

  return self;
};