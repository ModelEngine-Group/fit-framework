/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {DEFAULT_FLOW_META} from '@/common/Consts.js';
import {httpComponent} from '@/components/httpNode/httpComponent.jsx';
import {httpNodeDrawer} from '@/components/httpNode/httpNodeDrawer.jsx';
import {v4 as uuidv4} from 'uuid';
import {baseToolNodeState} from '@/components/base/baseToolNodeState.js';

/**
 * http节点.
 *
 * @override
 */
export const httpNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = baseToolNodeState(id, x, y, width, height, parent, drawer ? drawer : httpNodeDrawer);
  self.type = 'httpNodeState';
  self.text = 'http节点';
  self.width = 456;
  self.componentName = 'httpComponent';
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);

  /**
   * 序列化http节点，序列化为工具节点类型
   *
   * @override
   */
  const serializerJadeConfig = self.serializerJadeConfig;
  self.serializerJadeConfig = (jadeConfig) => {
    serializerJadeConfig.apply(self, [jadeConfig]);
    self.flowMeta.jober.entity.params = self.flowMeta.jober.converter.entity.inputParams.map(property => {
      return {name: property.name};
    });
    self.flowMeta.jober.entity.return.type = 'object';
    const allBodyDate = self.flowMeta.jober.converter.entity.inputParams.find(item => item.name === 'allBodyData').value;
    const activeBodyType = allBodyDate.find(item => item.name === 'activeBodyType').value;
    const activeBodyData = allBodyDate.find(item => item.name === activeBodyType);
    const newBodyType = {
      id: `type_${uuidv4()}`,
      name: 'type',
      type: 'String',
      from: 'Input',
      value: activeBodyType,
    };
    const newBodyData =
      {
        id: `data_${uuidv4()}`,
        name: 'data',
        type: activeBodyType === 'none' ? 'String' : activeBodyData.type,
        from: activeBodyType === 'none' ? 'Input' : activeBodyData.from,
        value: activeBodyType === 'none' ? null : activeBodyData.value,
      };
    self.flowMeta.jober.converter.entity.inputParams.find(item => item.name === 'httpRequest').value.find(v => v.name === 'requestBody').value = [newBodyType, newBodyData];
  };

  /**
   * 获取章节报告输入展示
   *
   * @returns {{}|*} 需要展示的输入信息
   */
  self.getInputData = () => {
    return self.input?.httpRequest?.args ?? {};
  };

  return self;
};