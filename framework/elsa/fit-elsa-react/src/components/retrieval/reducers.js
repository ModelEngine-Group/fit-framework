/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {v4 as uuidv4} from 'uuid';

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
    const newConfig = {...config};
    const query = newConfig.inputParams.find(item => item.name === 'query');
    const newQuery = {...query};
    newConfig.inputParams = [...newConfig.inputParams.filter(item => item.name !== 'query'), newQuery];
    action.changes.map(change => {
      newQuery[change.key] = change.value;
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
export const UpdateKnowledgeReducer = (component) => {
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
    const knowledgeValue = [...component.getKnowledgeRepos(newConfig).value];
    // 将 knowledgeValue 转换成更易操作的格式
    const knowledgeMap = knowledgeValue.reduce((map, item) => {
      if (item.value && item.value.length > 0) {
        const repoIdObj = item.value.find(v => v.name === 'repoId');
        const tableIdObj = item.value.find(v => v.name === 'tableId');

        if (repoIdObj && tableIdObj) {
          const repoId = repoIdObj.value;
          const tableId = tableIdObj.value;
          map[`${repoId}-${tableId}`] = item;
        }
      }
      return map;
    }, {});

    const actionValue = action.value;
    // 处理 actionValue 中的每个项
    actionValue.forEach(actionItem => {
      const key = `${actionItem.repoId}-${actionItem.tableId}`;
      if (knowledgeMap[key]) {
        // 更新现有条目
        knowledgeMap[key].value.forEach(v => {
          if (actionItem[v.name] !== undefined) {
            v.value = actionItem[v.name];
          }
        });
      } else {
        // 添加新条目
        knowledgeValue.push({
          id: uuidv4(),
          type: DATA_TYPES.OBJECT,
          from: FROM_TYPE.EXPAND,
          value: Object.keys(actionItem).map(k => ({
            id: uuidv4(),
            from: FROM_TYPE.INPUT,
            name: k,
            type: _getTypeOfValue(actionItem[k]),
            value: actionItem[k],
          })),
        });
      }
    });

    // 删除多余的条目
    Object.keys(knowledgeMap).forEach(k => {
      const [repoId, tableId] = k.split('-').map(Number);
      if (!actionValue.find(item => item.repoId === repoId && item.tableId === tableId)) {
        knowledgeValue.splice(knowledgeValue.indexOf(knowledgeMap[k]), 1);
      }
    });

    component.getKnowledgeRepos(newConfig).value = knowledgeValue;
    return newConfig;
  };

  const _getTypeOfValue = (value) => {
    if (typeof value === 'number') {
      return 'Integer';
    } else if (typeof value === 'boolean') {
      return 'Boolean';
    } else {
      return 'String';
    }
  };

  return self;
};

/**
 * deleteKnowledge 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteKnowledgeReducer = (component) => {
  const self = {};
  self.type = 'deleteKnowledge';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};

    const knowledgeValue = [...component.getKnowledgeRepos(newConfig).value];
    const indexToDelete = knowledgeValue.findIndex(item => item.id === action.id);
    if (indexToDelete !== -1) {
      knowledgeValue.splice(indexToDelete, 1);
    }
    component.getKnowledgeRepos(newConfig).value = knowledgeValue;

    return newConfig;
  };

  return self;
};

/**
 * changeMaximum 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeMaximumReducer = () => {
  const self = {};
  self.type = 'changeMaximum';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    newConfig.inputParams.filter(newTask => newTask.name === 'maximum').forEach(item => {
      item.value = action.value;
    });
    return newConfig;
  };

  return self;
};