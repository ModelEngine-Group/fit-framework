/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInputParam} from '@/components/util/JadeConfigUtils.js';

/**
 * changeImageCount 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeImageCountReducer = () => {
  const self = {};
  self.type = 'changeImageCount';

  const _updatePrompt = (item, action) => {
    if (item.name === 'imageCount') {
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
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'imageParam', (item) => {
        return _updatePrompt(item, action);
      })),
    };
  };

  return self;
};