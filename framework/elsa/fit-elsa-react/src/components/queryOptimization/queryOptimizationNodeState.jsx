/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '@/components/base/jadeNode.jsx';
import './style.css';
import {queryOptimizationNodeDrawer} from '@/components/queryOptimization/queryOptimizationNodeDrawer.jsx';
import {DEFAULT_FLOW_META, SECTION_TYPE} from '@/common/Consts.js';

/**
 * 问题改写节点.
 *
 * @override
 */
export const queryOptimizationNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : queryOptimizationNodeDrawer);
  self.type = 'queryOptimizationNodeState';
  self.text = '问题改写';
  self.componentName = 'queryOptimizationComponent';
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

  /**
   * 序列化问题改写节点，序列化为工具节点类型
   *
   * @override
   */
  const serializerJadeConfig = self.serializerJadeConfig;
  self.serializerJadeConfig = (jadeConfig) => {
    serializerJadeConfig.apply(self, [jadeConfig]);
    self.flowMeta.jober.entity.params = self.flowMeta.jober.converter.entity.inputParams.map(property => {
      return {name: property.name};
    });
  };

  /**
   * 处理元数据，给问题改写节点设置uniqueName
   *
   * @override
   */
  const processMetaData = self.processMetaData;
  self.processMetaData = (metaData) => {
    processMetaData.apply(self, [metaData]);
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
  };

  /**
   * 获取问题改写节点测试报告章节
   */
  self.getRunReportSections = () => {
    // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
    return [{
      no: '1',
      name: 'input',
      type: SECTION_TYPE.DEFAULT,
      data: self.input ? self.input : {},
    }, {
      no: '2',
      name: 'output',
      type: SECTION_TYPE.DEFAULT,
      data: self.getOutputData(self.output),
    }];
  };

  return self;
};