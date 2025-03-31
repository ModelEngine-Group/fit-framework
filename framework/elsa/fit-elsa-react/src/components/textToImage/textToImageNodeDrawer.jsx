/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from '@/components/base/jadeNodeDrawer.jsx';
import TextToImage from '../asserts/icon-text-to-image.svg?react'; // 导入背景图片

/**
 * 文件提取节点绘制器
 *
 * @override
 */
export const textToImageNodeDrawer = (shape, div, x, y) => {
  const self = jadeNodeDrawer(shape, div, x, y);
  self.type = 'textToImageNodeDrawer';

  /**
   * @override
   */
  self.getHeaderIcon = () => {
    return (<>
      <TextToImage className='jade-node-custom-header-icon'/>
    </>);
  };

  return self;
};