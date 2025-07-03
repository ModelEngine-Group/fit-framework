/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {manualCheckNodeState} from '@/components/manualCheck/manualCheckNodeState.jsx';
import {intelligentFormNodeDrawer} from '@/components/intelligentForm/intelligentFormNodeDrawer.jsx';
import {FORM_TYPE, RENDER_OPTIONS_TYPE} from '@/components/intelligentForm/Consts.js';
import {SECTION_TYPE} from '@/common/Consts.js';

/**
 * jadeStream中的智能表单节点.
 *
 * @override
 */
export const intelligentFormNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = manualCheckNodeState(id, x, y, width, height, parent, drawer ? drawer : intelligentFormNodeDrawer);
  self.type = 'intelligentFormNodeState';
  self.text = '表单';
  self.componentName = 'intelligentFormComponent';

  const transInputFormSchema = (originals) => {
    return originals.flatMap((original) => {
      // 提取原始对象的 options 部分
      const {options, renderType, ...rest} = original;

      const firstObject = {
        ...rest,
      };

      // 第二个对象：基于 options 创建，并添加新的 id 和 name
      const secondObject = RENDER_OPTIONS_TYPE.has(renderType) ? {
        id: uuidv4(), // 生成新的 UUID
        name: `${original.name}-options`, // 基于原始 name 生成新的 name
        from: options.from,
        referenceNode: options.referenceNode,
        referenceId: options.referenceId,
        referenceKey: options.referenceKey,
        value: options.value,
        type: options.type,
      } : null;

      return secondObject ? [firstObject, secondObject] : [firstObject];
    });
  };

  const transOutputFormSchema = (originals) => {
    return originals.flatMap((original) => {
      // 提取原始对象的 options 部分
      const {id, name, type} = original;

      return [{
        id, name, type, value: '',
      }];
    });
  };

  /**
   * @override
   */
  const serializerJadeConfig = self.serializerJadeConfig;
  self.serializerJadeConfig = (jadeConfig) => {
    serializerJadeConfig.apply(self, [jadeConfig]);
    if (self.flowMeta.task.formType !== FORM_TYPE.ORCHESTRATION) {
      return;
    }
    self.flowMeta.task.converter.entity.inputParams.find(item => item.name === 'data').value = transInputFormSchema(self.flowMeta.task.converter.entity.inputParams.find(item => item.name === 'schema').value.parameters);
    self.flowMeta.task.converter.entity.outputParams.find(item => item.name === 'output').value = transOutputFormSchema(self.flowMeta.task.converter.entity.inputParams.find(item => item.name === 'schema').value.parameters);
  };

  /**
   * 获取智能编排节点测试报告章节
   */
  self.getRunReportSections = () => {
    const _filterOptions = (obj) => {
      return Object.keys(obj).reduce((acc, key) => {
        if (!key.endsWith('-options')) {
          acc[key] = obj[key];
        }
        return acc;
      }, {});
    };

    const _getInputData = () => {
      if (self.input && self.input.data) {
        return _filterOptions(self.input.data);
      } else {
        return {};
      }
    };

    // 这里的data是每个节点的每个章节需要展示的数据，比如工具节点展示为输入、输出的数据
    return [{
      no: '1',
      name: 'input',
      type: SECTION_TYPE.DEFAULT,
      data: _getInputData(),
    }, {
      no: '2',
      name: 'output',
      type: SECTION_TYPE.DEFAULT,
      data: self.getOutputData(self.output),
    }];
  };

  return self;
};