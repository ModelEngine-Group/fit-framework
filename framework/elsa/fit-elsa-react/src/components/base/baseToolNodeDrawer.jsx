/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from '@/components/base/jadeNodeDrawer.jsx';

/**
 * 抽象的基础工具节点绘制器
 *
 * @override
 */
export const baseToolNodeDrawer = (shape, div, x, y) => {
  return jadeNodeDrawer(shape, div, x, y);
};