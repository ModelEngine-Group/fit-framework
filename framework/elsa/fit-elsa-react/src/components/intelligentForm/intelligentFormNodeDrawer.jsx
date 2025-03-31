/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from '@/components/base/jadeNodeDrawer.jsx';
import ManualCheckIcon from '../asserts/icon-manual-check.svg?react'; // 导入背景图片

/**
 * 智能表单节点绘制器
 *
 * @override
 */
export const intelligentFormNodeDrawer = (shape, div, x, y) => {
  const self = jadeNodeDrawer(shape, div, x, y);
  self.type = "intelligentFormNodeDrawer";

  /**
   * @override
   */
  self.getHeaderIcon = () => {
    return (<>
      <ManualCheckIcon className="jade-node-custom-header-icon"/>
    </>);
  };

  return self;
};