/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {appNodeDrawer} from '@/components/appNode/appNodeDrawer.jsx';
import {SECTION_TYPE} from '@/common/Consts.js';
import {baseToolNode} from '@/components/base/baseToolNode.jsx';

/**
 * 应用和工具流节点.
 *
 * @override
 */
export const appNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = baseToolNode(id, x, y, width, height, parent, drawer ? drawer : appNodeDrawer);
  self.type = 'appNodeState';
  self.text = 'app调用';
  self.componentName = 'appComponent';
  self.flowMeta.jober.fitables.push('modelengine.fit.jober.aipp.fitable.NodeAppComponent');
  self.flowMeta.jober.type = 'general_jober';
  self.flowMeta.jober.isAsync = 'true';

  /**
   * @override
   */
  const processMetaData = self.processMetaData;
  self.processMetaData = (metaData) => {
    if (!metaData) {
      return;
    }
    // 删除后端给每个应用、工具流自动增加的无用数据
    delete metaData.schema.parameters.properties.inputParams.properties.traceId;
    delete metaData.schema.parameters.properties.inputParams.properties.callbackId;
    delete metaData.schema.parameters.properties.inputParams.properties.userId;
    processMetaData.apply(self, [metaData]);
    self.text = `${metaData.schema.name}|${metaData.version}`;
    self.flowMeta.jober.entity.appCategory = metaData?.runnables?.APP?.appCategory ?? 'chatbot';
    self.drawer.unmountReact();
    self.invalidateAlone();
  };

  /**
   * 应用工具流节点的测试报告章节
   */
  self.getRunReportSections = () => {
    // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
    return [{no: '1', name: 'input', type: SECTION_TYPE.DEFAULT, data: self.input?.inputParams ?? {}}, {
      no: '2', name: 'output', type: SECTION_TYPE.DEFAULT, data: self.getOutputData(self.output),
    }];
  };

  return self;
};