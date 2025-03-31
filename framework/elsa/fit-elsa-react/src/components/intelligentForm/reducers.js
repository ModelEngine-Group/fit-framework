/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

export const AddParamReducer = () => {
  const self = {};
  self.type = 'addParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newParam = {
      id: action.id,
      name: '',
      displayName: '',
      type: DATA_TYPES.STRING,
      from: FROM_TYPE.INPUT,
      value: '',
      renderType: '',
      options: {
        id: uuidv4(),
        from: FROM_TYPE.REFERENCE,
        referenceNode: "",
        referenceId: "",
        referenceKey: "",
        value: [],
        type: DATA_TYPES.ARRAY
      },
    };

    const newConfig = {...config};

    const schemaParam = newConfig.converter?.entity?.inputParams?.find(
      (param) => param.name === 'schema',
    );

    if (schemaParam) {
      schemaParam.value = {
        ...schemaParam.value,
        parameters: [...(schemaParam.value?.parameters || []), newParam],
      };
    }

    return newConfig;
  };

  return self;
};

export const UpdateParamReducer = () => {
  const self = {};
  self.type = 'updateParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};

    const schemaParam = newConfig.converter?.entity?.inputParams?.find(
      (param) => param.name === 'schema',
    );

    if (schemaParam) {
      schemaParam.value = {
        ...schemaParam.value,
        parameters: schemaParam.value?.parameters?.map((param) =>
          param.id === action.id
            ? {...param, ...Object.fromEntries(action.changes)}
            : param,
        ),
      };
    }

    return newConfig;

  };

  return self;
};

export const DeleteParamReducer = () => {
  const self = {};
  self.type = 'deleteParam';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const newConfig = {...config};

    const schemaParam = newConfig.converter?.entity?.inputParams?.find(
      (param) => param.name === 'schema',
    );

    if (schemaParam) {
      schemaParam.value = {
        ...schemaParam.value,
        parameters: schemaParam.value?.parameters?.filter(
          (param) => param.id !== action.id,
        ),
      };
    }

    return newConfig;
  };

  return self;
};