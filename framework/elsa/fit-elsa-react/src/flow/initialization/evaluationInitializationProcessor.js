/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {pageProcessor} from '@/flow/pageProcessors.js';
import {DEFAULT_MAX_MEMORY_ROUNDS} from '@/common/Consts.js';

/**
 * @override
 */
export const evaluationPageInitializationProcessor = (pageData, graph) => {
  const self = pageProcessor(pageData, graph);

  /**
   * @override
   */
  self.createShapeProcessor = (shapeData) => {
    switch (shapeData.type) {
      case 'jadeEvent':
        return evaluationEventInitializationProcessor(shapeData, graph, self);
      case 'conditionNodeCondition':
        return evaluationConditionInitializationProcessor(shapeData, graph, self);
      case 'llmNodeState':
        return evaluationLlmInitializationProcessor(shapeData, graph, self);
      default:
        return evaluationShapeInitializationProcessor(shapeData, graph, self);
    }
  };

  return self;
};

/**
 * 评估图形初始化处理器.
 *
 * @param shapeData 图形数据.
 * @param graph 画布对象.
 * @param pageHandler 页面处理器.
 * @return {{}} 处理器对象.
 */
export const evaluationShapeInitializationProcessor = (shapeData, graph, pageHandler) => {
  const self = {};
  self.shapeData = shapeData;
  self.graph = graph;
  self.pageProcessor = pageHandler;

  /**
   * 初始化处理.
   */
  self.process = () => {
    self.shapeData.deletable = false;
    self.shapeData.runnable = false;
    self.shapeData.enableMask = true;
  };

  return self;
};

/**
 * 条件节点兼容性处理器.
 *
 * @override
 */
export const evaluationConditionInitializationProcessor = (shapeData, graph, pageHandler) => {
  const self = evaluationShapeInitializationProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.flowMeta.conditionParams.branches.forEach(b => {
      b.runnable = false;
      b.disabled = true;
    });
  };

  return self;
};

/**
 * 连线兼容性处理器.
 *
 * @override
 */
export const evaluationEventInitializationProcessor = (shapeData, graph, pageHandler) => {
  const self = evaluationShapeInitializationProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.selectable = false;
    delete self.shapeData.enableMask;
  };

  return self;
};

/**
 * 大模型节点兼容性处理器.
 *
 * @override
 */
export const evaluationLlmInitializationProcessor = (shapeData, graph, pageHandler) => {
  const self = evaluationShapeInitializationProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    const maxMemoryRounds = self.shapeData.flowMeta
      .jober
      .converter
      .entity
      .inputParams
      .find(i => i.name === 'maxMemoryRounds');
    if (!maxMemoryRounds) {
      self.shapeData.
      flowMeta.
      jober.
      converter.
      entity.
      inputParams.push(DEFAULT_MAX_MEMORY_ROUNDS);
    }
  };

  return self;
};
