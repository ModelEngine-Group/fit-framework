/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInputParam} from '@/components/util/JadeConfigUtils.js';
import {v4 as uuidv4} from 'uuid';

/**
 * changeQuestionClassificationDesc 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeQuestionClassificationDescReducer = (updateKey) => {
  const self = {};
  self.type = 'changeQuestionDesc';

  const _buildQuestionItem = (questionTypeItem, action) => ({
    ...questionTypeItem, value: questionTypeItem.value.map(questionTypeItemValue => {
      if (questionTypeItemValue.name === 'questionTypeDesc') {
        return {...questionTypeItemValue, value: action.value};
      } else {
        return questionTypeItemValue;
      }
    }),
  });

  const _buildQuestionTypeList = (item, action) => {
    return ({
      ...item,
      value: item.value.map(questionTypeItem => {
        if (questionTypeItem.id === action.id) {
          return _buildQuestionItem(questionTypeItem, action);
        } else {
          return questionTypeItem;
        }
      }),
    });
  };

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, (item) => {
        if (item.name === 'questionTypeList') {
          return _buildQuestionTypeList(item, action);
        } else {
          return item;
        }
      })),
    };
  };

  return self;
};

/**
 * addQuestionClassification 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddQuestionClassificationReducer = (updateKey) => {
  const self = {};
  self.type = 'addQuestion';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _updateItem = (item) => {
      if (item.name === 'questionTypeList') {
        const newQuestion = {
          id: uuidv4(),
          type: 'Object',
          from: 'Expand',
          conditionType: 'if',
          runnable: true,
          value: [{
            id: `questionTypeId_${uuidv4()}`,
            name: 'id',
            type: 'String',
            from: 'Input',
            value: uuidv4(),
          }, {
            id: `questionTypeDesc_${uuidv4()}`,
            name: 'questionTypeDesc',
            type: 'String',
            from: 'Input',
            value: '',
          }],
        };
        const newValue = [...item.value];
        newValue.splice(-1, 0, newQuestion);
        return {
          ...item,
          value: newValue,
        };
      } else {
        return item;
      }
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, _updateItem)),
    };
  };

  return self;
};

/**
 * deleteQuestionClassification 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteQuestionClassificationReducer = (updateKey) => {
  const self = {};
  self.type = 'deleteQuestion';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _updateItem = (item) => {
      if (item.name === 'questionTypeList') {
        return {
          ...item,
          value: item.value.filter(questionTypeItem => questionTypeItem.id !== action.value),
        };
      } else {
        return item;
      }
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, _updateItem)),
    };
  };

  return self;
};

/**
 * changeBranchesStatus 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeBranchesStatusReducer = (shape) => {
  const self = {};
  self.type = 'changeBranchesStatus';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const disabled = action.changes.find(change => change.key === 'disabled').value;
    const flowMeta = shape.getFlowMeta();
    flowMeta.conditionParams.branches.forEach(b => b.disabled = disabled);
    action.changes.forEach(c => {
      newConfig[c.key] = c.value;
    });
    return newConfig;
  };

  return self;
};