/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {EVALUATION_ALGORITHM_NODE_CONST, SECTION_TYPE} from '@/common/Consts.js';
import {
  evaluationAlgorithmsNodeDrawer,
} from '@/components/evaluation/evaluationAlgorithms/evaluationAlgorithmsNodeDrawer.jsx';
import {evaluationNode} from '@/components/evaluation/evaluationNode.jsx';

/**
 * 评估算法节点shape
 *
 * @override
 */
export const evaluationAlgorithmsNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = evaluationNode(id, x, y, width, height, parent, drawer ? drawer : evaluationAlgorithmsNodeDrawer);
  self.type = 'evaluationAlgorithmsNodeState';
  self.componentName = 'evaluationAlgorithmsComponent';
  self.text = '评估算法';
  self.width = 368;
  self.flowMeta.jober.fitables.push('modelengine.jade.app.engine.task.AlgorithmComponent');

  /**
   * @override
   */
  const serializerJadeConfig = self.serializerJadeConfig;
  self.serializerJadeConfig = (jadeConfig) => {
    // 把算法的输入写到到outputParam中传给后端
    serializerJadeConfig.apply(self, [jadeConfig]);
    const algorithmInput = self.flowMeta.jober.converter.entity.inputParams
      .filter(item => item.name !== EVALUATION_ALGORITHM_NODE_CONST.PASS_SCORE &&
        item.name !== EVALUATION_ALGORITHM_NODE_CONST.UNIQUE_NAME);
    self.flowMeta.jober.converter.entity.outputParams[0].value
      .remove(item => item.name !== EVALUATION_ALGORITHM_NODE_CONST.SCORE &&
        item.name !== EVALUATION_ALGORITHM_NODE_CONST.IS_PASS);
    self.flowMeta.jober.converter.entity.outputParams[0].value.push(...algorithmInput);
  };

  /**
   * 评估算法节点的测试报告章节
   */
  self.getRunReportSections = () => {
    return [{
      no: '1',
      name: '输出',
      type: SECTION_TYPE.DEFAULT,
      data: self.getOutputData(self.input),
    }];
  };

  return self;
};