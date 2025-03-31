/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInputParam} from '@/components/util/JadeConfigUtils.js';

/**
 * ChangePrompt 事件处理器.
 *
 * @param updateKey 需要更新的key
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangePromptReducer = (updateKey) => {
  const self = {};
  self.type = 'changePrompt';

  const _updatePrompt = (item, action) => {
    if (item.id === action.id) {
      return {...item, value: action.value};
    } else {
      return item;
    }
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, (item) => {
        return _updatePrompt(item, action);
      })),
    };
  };

  return self;
};

/**
 * editInput 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const EditInputReducer = () => {
  const self = {};
  self.type = 'editInput';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const updateItem = (item) => {
      if (item.id === action.id) {
        let newValue = {...item};
        action.changes.forEach(change => {
          newValue[change.key] = change.value;
        });
        return newValue;
      } else {
        return item;
      }
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'fileExtractionParam', updateItem)),
    };
  };

  return self;
};