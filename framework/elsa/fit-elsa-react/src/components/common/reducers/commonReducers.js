/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInputParam} from '@/components/util/JadeConfigUtils.js';

/**
 * changeWindowValue 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeWindowValueReducer = () => {
  const self = {};
  self.type = 'changeWindowValue';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _updateItemValue = (item) => {
      return item.name === 'property' ? {...item, value: action.value} : item;
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'memoryConfig', _updateItemValue)),
    };
  };

  return self;
};

/**
 * changeWindowType 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeWindowTypeReducer = () => {
  const self = {};
  self.type = 'changeWindowType';

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
      if (item.name === 'windowAlg') {
        return {...item, value: action.value};
      }
      if (item.name === 'property') {
        return {...item, value: 0};
      }
      return item;
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'memoryConfig', _updateItem)),
    };
  };

  return self;
};

/**
 * changeHistoryType 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeHistoryTypeReducer = () => {
  const self = {};
  self.type = 'changeHistoryType';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _updateItemValue = (item) => {
      return item.name === 'serializeAlg' ? {...item, value: action.value} : item;
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, 'memoryConfig', _updateItemValue)),
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
export const ChangePromptValueReducer = (updateKey) => {
  const self = {};
  self.type = 'changePromptValue';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _updateItemValue = (item) => {
      return item.name === 'template' ? {...item, value: action.value} : item;
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, _updateItemValue)),
    };
  };

  return self;
};

/**
 * changeMemorySwitch 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeMemorySwitchReducer = () => {
  const self = {};
  self.type = 'changeMemorySwitch';

  const _updateMemoryValue = inputParam => ({
    ...inputParam, value: inputParam.value.map(memoryConfigValue => {
      if (memoryConfigValue.name === 'property') {
        return {...memoryConfigValue, value: 0};
      } else {
        return memoryConfigValue;
      }
    }),
  });

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const newConfig = {...config};
    newConfig.jadeNodeConfigChangeIgnored = jadeNodeConfigChangeIgnored;
    newConfig.inputParams = newConfig.inputParams.map(inputParam => {
      if (inputParam.name === 'memorySwitch') {
        return {...inputParam, value: action.value};
      } else if (inputParam.name === 'memoryConfig') {
        return _updateMemoryValue(inputParam);
      } else {
        return inputParam;
      }
    });

    return newConfig;
  };

  return self;
};

/**
 * changeConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeConfigReducer = (updateKey) => {
  const self = {};
  self.type = 'changeConfig';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _updateItem = (item) =>
      item.id === action.id ? {...item, value: action.value} : item;

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, _updateItem)),
    };
  };

  return self;
};

/**
 * changeConfig 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeAccessInfoConfigReducer = (updateKey) => {
  const self = {};
  self.type = 'changeAccessInfoConfig';

  const _updateAccessInfoValue = (accessInfoValue, serviceName, tag) => {
    if (accessInfoValue.name === 'serviceName') {
      return {...accessInfoValue, value: serviceName};
    } else if (accessInfoValue.name === 'tag') {
      return {...accessInfoValue, value: tag};
    }
    return accessInfoValue;
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
        const [serviceName, tag] = action.value.split('&&');
        return item.name === 'accessInfo' ? {
          ...item,
          value: item.value.map(accessInfoValue => _updateAccessInfoValue(accessInfoValue, serviceName, tag)),
        } : item;
      })),
    };
  };

  return self;
};

/**
 * addInput 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const AddInputReducer = (updateKey) => {
  const self = {};
  self.type = 'addInput';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const _addArgumentToItem = (item) => {
      if (item.name === 'args') {
        return {
          ...item,
          value: [
            ...item.value,
            {
              id: action.id,
              name: '',
              type: 'String',
              from: 'Reference',
              value: '',
              referenceNode: '',
              referenceId: '',
              referenceKey: '',
            },
          ],
        };
      } else {
        return item;
      }
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, _addArgumentToItem)),
    };
  };

  return self;
};

/**
 * editInput 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const EditInputReducer = (updateKey) => {
  const self = {};
  self.type = 'editInput';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const applyChangesToValue = (v) => {
      if (v.id === action.id) {
        let newValue = {...v};
        action.changes.forEach(change => {
          newValue[change.key] = change.value;
        });
        return newValue;
      } else {
        return v;
      }
    };

    const updateItem = (item) => {
      if (item.name === 'args') {
        return {
          ...item,
          value: item.value.map(v => applyChangesToValue(v)),
        };
      } else {
        return item;
      }
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, updateItem)),
    };
  };

  return self;
};

/**
 * deleteInput 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const DeleteInputReducer = (updateKey) => {
  const self = {};
  self.type = 'deleteInput';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 事件对象.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    const filterArgs = (item) => {
      return item.name === 'args' ? {...item, value: item.value.filter(v => v.id !== action.id)} : item;
    };

    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      inputParams: config.inputParams.map(inputParam => updateInputParam(inputParam, updateKey, filterArgs)),
    };
  };

  return self;
};

/**
 * changeFlowMeta 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const ChangeFlowMetaReducer = () => {
  const self = {};
  self.type = 'changeFlowMeta';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @param jadeNodeConfigChangeIgnored 该修改是否需要忽略，如忽略外部将不会处理此次dirty事件.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action, jadeNodeConfigChangeIgnored = false) => {
    return {
      ...config,
      jadeNodeConfigChangeIgnored: jadeNodeConfigChangeIgnored,
      enableStageDesc: action.data.enableStageDesc,
      stageDesc: action.data.stageDesc,
    };
  };

  return self;
};