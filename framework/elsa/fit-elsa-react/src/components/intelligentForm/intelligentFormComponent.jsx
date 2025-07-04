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
import {
  AddParamReducer,
  ChangeFormByMetaDataReducer,
  ChangeFormTypeReducer, DeleteFormReducer,
  DeleteParamReducer, UpdateInputReducer,
  UpdateParamReducer,
} from '@/components/intelligentForm/reducers.js';
import {FORM_TYPE, ORCHESTRATION_INIT_ENTITY, ORCHESTRATION_TASK_ID} from '@/components/intelligentForm/Consts.js';

export const intelligentFormComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, AddParamReducer());
  addReducer(builtInReducers, UpdateParamReducer());
  addReducer(builtInReducers, DeleteParamReducer());
  addReducer(builtInReducers, ChangeFormTypeReducer());
  addReducer(builtInReducers, ChangeFormByMetaDataReducer());
  addReducer(builtInReducers, DeleteFormReducer());
  addReducer(builtInReducers, UpdateInputReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      converter: {
        type: 'mapping_converter',
        entity: JSON.parse(JSON.stringify(ORCHESTRATION_INIT_ENTITY)),
      },
      formType: FORM_TYPE.ORCHESTRATION,
      taskId: ORCHESTRATION_TASK_ID,
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