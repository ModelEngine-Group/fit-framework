/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {configToStruct, toConfig} from '@/components/util/JadeConfigUtils.js';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {ShapeDataValidationProcessor} from '@/data/ShapeDataValidationProcessor.js';

/**
 * 画布数据操纵器.
 *
 * @param graphString 画布数据.
 * @return {{}} 画布操纵器对象.
 */
const graphOperator = (graphString) => {
  const self = {};
  const graph = JSON.parse(graphString);
  const shapes = graph.pages[0].shapes;

  /**
   * 获取配置信息.
   *
   * @param keys 键值数组.
   * @return {{}|*|null} 配置信息.
   */
  self.getConfig = (keys) => {
    const config = getConfigByKeys(keys);
    return config ? configToStruct(config) : null;
  };

  const getInputParams = (shape) => {
    if (shape.type === 'startNodeStart') {
      return shape.flowMeta.inputParams;
    } else if (shape.type === 'endNodeEnd') {
      return shape.flowMeta.callback.converter.entity.inputParams;
    } else {
      return shape.flowMeta.jober.converter.entity.inputParams;
    }
  };

  const getConfigByKeys = (keys) => {
    if (!Array.isArray(keys)) {
      throw new Error('Expected keys to be an array');
    }

    if (keys.length === 0) {
      return null;
    }

    const tmpKeys = [...keys];
    const shapeId = tmpKeys.shift();
    const shape = getShapeById(shapeId);
    const inputParams = getInputParams(shape);
    if (!inputParams) {
      throw new Error('Expected inputParams exists');
    }
    let config = {type: DATA_TYPES.OBJECT, value: inputParams};
    while (tmpKeys.length > 0 && config && config.value) {
      const key = tmpKeys.shift();
      config = config.value.find(v => v.name === key);
    }
    return config;
  };

  const getShapeById = (shapeId) => {
    return shapes.find((shape) => shape.id === shapeId);
  };

  /**
   * 修改配置.
   *
   * @param keys 键值数组.
   * @param updates 待修改的值.
   */
  self.update = (keys, updates) => {
    const config = getConfigByKeys(keys);
    updateConfig(config, updates);
  };

  const updateConfig = (config, updates) => {
    if (Array.isArray(updates)) {
      config.value = updates.map(update => {
        return toConfig(null, update);
      })?.filter(item => item !== null);
    } else if (typeof updates === 'object') {
      updateObject(updates, config);
    } else {
      config.value = updates;
    }
  };

  const updateObject = (updates, config) => {
    Object.keys(updates).forEach(k => {
      if (Object.prototype.hasOwnProperty.call(updates, k)) {
        const update = updates[k];
        const correspondingConfig = config.value?.find(v => v.name === k);
        if (!correspondingConfig) {
          return;
        }

        // 处理对象
        if (typeof update === 'object') {
          updateConfig(correspondingConfig, update);
          return;
        }

        // 如果类型是reference，则不进行修改。
        if (correspondingConfig.from === FROM_TYPE.REFERENCE) {
          return;
        }
        updateConfig(correspondingConfig, update);
      }
    });
  };

  /**
   * 获取画布数据.
   *
   * @return {string} 画布数据.
   */
  self.getGraph = () => {
    return JSON.stringify(graph);
  };

  /**
   * 获取画布中需要校验的表单信息。
   *
   * @return {array} 画布数据.
   */
  self.getFormsToValidateInfo = () => {
    const classifiedData = shapes
      .map(shape => {
        const info = ShapeDataValidationProcessor(shape).extractValidationInfo();
        const isInfoDefined = info !== null && info !== undefined;
        if (isInfoDefined && Array.isArray(info) && info.length > 0) {
          return {
            type: shape.type,
            nodeId: shape.id,
            nodeName: shape.text,
            configs: info,
          };
        }
        return null;
      })
      .filter(item => item !== null)
      .reduce((acc, {type, nodeId, nodeName, configs}) => {
        if (!acc[type]) {
          acc[type] = {type, nodeInfos: []};
        }
        acc[type].nodeInfos.push({
          nodeId,
          nodeName,
          configs,
        });
        return acc;
      }, {});

    return Object.values(classifiedData);
  };

  /**
   * 获取画布中开始节点的入参信息。
   *
   * @return {array} 开始节点入参信息.
   */
  self.getStartNodeInputParams = () => {
    return shapes.filter(shape => shape.type === 'startNodeStart').map(startNode => startNode.flowMeta.inputParams);
  };

  return self;
};

/**
 * 创建画布操纵器.
 *
 * @param graphString 画布字符串.
 * @return {{}} 画布操纵器对象.
 */
export const createGraphOperator = (graphString) => {
  return graphOperator(graphString);
};
