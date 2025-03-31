/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import FlowMeta from '@/components/util/FlowMeta.js';

/**
 * 图形数据提取器.
 *
 * @param shape 图形数据.
 * @return {{}} 图形数据提取器.
 */
export const ShapeDataValidationProcessor = (shape) => {
  const SHAPE_EXTRACTOR = {
    endNodeEnd: endNodeDataValidationProcessor,
    huggingFaceNodeState: huggingFaceDataValidationProcessor,
    knowledgeRetrievalNodeState: knowledgeRetrievalNodeDataValidationProcessor,
    llmNodeState: llmNodeDataValidationProcessor,
    manualCheckNodeState: manualCheckDataValidationProcessor,
    retrievalNodeState: retrievalNodeDataValidationProcessor,
    toolInvokeNodeState: toolDataValidationProcessor,
    default: normalDataValidationProcessor,
  };

  // 根据 type 创建对应的提取器
  const extractorClass = SHAPE_EXTRACTOR[shape.type] || SHAPE_EXTRACTOR.default;
  return extractorClass(shape);
};

/**
 * 结束节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const endNodeDataValidationProcessor = (shape) => {
  const self = normalDataValidationProcessor(shape);

  const getEndFormIdObject = () => {
    const flowMeta = new FlowMeta(self.shape?.flowMeta);
    return flowMeta.callbackInput()?.find(inputParam => inputParam?.name === 'endFormId') ?? undefined;
  };

  /**
   * @override
   */
  self.extractValidationInfo = () => {
    const endFormId = getEndFormIdObject();
    if (!endFormId || !endFormId.value) {
      return undefined;
    }
    return [{
      configCheckId: endFormId.id,
      configName: 'formId',
      formId: endFormId.value,
    }];
  };

  /**
   * @override
   */
  self.isValidationInfoValid = (validationInfo) => {
    return validationInfo.formId === self.shape?.drawer?.getLatestJadeConfig()?.inputParams?.find(inputParam => inputParam?.name === 'endFormId')?.value ?? undefined;
  };

  return self;
};

/**
 * 人工检查节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const manualCheckDataValidationProcessor = (shape) => {
  const self = normalDataValidationProcessor(shape);

  const getFormId = () => {
    return self.shape?.flowMeta?.task?.taskId ?? undefined;
  };

  /**
   * @override
   */
  self.extractValidationInfo = () => {
    const formId = getFormId();
    if (!formId) {
      return undefined;
    }
    return [{
      configCheckId: self.shape.id,
      configName: 'formId',
      formId: formId,
    }];
  };

  /**
   * @override
   */
  self.isValidationInfoValid = (validationInfo) => {
    return validationInfo.formId === self.shape?.drawer?.getLatestJadeConfig()?.taskId ?? undefined;
  };

  return self;
};

/**
 * 大模型节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const llmNodeDataValidationProcessor = (shape) => {
  const self = normalDataValidationProcessor(shape);

  const getAccessInfoValidationInfo = (accessInfo) => {
    if (!accessInfo || !accessInfo.value) {
      return [];
    }
    return [{
      configCheckId: accessInfo.id,
      configName: accessInfo.name,
      ...accessInfo.value.reduce((acc, item) => {
        acc[item.name] = item.value;
        return acc;
      }, {}),
    }];
  };

  const getToolValidationInfo = (tools) => {
    if (tools.length === 0) {
      return [];
    }
    return tools.map(tool => {
      return {
        configCheckId: tool.id,
        configName: 'plugin',
        uniqueName: tool.value,
        name: tool.name,
        version: tool.version,
        tags: tool.tags,
      };
    });
  };

  /**
   * @override
   */
  self.extractValidationInfo = () => {
    const flowMeta = new FlowMeta(self.shape?.flowMeta);
    const accessInfo = flowMeta.joberInput()?.find(inputParam => inputParam?.name === 'accessInfo') ?? undefined;
    const accessInfoValidationInfo = getAccessInfoValidationInfo(accessInfo);
    const tools = [];
    const toolsInfo = flowMeta.joberInput()?.find(inputParam => inputParam?.name === 'tools') ?? undefined;
    if (toolsInfo && toolsInfo.value.length > 0) {
      tools.push(...toolsInfo.value);
    }
    const workflowsInfo = flowMeta.joberInput()?.find(inputParam => inputParam?.name === 'workflows') ?? undefined;
    if (workflowsInfo && workflowsInfo.value.length > 0) {
      tools.push(...workflowsInfo.value);
    }
    const toolValidationInfo = getToolValidationInfo(tools);
    return [...accessInfoValidationInfo, ...toolValidationInfo];
  };

  const isAccessInfoValid = (validationInfo) => {
    const config = self.shape?.drawer?.getLatestJadeConfig()?.inputParams?.find(inputParam => inputParam?.id === validationInfo.configCheckId) ?? undefined;
    return (config && config?.value?.find(item => item.name === 'serviceName')?.value === validationInfo.serviceName && config?.value?.find(item => item.name === 'tag')?.value === validationInfo.tag);
  };

  const isToolInfoValid = (validationInfo) => {
    const toolNameSet = new Set(['tools', 'workflows']);
    const config = self.shape?.drawer?.getLatestJadeConfig()?.inputParams?.filter(inputParam => toolNameSet.has(inputParam?.name))
      .map(inputParam => inputParam?.value);
    const mergedArray = [].concat(...config);
    return mergedArray.find(item => item.value === validationInfo.uniqueName);
  };

  /**
   * @override
   */
  self.isValidationInfoValid = (validationInfo) => {
    switch (validationInfo.configName) {
      case 'accessInfo':
        return isAccessInfoValid(validationInfo);
      case 'plugin':
        return isToolInfoValid(validationInfo);
      default:
        throw new Error(`Unsupported configName [${validationInfo.configName}]`);
    }
  };

  return self;
};

/**
 * 检索节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const retrievalNodeDataValidationProcessor = (shape) => {
  const self = normalDataValidationProcessor(shape);

  /**
   * @override
   */
  self.extractValidationInfo = () => {
    const flowMeta = new FlowMeta(self.shape?.flowMeta);
    const knowledge = flowMeta.joberInput()?.find(inputParam => inputParam?.name === 'knowledge') ?? undefined;
    if (!knowledge || !knowledge.value) {
      return undefined;
    }
    return knowledge.value.filter(v => v.value.length > 0).map(v => {
      return {
        configCheckId: v.id,
        configName: knowledge.name,
        ...v.value.reduce((acc, item) => {
          acc[item.name] = item.value;
          return acc;
        }, {}),
      };
    });
  };

  /**
   * @override
   */
  self.isValidationInfoValid = (validationInfo) => {
    const config = self.shape?.drawer?.getLatestJadeConfig()?.inputParams?.find(inputParam => inputParam?.name === 'knowledge')?.value?.find(v => v?.id === validationInfo.configCheckId) ?? undefined;
    return config && String(config?.value?.find(item => item.name === 'repoId')?.value) === validationInfo.repoId && String(config?.value?.find(item => item.name === 'tableId')?.value) === validationInfo.tableId && config?.value?.find(item => item.name === 'name')?.value === validationInfo.name;
  };

  return self;
};

/**
 * 知识检索节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const knowledgeRetrievalNodeDataValidationProcessor = (shape) => {
  const self = normalDataValidationProcessor(shape);

  /**
   * @override
   */
  self.extractValidationInfo = () => {
    const flowMeta = new FlowMeta(self.shape?.flowMeta);
    const knowledgeRepos = flowMeta.joberInput()?.find(inputParam => inputParam?.name === 'knowledgeRepos') ?? undefined;
    if (!knowledgeRepos || !knowledgeRepos.value) {
      return undefined;
    }
    return knowledgeRepos.value.filter(v => v.value.length > 0).map(v => {
      return {
        configCheckId: v.id,
        configName: knowledgeRepos.name,
        ...v.value.reduce((acc, item) => {
          acc[item.name] = item.value;
          return acc;
        }, {}),
      };
    });
  };

  /**
   * @override
   */
  self.isValidationInfoValid = (validationInfo) => {
    const config = self.shape?.drawer?.getLatestJadeConfig()?.inputParams?.find(inputParam => inputParam?.name === 'knowledgeRepos')?.value?.find(v => v?.id === validationInfo.configCheckId) ?? undefined;
    return config && String(config?.value?.find(item => item.name === 'id')?.value) === validationInfo.id && config?.value?.find(item => item.name === 'name')?.value === validationInfo.name;
  };

  return self;
};

/**
 * 工具节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const toolDataValidationProcessor = (shape) => {
  const self = normalDataValidationProcessor(shape);

  /**
   * @override
   */
  self.extractValidationInfo = () => {
    const uniqueName = self.shape?.flowMeta?.jober?.entity?.uniqueName ?? undefined;
    if (!uniqueName) {
      return undefined;
    }
    return [{
      configCheckId: self.shape.id,
      configName: self.shape.type,
      uniqueName: uniqueName,
    }];
  };

  /**
   * @override
   */
  self.isValidationInfoValid = (validationInfo) => {
    return validationInfo.configCheckId === self.shape?.id ?? undefined;
  };

  return self;
};

/**
 * huggingFace节点数据提取器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const huggingFaceDataValidationProcessor = (shape) => {
  return toolDataValidationProcessor(shape);
};

/**
 * 普通节点处理器.
 *
 * @param shape 节点信息。
 * @return {{}}
 */
const normalDataValidationProcessor = (shape) => {
  const self = {};
  self.shape = shape;

  /**
   * 提取校验信息.
   */
  self.extractValidationInfo = () => {
    return null;
  };

  /**
   * 验证校验信息是否合法.
   */
  self.isValidationInfoValid = () => {
    return true;
  };

  return self;
};