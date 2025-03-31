/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {defaultComponent} from '@/components/defaultComponent.js';
import {NoteWrapper} from '@/components/note/NoteWrapper.jsx';
import {ChangeNoteTextReducer, ChangeStyleReducer} from '@/components/note/reducers.js';

/**
 * 注释节点组件
 *
 * @param jadeConfig 组件配置信息
 * @return {{}} 组件
 */
export const noteComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);
  const addReducer = (map, reducer) => map.set(reducer.type, reducer);
  const builtInReducers = new Map();
  addReducer(builtInReducers, ChangeNoteTextReducer());
  addReducer(builtInReducers, ChangeStyleReducer());

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [{
        id: `text_${uuidv4()}`,
        name: 'text',
        type: 'String',
        from: 'Input',
        value: '',
      }, {
        id: `style_${uuidv4()}`,
        name: 'style',
        type: 'Object',
        from: 'Expand',
        value: [{
          id: `backgroundColor_${uuidv4()}`, // 用于控制图形backColor、focusBackColor和文本区域的background
          name: 'backgroundColor',
          type: 'String',
          from: 'Input',
          value: 'rgb(234, 243, 255)',
        }, {
          id: `outlineColor_${uuidv4()}`, // 用于控制图形outlineColor
          name: 'outlineColor',
          type: 'String',
          from: 'Input',
          value: '',
        }, {
          id: `borderColor_${uuidv4()}`, // 用于控制图形的borderColor
          name: 'borderColor',
          type: 'String',
          from: 'Input',
          value: '',
        }, {
          id: `fontSize_${uuidv4()}`, // 用于控制图形的borderColor
          name: 'fontSize',
          type: 'String',
          from: 'Input',
          value: '12',
        }, {
          id: `fontColor_${uuidv4()}`, // 用于控制图形的borderColor
          name: 'fontColor',
          type: 'String',
          from: 'Input',
          value: 'rgb(26, 26, 26)',
        }, {
          id: `align_${uuidv4()}`, // 用于控制图形的borderColor
          name: 'align',
          type: 'String',
          from: 'Input',
          value: 'JustifyLeft',
        }, {
          id: `listStyle_${uuidv4()}`, // 用于控制图形的borderColor
          name: 'listStyle',
          type: 'String',
          from: 'Input',
          value: 'InsertUnorderedList',
        }],
      }],
      outputParams: [],
    };
  };

  /**
   * 必须.
   *
   * @param shapeStatus 图形状态集合.
   * @param data 数据.
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<><NoteWrapper shapeStatus={shapeStatus} data={data}/></>);
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