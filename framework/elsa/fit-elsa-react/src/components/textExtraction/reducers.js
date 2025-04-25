/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {
  recursionAdd,
  recursionEdit,
  removeItemById,
  toJsonSchema,
  updateInputParam,
} from '@/components/util/JadeConfigUtils.js';
import {convertParameter} from '@/components/util/MethodMetaDataParser.js';

/**
 * EditInput 事件处理器.
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
    const extractParam = newConfig.inputParams.find(item => item.name === 'extractParam');
    const newExtractParam = {
      ...extractParam, value: extractParam.value.map(item => {
        if (item.name === 'text') {
          const newItem = {...item};
          action.changes.map(change => {
            newItem[change.key] = change.value;
          });
          return newItem;
        } else {
          return item;
        }
      }),
    };
    replaceExtractParam(newConfig, newExtractParam);

    return newConfig;
  };

  return self;
};

const replaceExtractParam = (newConfig, newExtractParam) => {
  newConfig.inputParams = newConfig.inputParams.map(inputParam => {
    if (inputParam.name === 'extractParam') {
      return newExtractParam;
    } else {
      return inputParam;
    }
  });
};

/**
 * 根据output生成jsonSchema
 *
 * @param newConfig 新的jadeConfig，基于原始的config
 */
const generateSchema = newConfig => {
  const extractParam = newConfig.inputParams.find(item => item.name === 'extractParam');
  const newExtractParam = {
    ...extractParam, value: extractParam.value.map(paramItem => {
      if (paramItem.name === 'outputSchema') {
        return {
          ...paramItem,
          value: JSON.stringify(toJsonSchema(newConfig.outputParams.find(item => item.name === 'output').value.find(v => v.name === 'extractedParams'))),
        };
      } else {
        return paramItem;
      }
    }),
  };
  replaceExtractParam(newConfig, newExtractParam);
};

const _buildOutputParams = (newConfig, newOutput) => newConfig.outputParams.map(outputParam => {
  if (outputParam.name === 'output') {
    return newOutput;
  } else {
    return outputParam;
  }
});

const processConfig = (newConfig, action, handleData) => {
  const newOutput = {...newConfig.outputParams.find(item => item.name === 'output')};
  const newData = {...newOutput.value.find(v => v.name === 'extractedParams')};

  // 调用传入的处理函数
  handleData(newData, action);

  newOutput.value = newOutput.value.map(item => {
    if (item.name === 'extractedParams') {
      return newData;
    } else {
      return item;
    }
  });

  newConfig.outputParams = _buildOutputParams(newConfig, newOutput);
  generateSchema(newConfig);
  return newConfig;
};


/**
 * EditOutputName 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const EditOutputFieldPropertyReducer = () => {
  const self = {};
  self.type = 'editOutputFieldProperty';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    return processConfig(newConfig, action, (newData, actionObj) => {
      recursionEdit(newData.value, actionObj);
    });
  };

  return self;
};


/**
 * EditOutputType 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const EditOutputTypeReducer = () => {
  const self = {};
  self.type = 'editOutputType';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    return processConfig(newConfig, action, (newData, actionObj) => {
      recursionEdit(newData.value, actionObj);
    });
  };

  return self;
};

/**
 * AddSubItem 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddSubItemReducer = () => {
  const self = {};
  self.type = 'addSubItem';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    return processConfig(newConfig, action, (newData, actionObj) => {
      recursionAdd(newConfig.outputParams, actionObj.id);
    });
  };

  return self;
};

/**
 * DeleteRow 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteRowReducer = () => {
  const self = {};
  self.type = 'deleteRow';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    return processConfig(newConfig, action, (newData, actionObj) => {
      newData.value = removeItemById(newData.value, actionObj.id);
    });
  };

  return self;
};

/**
 * changeConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeAccessInfoConfigReducer = () => {
  const self = {};
  self.type = 'changeAccessInfoConfig';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateAccessInfoValue = (accessInfoValue, serviceName, tag) => {
      if (accessInfoValue.name === 'serviceName') {
        return {...accessInfoValue, value: serviceName};
      } else if (accessInfoValue.name === 'tag') {
        return {...accessInfoValue, value: tag};
      }
      return accessInfoValue;
    };
    const newConfig = {...config};
    const extractParam = newConfig.inputParams.find(item => item.name === 'extractParam');
    const newExtractParam = {
      ...extractParam, value: newConfig.inputParams.find(item => item.name === 'extractParam').value.map(item => {
        if (item.name === 'accessInfo') {
          const [serviceName, tag] = action.value.split('&&');
          return {
            ...item,
            value: item.value.map(accessInfoValue => _updateAccessInfoValue(accessInfoValue, serviceName, tag)),
          };
        } else {
          return item;
        }
      }),
    };
    replaceExtractParam(newConfig, newExtractParam);

    return newConfig;
  };

  return self;
};

/**
 * selectTool 事件处理器
 *
 * @returns {{}} 处理器对象
 * @constructor
 */
export const SelectToolReducer = () => {
  const self = {};
  self.type = 'changeTool';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    const _updateItemValue = (item) => {
      return item.name === 'outputSchema' ? {...item, value: JSON.stringify(action.value.parameters)} : item;
    };

    const _generateInput = () => {
      // action.value是schema
      const parameters = action.value.parameters;
      const orderProperties = parameters.order ? parameters.order : Object.keys(parameters.properties);
      return orderProperties.map(key => {
        return convertParameter({
          propertyName: key,
          property: parameters.properties[key],
          isRequired: Array.isArray(parameters.required) ? parameters.required.includes(key) : false,
        });
      });
    };

    newConfig.inputParams = newConfig.inputParams.map(inputParam => updateInputParam(inputParam, 'extractParam', _updateItemValue));
    const newOutput = {...newConfig.outputParams.find(item => item.name === 'output')};
    const newData = {...newOutput.value.find(v => v.name === 'extractedParams'), value: _generateInput()};
    newOutput.value = newOutput.value.map(item => {
      if (item.name === 'extractedParams') {
        return newData;
      } else {
        return item;
      }
    });

    newConfig.outputParams = _buildOutputParams(newConfig, newOutput);
    return newConfig;
  };

  return self;
};

/**
 * changeTemplateValue 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeDescValueReducer = () => {
  const self = {};
  self.type = 'changePromptValue';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const _updateItemValue = (item) => {
      return item.name === 'desc' ? {...item, value: action.value} : item;
    };

    return {
      ...config,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'extractParam', _updateItemValue)),
    };
  };

  return self;
};