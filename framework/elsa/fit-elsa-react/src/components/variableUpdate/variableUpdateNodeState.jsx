/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '../base/jadeNode.jsx';
import {DEFAULT_FLOW_META} from '@/common/Consts.js';
import {variableUpdateDrawer} from '@/components/variableUpdate/variableUpdateDrawer.jsx';

/**
 * 变量更新节点.
 *
 * @override
 */
export const variableUpdateNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : variableUpdateDrawer);
  self.type = 'variableUpdateNodeState';
  self.text = '变量更新';
  self.componentName = 'variableUpdateComponent';
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
  };

  /**
   * @override
   */
  self.isAllowReferenceSystemEnv = () => {
    return false;
  };

  return self;
};