/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {v4 as uuidv4} from 'uuid';

/**
 * 更新jadeConfig。使用了InvokeInput的组件，在修改输入时需要使用此方法更新jadeConfig
 *
 * @param data jadeConfig
 * @param id 组件对应的id
 * @param changes 变化的数据
 * @return {*} 变化后的新对象
 * @private
 */
export const updateInput = (data, id, changes) => data.map(d => {
  const newD = {...d};
  if (d.id === id) {
    changes.forEach(change => {
      newD[change.key] = change.value;
      // 当对象变为引用或输入时，需要把对象的value置空
      if (change.value === 'Reference' || change.value === 'Input') {
        newD.value = [];
      }
    });
    return newD;
  }
  // 当处理的数据是对象，并且对象的from是Expand，则递归处理当前数据的属性
  if (newD.from === 'Expand') {
    newD.value = updateInput(newD.value, id, changes);
  }
  return newD;
});

/**
 * 更新jadeConfig工具方法: dispatch({actionType: 'changeConfig', id: model.id, value: e})
 *
 * @param data jadeConfig
 * @param action dispatch传递来的对象
 * @return {{}} 新的jadeConfig
 */
export const changeConfig = (data, action) => {
  const {inputParams, ...rest} = data;
  if (!inputParams) {
    return data;
  }
  const updatedParams = inputParams.map(item =>
    item.id === action.id ? {...item, value: action.value} : item,
  );
  return {...rest, inputParams: updatedParams};
};

/**
 * 添加输入
 *
 * @param config jadeConfig对象
 * @param action 需要更新的对象信息
 */
export const addInput = (config, action) => {
  config.inputParams.find(item => item.name === 'args').value = [...config.inputParams.find(item => item.name === 'args').value, {
    id: action.id,
    name: '',
    type: 'String',
    from: 'Reference',
    value: '',
    referenceNode: '',
    referenceId: '',
    referenceKey: '',
  }];
};

/**
 * 编辑输入属性
 *
 * @param config jadeConfig对象
 * @param action 需要更新的对象信息
 */
export const editInput = (config, action) => {
  const newArgsValue = [...config.inputParams.find(item => item.name === 'args').value];
  config.inputParams.find(item => item.name === 'args').value = newArgsValue.map(item => {
    if (item.id === action.id) {
      let newItem = {...item};
      action.changes.forEach(change => {
        newItem[change.key] = change.value;
      });
      return newItem;
    } else {
      return item;
    }
  });
};

/**
 * 删除输入
 *
 * @param config jadeConfig对象
 * @param action 需要更新的对象信息
 */
export const deleteInput = (config, action) => {
  const newArgsValue = [...config.inputParams.find(item => item.name === 'args').value];
  config.inputParams.find(item => item.name === 'args').value = newArgsValue.filter(item => item.id !== action.id);
};

/**
 * 获取配置值，针对形如jadeConfig格式的数据，提供通过keys获取target的方法.
 *
 * @param jadeConfig 数据.
 * @param keys 键值列表.
 * @param targetKey 目标key
 * @param defaultValue 默认值.
 * @return {*|null} 配置的值.
 */
export const getConfigValue = (jadeConfig, keys, targetKey = 'value', defaultValue = null) => {
  if (!jadeConfig || !jadeConfig.value) {
    return defaultValue;
  }
  const key = keys.shift();
  const config = jadeConfig.value.find(j => j.name === key);
  if (keys.length === 0) {
    if (targetKey !== '') {
      const result = config && config[targetKey];
      return result === null || result === undefined ? defaultValue : config[targetKey];
    } else {
      return (config === null || config === undefined) ? defaultValue : config;
    }
  }
  return getConfigValue(config, keys, targetKey);
};

/**
 * 通过obj修改jadeConfig配置数据.
 *
 * @param jadeConfig jadeConfig配置数据.
 * @param obj 对象.
 */
export const updateConfigValueByObject = (jadeConfig, obj) => {
  if (!jadeConfig || !obj) {
    return;
  }
  Object.keys(obj).forEach(k => {
    const v = obj[k];
    if (v === null || v === undefined) {
      return;
    }
    if (typeof v === 'object') {
      updateConfigValueByObject(jadeConfig.value.find(jcv => jcv.name === k), v);
    } else {
      const config = jadeConfig.value.find(jcv => jcv.name === k);
      if (config) {
        config.value = v;
      }
    }
  });
};

/**
 * 代码节点格式化转换
 *
 * @param data 数据，一般为output
 * @returns {{type: string, properties: {}}}
 */
export const toCodeOutputJsonSchema = data => {
  const schema = {};

  if (Array.isArray(data)) {
    data.forEach((item) => {
      schema[item.name] = _convertToJsonSchema(item);
    });
  }

  return schema;
};

/**
 * 将配置数据转换为结构体.
 *
 * @param config 配置数据.
 * @return {{}|*} 结构体.
 */
export const configToStruct = (config) => {
  if (config.type === DATA_TYPES.ARRAY) {
    return config.value.map(v => configToStruct(v));
  } else if (config.type === DATA_TYPES.OBJECT) {
    const obj = {};
    config.value.forEach(item => {
      obj[item.name] = configToStruct(item);
    });
    return obj;
  } else {
    return Object.prototype.hasOwnProperty.call(config, 'value') ? config.value : config;
  }
};

/**
 * 将结构体转换成配置.
 *
 * @param struct 结构体.
 * @returns {*[]} 配置.
 */
export const structToConfig = (struct) => {
  const configs = [];
  Object.keys(struct).forEach(k => {
    if (!Object.prototype.hasOwnProperty.call(struct, k)) {
      return;
    }
    const data = toConfig(k, struct[k]);
    if (data !== null) {
      configs.push(data);
    }
  });
  return configs;
};

export const toConfig = (key, value) => {
  if (value === null || value === undefined) {
    return null;
  }
  if (Array.isArray(value)) {
    return {
      id: uuidv4(),
      name: key,
      type: DATA_TYPES.ARRAY,
      from: FROM_TYPE.EXPAND,
      value: value.map(v => toConfig(null, v))?.filter(item => item !== null),
    };
  } else if (typeof value === 'object') {
    return {
      id: uuidv4(),
      name: key,
      type: DATA_TYPES.OBJECT,
      from: FROM_TYPE.EXPAND,
      value: structToConfig(value),
    };
  } else {
    return {
      id: uuidv4(),
      name: key,
      type: toConfigType(value),
      from: FROM_TYPE.INPUT,
      value: value,
    };
  }
};

/**
 * 转换成对应的config类型.
 *
 * @param v
 * @returns {string|string}
 */
export const toConfigType = (v) => {
  if (typeof v === 'boolean') {
    return DATA_TYPES.BOOLEAN;
  }

  if (typeof v === 'object') {
    return DATA_TYPES.OBJECT;
  }

  if (typeof v === 'number') {
    return Number.isInteger(v) ? DATA_TYPES.INTEGER : DATA_TYPES.NUMBER;
  }

  if (Array.isArray(v)) {
    return DATA_TYPES.ARRAY;
  }

  return DATA_TYPES.STRING;
};

/**
 * 将jadeConfig转换成TreeData
 *
 * @param data output数据
 * @param level 层级
 * @param parent 父id
 * @param action 数据转换中需要自定义的处理方法
 * @return {{}}
 */
export const convertToTreeData = (data, level, parent, action) => {
  if (!data) {
    return {};
  }
  if (action) {
    action(data, parent);
  }
  const {id, name, type, description, value} = data;
  const children = Array.isArray(value) ? value.map(item => convertToTreeData(item, level + 1, data, action)) : [];
  return {
    key: id,
    title: name,
    type: type,
    children: children,
    level: level,
    description: description,
    expanded: true,
  };
};

const _buildOutputParams = (newConfig, newOutput) => newConfig.outputParams.map(outputParam => {
  if (outputParam.name === 'output') {
    return newOutput;
  } else {
    return outputParam;
  }
});

/**
 * 添加一个子项
 *
 * @param newConfig 新的配置对象
 * @param action 需要修改的属性值
 */
export const addSubItem = (newConfig, action) => {
  const outputParams = newConfig.outputParams;
  recursionAdd(outputParams, action.id);
  const newOutput = {...outputParams.find(item => item.name === 'output')};
  newConfig.outputParams = _buildOutputParams(newConfig, newOutput);
};

/**
 * 删除一个属性(output不支持删除)
 *
 * @param newConfig 新的配置对象
 * @param action 需要修改的属性值
 */
export const deleteProperty = (newConfig, action) => {
  const newOutput = {...newConfig.outputParams.find(item => item.name === 'output')};
  newOutput.value = removeItemById(newOutput.value, action.id);
  newConfig.outputParams = _buildOutputParams(newConfig, newOutput);
};

/**
 * 编辑属性名
 *
 * @param newConfig 新的配置对象
 * @param action 需要修改的属性值
 */
export const editOutputFieldProperty = (newConfig, action) => {
  const newOutput = {...newConfig.outputParams.find(item => item.name === 'output')};
  recursionEdit(newOutput.value, action);
  newConfig.outputParams = _buildOutputParams(newConfig, newOutput);
};

/**
 * 修改属性类型
 *
 * @param newConfig 新的配置对象
 * @param action 需要修改的属性值
 */
export const editOutputType = (newConfig, action) => {
  const outputParams = newConfig.outputParams;
  recursionEdit(outputParams, action);
  const newOutput = {...outputParams.find(item => item.name === 'output')};
  newConfig.outputParams = _buildOutputParams(newConfig, newOutput);
};

/**
 * 递归添加子项
 *
 * @param items key的配置信息
 * @param id 需要添加子项的父项id
 */
export const recursionAdd = (items, id) => {
  items.forEach(item => {
    if (item.id === id) {
      const newItem = {
        id: uuidv4(),
        name: '',
        type: 'String',
        from: 'Input',
        description: '',
        value: '',
      };
      item.value.push(newItem);
      return;
    }
    if (item.type === 'Object') {
      recursionAdd(item.value, id);
    }
  });
};

/**
 * 更新inputParam中的updateKey
 *
 * @param inputParam 输入
 * @param updateKey 需要更新的key
 * @param updateItemValue 具体更新动作
 * @returns {(*&{value: *})|*}
 */
export const updateInputParam = (inputParam, updateKey, updateItemValue) => {
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
 * 把jadeConfig格式的数据转为jsonSchema格式
 *
 * @param data 数据，一般为output
 * @returns {{type: string, properties: {}}}
 */
export const toJsonSchema = data => {
  const schema = {
    type: 'object',
    properties: {},
  };

  if (Array.isArray(data.value)) {
    data.value.forEach((item) => {
      schema.properties[item.name] = _convertToJsonSchema(item);
    });
  }

  return schema;
};

/**
 * 查找目标数据id以及对应子数据的id
 *
 * @param data 数据
 * @param targetId 需要查询的数据
 * @return {*[]} id数组
 */
export const findChildIds = (data, targetId) => {
  let resultIds = [];

  const collectChildIds = (item) => {
    resultIds.push(item.id);
    if (item.type === 'Object' && Array.isArray(item.value)) {
      item.value.forEach(collectChildIds);
    }
  };

  const recursiveFind = (arr, id) => {
    for (let item of arr) {
      if (item.id === id) {
        collectChildIds(item);
      } else if (item.type === 'Object' && Array.isArray(item.value)) {
        recursiveFind(item.value, id);
      }
    }
  };

  recursiveFind(data, targetId);
  return resultIds;
};

/**
 * 递归修改属性
 *
 * @param items 数据
 * @param action 需要修改的属性
 * @private
 */
export const recursionEdit = (items, action) => {
  items.forEach(item => {
    if (item.id === action.id) {
      action.changes.forEach(change => {
        item[change.key] = change.value;
      });
      return;
    }
    if (item.type === 'Object') {
      recursionEdit(item.value, action);
    }
  });
};

/**
 * 重新构造一个不包含需要删除id的数组
 *
 * @param arr 原始数组
 * @param idToRemove 需要删除的id对应的数据
 * @return {*}
 */
export const removeItemById = (arr, idToRemove) => arr.reduce((acc, item) => {
  if (item.id === idToRemove) {
    return acc;
  }

  if (item.type === 'Object' && Array.isArray(item.value)) {
    item.value = removeItemById(item.value, idToRemove);
  }

  acc.push(item);
  return acc;
}, []);

const _convertToJsonSchema = item => {
  const mappedSchema = {
    type: item.type.toLowerCase(),
    description: item.description || '',
  };

  if (item.type === 'Object' && Array.isArray(item.value)) {
    mappedSchema.type = 'object';
    mappedSchema.properties = {};
    item.value.forEach((subItem) => {
      mappedSchema.properties[subItem.name] = _convertToJsonSchema(subItem);
    });
  } else if (item.type === 'Array') {
    mappedSchema.type = 'array';
  }

  return mappedSchema;
};

/**
 * 将jadeConfig结构转换为条件节点的branches格式
 *
 * @returns {Array} 转换后的目标格式数组
 * @param jadeConfig jadeConfig数据
 * @param id 节点id
 */
export const convertToBranchesFormat = (jadeConfig, id) => {
  const source = jadeConfig.inputParams.find(item => item.name === 'classifyQuestionParam')
    .value.find(param => param.name === 'questionTypeList');
  const outputParams = jadeConfig.outputParams;
  if (!source || !source.value || !Array.isArray(source.value)) {
    throw new Error('Invalid source structure');
  }

  return source.value.map(branch => {
    const {conditionType, value} = branch;
    const defaultLeftValue = {
      id: uuidv4(),
      name: 'left',
      type: 'String',
      from: 'Reference',
      value: ['output'],
      referenceNode: `${id}`,
      referenceId: `${outputParams[0].id}`,
      referenceKey: 'output',
    };

    // 获取右侧值 (第二个值)
    const rightValue = {
      id: uuidv4(),
      name: 'right',
      type: 'String',
      from: 'Input',
      value: value.find(item => item.name === 'id')?.value || '',
    };

    // 创建分支的 conditions
    const conditions = [{
      id: uuidv4(),
      condition: conditionType === 'if' ? 'equal' : 'true',
      value: conditionType === 'if' ? [defaultLeftValue, rightValue] : [],
    }];

    return {
      id: uuidv4(), conditionRelation: 'and', // 默认值
      type: conditionType, runnable: true, conditions: conditions,
    };
  });
};