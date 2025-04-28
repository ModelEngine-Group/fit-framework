/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeFlowGraph} from './jadeFlowGraph.js';
import {jadeEvaluationGraph} from '@/flow/evaluation/jadeEvaluationGraph.js';
import {ShapeDataValidationProcessor} from '@/data/ShapeDataValidationProcessor.js';
import {SYSTEM_ACTION} from '@/common/Consts.js';

/**
 * react流程代码，对外暴露接口，以便对流程进行操作以及获取数据.
 */
const jadeFlowAgent = (graph) => {
    const self = {};
    self.graph = graph;

    /**
     * 添加图形.
     *
     * @param shapeType 图形类型.
     * @param properties 初始化属性.
     */
    self.want = (shapeType, properties) => {
        graph.activePage.want(shapeType, properties);
    };

    /**
     * 导入用户自定义组件.
     *
     * @param importStatement import语句.
     * @return {*}
     */
    self.import = (importStatement) => {
        return self.graph.staticImport(importStatement);
    };

    /**
     * 序列化数据.
     *
     * @returns {*} 流程的全量序列化数据.
     */
    self.serialize = () => {
        graph.activePage.serialize();
        return graph.serialize();
    };

    /**
     * 获取流程试运行入参元数据
     */
    self.getFlowRunInputMetaData = () => {
        return graph.activePage.sm.findShapeBy(s => s.type === 'startNodeStart').getRunInputParams();
    };

    /**
     * 运行流程.
     *
     * @return {*} 通过refresh刷新流程状态，通过reset重置流程，通过stop结束流程.
     */
    self.run = () => {
        const nodes = graph.activePage.testRun();
        return {
            // 刷新流程节点状态.
            refresh: (dataList) => graph.activePage.refreshRun(nodes, dataList),
            // 重置流程节点装填.
            reset: () => graph.activePage.resetRun(nodes),
            // 结束运行.
            stop: (dataList) => graph.activePage.stopRun(nodes, dataList)
        };
    };

    /**
     * 获取可创建的节点列表.
     */
    self.getAvailableNodes = () => {
        return [
            {type: 'retrievalNodeState', name: '数据检索'},
            {type: 'llmNodeState', name: '大模型'},
            {type: 'manualCheckNodeState', name: '人工检查'},
            {type: 'fitInvokeNodeState', name: 'FIT调用'},
        ];
    };

    /**
     * 创建节点.
     *
     * @param type 节点类型.
     * @param e 鼠标事件.
     * @param metaData 元数据
     */
    self.createNode = (type, e, metaData) => {
        const position = graph.activePage.calculatePosition(e);
        graph.activePage.createNew(type, position.x, position.y, null, null, null, null, null, metaData);
    };

    /**
     * 创建工具节点
     *
     * @param e 鼠标事件.
     * @param schemaData schema元数据
     */
    self.createTool = (e, schemaData) => {
        const position = graph.activePage.calculatePosition(e);
        self.createToolByPosition(position, schemaData);
    };

    /**
     * 创建节点.
     *
     * @param type 节点类型.
     * @param position 坐标.
     * @param metaData 元数据
     */
    self.createNodeByPosition = (type, position, metaData) => {
        graph.activePage.createNew(type, position.x, position.y, null, null, null, null, null, metaData);
    };

    /**
     * 创建工具节点
     *
     * @param position 坐标.
     * @param schemaData schema元数据
     */
    self.createToolByPosition = (position, schemaData) => {
        graph.activePage.createNew('toolInvokeNodeState', position.x, position.y, null, null, null, null, null, schemaData);
    };

  /**
   * 画布发生变化时触发.
   *
   * @param callback 回调函数.
   */
  self.onChange = (callback) => {
    graph.onChangeCallback = (dirtyAction) => {
      if (!graph.validateInfo || dirtyAction.action !== SYSTEM_ACTION.JADE_NODE_CONFIG_CHANGE) {
        callback(dirtyAction);
        return;
      }
      const indicesToRemove = [];
      graph.validateInfo.forEach((v, i) => {
        const shape = graph.activePage.getShapeById(v.nodeId);
        if (!shape) {
          indicesToRemove.push(i);
          return;
        }
        const validationProcessor = ShapeDataValidationProcessor(shape);
        for (let j = v.configChecks.length - 1; j >= 0; j--) {
          const c = v.configChecks[j];
          if (!validationProcessor.isValidationInfoValid(c)) {
            v.configChecks.splice(j, 1);
          }
        }
        if (v.configChecks.length === 0) {
          indicesToRemove.push(i);
        }
      });
      // 从后往前删除标记的索引
      for (let i = indicesToRemove.length - 1; i >= 0; i--) {
        graph.validateInfo.splice(indicesToRemove[i], 1);
      }
      callback(dirtyAction);
    };
  };

    /**
     * 获取所有节点的配置数据.
     * 返回格式: {
     *     'nodeId': [] // 这里的数组是每个节点的配置数据.
     * }
     *
     * @return {*} 返回配置数据.
     */
    self.getNodeConfigs = () => {
        return graph.activePage.sm.getShapes(s => s.isTypeof('jadeNode')).map(s => {
            return {
                [s.id]: s.drawer.getLatestJadeConfig()
            };
        });
    };

    /**
     * 校验所有节点数据是否合法.
     *
     * @return Promise 校验结果
     */
    self.validate = async (validateInfo, refresh) => {
        const createRecursiveProxy = (target, re) => {
            if (typeof target === 'object' && target !== null) {
                return new Proxy(target, {
                    get(tar, prop) {
                        const value = tar[prop];
                        // 如果属性值是对象或数组，递归包装
                        if (typeof value === 'object' && value !== null) {
                            return createRecursiveProxy(value, re);
                        }
                        return value;
                    },
                    set(tar, prop, value) {
                        tar[prop] = value;
                        re(self.graph.validateInfo); // 触发刷新
                        return true;
                    },
                });
            }
            return target;
        };

        if (validateInfo && refresh) {
            self.graph.validateInfo = createRecursiveProxy(validateInfo, refresh);
        }
        return await graph.activePage.validate();
    };

    /**
     * 添加事件监听并执行回调函数.
     *
     * @param eventType 事件类型.
     * @param callback 回调函数.
     */
    const addSelectEventListener = (eventType, callback) => {
        graph.activePage.addEventListener(eventType, (event) => {
            callback(event);
        });
    };

    /**
     * 当需要触发模型选择时的回调.
     *
     * @param callback 回调函数.
     */
    self.onModelSelect = (callback) => {
        addSelectEventListener('SELECT_MODEL', callback);
    };

    /**
     * 当点击创建测试集按钮时的回调.
     *
     * @param callback 回调函数.
     */
    self.onCreateButtonClick = (callback) => {
        addSelectEventListener('CREATE_TEST_SET', callback);
    };

    /**
     * 当点击导入工具按钮时的回调.
     *
     * @param callback 回调函数.
     */
    self.onImportButtonClick = (callback) => {
        addSelectEventListener('SELECT_TOOL', callback);
    };

    /**
     * 当需要触发知识库选择时的回调.
     *
     * @param callback 回调函数.
     */
    self.onKnowledgeBaseSelect = (callback) => {
        addSelectEventListener('SELECT_KNOWLEDGE_BASE', callback);
    };

    /**
     * 当需要触发插件选择时的回调.
     *
     * @param callback 回调函数.
     */
    self.onPluginSelect = (callback) => {
        addSelectEventListener('SELECT_PLUGIN', callback);
    };

    /**
     * 插件选择框中选择了某个工具、工具流或者智能体.
     *
     * @param callback 回调函数.
     */
    self.onToolSelect = (callback) => {
        addSelectEventListener('SELECT_SKILL', callback);
    };

  /**
   * 循环节点选择框中选择了某个工具、工具流.
   *
   * @param callback 回调函数.
   */
  self.onLoopSelect = (callback) => {
    addSelectEventListener('SELECT_LOOP_PLUGIN', callback);
  };

  /**
   * 并行节点选择框中选择了某些工具、工具流.
   *
   * @param callback 回调函数.
   */
  self.onParallelSelect = (callback) => {
    addSelectEventListener('SELECT_PARALLEL_PLUGINS', callback);
  };

    /**
     * 当需要触发搜索参数配置时的回调.
     *
     * @param callback 回调函数.
     */
    self.onKnowledgeSearchArgsSelect = (callback) => {
        addSelectEventListener('KNOWLEDGE_SEARCH_ARGS_EVENT', callback);
    };

    /**
     * 监听某个事件.
     *
     * @param event 事件类型.
     * @param listener 监听器.
     */
    self.listen = (event, listener) => {
        graph.activePage.addEventListener(event, (e) => {
            listener(e);
        });
    };

    /**
     * 将画布中心移至某个图形处.
     *
     * @param shapeId 需要聚焦到的图形id.
     */
    self.scrollToShape = (shapeId) => {
        graph.activePage.shapes.find(s => s.id === shapeId).toScreenCenter();
    };

    /**
     * 当需要触发表单选择时的回调.
     *
     * @param callback 回调函数.
     */
    self.onFormSelect = (callback) => {
        addSelectEventListener('SELECT_FORM_BASE', callback);
    };

  /**
   * 当需要销毁agent时调用.
   */
  self.destroy = () => {
    graph.destroy();
  };

    return self;
};

/**
 * Aipp流程对外接口.
 */
export const JadeFlow = (() => {
    const self = {};

    /**
     * 新建流程.
     *
     * @param div 待渲染的dom元素.
     * @param tenant 租户.
     * @param configs 传入的其他参数列表.
     */
    self.new = async (div, tenant, configs) => {
        const graphDom = getGraphDom(div);
        const g = jadeFlowGraph(graphDom, 'jadeFlow');
        g.configs = configs;
        g.collaboration.mute = true;
        g.tenant = tenant;
        await g.initialize();
        const page = g.addPage('newFlowPage');

        // 新建的默认创建出start、end和一个连线
        const start = page.createShape('startNodeStart', 100, 100);
        const end = page.createShape('endNodeEnd', start.x + start.width + 200, 100);
        const jadeEvent = page.createNew('jadeEvent', 0, 0);
        page.reset();

        // reset完成之后进行connect操作.
        jadeEvent.connect(start.id, 'E', end.id, 'W');

        page.fillScreen();
        return jadeFlowAgent(g);
    };

    /**
     * 编辑流程.
     *
     * @param div 待渲染的dom元素.
     * @param tenant 租户.
     * @param appId appId
     * @param flowConfigData 流程元数据.
     * @param configs 传入的其他参数列表.
     * @param i18n 传入的多语言翻译组件.
     * @param importStatements 传入的需要加载的语句.
     * @param flowType 流程类型.
     */
    self.edit = async ({
                           div,
                           tenant,
                           appId,
                           flowConfigData,
                           configs,
                           i18n,
                           importStatements = [],
                           flowType = 'app',
                       }) => {
        const graphDom = getGraphDom(div);
        const g = jadeFlowGraph(div, 'jadeFlow');
        await configGraph(g, tenant, appId, flowConfigData, configs, i18n, importStatements);
        g.flowType = flowType;
        const pageData = g.getPageData(0);
        await g.editFlow(0, graphDom, pageData.id);
        await g.activePage.awaitShapesRendered();
        return jadeFlowAgent(g);
    };

    /**
     * 评估流程.
     *
     * @param div 待渲染的dom元素.
     * @param tenant 租户.
     * @param appId appId.
     * @param flowConfigData 流程元数据.
     * @param isPublished 是否已发布.
     * @param configs 传入的其他参数列表.
     * @param i18n 传入的多语言翻译组件.
     * @param importStatements 传入的需要加载的语句.
     * @return {Promise<{}>} JadeFlowAgent代理.
     */
    self.evaluate = async (div, tenant, appId, flowConfigData, isPublished, configs, i18n, importStatements = []) => {
        const graphDom = getGraphDom(div);
        const g = jadeEvaluationGraph(graphDom, 'jadeEvaluation');
        await configGraph(g, tenant, appId, flowConfigData, configs, i18n, importStatements);
        await g.evaluate(flowConfigData, isPublished);
        await g.activePage.awaitShapesRendered();
        return jadeFlowAgent(g);
    };

    const configGraph = async (g, tenant, appId, flowConfigData, configs, i18n, importStatements) => {
        g.collaboration.mute = true;
        g.configs = configs;
        g.i18n = i18n;
        for (let i = 0; i < importStatements.length; i++) {
            await g.dynamicImportStatement(importStatements[i]);
        }
        await g.initialize();
        g.deSerialize(flowConfigData);
        g.tenant = tenant;
        g.appId = appId;
        return g;
    };

    /**
     * 创建一个新的用于承载elsa graph内容的dom元素
     *
     * @param parentDom 父dom
     * @return {HTMLElement|HTMLDivElement}
     */
    const getGraphDom = (parentDom) => {
        const graphDom = document.getElementById('elsa-graph');
        if (graphDom) {
            return graphDom;
        } else {
            const newGraphDom = document.createElement('div');
            newGraphDom.style.width = `${parentDom.clientWidth}px`;
            newGraphDom.style.height = `${parentDom.clientHeight}px`;
            newGraphDom.style.position = 'relative';
            newGraphDom.id = 'elsa-graph';
            parentDom.appendChild(newGraphDom);
            return newGraphDom;
        }
    };

    return self;
})();
