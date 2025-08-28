/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {getDefaultReference} from '@/components/util/ReferenceUtil.js';
import {
  DATA_TYPES,
  DEFAULT_ADD_TOOL_NODE_CONTEXT,
  DEFAULT_KNOWLEDGE_NODE_ACCESS_INFO,
  DEFAULT_KNOWLEDGE_NODE_RERANK_TOP_N,
  DEFAULT_KNOWLEDGE_REPO_GROUP_STRUCT,
  DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_KNOWLEDGE_CONFIG_ID,
  DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_EXTENSIONS,
  DEFAULT_LLM_KNOWLEDGE_BASES,
  DEFAULT_LLM_REFERENCE_OUTPUT,
  DEFAULT_MAX_MEMORY_ROUNDS,
  DEFAULT_MCP_SERVERS,
  END_NODE_TYPE,
  FLOW_TYPE,
  FROM_TYPE,
} from '@/common/Consts.js';
import {getEndNodeType} from '@/components/end/endNodeUtils.js';
import {pageProcessor} from '@/flow/pageProcessors.js';
import {FORM_TYPE} from '@/components/intelligentForm/Consts.js';

/**
 * page 兼容处理器.
 *
 * @param pageData 页面数据.
 * @param graph 画布对象.
 * @return {{}} 处理器.
 */
export const pageCompatibilityProcessor = (pageData, graph) => {
  const self = pageProcessor(pageData, graph);
  const shapes = pageData.shapes;
  const shapeMap = new Map();
  shapes.forEach(s => shapeMap.set(s.id, s));

  /**
   * @override
   */
  self.createShapeProcessor = (shapeData, g) => {
    switch (shapeData.type) {
      case 'endNodeEnd':
        return endNodeCompatibilityProcessor(shapeData, g, self);
      case 'llmNodeState':
        return llmCompatibilityProcessor(shapeData, g, self);
      case 'conditionNodeCondition':
        return conditionCompatibilityProcessor(shapeData, g, self);
      case 'startNodeStart':
        return startNodeCompatibilityProcessor(shapeData, g, self);
      case 'knowledgeRetrievalNodeState':
        return knowledgeRetrievalCompatibilityProcessor(shapeData, g, self);
      case 'questionClassificationNodeCondition':
        return questionClassificationCompatibilityProcessor(shapeData, g, self);
      case 'loopNodeState':
        return loopNodeCompatibilityProcessor(shapeData, g, self);
      case 'manualCheckNodeState':
        return manualCheckNodeCompatibilityProcessor(shapeData, g, self);
      case 'intelligentFormNodeState':
        return intelligentFormNodeCompatibilityProcessor(shapeData, g, self);
      default:
        return shapeCompatibilityProcessor(shapeData, g, self);
    }
  };

  /**
   * 通过条件获取图形数据列表.
   *
   * @param condition 条件.
   * @return {*} 图形数据列表.
   */
  self.getShapes = (condition) => {
    return shapes.filter(s => condition(s));
  };

  /**
   * 获取从源节点到目标节点之间的所有节点数据.
   *
   * @param srcData 源节点数据.
   * @param targetData 目标节点数据.
   * @return {*} 节点数据集合.
   */
  self.getNodesBetween = (srcData, targetData) => {
    const chain = new Set();
    chain.add(srcData);
    if (!traverse(srcData, targetData, chain)) {
      chain.clear();
    }
    return Array.from(chain);
  };

  const traverse = (shapeData, targetData, chain) => {
    if (shapeData === targetData) {
      return true;
    }
    const nextBatchData = getNextNodes(shapeData);
    if (nextBatchData.length === 0) {
      return false;
    }
    return nextBatchData.map(n => {
      chain.add(n);
      if (!traverse(n, targetData, chain)) {
        chain.delete(n);
        return false;
      }
      return true;
    }).reduce((acc, v) => acc || v, false);
  };

  const getNextNodes = (shapeData) => {
    const lines = self.getShapes(s => s.type === 'jadeEvent');
    const linkedLines = lines.filter(l => l.fromShape === shapeData.id);
    if (!linkedLines || linkedLines.length === 0) {
      return [];
    }
    return self.getShapes(s => s.type !== 'jadeEvent')
      .filter(s => linkedLines.some(l => l.toShape === s.id));
  };

  return self;
};

/**
 * 图形兼容处理器.
 *
 * @param shapeData 图形数据.
 * @param graph 画布对象.
 * @param pageHandler 页面处理器.
 * @return {{}} 处理器对象.
 */
export const shapeCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = {};
  self.shapeData = shapeData;
  self.graph = graph;
  self.pageProcessor = pageHandler;

  /**
   * 兼容性处理.
   */
  self.process = () => {
    if (self.shapeData.runnable === undefined || self.shapeData.runnable === null) {
      self.shapeData.runnable = true;
    }
  };

  return self;
};

/**
 * 条件节点兼容性处理器.
 *
 * @override
 */
export const conditionCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.flowMeta.conditionParams.branches.forEach(b => {
      if (b.runnable === undefined || b.runnable === null) {
        b.runnable = true;
      }
    });
  };

  return self;
};

/**
 * 问题分类节点兼容性处理器.
 *
 * @override
 */
export const questionClassificationCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.flowMeta.jober.converter.entity.inputParams.find(param => param.name === 'classifyQuestionParam').value.find(questionParam => questionParam.name === 'questionTypeList').value.forEach(b => {
      if (b.runnable === undefined || b.runnable === null) {
        b.runnable = true;
      }
    });
  };

  return self;
};

/**
 * 循环节点兼容性处理器.
 *
 * @override
 */
export const loopNodeCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    const jober = self.shapeData.flowMeta.jober;
    if (!jober.entity.params.exist(param => param.name === 'context')) {
       jober.entity.params.push({name: 'context'});
       jober.converter.entity.inputParams.push(JSON.parse(JSON.stringify(DEFAULT_ADD_TOOL_NODE_CONTEXT)));
    }
  };

  return self;
};

/**
 * 人工表单节点兼容性处理器.
 *
 * @override
 */
export const manualCheckNodeCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.type = 'intelligentFormNodeState';
    self.shapeData.componentName = 'intelligentFormComponent';
    const task = self.shapeData.flowMeta.task;
    if (!task.formType) {
      task.formType = FORM_TYPE.MANUAL;
    }
  };

  return self;
};

/**
 * 智能表单节点兼容性处理器.
 *
 * @override
 */
export const intelligentFormNodeCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    const task = self.shapeData.flowMeta.task;
    if (!task.formType) {
      task.formType = FORM_TYPE.ORCHESTRATION;
    }
  };

  return self;
};

/**
 * 开始节点兼容性处理器.
 *
 * @override
 */
export const startNodeCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);
  const i18n = graph.i18n;

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.deletable = false;
    self.shapeData.flowMeta.inputParams.find(inputParam => inputParam.name === 'input').value.forEach(item => {
      if (item.name === 'Question') {
        item.displayName = `${i18n?.t('userQuestion') ?? 'userQuestion'}`;
      }
      if (item.isRequired === undefined || item.isRequired === null) {
        item.isRequired = true;
      }
      if (item.isVisible === undefined || item.isVisible === null) {
        item.isVisible = item.isRequired;
      }
    });
  };

  return self;
};

/**
 * 结束节点兼容性处理器.
 *
 * @override
 */
export const endNodeCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    self.shapeData.deletable = true;
    if (graph.flowType === FLOW_TYPE.WORK_FLOW) {
      return;
    }
    const inputParams = self.shapeData.flowMeta.callback.converter.entity.inputParams;
    const mode = getEndNodeType(inputParams);
    if (mode === END_NODE_TYPE.MANUAL_CHECK) {
      return;
    }
    const inputParam = inputParams[0];
    addEndNodeEnableLog(inputParams);
    if (inputParam.from === FROM_TYPE.EXPAND) {
      inputParam.value.forEach(item => item.isRequired = true);
      return;
    }
    const prevInput = {...inputParam};
    const id = uuidv4();
    inputParam.id = uuidv4();
    inputParam.from = FROM_TYPE.EXPAND;
    inputParam.type = DATA_TYPES.OBJECT;
    inputParam.editable = false;
    inputParam.value = [getDefaultReference(id)];
    inputParam.isRequired = false;
    inputParam.referenceNode = '';
    inputParam.referenceKey = '';
    inputParam.referenceId = '';
    Object.keys(prevInput).forEach(k => {
      inputParam.value[0][k] = prevInput[k];
    });
    inputParam.value.forEach(item => item.isRequired = true);
    calculateLlmEnableLogStatus(inputParam);
  };

  const addEndNodeEnableLog = (inputParams) => {
    if (inputParams && !inputParams.some(item => item.name === 'enableLog')) {
      inputParams.push({
        id: uuidv4(),
        from: 'Input',
        name: 'enableLog',
        type: 'Boolean',
        value: false,
      });
    }
  };

  const calculateLlmEnableLogStatus = (inputParam) => {
    if (inputParam.from !== FROM_TYPE.EXPAND) {
      return;
    }
    const llmNodes = self.pageProcessor.getShapes(sd => sd.type === 'llmNodeState').map((n, i) => {
      return {index: i, data: n};
    });
    updateLlmFlowMetas(llmNodes);
  };

  const updateLlmFlowMetas = (llmNodes) => {
    llmNodes.forEach((v) => {
      const inputParams = v.data.flowMeta.jober.converter.entity.inputParams;
      const enableLog = inputParams.find(ip => ip.name === 'enableLog');
      if (!enableLog) {
        inputParams.push({
          id: uuidv4(),
          from: FROM_TYPE.INPUT,
          name: 'enableLog',
          type: DATA_TYPES.BOOLEAN,
          value: true,
        });
      }
    });
  };

  return self;
};

/**
 * 大模型节点兼容性处理器.
 *
 * @override
 */
export const llmCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  const moveWorkFlows2Tools = (inputParams) => {
    const workflows = inputParams.find(item => item.name === 'workflows');
    if (workflows && workflows.value.length > 0) {
      const tools = inputParams.find(item => item.name === 'tools');
      tools.value.push(...workflows.value);
      workflows.value = [];
    }
  };

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);
    const ensureParam = (params, defaultParam) => {
      const existingParam = params.find(i => i.name === defaultParam.name);
      if (!existingParam) {
        params.push(JSON.parse(JSON.stringify(defaultParam)));
      }
    };

    const addEnableLog = (inputParams) => {
      if (!inputParams.some(item => item.name === 'enableLog')) {
        inputParams.push({
          id: uuidv4(),
          from: FROM_TYPE.INPUT,
          name: 'enableLog',
          type: DATA_TYPES.BOOLEAN,
          value: true,
        });
      }
    }

    const inputParams = self.shapeData.flowMeta.jober.converter.entity.inputParams;
    const outputObject = self.shapeData.flowMeta.jober.converter.entity.outputParams.find(i => i.name === 'output');
    ensureParam(inputParams, DEFAULT_MAX_MEMORY_ROUNDS);
    ensureParam(inputParams, DEFAULT_LLM_KNOWLEDGE_BASES);
    ensureParam(inputParams, DEFAULT_MCP_SERVERS);
    ensureParam(outputObject.value, DEFAULT_LLM_REFERENCE_OUTPUT);
    addEnableLog(inputParams);
    if (!self.shapeData.flowMeta.jober.converter.entity.tempReference) {
      self.shapeData.flowMeta.jober.converter.entity.tempReference = {};
    }
    moveWorkFlows2Tools(inputParams);
  };

  return self;
};

/**
 * 知识检索节点兼容性处理器.
 *
 * @override
 */
export const knowledgeRetrievalCompatibilityProcessor = (shapeData, graph, pageHandler) => {
  const self = shapeCompatibilityProcessor(shapeData, graph, pageHandler);

  /**
   * @override
   */
  const process = self.process;
  self.process = () => {
    process.apply(self);

    const optionParamProcess = () => {
      const optionValue = self.shapeData.flowMeta.jober.converter.entity.inputParams
        .find(inputParam => inputParam.name === 'option')?.value;

      if (Array.isArray(optionValue) && !optionValue.some(v => v.name === 'groupId')) {
        optionValue.push(DEFAULT_KNOWLEDGE_REPO_GROUP_STRUCT);
      }

      if (Array.isArray(optionValue) && !optionValue.some(v => v.name === 'knowledgeConfigId')) {
        optionValue.push(JSON.parse(JSON.stringify(DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_KNOWLEDGE_CONFIG_ID)));
      }

      if (Array.isArray(optionValue)) {
        const rerankParamValue = optionValue.find(v => v.name === 'rerankParam')?.value;
        if (!rerankParamValue.some(v => v.name === 'accessInfo')) {
          rerankParamValue.push(DEFAULT_KNOWLEDGE_NODE_ACCESS_INFO);
          rerankParamValue.push(DEFAULT_KNOWLEDGE_NODE_RERANK_TOP_N);
        }
      }

      if (Array.isArray(optionValue) && !optionValue.some(v => v.name === 'extensions')) {
        optionValue.push(DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_EXTENSIONS);
      }
    };

    const userIdParamProcess = () => {
      const inputParams = self.shapeData.flowMeta.jober.converter.entity.inputParams;
      const userIdInputParamIndex = inputParams.findIndex(inputParam => inputParam.name === 'userId');
      if (userIdInputParamIndex !== -1) {
        inputParams.splice(userIdInputParamIndex, 1);
      }
      const entityParams = self.shapeData.flowMeta.jober.entity.params;
      const userIdIndex = entityParams.findIndex(param => param.name === 'userId');
      if (userIdIndex !== -1) {
        entityParams.splice(userIdIndex, 1);
      }
    };

    optionParamProcess();
    userIdParamProcess();
  };

  return self;
};