/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInput} from '@/components/util/JadeConfigUtils.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {ARGS, OUTPUT, OUTPUT_NAME, TOOL_CALLS} from '@/components/parallelNode/consts.js';

export const AddPluginByMetaDataReducer = () => {
  const self = {};
  self.type = 'addPluginByMetaData';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};

    const generatePluginName = (text) => {
      const toolList = newConfig?.inputParams?.find(value => value.name === TOOL_CALLS)?.value ?? [];
      let textVal = text;
      const textArray = toolList.map(tool =>
        tool.value.find(item => item.name === OUTPUT_NAME)?.value
      );
      if (textArray.filter(t => t === textVal).length < 1) {
        return textVal;
      }
      const separator = '_';
      let index = 1;
      while (true) {
        // 不带下划线，直接拼接_1
        const lastSeparatorIndex = textVal.lastIndexOf(separator);
        const last = textVal.substring(lastSeparatorIndex + 1, textVal.length);
        // 如果是数字，把数字+1  如果不是数字，拼接_1
        if (lastSeparatorIndex !== -1 && !isNaN(parseInt(last))) {
          textVal = textVal.substring(0, lastSeparatorIndex) + separator + index;
        } else {
          textVal = textVal + separator + index;
        }
        if (!textArray.includes(textVal)) {
          return textVal;
        }
        index++;
      }
    };

    const uniquePluginName = generatePluginName(action.pluginName);

    const PLUGIN_INPUT = {
      id: uuidv4(),
      type: DATA_TYPES.OBJECT,
      from: FROM_TYPE.EXPAND,
      value: [{
        id: uuidv4(),
        name: 'uniqueName',
        type: DATA_TYPES.STRING,
        from: FROM_TYPE.INPUT,
        value: action.uniqueName,
      },{
        id: uuidv4(),
        name: ARGS,
        type: DATA_TYPES.OBJECT,
        from: FROM_TYPE.EXPAND,
        value: action.entity.inputParams,
      },{
        id: uuidv4(),
        name: 'order',
        type: DATA_TYPES.ARRAY,
        from: FROM_TYPE.INPUT,
        value: action.entity.inputParams.map(({name}) => ({name})) || [],
      }, {
        id: uuidv4(),
        name: OUTPUT_NAME,
        type: DATA_TYPES.STRING,
        from: FROM_TYPE.INPUT,
        value: uniquePluginName,
      }, {
        id: uuidv4(),
        name: 'tags',
        type: DATA_TYPES.ARRAY,
        from: FROM_TYPE.INPUT,
        value: action.tags,
      }],
    };

    const convertedOutput = action.entity?.outputParams?.find(item => item.name === OUTPUT) ?? {};

    const PLUGIN_OUTPUT = {
      id: uuidv4(),
      type: convertedOutput?.type ?? DATA_TYPES.OBJECT,
      name: uniquePluginName,
      value: convertedOutput?.value ?? {},
    };

    return Object.entries(newConfig).reduce((acc, [key, value]) => {
      switch (key) {
        case 'inputParams':
          acc[key] = value.map(item =>
            item.name === TOOL_CALLS
              ? {...item, value: [...item.value, PLUGIN_INPUT]}
              : item
          );
          break;
        case 'outputParams':
          acc[key] = value.map(item =>
            item.name === OUTPUT
              ? {...item, value: [...item.value, PLUGIN_OUTPUT]}
              : item
          );
          break;
        default:
          acc[key] = value;
      }
      return acc;
    }, {});
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
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};
    Object.entries(config).forEach(([key, value]) => {
      if (key === 'inputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === TOOL_CALLS) {
            return {
              ...item,
              value: item.value.filter(v => v.value.find(arg => arg.name === OUTPUT_NAME).value !== action.outputName),
            };
          } else {
            return item;
          }
        });
      } else if (key === 'outputParams') {
        newConfig[key] = value.map(item => {
          if (item.name === OUTPUT) {
            return {
              ...item,
              value: item.value.filter(v => v.name !== action.outputName),
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
          if (item.name === TOOL_CALLS) {
            return {
              ...item, value: item.value.map(plugin => {
                if (plugin.id === action.parentId) {
                  return {
                    ...plugin, value: plugin.value.map(pluginValue => {
                      if (pluginValue.name === 'args') {
                        return {
                          ...pluginValue,
                          value: updateInput(pluginValue.value, action.id, action.changes),
                        };
                      } else {
                        return pluginValue;
                      }
                    }),
                  };
                } else {
                  return plugin;
                }
              }),
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