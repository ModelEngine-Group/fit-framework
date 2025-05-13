/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {parallelNodeDrawer} from '@/components/parallelNode/parallelNodeDrawer.jsx';
import {jadeNode} from '@/components/base/jadeNode.jsx';
import {SECTION_TYPE, TOOL_TYPE} from '@/common/Consts.js';
import {TOOL_CALLS} from '@/components/parallelNode/consts.js';

/**
 * jadeStream中的并行节点.
 *
 * @override
 */
export const parallelNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : parallelNodeDrawer);
  self.type = 'parallelNodeState';
  self.text = self.graph.i18n?.t('parallelNode') ?? 'parallelNode';
  self.componentName = 'parallelComponent';
  self.width = 380;
  self.flowMeta.jober.type = 'STORE_JOBER';
  const parallelNodeEntity = {
    uniqueName: "",
    params: [{"name": TOOL_CALLS}, {"name": "context"}],
    return: {type: "object"}
  };

  /**
   * @override
   */
  const processMetaData = self.processMetaData;
  self.processMetaData = (metaData) => {
    if (!metaData) {
      return;
    }
    processMetaData.apply(self, [metaData]);
    self.flowMeta.jober.entity = parallelNodeEntity;
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
  };

  /**
   * 应用工具流节点的测试报告章节
   */
  self.getRunReportSections = () => {
    const inputData = {};
    self.input?.toolCalls?.forEach((toolCall) => {
      const isWaterFlow = toolCall?.tags?.includes(TOOL_TYPE.WATER_FLOW) ?? false;
      inputData[toolCall?.outputName ?? 'output'] = isWaterFlow ? toolCall?.args?.inputParams : toolCall?.args;
    });
    return [{no: '1', name: 'input', type: SECTION_TYPE.DEFAULT, data: inputData ?? {}}, {
      no: '2', name: 'output', type: SECTION_TYPE.DEFAULT, data: self.getOutputData(self.output),
    }];
  };

  return self;
};
