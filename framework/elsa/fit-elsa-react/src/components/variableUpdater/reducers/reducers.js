/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {createDefaultVariable} from '@/components/variableUpdater/Constant.js';

/**
 * 添加变量 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddVariableReducer = () => {
  const self = {};
  self.type = 'addVariable';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const variables = newConfig.inputParams[0];
    const newVariable = createDefaultVariable();
    newVariable.id = action.id;
    variables.value.push(newVariable);
    return newConfig;
  };

  return self;
};

/**
 * 修改变量 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateVariableReducer = () => {
  const self = {};
  self.type = 'updateVariable';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = { ...config };
    const variables = newConfig.inputParams[0];
    const index = variables.value.findIndex(v => v.id === action.data.id);

    if (index !== -1) {
      const variable = variables.value[index];
      const updates = action.data.updates;
      const specifyItem = variable.value.find(item => item.name === action.data.key);

      // 遍历 updates 数组，更新 specifyItem 的对应字段
      updates.forEach(({ key, value }) => {
        if (key && Object.prototype.hasOwnProperty.call(specifyItem, key)) {
          specifyItem[key] = value;
        }
      });

      // 使用新对象避免引用问题
      variables.value[index] = { ...variable };
    }

    return newConfig;
  };

  return self;
};

/**
 * 删除变量 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteVariableReducer = () => {
  const self = {};
  self.type = 'deleteVariable';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const variables = newConfig.inputParams[0];
    variables.value = variables.value.filter(v => v.id !== action.data.id);
    return newConfig;
  };

  return self;
};