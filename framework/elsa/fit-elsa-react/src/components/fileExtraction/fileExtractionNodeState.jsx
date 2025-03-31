/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {baseToolNodeState} from '../base/baseToolNodeState.js';
import {fileExtractionNodeDrawer} from './fileExtractionNodeDrawer.jsx';
import {DEFAULT_FLOW_META} from '@/common/Consts.js';

/**
 * 多模态文件提取节点.
 *
 * @override
 */
export const fileExtractionNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = baseToolNodeState(id, x, y, width, height, parent, drawer ? drawer : fileExtractionNodeDrawer);
  self.type = 'fileExtractionNodeState';
  self.text = '多模态文件提取节点';
  self.width = 368;
  self.componentName = 'fileExtractionComponent';
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

  /**
   * 序列化文件企图节点，序列化为工具节点类型
   *
   * @override
   */
  const serializerJadeConfig = self.serializerJadeConfig;
  self.serializerJadeConfig = (jadeConfig) => {
    serializerJadeConfig.apply(self, [jadeConfig]);
    self.flowMeta.jober.entity.return.type = 'string';
  };

  /**
   * 获取章节报告输入展示
   *
   * @returns {{}|*} 需要展示的输入信息
   */
  self.getInputData = () => {
    return self.input?.fileExtraction?.args ?? {};
  };

  return self;
};