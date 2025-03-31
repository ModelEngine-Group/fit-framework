/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 更新inputParam中的rewriteParam
 *
 * @param inputParam 输入
 * @param updateKey 需要更新的key
 * @param updateItemValue 具体更新动作
 * @returns {(*&{value: *})|*}
 */
const updateInputParam = (inputParam, updateKey, updateItemValue) => {
  if (inputParam.name === updateKey) {
    return {
      ...inputParam,
      value: inputParam.value.map(item => updateItemValue(item)),
    };
  } else {
    return inputParam;
  }
};

/**
 * changeTemplateType 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeTemplateTypeReducer = () => {
  const self = {};
  self.type = 'changeTemplateType';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItemValue = (item) => {
      if (item.name === 'template') {
        return {...item, value: ''};
      }
      if (item.name === 'strategy') {
        return {...item, value: action.value};
      } else {
        return item;
      }
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'rewriteParam', _updateItemValue)),
    };
  };

  return self;
};

/**
 * changeTemplateValue 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeTemplateValueReducer = () => {
  const self = {};
  self.type = 'changeTemplateValue';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItemValue = (item) => {
      return item.name === 'template' ? {...item, value: action.value} : item;
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'rewriteParam', _updateItemValue)),
    };
  };

  return self;
};