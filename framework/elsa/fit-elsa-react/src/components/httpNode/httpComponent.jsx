/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {HttpWrapper} from '@/components/httpNode/HttpWrapper.jsx';
import {
  AddBodyParamReducer,
  AddConfigReducer,
  BodyParamChangeReducer,
  BodyTypeChangeReducer,
  ChangeRequestConfigReducer,
  ChangeRequestUrlReducer,
  ConfigChangeReducer,
  ConfirmReducer,
  DataChangeReducer,
  DeleteBodyParamReducer,
  DeleteConfigReducer,
  TabChangeReducer,
} from '@/components/httpNode/reducers.js';
import {AddInputReducer, ChangeFlowMetaReducer, DeleteInputReducer, EditInputReducer} from '@/components/common/reducers/commonReducers.js';

/**
 * http节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const httpComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, AddInputReducer('httpRequest'));
  addReducer(builtInReducers, EditInputReducer('httpRequest'));
  addReducer(builtInReducers, DeleteInputReducer('httpRequest'));
  addReducer(builtInReducers, ChangeFlowMetaReducer());
  addReducer(builtInReducers, ChangeRequestUrlReducer());
  addReducer(builtInReducers, ChangeRequestConfigReducer());
  addReducer(builtInReducers, ConfirmReducer());
  addReducer(builtInReducers, ConfigChangeReducer());
  addReducer(builtInReducers, DeleteConfigReducer());
  addReducer(builtInReducers, ConfigChangeReducer());
  addReducer(builtInReducers, AddConfigReducer());
  addReducer(builtInReducers, BodyTypeChangeReducer());
  addReducer(builtInReducers, DataChangeReducer());
  addReducer(builtInReducers, BodyParamChangeReducer());
  addReducer(builtInReducers, AddBodyParamReducer());
  addReducer(builtInReducers, DeleteBodyParamReducer());
  addReducer(builtInReducers, TabChangeReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: uuidv4(),
          name: 'activeKey',
          type: 'String',
          from: 'Input',
          value: '2',
        }, {
          id: `allBodyData_${uuidv4()}`,
          name: 'allBodyData',
          type: 'Object',
          from: 'Expand',
          value: [{
            id: uuidv4(),
            name: 'activeBodyType',
            type: 'String',
            from: 'Input',
            value: 'none',
          }, {
            id: uuidv4(),
            name: 'json',
            type: 'String',
            from: 'Input',
            value: null,
          }, {
            id: uuidv4(),
            name: 'text',
            type: 'String',
            from: 'Input',
            value: null,
          }, {
            id: uuidv4(),
            name: 'x-www-form-urlencoded',
            type: 'Object',
            from: 'Expand',
            value: [],
          }],
        },
        {
          id: `httpRequest_${uuidv4()}`,
          name: 'httpRequest',
          type: 'Object',
          from: 'Expand',
          value: [{
            id: uuidv4(),
            name: 'args',
            type: 'Object',
            from: 'Expand',
            value: [
              {
                id: uuidv4(),
                name: 'input',
                type: 'String',
                from: 'Reference',
                value: '',
                referenceNode: '',
                referenceId: '',
                referenceKey: '',
              },
            ],
          }, {
            id: `httpMethod_${uuidv4()}`,
            name: 'httpMethod',
            type: 'String',
            from: 'Input',
            value: 'get',
          }, {
            id: `url_${uuidv4()}`,
            name: 'url',
            type: 'String',
            from: 'Input',
            value: '',
          }, {
            id: `timeout_${uuidv4()}`,
            name: 'timeout',
            type: 'Integer',
            from: 'Input',
            value: 1000,
          }, {
            id: `headers_${uuidv4()}`,
            name: 'headers',
            type: 'Object',
            from: 'Expand',
            value: [],
          }, {
            id: `params_${uuidv4()}`,
            name: 'params',
            type: 'Object',
            from: 'Expand',
            value: [],
          }, {
            id: `requestBody_${uuidv4()}`,
            name: 'requestBody',
            type: 'Object',
            from: 'Expand',
            value: [{
              id: `type_${uuidv4()}`,
              name: 'type',
              type: 'String',
              from: 'Input',
              value: null,
            }, {
              id: `data_${uuidv4()}`,
              name: 'data',
              type: 'String',
              from: 'Input',
              value: null,
            }],
          }, {
            id: `authentication_${uuidv4()}`,
            name: 'authentication',
            type: 'Object',
            from: 'Expand',
            value: [{
              id: `type_${uuidv4()}`,
              name: 'type',
              type: 'String',
              from: 'Input',
              value: 'none',
            }, {
              id: `header_${uuidv4()}`,
              name: 'header',
              type: 'String',
              from: 'Input',
              value: null,
            }, {
              id: `key_${uuidv4()}`,
              name: 'authKey',
              type: 'String',
              from: 'Input',
              value: null,
            }],
          }],
        },
      ],
      outputParams: [
        {
          id: `output_${uuidv4()}`,
          name: 'output',
          type: 'Object',
          from: 'Expand',
          value: [{
            id: `data_${uuidv4()}`,
            name: 'data',
            type: 'Object',
            from: 'Expand',
            value: [],
          }, {
            id: `status_${uuidv4()}`,
            name: 'status',
            type: 'Integer',
            from: 'Input',
            value: '',
          }, {
              id: `errorMsg_${uuidv4()}`,
              name: 'errorMsg',
              type: 'String',
              from: 'Input',
              value: '',
            }],
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
    return (<><HttpWrapper shapeStatus={shapeStatus} data={data}/></>);
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