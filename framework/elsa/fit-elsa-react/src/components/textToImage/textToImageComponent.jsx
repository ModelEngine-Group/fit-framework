/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {TextToImageWrapper} from './TextToImageWrapper.jsx';
import {defaultComponent} from '../defaultComponent.js';
import {AddInputReducer, DeleteInputReducer, EditInputReducer, ChangeFlowMetaReducer} from '@/components/common/reducers/commonReducers.js';
import {ChangePromptReducer} from '@/components/fileExtraction/reducers.js';
import {ChangeImageCountReducer} from '@/components/textToImage/reducers.js';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

/**
 * 文件提取节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const textToImageComponent = (jadeConfig, shape) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, AddInputReducer('imageParam'));
  addReducer(builtInReducers, EditInputReducer('imageParam'));
  addReducer(builtInReducers, DeleteInputReducer('imageParam'));
  addReducer(builtInReducers, ChangePromptReducer('imageParam'));
  addReducer(builtInReducers, ChangeImageCountReducer());
  addReducer(builtInReducers, ChangeFlowMetaReducer(shape, self));

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: `imageParam_${uuidv4()}`,
          name: 'imageParam',
          type: DATA_TYPES.OBJECT,
          from: FROM_TYPE.EXPAND,
          value: [
            {
              id: uuidv4(),
              name: 'args',
              type: DATA_TYPES.OBJECT,
              from: FROM_TYPE.EXPAND,
              value: [{
                id: uuidv4(),
                name: '',
                type: DATA_TYPES.STRING,
                from: FROM_TYPE.REFERENCE,
                value: '',
                referenceNode: '',
                referenceId: '',
                referenceKey: '',
              }],
            },
            {
              id: `description_${uuidv4()}`,
              name: 'description',
              type: DATA_TYPES.STRING,
              from: FROM_TYPE.INPUT,
              value: '',
            }, {
              id: `imageCount_${uuidv4()}`,
              name: 'imageCount',
              type: DATA_TYPES.INTEGER,
              from: FROM_TYPE.INPUT,
              value: 2,
            }],
        },
      ],
      outputParams: [
        {
          id: `output_${uuidv4()}`,
          name: 'output',
          type: DATA_TYPES.ARRAY,
          from: FROM_TYPE.EXPAND,
          value: [],
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
    return (<><TextToImageWrapper shapeStatus={shapeStatus} data={data}/></>);
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