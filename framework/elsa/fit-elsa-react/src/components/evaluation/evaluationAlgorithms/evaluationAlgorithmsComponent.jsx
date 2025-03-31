/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import EvaluationAlgorithmsWrapper from '@/components/evaluation/evaluationAlgorithms/EvaluationAlgorithmsWrapper.jsx';
import {convertParameter} from '@/components/util/MethodMetaDataParser.js';
import {updateInput} from '@/components/util/JadeConfigUtils.js';
import {defaultComponent} from '@/components/defaultComponent.js';
import {EVALUATION_ALGORITHM_NODE_CONST} from '@/common/Consts.js';

/**
 * 评估算法节点组件
 *
 * @param jadeConfig
 */
export const evaluationAlgorithmsComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [
        {
          id: uuidv4(),
          name: 'uniqueName',
          type: 'String',
          from: 'Input',
          value: '',
        }, {
          id: `passScore_${uuidv4()}`,
          name: 'passScore',
          type: 'Number',
          from: 'Input',
          value: 0.0,
        }, {
          id: `algorithmArgs_${uuidv4()}`,
          name: 'algorithmArgs',
          type: 'Object',
          from: 'Expand',
          // 保存当前选中的算法的input信息
          value: [],
        }],
      outputParams: [{
        id: `output_${uuidv4()}`,
        name: 'output',
        type: 'Object',
        from: 'Expand',
        value: [
          {
            id: `isPass_${uuidv4()}`,
            name: 'isPass',
            type: 'Boolean',
            from: 'Input',
            value: 'Boolean',
          },
          {
            id: `score_${uuidv4()}`,
            name: 'score',
            type: 'Number',
            from: 'Input',
            value: 'Number',
          },
        ],
      }],
    };
  };

  /**
   * @override
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <EvaluationAlgorithmsWrapper shapeStatus={shapeStatus} data={data}/>
    </>);
  };

  /**
   * @override
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    /**
     * 切换算法
     *
     * @private
     */
    const _changeAlgorithm = () => {
      // 这里action包含了整个接口返回的数据
      if (action.value) {
        _getUniqueName().value = action.value.uniqueName;
      }
    };

    /**
     * 修改及格分数
     *
     * @private
     */
    const _editScore = () => {
      newConfig.inputParams.find(item => item.name === EVALUATION_ALGORITHM_NODE_CONST.PASS_SCORE).value = action.value;
    };

    /**
     * 获取算法的uniqueName
     *
     * @return {*}
     * @private
     */
    const _getUniqueName = () => {
      return newConfig.inputParams.find(item => item.name === EVALUATION_ALGORITHM_NODE_CONST.UNIQUE_NAME);
    };

    /**
     * 清除选项
     *
     * @private
     */
    const _clearAlgorithm = () => {
      _getUniqueName().value = [];
    };

    /**
     * 获取算法的input信息
     *
     * @return {*} 算法input信息
     * @private
     */
    const _getInput = () => newConfig.inputParams.find(item => item.name === 'algorithmArgs');

    /**
     * 创建输入
     *
     * @private
     */
    const _generateInput = () => {
      if (!action.value) {
        return;
      }
      const inputJson = action.value;
      _getInput().value = Object.keys(inputJson.schema.parameters.properties).map(key => {
        return convertParameter({
          propertyName: key,
          property: inputJson.schema.parameters.properties[key],
        });
      });
    };

    /**
     * 清除输入输出
     *
     * @private
     */
    const _clearSchema = () => {
      _getInput().value = [];
      newConfig.outputParams[0].value.remove(item => item.name !== EVALUATION_ALGORITHM_NODE_CONST.IS_PASS && item.name !== EVALUATION_ALGORITHM_NODE_CONST.SCORE);
    };

    let newConfig = {...config};
    switch (action.type) {
      case 'changeAlgorithm':
        _changeAlgorithm();
        _generateInput();
        return newConfig;
      case 'editScore':
        _editScore();
        return newConfig;
      case 'clearAlgorithm':
        _clearSchema();
        _clearAlgorithm();
        return newConfig;
      case 'update':
        newConfig.inputParams = updateInput(config.inputParams, action.id, action.changes);
        return newConfig;
      default: {
        return reducers.apply(self, [config, action]);
      }
    }
  };

  return self;
};
