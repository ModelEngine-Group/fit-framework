/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DATA_TYPES, DEFAULT_FLOW_META} from '@/common/Consts.js';
import {variableAggregationDrawer} from '@/components/variableAggregation/variableAggregationDrawer.jsx';
import {jadeNode} from '@/components/base/jadeNode.jsx';

/**
 * 变量聚合节点.
 *
 * @override
 */
export const variableAggregationNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : variableAggregationDrawer);
  self.type = 'variableAggregationNodeState';
  self.text = '变量聚合';
  self.componentName = 'variableAggregationComponent';
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

  /**
   * 处理传递的元数据
   *
   * @param metaData 元数据信息
   */
  self.processMetaData = (metaData) => {
    if (metaData && metaData.name) {
      self.text = metaData.name;
    }
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
  };

  /**
   * 序列化
   *
   * @override
   */
  self.serializerJadeConfig = (jadeConfig) => {
    self.flowMeta.jober.converter.entity = jadeConfig;
    self.flowMeta.jober.entity.params = self.flowMeta.jober.converter.entity.inputParams.map(property => {
      return {name: property.name};
    });
    self.flowMeta.jober.entity.return.type = self.flowMeta.jober.converter.entity.outputParams[0].type ?? DATA_TYPES.STRING;
  };

  /**
   * @override
   */
  self.maxNumToLink = () => {
    return 10;
  };

  return self;
};