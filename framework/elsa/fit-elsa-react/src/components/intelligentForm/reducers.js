/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';
import {updateInput} from '@/components/util/JadeConfigUtils.js';
import {MANUAL_INIT_ENTITY, ORCHESTRATION_INIT_ENTITY, ORCHESTRATION_TASK_ID} from '@/components/intelligentForm/Consts.js';

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
      type: undefined,
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

export const ChangeFormTypeReducer = () => {
  const self = {};
  self.type = 'changeFormType';

  /**
   * 处理方法.
   *
   * @param config 配置数据.
   * @return {*} 处理之后的数据.
   */
  self.reduce = (config, action) => {
    const isOrchestration = action.value === 'orchestration';
    return {
      ...config,
      converter: {
        ...config.converter,
        entity: JSON.parse(JSON.stringify(isOrchestration ? ORCHESTRATION_INIT_ENTITY : MANUAL_INIT_ENTITY)),
      },
      formType: action.value,
      taskId: isOrchestration ? ORCHESTRATION_TASK_ID : '',
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