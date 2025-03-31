/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNodeDrawer} from '@/components/base/jadeNodeDrawer.jsx';
import {isPointInRect} from '@fit-elsa/elsa-core';

/**
 * 注释节点绘制器
 *
 * @override
 */
export const noteNodeDrawer = (shape, div, x, y) => {
  const self = jadeNodeDrawer(shape, div, x, y);
  self.type = 'noteNodeDrawer';

  /**
   * @override
   */
  self.getHeaderComponent = (data, shapeStatus) => {
    return (<></>);
  };

  /**
   * @override
   */
  self.getFooterComponent = (data, shapeStatus) => {
    return (<></>);
  };

  /**
   * 适配autoWidth
   *
   * @param shapeObj
   * @returns {*}
   */
  self.calculateWidth = shapeObj => {
    return shapeObj.width;
  };

  /**
   * 是否点击到边框
   *
   * @param coordinateX x坐标
   * @param coordinateY y坐标
   * @returns {*|boolean}
   */
  self.containsBorder = (coordinateX, coordinateY) => {
    return isPointInRect({x: coordinateX, y: coordinateY}, outer()) && !isPointInRect({
      x: coordinateX,
      y: coordinateY,
    }, inner());
  };

  const inner = () => {
    const space = 4;
    return {
      x: shape.x + shape.borderWidth + space,
      y: shape.y + shape.borderWidth + space,
      width: shape.width - (2 * shape.borderWidth) - (2 * space),
      height: shape.height - (2 * shape.borderWidth) - (2 * space),
    };
  };

  // 选边框外面更容易选中
  const outer = () => {
    const space = 15;
    return {
      x: shape.x - space, y: shape.y - space, width: shape.width + (2 * space), height: shape.height + (2 * space),
    };
  };

  return self;
};