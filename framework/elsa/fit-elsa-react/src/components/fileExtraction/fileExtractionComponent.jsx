/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {FileExtractionWrapper} from './FileExtractionWrapper.jsx';
import {defaultComponent} from '../defaultComponent.js';
import {ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import {ChangePromptReducer, EditInputReducer} from '@/components/fileExtraction/reducers.js';

/**
 * 文件提取节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const fileExtractionComponent = (jadeConfig, shape) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, EditInputReducer());
  addReducer(builtInReducers, ChangePromptReducer('fileExtractionParam'));
  addReducer(builtInReducers, ChangeFlowMetaReducer(shape, self));

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: `fileExtraction_${uuidv4()}`,
          name: 'fileExtractionParam',
          type: 'Object',
          from: 'Expand',
          value: [
            {
              id: uuidv4(),
              name: 'files',
              type: 'Array',
              from: 'Reference',
              editable: false,
              value: '',
              referenceNode: '',
              referenceId: '',
              referenceKey: '',
            },
            {
              id: `prompt_${uuidv4()}`,
              name: 'prompt',
              type: 'String',
              from: 'Input',
              value: '',
            }],
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
    return (<><FileExtractionWrapper shapeStatus={shapeStatus} data={data}/></>);
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