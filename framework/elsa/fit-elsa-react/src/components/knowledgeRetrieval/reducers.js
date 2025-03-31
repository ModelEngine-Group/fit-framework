/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {structToConfig, toConfigType, updateConfigValueByObject, updateInput} from '@/components/util/JadeConfigUtils.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, DEFAULT_KNOWLEDGE_REPO_GROUP_STRUCT, FROM_TYPE} from '@/common/Consts.js';

/**
 * updateInputParams 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateInputParamReducer = () => {
  const self = {};
  self.type = 'updateInputParams';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    newConfig.inputParams = updateInput(newConfig.inputParams, action.id, action.changes);
    return newConfig;
  };

  return self;
};

/**
 * updateOption 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateOptionReducer = () => {
  const self = {};
  self.type = 'updateOption';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    newConfig.inputParams = newConfig.inputParams.map(ip => {
      if (ip.name === 'option') {
        const newOption = {...ip};
        if (newOption.value.length > 0) {
          updateConfigValueByObject(newOption, action.option);
        } else {
          newOption.value = structToConfig(action.option);
        }
        return newOption;
      } else {
        return ip;
      }
    });
    return newConfig;
  };

  return self;
};

/**
 * updateKnowledge 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateKnowledgeReducer = () => {
  const self = {};
  self.type = 'updateKnowledge';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    newConfig.inputParams = newConfig.inputParams.map(ip => {
      if (ip.name === 'knowledgeRepos') {
        const knowledgeRepos = {...ip};
        updateKnowledgeRepos(knowledgeRepos, action);
        return knowledgeRepos;
      } else {
        return ip;
      }
    });
    return newConfig;
  };

  const updateKnowledgeRepos = (knowledgeRepos, action) => {
    // 转换为jadeConfig格式.
    const newItems = action.value.map(v => {
      return {
        id: uuidv4(),
        type: DATA_TYPES.OBJECT,
        from: FROM_TYPE.EXPAND,
        value: Object.keys(v).map(k => ({
          id: uuidv4(),
          from: FROM_TYPE.INPUT,
          name: k,
          type: toConfigType(v[k]),
          value: v[k],
        })),
      };
    });

    knowledgeRepos.value = [...newItems];
  };

  return self;
};

/**
 * updateGroupId 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateGroupIdReducer = () => {
  const self = {};
  self.type = 'updateGroupId';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const option = newConfig.inputParams.find(ip => ip.name === 'option');
    let groupId = option.value.find(v => v.name === 'groupId');
    if (!groupId) {
      groupId = {...DEFAULT_KNOWLEDGE_REPO_GROUP_STRUCT};
      option.value.push(groupId);
    }

    if (groupId.value !== action.value) {
      clearKnowledgeRepos(newConfig);
    }

    groupId.value = action.value;
    return newConfig;
  };

  const clearKnowledgeRepos = (config) => {
    const knowledgeRepos = config.inputParams.find(ip => ip.name === 'knowledgeRepos');
    knowledgeRepos.value = [];
  };

  return self;
};