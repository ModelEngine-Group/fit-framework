/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeFlowPage} from '@/flow/jadeFlowPage.js';
import {createProcessor} from '@/flow/evaluation/runnableProcessors.js';
import {eventDecorator} from '@/flow/evaluation/decorators/eventDecorator.js';
import {conditionEvaluationRunner, evaluationRunner} from '@/flow/evaluation/runners.js';

const DISTANCE = 20;
const EVALUATION_START_NODE = 'evaluationStartNodeStart';
const EVALUATION_END_NODE = 'evaluationEndNodeEnd';

const DECORATORS = [eventDecorator()];

/**
 * 评估页面.
 *
 * @param div dom元素.
 * @param graph 画布.
 * @param name 名称.
 * @param id 唯一标识.
 * @returns {*} 页面对象.
 */
export const jadeEvaluationPage = (div, graph, name, id) => {
  const self = jadeFlowPage(div, graph, name, id);
  self.type = 'jadeEvaluationPage';
  self.serializedFields.batchAdd('flowMetas');
  self.evaluationChain = [];
  self.latestFlowMetas = {};

  /**
   * 归一化，使评估页面在可使用之前处于正常状态.
   * 1、如果是发布过，那么图形基本处于readOnly状态，只能查看
   * 2、如果是未发布过
   *  2.1、如果没有【评估开始】和【评估结束】节点，那么要默认创建出两个节点，
   *       此时说明是第一次进入，需要将所有的非评估节点设置为不可运行状态(runnable = false)
   *  2.2、如果存在【评估开始】和【评估结束】节点，那么说明不是第一次进入，此时应该不需要做任何处理.
   *
   * @param graphData 画布数据.
   * @param isPublished 是否发布过.
   */
  self.normalize = (graphData, isPublished) => {
    if (isPublished) {
      self.sm.getShapes().forEach(s => {
        s.ignoreChange(() => {
          s.moveable = false;
          s.selectable = false;
          s.deletable = false;
        });
        s.statusManager.setDisabled(true);
        s.statusManager.setReferenceDisabled(true);
        s.invalidateAlone();
      });
    } else {
      const startNode = self.getStartNode();
      if (!startNode) {
        firstEvaluate();
      }
      self.sm.getShapes(s => !self.isEvaluationNode(s)).filter(s => s.flowMeta).forEach(s => {
        self.latestFlowMetas[s.id] = JSON.parse(JSON.stringify(s.flowMeta));
      });
    }
  };

  const firstEvaluate = () => {
    self.flowMetas = {};

    const shapes = self.sm.getShapes();
    let minX = shapes[0].x;
    let maxX = shapes[0].x + shapes[0].width;
    let maxY = shapes[0].y + shapes[0].height;
    shapes.forEach(s => {
      if (s.flowMeta) {
        self.flowMetas[s.id] = JSON.parse(JSON.stringify(s.flowMeta));
      }
      minX = Math.min(minX, s.x);
      maxX = Math.max(maxX, s.x + s.width);
      maxY = Math.max(maxY, s.y + s.height);
      if (s.type !== 'jadeEvent') {
        s.statusManager.setDisabled(true);
        s.statusManager.setReferenceDisabled(true);
      }
    });

    // 创建开始结束节点.
    self.createNew(EVALUATION_START_NODE, minX, maxY + DISTANCE);
    self.createNew(EVALUATION_END_NODE, maxX, maxY + DISTANCE);
  };

  /**
   * 获取所有开始节点.
   *
   * @return {*} 开始节点列表.
   */
  self.getStartNodes = () => {
    return self.sm.getShapes(s => s.type === EVALUATION_START_NODE);
  };

  /**
   * 获取开始节点.
   *
   * @return {*}
   */
  self.getStartNode = () => {
    return self.sm.findShapeBy(s => s.type === EVALUATION_START_NODE);
  };

  /**
   * 当有图形被连接上时触发.
   */
  self.onShapeConnect = () => {
    self.runnableFlow = getEvaluationFlow();

    // 先修改所有节点的runnable状态，并返回runnable发生了变化的节点，再处理变化的节点
    const processors = self.sm.getShapes().map(s => {
      const processor = createProcessor(s);
      processor.setRunnable(self.runnableFlow);
      return processor;
    });
    processors.filter(p => p.isUpdated).forEach(p => p.process(self));
  };

  /**
   * 找到从当前shape的当前connector拖出去的线
   *
   * @param node 图形
   * @param connector 锚点
   * @return {*} 拖出去的线
   */
  const getDragOutEvents = (node, connector) => self.getEvents().filter(s => s.fromShape === node.id && s.fromShapeConnector === connector);

  /**
   * 锚点拖出线条的条件判断，如果锚点上的线条数量大于2，则无法拖出
   *
   * @param node 图形
   * @param connector 需要拖出线条的锚点
   * @return bool 是否允许拖出线条
   */
  self.canDragOut = (node, connector) => {
    const lines = getDragOutEvents(node, connector);
    // 评估节点只能拖出一条线
    if (self.isEvaluationNode(node) && lines && lines.length > 0) {
      return false;
    }
    return lines && lines.length < 2;
  };

  /**
   * 获取和当前event拖入的connector相同的所有线条
   *
   * @param jadeEvent 线条
   * @return {*} event数组
   */
  const getDragInEvents = jadeEvent => self.getEvents().filter(s => s.toShapeConnector === jadeEvent.toShapeConnector);

  /**
   * 锚点允许拖入的线条数量
   *
   * @param jadeEvent 线条
   * @return {boolean} 是否允许拖入
   */
  const canDragIn = self.canDragIn;
  self.canDragIn = (jadeEvent) => {
    const isValid = () => {
      if (!jadeEvent.toShape) {
        return false;
      }
      // 找到拖入当前锚点的线，
      const runnableLines = getDragInEvents(jadeEvent).filter(e => e.runnable);
      return runnableLines && runnableLines.length < 2;
    };
    return canDragIn.apply(self, [jadeEvent]) && isValid();
  };

  /**
   * 判断当前shape是否是评估节点
   *
   * @param shape 图形
   * @return {*} 是否是评估节点
   */
  self.isEvaluationNode = shape => shape.isTypeof('evaluationNode');

  const getEvaluationFlow = () => {
    const startNode = self.getStartNode();
    const evaluationChain = new Set();
    if (traverse(startNode, evaluationChain)) {
      evaluationChain.add(startNode.id);
    } else {
      evaluationChain.clear();
    }
    return evaluationChain;
  };

  const traverse = (node, evaluationChain) => {
    if (node.type === EVALUATION_END_NODE) {
      return true;
    }

    // 既不是结束节点，并且也没有后继节点，那么说明当前链路不是评估链路.
    const nextNodes = node.getNextNodes();
    if (nextNodes.length === 0) {
      return false;
    }

    // 将jadeEvaluationFlow的线排在前面.
    const events = self.getEvents().filter(e => e.fromShape === node.id).sort((a, b) => {
      if (a.namespace === 'jadeEvaluationFlow' && b.namespace !== 'jadeEvaluationFlow') {
        return -1;
      } else if (a.namespace !== 'evaluationFlow' && b.namespace === 'evaluationFlow') {
        return 1;
      } else {
        return 0;
      }
    });

    // 先走评估的线，如果能走通，那么就直接返回true；否则再走非评估的线.
    for (let i = 0; i < events.length; i++) {
      const event = events[i];
      const nextNode = event.getToNode();
      if (nextNode && traverse(nextNode, evaluationChain)) {
        evaluationChain.add(event.id);
        evaluationChain.add(nextNode.id);
        return true;
      }
    }

    return false;
  };

  /**
   * 和onShapeConnect保持一致.
   */
  self.onShapeOffConnect = () => {
    self.onShapeConnect();
  };

  /**
   * @override
   */
  const validate = self.validate;
  self.validate = async () => {
    const runnableFlow = getEvaluationFlow();
    if (runnableFlow.size === 0) {
      return Promise.reject('评估流程不存在.');
    }
    return await validate.apply(self);
  };

  /**
   * @override
   */
  self.createRunner = (node) => {
    if (node.isTypeof('conditionNodeCondition')) {
      return conditionEvaluationRunner(node);
    }
    return evaluationRunner(node);
  };

  /**
   * 图形创建之后对其进行装饰.
   *
   * @override
   */
  const shapeCreated = self.shapeCreated;
  self.shapeCreated = shape => {
    shapeCreated.apply(self, [shape]);
    DECORATORS.forEach(d => {
      if (d.isMatch(shape)) {
        d.decorate(shape);
      }
    });
  };

  // 修改图形的namespace.
  self.registerShapeCreationHandler({
    type: 'after',
    handle: (page, shape) => {
      shape.namespace = 'jadeEvaluationFlow';
    },
  });

  /**
   * 判断节点是否是评估节点.
   *
   * @param node 节点对象.
   * @return {boolean} true/false.
   */
  self.isEvaluationNode = (node) => {
    return node.namespace === 'jadeEvaluationFlow';
  };

  return self;
};
