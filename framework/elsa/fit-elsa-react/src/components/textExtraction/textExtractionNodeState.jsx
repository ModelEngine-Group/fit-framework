/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {textExtractionNodeDrawer} from '@/components/textExtraction/textExtractionNodeDrawer.jsx';
import {DEFAULT_FLOW_META} from '@/common/Consts.js';
import {baseToolNodeState} from '@/components/base/baseToolNodeState.js';

/**
 * 问题改写节点.
 *
 * @override
 */
export const textExtractionNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = baseToolNodeState(id, x, y, width, height, parent, drawer ? drawer : textExtractionNodeDrawer);
  self.type = 'textExtractionNodeState';
  self.text = '文本提取';
  self.componentName = 'textExtractionComponent';
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

  /**
   * 序列化问题改写节点，序列化为工具节点类型
   *
   * @override
   */
  const serializerJadeConfig = self.serializerJadeConfig;
  self.serializerJadeConfig = (jadeConfig) => {
    serializerJadeConfig.apply(self, [jadeConfig]);
    self.flowMeta.jober.entity.return.type = 'object';
  };

  /**
   * 获取文本提取节点章节报告输入展示
   *
   * @returns {{}|*} 需要展示的输入信息
   */
  self.getInputData = () => {
    return {text: self.input?.extractParam?.text} ?? {};
  };

  return self;
};