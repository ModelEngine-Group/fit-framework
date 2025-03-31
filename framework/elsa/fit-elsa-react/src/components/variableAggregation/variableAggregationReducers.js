/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DEFAULT_VARIABLE} from '@/components/variableAggregation/variableAggregationConstant.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES} from '@/common/Consts.js';

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
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config) => {
    const newConfig = {...config};
    newConfig.inputParams = [...newConfig.inputParams];
    newConfig.inputParams[0] = {...newConfig.inputParams[0]};
    newConfig.inputParams[0].value = [...newConfig.inputParams[0].value];
    const variables = newConfig.inputParams[0];
    if (variables.value.length > 0) {
      const newVariable = {...variables.value[0]};
      newVariable.id = uuidv4();
      newVariable.value = [];
      newVariable.referenceKey = null;
      newVariable.referenceNode = null;
      newVariable.referenceId = null;
      variables.value.push(newVariable);
    } else {
      variables.value.push(DEFAULT_VARIABLE);
    }
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
    const newConfig = {...config};
    const variables = newConfig.inputParams[0];
    const index = variables.value.findIndex(v => v.id === action.data.id);
    if (index !== -1) {
      const variable = variables.value[index];
      Object.keys(action.data.updates).forEach(k => {
        if (Object.prototype.hasOwnProperty.call(action.data.updates, k)) {
          variable[k] = action.data.updates[k];
        }
      });
      variables.value[index] = {...variable};
      updateOutputParams(newConfig, variable);
    }
    return newConfig;
  };

  const updateOutputParams = (newConfig, variable) => {
    if (variable.type && variable.type.toLowerCase() === newConfig.outputParams[0].type.toLowerCase()) {
      return;
    }
    newConfig.outputParams = [...newConfig.outputParams];
    newConfig.outputParams[0] = {...newConfig.outputParams[0]};
    newConfig.outputParams[0].type = variable.type ?? DATA_TYPES.STRING;

    // 对象和数组的value是数组.
    if (newConfig.outputParams[0].type === DATA_TYPES.ARRAY || newConfig.outputParams[0].type === DATA_TYPES.OBJECT) {
      newConfig.outputParams[0].value = [];
    }
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