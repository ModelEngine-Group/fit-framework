/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {node} from '@fit-elsa/elsa-core';
import {v4 as uuidv4} from 'uuid';
import {NODE_STATUS, SECTION_TYPE, SOURCE_PLATFORM, VIRTUAL_CONTEXT_NODE} from '@/common/Consts.js';
import {jadeNodeDrawer} from '@/components/base/jadeNodeDrawer.jsx';
import {overrideMethods} from '@/components/base/overrides.js';
import {referenceDecorate} from '@/components/base/references.js';
import {addDetections} from '@/components/base/detections.js';
import {statusManager} from '@/components/base/statusManager.js';
import {FormValidator, NormalNodeConnectorValidator} from '@/components/base/validator.js';

/**
 * jadeStream中的流程编排节点.
 *
 * @override
 */
export const jadeNode = (id, x, y, width, height, parent, drawer) => {
  const self = node(id, x, y, width, height, parent, false, drawer ? drawer : jadeNodeDrawer);
  self.type = 'jadeNode';
  self.serializedFields.batchAdd(
    'toolConfigs',
    'componentName',
    'flowMeta',
    'outlineWidth',
    'outlineColor',
    'sourcePlatform',
    'runnable',
    'enableMask',
    'hasError',
  );
  self.eventType = 'jadeEvent';
  self.namespace = 'jadeFlow';
  self.hideText = true;
  self.autoHeight = true;
  self.width = 360;
  self.borderColor = 'rgba(28,31,35,.08)';
  self.mouseInBorderColor = '#B1B1B7';
  self.outlineColor = 'rgba(74,147,255,0.12)';
  self.borderWidth = 1;
  self.focusBorderWidth = 1;
  self.outlineWidth = 10;
  self.dashWidth = 0;
  self.backColor = 'white';
  self.focusBackColor = 'white';
  self.borderRadius = 8;
  self.cornerRadius = 8;
  self.enableAnimation = false;
  self.modeRegion.visible = false;
  self.runStatus = NODE_STATUS.DEFAULT;
  self.emphasizedOffset = -5;
  self.flowMeta = {
    triggerMode: 'auto',
    jober: {
      type: 'general_jober',
      name: '',
      fitables: [],
      converter: {
        type: 'mapping_converter',
      },
    },
    joberFilter: {
      type: 'MINIMUM_SIZE_FILTER',
      threshold: 1,
    },
  };
  self.sourcePlatform = SOURCE_PLATFORM.OFFICIAL;
  self.observed = [];
  self.runnable = true;
  self.disabled = false;
  self.enableMask = false;
  self.referenceDisabled = false;
  self.statusManager = statusManager(self);
  self.isActiveInFlow = false;

  overrideMethods(self);
  referenceDecorate(self);
  addDetections(self);

  /**
   * 获取节点默认的测试报告章节
   */
  self.getRunReportSections = () => {
    // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
    return [{no: '1', name: 'input', type: SECTION_TYPE.DEFAULT, data: self.input ?? {}}, {
      no: '2',
      name: 'output',
      type: SECTION_TYPE.DEFAULT,
      data: self.getOutputData(self.output),
    }];
  };

  /**
   * 获取输出的数据
   *
   * @param source 数据源
   * @return {*|{}}
   */
  self.getOutputData = (source) => {
    if (self.runStatus === NODE_STATUS.ERROR) {
      return self.errorMsg ? self.errorMsg : '';
    } else {
      return source ? source : {};
    }
  };

  /**
   * 获取节点默认的测试报告章节.
   */
  self.setRunReportSections = (data) => {
    // 把节点推送来的的data处理成Section
    // 开始节点只有输入，结束节点只有输出，普通节点输入输出，条件节点有条件1...n和输出
    self.output = {};
    self.input = {};
    if (data.parameters[0]) {
      self.output = JSON.parse(data.parameters[0].output);
      self.input = JSON.parse(data.parameters[0].input);
    }
    self.errorMsg = data.errorMsg;
    self.cost = data.runCost;
  };

  /**
   * 处理传递的元数据
   *
   * @param metaData 元数据信息
   */
  self.processMetaData = (metaData) => {
    if (metaData && metaData.name) {
      self.text = metaData.name;
    }
  };

  /**
   * 获取节点之前可达的节点信息
   *
   * @returns {*[]} 包含前方节点的名称和info信息的数组
   */
  self.getPreNodeInfos = () => {
    if (!self.allowToLink) {
      return [];
    }
    const allLines = self.page.getEvents();
    const preNodeInfos = []; // 存储前方节点信息的数组
    const visitedShapes = new Set(); // 记录已经访问过的形状id

    /**
     * 递归函数，探索当前shape前方所有shape
     *
     * @param currentShapeId 当前shape的id
     */
    const explorePreShapesRecursive = (currentShapeId) => {
      // 如果当前形状已经访问过，则跳过
      if (visitedShapes.has(currentShapeId)) {
        return;
      }
      // 将当前形状id添加到visitedShapes中
      visitedShapes.add(currentShapeId);

      // 获取当前形状对象
      const currentShape = self.page.getShapeById(currentShapeId);
      if (!currentShape) {
        // 如果找不到当前形状对象，则返回
        return;
      }

      // 将当前形状的名称和获取到的info添加到formerNodes中
      preNodeInfos.push({
        id: currentShapeId,
        node: currentShape,
        name: currentShape.text,
        runnable: currentShape.runnable,
        observableList: currentShape.page.getObservableList(currentShapeId),
      });

      // 找到当前形状连接的所有线
      const connectedLines = allLines.filter(s => s.toShape === currentShapeId);

      // 遍历连接线，将每个连接线的起点形状ID递归探索
      for (const line of connectedLines) {
        explorePreShapesRecursive(line.fromShape);
      }
    };

    // 从当前节点开始启动递归
    explorePreShapesRecursive(self.id);
    const systemEnv = self.page.getShapeById(VIRTUAL_CONTEXT_NODE.id);
    systemEnv.runnable = true;
    preNodeInfos.push({
      id: systemEnv.id,
      name: systemEnv.text,
      node: systemEnv,
      runnable: true,
      observableList: self.page.getObservableList(systemEnv.id),
    });
    preNodeInfos.shift();
    return preNodeInfos;
  };

  /**
   * 获取可引用的前置节点信息列表.
   *
   * @return {*[]} 前置节点信息列表.
   */
  self.getPreReferencableNodeInfos = () => {
    return self.getPreNodeInfos().filter(s => s.runnable === self.runnable);
  };

  /**
   * 获取直接前继节点信息
   */
  self.getDirectPreNodeIds = () => {
    if (!self.allowToLink) {
      return [];
    }
    return self.page.getEvents()
      .filter(s => s.toShape === self.id)
      .map(line => self.page.getShapeById(line.fromShape));
  };

  /**
   * 监听dom容器resize的变化.
   */
  self.observe = () => {
    self.drawer.observe();
  };

  /**
   * 获取用户自定义组件.
   *
   * @return {*}
   */
  self.getComponent = () => {
    return self.graph.plugins[self.componentName](self.flowMeta.jober.converter.entity, self);
  };

  /**
   * 复制节点.
   */
  self.duplicate = () => {
    const shapes = JSON.stringify([self.serialize()]);
    const newShapes = self.page.copyPasteHelper.pasteShapes(shapes, '', self.page);
    self.page.triggerEvent({
      type: 'COPY_SHAPE',
      value: {
        shapeId: newShapes[0].shape.id,
      },
    });
  };

  /**
   * 序列化jade配置.
   *
   * @param jadeConfig 配置.
   */
  self.serializerJadeConfig = (jadeConfig) => {
    self.flowMeta.jober.converter.entity = jadeConfig;
    self.flowMeta.enableStageDesc = self.flowMeta.jober.converter.entity.enableStageDesc;
    self.flowMeta.stageDesc = self.flowMeta.jober.converter.entity.stageDesc;
  };

  /*
   * 更新粘贴出来的图形的id
   */
  const updateCopiedNodeIds = entity => {
    if (typeof entity === 'object' && entity !== null) {
      if (Array.isArray(entity)) {
        entity.forEach((item) => {
          updateCopiedNodeIds(item);
        });
      } else {
        Object.keys(entity).forEach((key) => {
          if (key === 'id') {
            entity[key] = uuidv4();
          } else {
            updateCopiedNodeIds(entity[key]);
          }
        });
      }
    }
  };

  /**
   * 图形粘贴后的回调
   */
  self.pasted = () => {
    updateCopiedNodeIds(self.getEntity());
  };

  /**
   * 获取flowMeta的entity
   */
  self.getEntity = () => {
    return self.flowMeta.jober.converter.entity;
  };

  /**
   * 断开连接.
   */
  self.offConnect = () => {
    const preNodes = self.getPreNodeInfos();
    const visited = new Set();
    visitNext(self, visited, preNodes, (n, pNodes) => {
      n.observed.filter(o => !n.isReferenceAvailable(pNodes, o))
        .forEach(o => o.disable());
    });
  };

  /**
   * 连接.
   */
  self.onConnect = () => {
    const preNodes = self.getPreNodeInfos();
    const visited = new Set();
    visitNext(self, visited, preNodes, (n, pNodes) => {
      n.observed.filter(o => n.isReferenceAvailable(pNodes, o)).forEach(o => o.enable());
    });
  };

  // 访问后续节点.
  const visitNext = (currentNode, visited, preNodes, action) => {
    if (visited.has(currentNode)) {
      return;
    }
    action(currentNode, preNodes);
    visited.add(currentNode);
    preNodes.push({id: currentNode.id, node: currentNode, runnable: currentNode.runnable});
    currentNode.getNextNodes().forEach(n => visitNext(n, visited, preNodes, action));
    preNodes.pop();
  };

  /**
   * 获取下一批节点.
   */
  self.getNextNodes = () => {
    const lines = self.page.getEvents().filter(e => e.fromShape === self.id);
    if (!lines || lines.length === 0) {
      return [];
    }
    return self.page.sm.getShapes(s => s.type !== 'jadeEvent' && lines.some(l => l.toShape === s.id));
  };

  /**
   * 获取后续的可运行节点.
   *
   * @return {T[]} 可运行节点列表.
   */
  self.getNextRunnableNodes = () => {
    return self.getNextNodes().filter(s => s.runnable);
  };

  /**
   * 获取后续的可运行event.
   *
   * @returns {*} 可运行event列表.
   */
  self.getNextRunnableEvents = () => {
    return self.page.getEvents().filter(l => l.fromShape === self.id && l.runnable);
  };

  /**
   * 校验节点状态是否正常.
   *
   * @param linkNodeSet 链路中的节点列表的Set
   * @return Promise 校验结果
   */
  self.validate = (linkNodeSet) => {
    const validators = [new FormValidator(self)];
    if (linkNodeSet.has(self.id)) {
      validators.push(new NormalNodeConnectorValidator(self));
    }
    return self.runValidators(validators);
  };

  self.runValidators = async (validators) => {
    for (const validator of validators) {
      await validator.validate();
    }
  };

  /**
   * 设置元数据.
   *
   * @param flowMeta 元数据信息.
   * @param jadeNodeConfigChangeIgnored 是否忽略节点数据变化.
   */
  self.setFlowMeta = (flowMeta, jadeNodeConfigChangeIgnored = false) => {
    self.drawer.dispatch({
      type: 'system_update',
      changes: [
        {key: 'inputParams', value: flowMeta?.jober?.converter?.entity?.inputParams},
        {key: 'jadeNodeConfigChangeIgnored', value: jadeNodeConfigChangeIgnored || self.page.isRunning},
      ],
    });
  };

  /**
   * 获取flowMeta.
   *
   * @return {*} 元数据.
   */
  self.getFlowMeta = () => {
    const jadeConfig = self.drawer.getLatestJadeConfig();
    if (jadeConfig) {
      self.serializerJadeConfig(jadeConfig);
    }
    return self.flowMeta;
  };

  /**
   * 获取从当前节点到目标target节点之间的所有节点.
   *
   * @param target 目标节点.
   * @return {*} 节点集合.
   */
  self.getChains = (target) => {
    const chain = new Set();
    chain.add(self);
    if (!traverse(self, target, chain)) {
      chain.clear();
    }
    return Array.from(chain);
  };

  const traverse = (shape, target, chain) => {
    if (shape === target) {
      return true;
    }

    // 既不是结束节点，并且也没有后继节点，那么说明当前链路不是评估链路.
    const nextNodes = shape.getNextNodes();
    if (nextNodes.length === 0) {
      return false;
    }

    return nextNodes.map(n => {
      chain.add(n);
      if (!traverse(n, target, chain)) {
        chain.delete(n);
        return false;
      }
      return true;
    }).reduce((acc, v) => acc || v, false);
  };

  /**
   * 节点最大的被连接数。
   *
   * @returns {number} 连接数。
   */
  self.maxNumToLink = () => {
    return 1;
  };

  /**
   * 节点校验异常时的处理方法。
   */
  self.onError = () => {
    if (self.hasError) {
      return;
    }
    self.borderColor = 'rgb(198, 57, 57)';
    self.focusBorderColor = 'rgb(198, 57, 57)';
    self.mouseInBorderColor = 'rgb(198, 57, 57)';
    self.outlineColor = 'rgb(198, 57, 57, 0.12)';
    self.hasError = true;
    self.invalidateAlone();
  };

  /**
   * 节点校验无异常时的处理方法。
   */
  self.offError = () => {
    if (!self.hasError) {
      return;
    }
    self.borderColor = 'rgba(28, 31, 35, 0.08)';
    self.focusBorderColor = 'rgb(4, 123, 252)';
    self.mouseInBorderColor = 'rgb(4, 123, 252)';
    self.outlineColor = 'rgba(74, 147, 255, 0.12)';
    self.hasError = false;
    self.invalidateAlone();
  };

  return self;
};
