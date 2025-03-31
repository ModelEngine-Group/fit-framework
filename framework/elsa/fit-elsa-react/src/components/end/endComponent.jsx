/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {
  AddInputReducer,
  ChangeFormByMetaDataReducer,
  DeleteFormReducer,
  ChangeModeReducer,
  DeleteInputReducer,
  EditOutputVariableReducer,
  UpdateInputReducer, UpdateLogStatusReducer,
} from '@/components/end/reducers/reducers.js';
import {EndNodeWrapper} from '@/components/end/EndNodeWrapper.jsx';
import {FLOW_TYPE} from '@/common/Consts.js';
import {getDefaultReference} from '@/components/util/ReferenceUtil.js';

/**
 * 结束节点组件
 *
 * @param jadeConfig
 * @param shape 图形对象.
 */
export const endComponent = (jadeConfig, shape) => {
    const self = defaultComponent(jadeConfig);
    const addReducer = (map, reducer) => map.set(reducer.type, reducer);
    const builtInReducers = new Map();
    addReducer(builtInReducers, EditOutputVariableReducer(shape, self));
    addReducer(builtInReducers, ChangeFormByMetaDataReducer(shape, self));
    addReducer(builtInReducers, DeleteFormReducer(shape, self));
    addReducer(builtInReducers, ChangeModeReducer(shape, self));
    addReducer(builtInReducers, UpdateInputReducer(shape, self));
    addReducer(builtInReducers, DeleteInputReducer(shape, self));
    addReducer(builtInReducers, AddInputReducer(shape, self));
    addReducer(builtInReducers, UpdateLogStatusReducer(shape, self));

  /**
   * 必填
   *
   * @return 组件信息
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: shape.graph.flowType === FLOW_TYPE.APP ?
        self.getDefaultAppInputParams(uuidv4()) : self.getDefaultWorkflowInputParams(),
      outputParams: [{}],
    };
  };

  /**
   * 获取默认workflow输入参数.
   *
   * @returns {[{}]} 输入参数.
   */
  self.getDefaultWorkflowInputParams = () => {
    return [{
      id: uuidv4(),
      name: 'finalOutput',
      type: 'String',
      from: 'Reference',
      referenceNode: '',
      referenceId: '',
      referenceKey: '',
      value: [],
    }];
  };

  /**
   * 获取默认app输入参数.
   *
   * @param id
   * @returns 输入参数.
   */
  self.getDefaultAppInputParams = (id) => {
    return [{
      id: uuidv4(),
      name: 'finalOutput',
      from: 'Expand',
      type: 'Object',
      editable: false,
      value: [getDefaultReference(id)],
      isRequired: false,
      referenceNode: '',
      referenceKey: '',
      referenceId: '',
    }, {
      id: uuidv4(),
      from: 'Input',
      name: 'enableLog',
      type: 'Boolean',
      value: false,
    }];
  };

  /**
   * @override
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<EndNodeWrapper shapeStatus={shapeStatus} data={data}/>);
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