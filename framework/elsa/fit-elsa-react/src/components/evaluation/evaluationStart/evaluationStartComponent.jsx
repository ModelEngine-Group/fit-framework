/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import EvaluationStartWrapper from '@/components/evaluation/evaluationStart/EvaluationStartWrapper.jsx';
import {defaultComponent} from '@/components/defaultComponent.js';
import {v4 as uuidv4} from 'uuid';

/**
 * 评估开始节点组件
 *
 * @param jadeConfig
 */
export const evaluationStartComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);

  /**
   * 必须.
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : [{
      id: uuidv4(),
      name: 'memory',
      type: 'Object',
      from: 'Expand',
      value: [{
        id: uuidv4(),
        name: 'memorySwitch',
        type: 'Boolean',
        from: 'Input',
        value: false,
      }, {
        id: uuidv4(),
        name: 'type',
        type: 'String',
        from: 'Input',
        value: 'NotUseMemory',
      }],
    }];
  };

  /**
   * @override
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <EvaluationStartWrapper shapeStatus={shapeStatus} data={data}/>
    </>);
  };

  return self;
};
