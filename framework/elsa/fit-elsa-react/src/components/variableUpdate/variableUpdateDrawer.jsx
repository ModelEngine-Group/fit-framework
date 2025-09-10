/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from '../base/jadeNodeDrawer.jsx';
import VariableUpdate from '../asserts/icon-variable-update.svg?react'; // 导入背景图片

/**
 * 变量更新节点绘制器
 *
 * @override
 */
export const variableUpdateDrawer = (shape, div, x, y) => {
  const self = jadeNodeDrawer(shape, div, x, y);
  self.type = 'variableUpdateDrawer';

  /**
   * @override
   */
  self.getHeaderIcon = () => {
    return (<>
      <VariableUpdate className='jade-node-custom-header-icon'/>
    </>);
  };

  return self;
};