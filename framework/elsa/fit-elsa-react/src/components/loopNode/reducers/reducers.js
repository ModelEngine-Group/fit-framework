/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInput} from '@/components/util/JadeConfigUtils.js';
import {DEFAULT_INPUT_PARAMS} from '@/components/loopNode/LoopConsts.js';
import {TOOL_TYPE} from '@/common/Consts.js';

export const ChangePluginByMetaDataReducer = () => {
  const self = {};
  self.type = 'changePluginByMetaData';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'args') {
            return {
              ...item, value: action.entity.inputParams,
            };
          } else {
            return item;
          }
        });
      } else if (key === 'outputParams') {
        newConfig[key] = action.entity.outputParams;
      } else {
        newConfig[key] = value;
      }
    });

    const updateToolInfo = (toolInfo = {}) => {
      return {
        ...toolInfo,
        params: newConfig.inputParams?.find(param => param.name === 'args')?.value?.map(({name}) => ({name})) || [],
        uniqueName: action.uniqueName,
        return: { type: 'array' },
        pluginName: action.pluginName,
        tags: action.tags
      };
    };

    const newToolInfo = updateToolInfo();

    Object.entries(newConfig).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'toolInfo') {
            return {
              ...item,
              value: newToolInfo,
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });

    return newConfig;
  };



  return self;
};

export const DeletePluginReducer = () => {
  const self = {};
  self.type = 'deletePlugin';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config) => {
    return {
      ...config,
      inputParams: DEFAULT_INPUT_PARAMS,
      outputParams: [],
    };
  };

  return self;
};

/**
 * update 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateInputReducer = () => {
  const self = {};
  self.type = 'update';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'args') {
            return {
              ...item,
              value: updateInput(config.inputParams.find(param => param.name === 'args').value, action.id, action.changes),
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });

    return newConfig;
  };

  return self;
};

/**
 * updateSwitchInfo 事件处理器.
 *
 * @return {{}} 处理器对象.
 * @constructor
 */
export const UpdateRadioInfoReducer = () => {
  const self = {};
  self.type = 'updateRadioInfo';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {};
    const isWaterFlow = config['inputParams'].find(param => param.name === 'toolInfo').value.tags.includes(TOOL_TYPE.WATER_FLOW);
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === 'config') {
            return {
              ...item,
              value: {
                ... item.value,
                loopKeys: [isWaterFlow ? `inputParams.${action.paths}`: action.paths]
              },
            };
          } else {
            return item;
          }
        });
      } else {
        newConfig[key] = value;
      }
    });
    return newConfig;
  };

  return self;
};