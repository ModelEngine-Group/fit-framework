/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {updateInput} from '@/components/util/JadeConfigUtils.js';

export const ChangeFormByMetaDataReducer = () => {
  const self = {};
  self.type = 'changeFormByMetaData';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @param action 行为参数.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    return {
      ...config,
      taskId: action.formId,
      formName: action.formName,
      imgUrl: action.imgUrl,
      converter: {
        ...config.converter,
        entity: action.entity,
      },
    };
  };

  return self;
};

export const DeleteFormReducer = () => {
  const self = {};
  self.type = 'deleteForm';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config) => {
    return {
      ...config,
      taskId: '',
      formName: '',
      imgUrl: undefined,
      converter: {
        ...config.converter,
        entity: {
          inputParams: [],
          outputParams: [],
        },
      },
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
    newConfig.converter.entity.inputParams = updateInput(config.converter.entity.inputParams, action.id, action.changes);
    return newConfig;
  };

  return self;
};