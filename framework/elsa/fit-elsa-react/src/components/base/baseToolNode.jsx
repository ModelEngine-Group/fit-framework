/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '@/components/base/jadeNode.jsx';
import {convertParameter, convertReturnFormat} from '@/components/util/MethodMetaDataParser.js';
import {DEFAULT_FLOW_META} from '@/common/Consts.js';

/**
 * 抽象的基础工具节点shape，不作为实例化存在
 *
 * @override
 */
export const baseToolNode = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer);
  self.width = 360;
  self.flowMeta = JSON.parse(DEFAULT_FLOW_META);
  const template = {
    inputParams: [],
    outputParams: [],
  };

  /**
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
   * @override
   */
  const processMetaData = self.processMetaData;
  self.processMetaData = (metaData) => {
    const _generateOutput = () => {
      newConfig.outputParams.push(convertReturnFormat(metaData.schema.return));
    };

    const _generateInput = () => {
      // 这里需要确认，返回的到底是什么数据类型，data是个数组还是对象
      delete newConfig.inputParams;
      const orderProperties = metaData.schema.parameters.order ? metaData.schema.parameters.order : Object.keys(metaData.schema.parameters.properties);
      newConfig.inputParams = orderProperties.map(key => {
        return convertParameter({
          propertyName: key,
          property: metaData.schema.parameters.properties[key],
          isRequired: Array.isArray(metaData.schema.parameters.required) ? metaData.schema.parameters.required.includes(key) : false,
        });
      });
    };

    if (!metaData) {
      return;
    }
    processMetaData.apply(self, [metaData]);
    const newConfig = {...template};
    _generateInput();
    _generateOutput();
    self.flowMeta.jober.converter.entity = newConfig;
    self.sourcePlatform = metaData.source?.toLowerCase() ?? '';
    self.flowMeta.jober.entity.uniqueName = metaData.uniqueName;
    self.flowMeta.jober.entity.return.type = metaData.schema.return.type;
    self.text = self.page.generateNodeName(metaData.name, self.type);
    self.drawer.unmountReact();
    self.invalidateAlone();
  };

  return self;
};