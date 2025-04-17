/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {loopNodeDrawer} from '@/components/loopNode/loopNodeDrawer.jsx';
import {jadeNode} from '@/components/base/jadeNode.jsx';
import {SECTION_TYPE, TOOL_TYPE} from '@/common/Consts.js';

/**
 * jadeStream中的循环节点.
 *
 * @override
 */
export const loopNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : loopNodeDrawer);
  self.type = 'loopNodeState';
  self.text = '循环节点';
  self.componentName = 'loopComponent';
  self.flowMeta.jober.type = 'STORE_JOBER';
  const loopNodeEntity = {
    uniqueName: "",
    params: [{"name": "args"}, {"name": "config"}, {"name": "toolInfo"}, {"name": "context"}],
    return: {type: "array"}
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
    self.flowMeta.jober.entity = loopNodeEntity;
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
  };

  /**
   * 应用工具流节点的测试报告章节
   */
  self.getRunReportSections = () => {
    const isWaterFlow = self.flowMeta?.jober?.converter?.entity?.inputParams?.find(param => param.name === 'toolInfo')?.value?.tags?.some(tag => tag === TOOL_TYPE.WATER_FLOW) ?? false;
    // 选择合适的 data
    const inputData = isWaterFlow ? self.input?.args?.inputParams : self.input?.args;
    // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
    return [{no: '1', name: 'input', type: SECTION_TYPE.DEFAULT, data: inputData ?? {}}, {
      no: '2', name: 'output', type: SECTION_TYPE.DEFAULT, data: self.getOutputData(self.output),
    }];
  };

  return self;
};
