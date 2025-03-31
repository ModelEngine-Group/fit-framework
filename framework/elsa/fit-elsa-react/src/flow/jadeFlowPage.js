/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {copyPasteHelper, ElsaCopyHandler, page, shapeDataHelper, sleep, uuid} from '@fit-elsa/elsa-core';
import {SYSTEM_ACTION, VIRTUAL_CONTEXT_NODE} from '@/common/Consts.js';
import {conditionRunner, inactiveNodeRunner, standardRunner} from '@/flow/runners.js';
import {message} from 'antd';

const START_NODE = 'startNodeStart';

/**
 * jadeFlow的page.
 *
 * @param div dom元素.
 * @param graph 画布.
 * @param name 名称.
 * @param id 唯一标识.
 * @returns {(WorkerGlobalScope & Window) | Window}
 */
export const jadeFlowPage = (div, graph, name, id) => {
  const self = page(div, graph, name, id);
  self.type = 'jadeFlowPage';
  self.serializedFields.batchAdd('x', 'y', 'scaleX', 'scaleY');
  self.namespace = 'jadeFlow';
  self.backgroundGrid = 'point';
  self.backgroundGridSize = 16;
  self.backgroundGridMargin = 16;
  self.backColor = '#fbfbfc';
  self.focusBackColor = '#fbfbfc';
  self.gridColor = '#e1e1e3';
  self.disableContextMenu = true;
  self.moveAble = true;
  self.observableStore = ObservableStore();
  self.copyPasteHelper = jadeCopyPasteHelper();
  const shapeChangeListener = (e) => self.graph.dirtied(null, {action: SYSTEM_ACTION.JADE_NODE_CONFIG_CHANGE, shape: e.shapeId});
  self.addEventListener('COPY_SHAPE', shapeChangeListener);
  self.addEventListener('DELETE_SHAPE', shapeChangeListener);

  /**
   * @override
   */
  const onLoaded = self.onLoaded;
  self.onLoaded = () => {
    onLoaded.apply(self);
    self.sm.getShapes().forEach(s => s.onPageLoaded && s.onPageLoaded());

    // 上下文虚拟节点信息注册
    registerVirtualNodeInfo();
  };

  const registerVirtualNodeInfo = () => {
    const systemNode = self.createShape('systemEnv', 0, 0, VIRTUAL_CONTEXT_NODE.id);
    systemNode.registerObservables();
  };

  /**
   * 获取所有开始节点.
   *
   * @return {*} 开始节点列表.
   */
  self.getStartNodes = () => {
    return self.sm.getShapes(s => s.type === START_NODE);
  };

  /**
   * 获取开始节点.
   *
   * @return {*} 开始节点.
   */
  self.getStartNode = () => {
    return self.sm.findShapeBy(s => s.type === START_NODE);
  };

  /**
   * 具有唯一性的图形以及线都无法拷贝.
   *
   * @override
   */
  const onCopy = self.onCopy;
  self.onCopy = (shapes) => {
    const copiableShapes = shapes.filter(s => {
      const isConnectedEvent = s.isTypeof('jadeEvent') && shapes.some(shape => shape.id === s.toShape) && shapes.some(shape => shape.id === s.fromShape);
      return !s.isTypeof('jadeEvent') || isConnectedEvent;
    });
    return onCopy.apply(self, [copiableShapes]);
  };

  /**
   * 注册可被监听的id.
   *
   * @param props 相关属性.
   */
  self.registerObservable = (props) => {
    self.observableStore.add(props);
  };

  /**
   * 删除可被监听的id.若不传observableId，则删除该节点所有可被监听的id.
   *
   * @param nodeId 节点id.
   * @param observableId 可被监听的id.
   */
  self.removeObservable = (nodeId, observableId = null) => {
    self.observableStore.remove(nodeId, observableId);
  };

  /**
   * 获取可被监听的树装列表.
   *
   * @param nodeId 节点id.
   * @return {*[]}
   */
  self.getObservableList = (nodeId) => {
    return self.observableStore.getObservableList(nodeId);
  };

  /**
   * 监听某个observable.
   *
   * @param nodeId 被监听节点的id.
   * @param observableId 待监听的id.
   * @param observer 监听器.
   */
  self.observeTo = (nodeId, observableId, observer) => {
    self.observableStore.addObserver(nodeId, observableId, observer);
  };

  /**
   * 停止监听某个observable.
   *
   * @param nodeId 被监听节点的id.
   * @param observableId 待监听的id.
   * @param observer 监听器.
   */
  self.stopObserving = (nodeId, observableId, observer) => {
    self.observableStore.removeObserver(nodeId, observableId, observer);
  };

  /**
   * 获取observable.
   *
   * @param nodeId 被监听节点的id.
   * @param observableId 待监听的id.
   * @return {*|null}
   */
  self.getObservable = (nodeId, observableId) => {
    return self.observableStore.getObservable(nodeId, observableId);
  };

  /**
   * 清空时，同时清空observableStore.
   *
   * @override
   */
  const clear = self.clear;
  self.clear = () => {
    clear.apply(self);
    self.observableStore.clear();
  };

  const initJadeId = (id) => {
    let newId = id; // 使用局部变量来存储结果
    if (id === null || id === undefined || id === '') {
      // 变量为空，使用 uuid 创建
      newId = `jade${uuid()}`;
    } else if (!id.startsWith('jade')) {
      // 如果 id 不以 "jade" 开头，则在前面添加 "jade"
      newId = `jade${newId}`;
    }
    return newId;
  };

  /**
   * 添加对图形创建的前后处理.
   *
   * @override
   */
  const createNew = self.createNew;
  self.createNew = (shapeType, x, y, id, properties, parent, ignoreLimit, data, metaData) => {
    shapeCreationHandler.filter(v => v.type === 'before')
      .forEach(v => v.handle(self, shapeType, x, y, properties, parent));
    const shape = createNew.apply(self, [shapeType, x, y, id, properties, parent, ignoreLimit, data]);
    shape.processMetaData && shape.processMetaData(metaData);
    shapeCreationHandler.filter(v => v.type === 'after')
      .forEach(v => v.handle(self, shape));
    return shape;
  };

  /**
   * 生成节点的名称
   *
   * @param text 节点text
   * @param type 节点类型
   */
  self.generateNodeName = (text, type) => {
    let textVal = text;
    const jadeNodes = self.sm.getShapes(s => s.isTypeof('jadeNode'));
    // 找到所有节点text
    const textArray = jadeNodes.map(s => s.text);
    if (textArray.filter(t => t === textVal).length <= 1) {
      return textVal;
    }
    const separator = '_';
    if (jadeNodes.filter(s => s.type === type).length <= 1) {
      return textVal;
    }
    let index = 1;
    while (true) {
      // 不带下划线，直接拼接_1
      const lastSeparatorIndex = textVal.lastIndexOf(separator);
      const last = textVal.substring(lastSeparatorIndex + 1, textVal.length);
      // 如果是数字，把数字+1  如果不是数字，拼接_1
      if (lastSeparatorIndex !== -1 && !isNaN(parseInt(last))) {
        textVal = textVal.substring(0, lastSeparatorIndex) + separator + index;
      } else {
        textVal = textVal + separator + index;
      }
      if (!textArray.includes(textVal)) {
        return textVal;
      }
      index++;
    }
  };

  /**
   * 注册对图形创建的前后处理.
   *
   * @param handler 处理器.
   */
  const shapeCreationHandler = [];
  self.registerShapeCreationHandler = (handler) => {
    shapeCreationHandler.push(handler);
  };

  self.getMenuScript = () => [];

  // 注册处理器，一次只能有一个开始和结束节点.
  self.registerShapeCreationHandler({
    type: 'before',
    handle: (page, shapeType) => {
      if (shapeType === 'startNodeStart') {
        if (page.sm.findShapeBy(s => s.type === shapeType)) {
          throw new Error('最多只能有一个开始节点.');
        }
      }
    },
  });

  // 注册处理器，每种节点不能超过20个，总节点不能超过100个.
  self.registerShapeCreationHandler({
    type: 'before',
    handle: (page, shapeType) => {
      if (shapeType === 'jadeEvent') {
        return;
      }
      const i18n = page.graph.i18n;
      if (page.getNodes().filter(shape => shape.type === shapeType).length > page.graph.setting.sameTypeNodeNumLimit) {
        const exMessage = i18n.t('sameTypeNodeCannotMoreThan20');
        message.warning(exMessage);
        throw new Error(exMessage);
      }
      if (page.getNodes().length > page.graph.setting.allNodeNumLimit) {
        const exMessage = i18n.t('allNodeCannotMoreThan100');
        message.warning(exMessage);
        throw new Error(exMessage);
      }
    },
  });

  self.registerShapeCreationHandler({
    type: 'after',
    handle: (page, shape) => {
      shape.text = self.generateNodeName(shape.text, shape.type);
    },
  });

  self.registerShapeCreationHandler({
    type: 'after',
    handle: (page, shape) => {
      if (shape.type !== 'jadeEvent') {
        shape.id = initJadeId(shape.id);
      }
    },
  });

  /**
   * 获取全量线条
   *
   * @return 全量线条
   */
  self.getEvents = () => {
    return self.sm.getShapes(s => s.isTypeof('jadeEvent'));
  };

  /**
   * 锚点拖出线条的条件判断，如果锚点上的线条数量大于0，则无法拖出
   *
   * @param node 节点
   * @param connector 需要拖出线条的锚点
   * @return bool 是否允许拖出线条
   */
  self.canDragOut = (node, connector) => {
    const lines = self.getEvents().filter(s => s.fromShape === node.id && s.getDefinedFromConnector() === connector);
    return lines && lines.length < 1;
  };

  /**
   * 锚点允许拖入的线条数量
   *
   * @param jadeEvent 线条
   * @return {boolean} 是否允许拖入
   */
  self.canDragIn = (jadeEvent) => {
    // 当前的线条连接的目标Connector是否已经超出该Connector的最大限制
    const isConnectorWithinLimit = () => {
      return self.sm.getShapes().filter(s => s.isTypeof('jadeEvent') && s.id !== jadeEvent.id).filter(s => {
        return s.toShape === jadeEvent.toShape && s.toShapeConnector === jadeEvent.toShapeConnector;
      }).length < self.sm.getShapeById(jadeEvent.toShape).maxNumToLink();
    };

    const isConnectorAllowToLink = () => {
      if (jadeEvent.toShapeConnector) {
        return jadeEvent.toShapeConnector?.allowToLink;
      } else {
        return false;
      }
    };

    return jadeEvent.fromShape !== jadeEvent.toShape && isConnectorAllowToLink() && isConnectorWithinLimit();
  };

  /**
   * 获取page内所有节点
   *
   * @returns {*} page内的所有节点信息
   */
  self.getNodes = () => {
    return self.sm.getShapes(shape => shape.isTypeof('jadeNode'));
  };

  /**
   * 传入需要遍历的起始节点信息，获取这个节点可达的所有节点信息
   *
   * @param node 起始节点
   * @returns {*} 起始节点所有可达节点的信息
   */
  self.getReachableNodes = (node) => {
    // 存储所有可达节点的数组
    const reachableNodes = [];

    // 递归函数，用于遍历节点
    const traverse = (n) => {
      // 将当前节点添加到可达节点的列表
      reachableNodes.push(n);

      // 获取当前节点的下一个节点
      const nextNodes = n.getNextNodes();

      // 如果没有下一个节点，结束递归
      if (nextNodes.length === 0) {
        return;
      }

      // 遍历每个下一个节点并递归调用 traverse
      nextNodes.forEach(nextNode => {
        traverse(nextNode);
      });
    };

    // 开始从传入的起始节点遍历
    traverse(node);

    // 返回所有可达节点的信息
    return reachableNodes;
  };

  /**
   * 等待所有图形绘制完成.
   *
   * @return {Promise<void>} promise.
   */
  self.awaitShapesRendered = async () => {
    const shapeRenderedArray = self.sm.getShapes(s => s.isTypeof('jadeNode')).map(s => {
      return {
        id: s.id,
        rendered: false,
      };
    });
    const listener = (e) => shapeRenderedArray.find(s => s.id === e.id).rendered = true;
    self.addEventListener('shape_rendered', listener);
    while (!shapeRenderedArray.every(s => s.rendered)) {
      await sleep(50);
    }
    self.removeEventListener('shape_rendered', listener);
  };

  /**
   * 启动调试.
   *
   * @return {*} 调试的节点列表.
   */
  self.testRun = () => {
    self.getNodes().forEach(node => {
      node.isActiveInFlow = false;
    });
    const startNode = self.getStartNode();
    self.getReachableNodes(startNode).forEach(node => {
      node.isActiveInFlow = true;
    });
    self.isRunning = true;
    const nodes = self.sm.getShapes().filter(s => s.isTypeof('jadeNode')).filter(s => s.runnable !== false);
    nodes.map(n => {
      const h = self.createRunner(n);
      n.ignoreChange(() => {
        h.testRun();
      });
    });
    return nodes;
  };

  /**
   * 重置当前页中节点状态
   *
   * @param nodes 节点
   */
  self.resetRun = nodes => {
    nodes.map(n => {
      const h = self.createRunner(n);
      n.ignoreChange(() => {
        h.resetRun();
      });
    });
    graph.activePage.isRunning = false;
  };

  /**
   * 刷新节点运行状态.
   *
   * @param nodes 节点列表.
   * @param dataList 数据列表.
   */
  self.refreshRun = (nodes, dataList) => {
    if (!graph.activePage.isRunning) {
      return;
    }
    nodes.map(n => {
      const h = self.createRunner(n);
      n.ignoreChange(() => {
        h.refreshRun(dataList);
      });
    });
  };

  /**
   * 返回停止运行流程测试方法
   *
   * @return 停止运行流程测试方法
   */
  self.stopRun = (nodes, dataList) => {
    nodes.map(n => {
      const h = self.createRunner(n);
      n.ignoreChange(() => {
        h.stopRun(dataList);
      });
    });
    graph.activePage.isRunning = false;
  };

  /**
   * 校验流程.
   *
   * @return {Promise<void>} Promise 校验结果
   */
  self.validate = async () => {
    const startNodes = self.getStartNodes();
    if (startNodes.length < 1) {
      return Promise.reject(new Error(JSON.stringify({
        errorFields: [{
          errors: ['未找到开始节点'],
          name: 'node-error',
        }],
      })));
    }
    if (startNodes.length > 1) {
      return Promise.reject(new Error(JSON.stringify({
        errorFields: [{
          errors: ['开始节点只有一个时才允许运行'],
          name: 'node-error',
        }],
      })));
    }
    const startNode = startNodes[0];

    const traverseNodesOnTheLink = () => {
      const visited = new Set(); // 记录已访问的节点
      const queue = [startNode]; // 初始化队列，从开始节点开始
      // BFS 遍历所有节点
      while (queue.length > 0) {
        const node = queue.shift();
        visited.add(node.id); // 标记节点为已访问
        const nextNodes = node.getNextRunnableNodes();
        // 将所有未访问过的下一个节点加入队列
        nextNodes.forEach(nextNode => {
          if (!visited.has(nextNode.id)) {
            queue.push(nextNode);
          }
        });
      }
      return visited;
    };
    const linkNodeSet = traverseNodesOnTheLink();

    const nodes = graph.activePage.sm.getShapes(s => s.isTypeof('jadeNode'));
    if (nodes.length < 3) {
      return Promise.reject(graph.i18n.t('workflowAtLeast3Nodes'));
    }
    const validationPromises = nodes.map(s => s.validate(linkNodeSet).then(() => s.offError()).catch(error => {
      s.onError();
      return error;
    }));
    const results = await Promise.all(validationPromises);
    // 获取所有校验失败的信息
    const errors = results.filter(result => result && result.errorFields);
    if (errors.length > 0) {
      return Promise.reject(errors);
    }
    // 可选：.then()中可以获取校验的所有节点信息 Promise.resolve(results.filter(result => !errors.includes(result)))
    return Promise.resolve();
  };

  /**
   * 创建调试 runner.
   *
   * @param node 节点对象.
   * @return {{}} runner 对象.
   */
  self.createRunner = (node) => {
    if (!node.isActiveInFlow) {
      return inactiveNodeRunner(node);
    }
    if (node.isTypeof('conditionNodeCondition')) {
      return conditionRunner(node);
    }
    return standardRunner(node);
  };

  /**
   * @override.
   */
  const onPaste = self.onPaste;
  self.onPaste = (event) => {
    const shapeIds = onPaste.apply(self, [event]);
    self.triggerEvent({
      type: 'COPY_SHAPE',
      value: {
        shapeId: shapeIds.length > 0 ? shapeIds[0].shape.id : undefined,
      },
    });
    return shapeIds;
  };

  /**
   * @override.
   */
  const onDelete = self.onDelete;
  self.onDelete = () => {
    const beDeletedShapes = onDelete.apply(self, []);
    self.triggerEvent({
      type: 'DELETE_SHAPE',
      value: {
        shapeId: beDeletedShapes.length > 0 ? beDeletedShapes[0].id : undefined,
      },
    });
    return beDeletedShapes;
  };

  return self;
};

/**
 * jade流程的复制粘贴helper，去除text以及image等拷贝方式.
 *
 * @return {*}
 */
const jadeCopyPasteHelper = () => {
  const self = copyPasteHelper();
  self.handlers = [];
  self.handlers.push(new ElsaCopyHandler(self));
  self.shapeDataHelper = jadeShapeDataHelper;
  return self;
};

/**
 * @override
 * jade图形拷贝时的图形数据帮助器.
 *
 * @param data 待处理的图形数据.
 * @param targetPage 拷贝的目标page.
 * @param shapeDataArray 所有的图形数据所形成的数组.
 */
const jadeShapeDataHelper = (data, targetPage, shapeDataArray) => {
  const self = shapeDataHelper(data, targetPage, shapeDataArray);

  /**
   * @override
   */
  const preProcessShapeData = self.preProcessShapeData;
  self.preProcessShapeData = (plainText, idMap) => {
    preProcessShapeData.apply(self, [plainText]);
    const flowMeta = self.data.flowMeta;
    if (flowMeta) {
      let jsonString = JSON.stringify(flowMeta);
      idMap.forEach((value, key) => {
        const replaceString = `"referenceNode":"${key}"`;
        const replaceValue = `"referenceNode":"${value}"`;
        jsonString = jsonString.replace(replaceString, replaceValue);
      });
      self.data.flowMeta = JSON.parse(jsonString);
    }
  };

  return self;
};

/**
 * 存储Observable.
 *
 * @return {{}}
 * @constructor
 */
const ObservableStore = () => {
  const self = {};
  self.store = new Map();

  /**
   * 添加.
   *
   * @param props 相关属性.
   */
  self.add = (props) => {
    const {nodeId, observableId, value, type, parentId, selectable, visible} = props;
    const observableMap = getOrCreate(self.store, nodeId, () => new Map());
    const observable = getOrCreate(observableMap, observableId, () => {
      return {
        observableId, value: null, type: null, observers: [], parentId, selectable: selectable, visible,
      };
    });
    observable.value = value;
    observable.type = type;
    observable.parentId = parentId;
    observable.selectable = selectable;
    observable.visible = visible;
    if (observable.observers.length > 0) {
      observable.observers.forEach(observe => observe.notify({value: value, type: type}));
    }
  };

  /**
   * 删除.
   *
   * @param nodeId 节点id.
   * @param observableId 可被监听的id.
   */
  self.remove = (nodeId, observableId = null) => {
    if (observableId) {
      const observableMap = self.store.get(nodeId);
      if (observableMap) {
        if (observableId) {
          const observable = observableMap.get(observableId);
          observableMap.delete(observableId);
          if (observable) {
            observable.observers.forEach(o => {
              o.cleanObserve();
              o.stopObserve();
            });
          }
          if (observableMap.size === 0) {
            self.store.delete(nodeId);
          }
        }
      }
    } else {
      self.store.delete(nodeId);
    }
  };

  /**
   * 添加监听器.
   *
   * @param nodeId 被监听节点的id.
   * @param observableId 待监听的id.
   * @param observer 监听器.
   */
  self.addObserver = (nodeId, observableId, observer) => {
    const observableMap = getOrCreate(self.store, nodeId, () => new Map());
    const observable = getOrCreate(observableMap, observableId, () => {
      return {
        observableId, value: null, observers: [],
      };
    });
    observable.observers.push(observer);
  };

  /**
   * 删除监听器.
   *
   * @param nodeId 被监听节点的id.
   * @param observableId 待监听的id.
   * @param observer 监听器.
   */
  self.removeObserver = (nodeId, observableId, observer) => {
    const observableMap = self.store.get(nodeId);
    if (!observableMap) {
      return;
    }
    const observable = observableMap.get(observableId);
    if (!observable) {
      return;
    }
    const index = observable.observers.findIndex(o => o === observer);
    if (index !== -1) {
      observable.observers.splice(index, 1);
    }
  };

  const getOrCreate = (map, key, supplier) => {
    let value = map.get(key);
    if (!value) {
      value = supplier();
      map.set(key, value);
    }
    return value;
  };

  /**
   * 获取可被监听的列表.
   *
   * @param nodeId 节点id.
   * @return {*[]}
   */
  self.getObservableList = (nodeId) => {
    const observableMap = self.store.get(nodeId);
    if (!observableMap) {
      return [];
    }

    return Array.from(observableMap.values()).map(o => {
      return {
        nodeId,
        observableId: o.observableId,
        parentId: o.parentId,
        value: o.value,
        type: o.type,
        selectable: o.selectable,
        visible: o.visible,
      };
    });
  };

  /**
   * 获取单个observable.
   *
   * @param nodeId 被监听节点的id.
   * @param observableId 待监听的id.
   * @return {*|null}
   */
  self.getObservable = (nodeId, observableId) => {
    const observableMap = self.store.get(nodeId);
    return observableMap ? observableMap.get(observableId) : null;
  };

  /**
   * 清空.
   */
  self.clear = () => {
    self.store.clear();
  };

  return self;
};
