/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {TextExtractionWrapper} from '@/components/textExtraction/TextExtractionWrapper.jsx';
import {
  AddSubItemReducer,
  ChangeAccessInfoConfigReducer,
  ChangeDescValueReducer,
  DeleteRowReducer,
  EditInputReducer,
  EditOutputFieldPropertyReducer,
  EditOutputTypeReducer,
  SelectToolReducer,
} from '@/components/textExtraction/reducers.js';
import {
  ChangeConfigReducer,
  ChangeFlowMetaReducer,
  ChangeHistoryTypeReducer,
  ChangeMemorySwitchReducer,
  ChangeWindowTypeReducer,
  ChangeWindowValueReducer,
} from '@/components/common/reducers/commonReducers.js';

/**
 * 问题改写节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const textExtractionComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, EditInputReducer());
  addReducer(builtInReducers, EditOutputFieldPropertyReducer());
  addReducer(builtInReducers, EditOutputTypeReducer());
  addReducer(builtInReducers, AddSubItemReducer());
  addReducer(builtInReducers, DeleteRowReducer());
  addReducer(builtInReducers, ChangeConfigReducer('extractParam'));
  addReducer(builtInReducers, ChangeAccessInfoConfigReducer());
  addReducer(builtInReducers, ChangeDescValueReducer());
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, ChangeMemorySwitchReducer());
  addReducer(builtInReducers, ChangeWindowValueReducer());
  addReducer(builtInReducers, ChangeWindowTypeReducer());
  addReducer(builtInReducers, ChangeHistoryTypeReducer());
  addReducer(builtInReducers, SelectToolReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [{
        id: `extractParam_${uuidv4()}`, name: 'extractParam', type: 'Object', from: 'Expand', value: [{
          id: `text_${uuidv4()}`,
          name: 'text',
          type: 'String',
          from: 'Reference',
          referenceNode: '',
          referenceId: '',
          referenceKey: '',
          value: [],
        }, {
          id: `desc_${uuidv4()}`, name: 'desc', type: 'String', from: 'Input', value: '',
        }, {
          id: `outputSchema_${uuidv4()}`,
          name: 'outputSchema',
          type: 'String',
          from: 'Input',
          value: '{"type":"object","properties":{"key0":{"type":"string","description":"this is key0 description"},"key1":{"type":"array","description":"this is key1 description","items":{"type":"string"}}}}',
        }, {
          id: uuidv4(),
          name: 'accessInfo',
          type: 'Object',
          from: 'Expand',
          value: [{id: uuidv4(), name: 'serviceName', type: 'String', from: 'Input', value: ''}, {
            id: uuidv4(), name: 'tag', type: 'String', from: 'Input', value: '',
          }],
        }, {
          id: `temperature_${uuidv4()}`, name: 'temperature', type: 'Number', from: 'Input', value: '0.3',
        }],
      }, {
        id: `memoryConfig_${uuidv4()}`, name: 'memoryConfig', type: 'Object', from: 'Expand', value: [{
          id: `windowAlg_${uuidv4()}`, name: 'windowAlg', type: 'String', from: 'Input', value: 'buffer_window',
        }, {
          id: `serializeAlg_${uuidv4()}`, name: 'serializeAlg', type: 'String', from: 'Input', value: 'full',
        }, {
          id: `property_${uuidv4()}`, name: 'property', type: 'Integer', from: 'Input', value: '0',
        }],
      }, {
        id: `memorySwitch_${uuidv4()}`, name: 'memorySwitch', type: 'Boolean', from: 'Input', value: false,
      }, {
        id: `histories_${uuidv4()}`,
        name: 'histories',
        type: 'Array',
        from: 'Reference',
        referenceNode: '_systemEnv',
        referenceId: 'memories',
        referenceKey: 'memories',
        value: ['memories'],
      }],
      outputParams: [{
        id: uuidv4(),
        name: 'output',
        type: 'Object',
        from: 'Expand',
        value: [
          {
            id: uuidv4(),
            name: 'extractedParams',
            type: 'Object',
            from: 'Expand',
            value: [{
              id: uuidv4(),
              name: '',
              type: 'String',
              from: 'Input',
              description: '',
              value: '',
            }],
          }, {
            id: `success_${uuidv4()}`,
            name: 'success',
            type: 'Boolean',
            from: 'Input',
            value: 'Boolean',
          },
        ],
      }],
    };
  };

  /**
   * 必须.
   *
   * @param shapeStatus 图形状态集合.
   * @param data 数据.
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<><TextExtractionWrapper shapeStatus={shapeStatus} data={data}/></>);
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