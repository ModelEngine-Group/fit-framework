/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {defaultGraph} from '@fit-elsa/elsa-core';
import {jadeFlowPage} from '@/flow/jadeFlowPage.js';
import {jadeEvent} from '@/components/jadeEvent.jsx';
import {taskNode} from '@/components/testDemo/taskNode.jsx';
import {endNodeEnd} from '@/components/end/endNodeEnd.jsx';
import {endComponent} from '@/components/end/endComponent.jsx';
import {retrievalNodeState} from '@/components/retrieval/retrievalNodeState.jsx';
import {retrievalComponent} from '@/components/retrieval/retrievalComponent.jsx';
import {testNode} from '@/components/replaceDemo/testNode.jsx';
import {testComponent} from '@/components/replaceDemo/testComponent.jsx';
import {replaceComponent} from '@/components/replaceDemo/replaceComponent.jsx';
import {startNodeStart} from '@/components/start/startNodeStart.jsx';
import {startComponent} from '@/components/start/startComponent.jsx';
import {llmNodeState} from '@/components/llm/llmNodeState.jsx';
import {llmComponent} from '@/components/llm/llmComponent.jsx';
import {manualCheckNodeState} from '@/components/manualCheck/manualCheckNodeState.jsx';
import {manualCheckComponent} from '@/components/manualCheck/manualCheckComponent.jsx';
import {fitInvokeNodeState} from '@/components/fitInvokeNode/fitInvokeNodeState.jsx';
import {fitInvokeComponent} from '@/components/fitInvokeNode/fitInvokeComponent.jsx';
import {toolInvokeComponent} from '@/components/toolInvokeNode/toolInvokeComponent.jsx';
import {toolInvokeNodeState} from '@/components/toolInvokeNode/toolInvokeNodeState.jsx';
import {conditionNodeCondition} from '@/components/condition/conditionNodeCondition.jsx';
import {conditionComponent} from '@/components/condition/conditionComponent.jsx';
import {huggingFaceNodeState} from '@/components/huggingFace/huggingFaceNodeState.jsx';
import {huggingFaceComponent} from '@/components/huggingFace/huggingFaceComponent.jsx';
import {codeComponent} from '@/components/code/codeComponent.jsx';
import {codeNodeState} from '@/components/code/codeNodeState.jsx';
import {compatibilityManager} from '@/flow/compatibility/compatibilities.js';
import {knowledgeRetrievalNodeState} from '@/components/knowledgeRetrieval/knowledgeRetrievalNodeState.jsx';
import {knowledgeRetrievalComponent} from '@/components/knowledgeRetrieval/knowledgeRetrievalComponent.jsx';
import {queryOptimizationComponent} from '@/components/queryOptimization/queryOptimizationComponent.jsx';
import {queryOptimizationNodeState} from '@/components/queryOptimization/queryOptimizationNodeState.jsx';
import {textExtractionNodeState} from '@/components/textExtraction/textExtractionNodeState.jsx';
import {textExtractionComponent} from '@/components/textExtraction/textExtractionComponent.jsx';
import {questionClassificationNodeCondition} from '@/components/questionClassification/questionClassificationNodeCondition.jsx';
import {questionClassificationComponent} from '@/components/questionClassification/questionClassificationComponent.jsx';
import {variableAggregationNodeState} from '@/components/variableAggregation/variableAggregationNodeState.jsx';
import {variableAggregationComponent} from '@/components/variableAggregation/variableAggregationComponent.jsx';
import {appNodeState} from '@/components/appNode/appNodeState.jsx';
import {appComponent} from '@/components/appNode/appComponent.jsx';
import {systemEnv} from '@/shapes/systemEnv.jsx';
import {httpComponent} from '@/components/httpNode/httpComponent.jsx';
import {httpNodeState} from '@/components/httpNode/httpNodeState.jsx';
import {fileExtractionNodeState} from '../components/fileExtraction/fileExtractionNodeState.jsx';
import {fileExtractionComponent} from '../components/fileExtraction/fileExtractionComponent.jsx';
import {textToImageNodeState} from '@/components/textToImage/textToImageNodeState.jsx';
import {textToImageComponent} from '@/components/textToImage/textToImageComponent.jsx';
import {noteNode} from '@/components/note/noteNode.jsx';
import {noteComponent} from '@/components/note/noteComponent.jsx';
import {loopNodeState} from '@/components/loopNode/loopNodeState.jsx';
import {loopComponent} from '@/components/loopNode/loopComponent.jsx';
import {intelligentFormNodeState} from '@/components/intelligentForm/intelligentFormNodeState.jsx';
import {intelligentFormComponent} from '@/components/intelligentForm/intelligentFormComponent.jsx';
import {parallelNodeState} from '@/components/parallelNode/parallelNodeState.jsx';
import {parallelComponent} from '@/components/parallelNode/parallelComponent.jsx';

/**
 * jadeFlow的专用画布.
 *
 * @param div dom元素.
 * @param title 名称.
 */
export const jadeFlowGraph = (div, title) => {
  const self = defaultGraph(div, title);
  self.type = 'jadeFlowGraph';
  self.pageType = 'jadeFlowPage';
  self.enableText = false;
  self.flowMeta = {
    exceptionFitables: ['modelengine.fit.jober.aipp.fitable.AippFlowExceptionHandler'],
    enableOutputScope: true,
  };
  self.setting.borderColor = '#047bfc';
  self.setting.focusBorderColor = '#047bfc';
  self.setting.mouseInBorderColor = '#047bfc';
  self.setting.allNodeNumLimit = 99;
  self.setting.sameTypeNodeNumLimit = 19;
  self.compatibilityManager = compatibilityManager();

  /**
   * 添加jade前缀.
   *
   * @returns {`jade${string}`} id值.
   */
  const uuid = self.uuid;
  self.uuid = () => {
    const id = uuid.apply(self);
    return `jade${id}`;
  };

  /**
   * 序列化新增flowMeta字段.
   *
   * @override
   */
  const serialize = self.serialize;
  self.serialize = () => {
    const serialized = serialize.apply(self);
    serialized.flowMeta = self.flowMeta;
    return serialized;
  };

  /**
   * 导入flow相关依赖.
   *
   * @override
   */
  const initialize = self.initialize;
  self.initialize = async () => {
    self.registerPlugin('jadeFlowPage', jadeFlowPage);
    self.registerPlugin('jadeEvent', jadeEvent);
    self.registerPlugin('taskNode', taskNode);
    self.registerPlugin('endNodeEnd', endNodeEnd);
    self.registerPlugin('endComponent', endComponent);
    self.registerPlugin('retrievalNodeState', retrievalNodeState);
    self.registerPlugin('retrievalComponent', retrievalComponent);
    self.registerPlugin('testNode', testNode);
    self.registerPlugin('testComponent', testComponent);
    self.registerPlugin('replaceComponent', replaceComponent);
    self.registerPlugin('startNodeStart', startNodeStart);
    self.registerPlugin('startComponent', startComponent);
    self.registerPlugin('llmNodeState', llmNodeState);
    self.registerPlugin('llmComponent', llmComponent);
    self.registerPlugin('manualCheckNodeState', manualCheckNodeState);
    self.registerPlugin('manualCheckComponent', manualCheckComponent);
    self.registerPlugin('fitInvokeNodeState', fitInvokeNodeState);
    self.registerPlugin('toolInvokeNodeState', toolInvokeNodeState);
    self.registerPlugin('fitInvokeComponent', fitInvokeComponent);
    self.registerPlugin('toolInvokeComponent', toolInvokeComponent);
    self.registerPlugin('conditionNodeCondition', conditionNodeCondition);
    self.registerPlugin('conditionComponent', conditionComponent);
    self.registerPlugin('huggingFaceNodeState', huggingFaceNodeState);
    self.registerPlugin('huggingFaceComponent', huggingFaceComponent);
    self.registerPlugin('codeComponent', codeComponent);
    self.registerPlugin('codeNodeState', codeNodeState);
    self.registerPlugin('knowledgeRetrievalNodeState', knowledgeRetrievalNodeState);
    self.registerPlugin('knowledgeRetrievalComponent', knowledgeRetrievalComponent);
    self.registerPlugin('queryOptimizationComponent', queryOptimizationComponent);
    self.registerPlugin('queryOptimizationNodeState', queryOptimizationNodeState);
    self.registerPlugin('textExtractionNodeState', textExtractionNodeState);
    self.registerPlugin('textExtractionComponent', textExtractionComponent);
    self.registerPlugin('questionClassificationNodeCondition', questionClassificationNodeCondition);
    self.registerPlugin('httpNodeState', httpNodeState);
    self.registerPlugin('questionClassificationComponent', questionClassificationComponent);
    self.registerPlugin('systemEnv', systemEnv);
    self.registerPlugin('httpComponent', httpComponent);
    self.registerPlugin('variableAggregationNodeState', variableAggregationNodeState);
    self.registerPlugin('variableAggregationComponent', variableAggregationComponent);
    self.registerPlugin('appNodeState', appNodeState);
    self.registerPlugin('appComponent', appComponent);
    self.registerPlugin('fileExtractionNodeState', fileExtractionNodeState);
    self.registerPlugin('fileExtractionComponent', fileExtractionComponent);
    self.registerPlugin('textToImageNodeState', textToImageNodeState);
    self.registerPlugin('textToImageComponent', textToImageComponent);
    self.registerPlugin('noteNode', noteNode);
    self.registerPlugin('noteComponent', noteComponent);
    self.registerPlugin('loopNodeState', loopNodeState);
    self.registerPlugin('loopComponent', loopComponent);
    self.registerPlugin('intelligentFormNodeState', intelligentFormNodeState);
    self.registerPlugin('intelligentFormComponent', intelligentFormComponent);
    self.registerPlugin('parallelNodeState', parallelNodeState);
    self.registerPlugin('parallelComponent', parallelComponent);
    return initialize.apply(self);
  };

  /**
   * 注册plugin.
   *
   * @param pluginName 插件名称.
   * @param plugin 组件.
   * @param namespace 命名空间.
   */
  self.registerPlugin = (pluginName, plugin, namespace = null) => {
    if (namespace) {
      self.plugins[`${namespace}.${pluginName}`] = plugin;
    } else {
      self.plugins[pluginName] = plugin;
    }
  };

  /**
   * 数据发生变化时触发.
   *
   * @param data 数据.
   * @param dirtyAction 数据变化的action.
   */
  const dirtied = self.dirtied;
  self.dirtied = (data, dirtyAction) => {
    dirtied.call(self, data, dirtyAction);
    if (self.activePage.isRunning) {
      return;
    }
    self.onChangeCallback && self.onChangeCallback(dirtyAction);
  };

  /**
   * 获取对应节点的配置.
   *
   * @param shape 图形.
   * @return {*} 配置信息.
   */
  self.getConfig = (shape) => {
    let chain = shape.typeChain;
    while (chain !== null) {
      if (chain.type) {
        const config = self.configs?.find(config => config.node === chain.type);
        if (config) {
          return config;
        }
      }
      chain = chain.parent;
    }
    return null;
  };

  /**
   * 编辑流.
   *
   * @param index 页数.
   * @param div dom对象.
   * @param id 唯一标识.
   */
  self.editFlow = async (index, div, id) => {
    const pageData = self.getPageData(index);
    self.compatibilityManager.compatibleWith(pageData, self);
    return self.edit(index, div, id);
  };

  return self;
};
