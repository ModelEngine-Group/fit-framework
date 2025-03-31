/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {defaultComponent} from '@/components/defaultComponent.js';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import IntelligentFormWrapper from '@/components/intelligentForm/IntelligentFormWrapper.jsx';
import {AddParamReducer, DeleteParamReducer, UpdateParamReducer} from '@/components/intelligentForm/reducers.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

export const intelligentFormComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, AddParamReducer());
  addReducer(builtInReducers, UpdateParamReducer());
  addReducer(builtInReducers, DeleteParamReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      converter: {
        type: 'mapping_converter',
        entity: {
          inputParams: [{
            id: uuidv4(),
            name: 'data',
            type: DATA_TYPES.OBJECT,
            from: FROM_TYPE.EXPAND,
            value: [],
          }, {
            id: uuidv4(),
            name: 'schema',
            type: DATA_TYPES.OBJECT,
            from: FROM_TYPE.INPUT,
            value: {
              parameters: [],
            },
          }],
          outputParams: [{
            id: uuidv4(),
            name: 'output',
            type: DATA_TYPES.OBJECT,
            value: [],
          }],
        },
      },
      taskId: 'a910a3d38a4549eda1112beee008419d',
      type: 'AIPP_SMART_FORM',
    };
  };

  /**
   *
   * 必须.
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <IntelligentFormWrapper data={data} shapeStatus={shapeStatus}/>
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