/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInputParam} from '@/components/util/JadeConfigUtils.js';

/**
 * textChange 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeNoteTextReducer = () => {
  const self = {};
  self.type = 'textChange';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    newConfig.inputParams[0] = {...config.inputParams[0], value: action.value};
    return newConfig;
  };

  return self;
};

/**
 * bgColorChange 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeStyleReducer = () => {
  const self = {};
  self.type = 'styleChange';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItemValue = (item) => {
      return item.name === action.item ? {...item, value: action.value} : item;
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'style', _updateItemValue)),
    };
  };

  return self;
};