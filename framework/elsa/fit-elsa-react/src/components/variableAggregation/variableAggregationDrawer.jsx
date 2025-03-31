/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from '@/components/base/jadeNodeDrawer.jsx';
import VariableAggregation from '../asserts/icon-variable-aggregation.svg?react'; // 导入背景图片

/**
 * 变量聚合节点绘制器
 *
 * @override
 */
export const variableAggregationDrawer = (shape, div, x, y) => {
  const self = jadeNodeDrawer(shape, div, x, y);
  self.type = 'variableAggregationDrawer';

  /**
   * @override
   */
  self.getHeaderIcon = () => {
    return (<>
      <VariableAggregation className='jade-node-custom-header-icon'/>
    </>);
  };

  return self;
};