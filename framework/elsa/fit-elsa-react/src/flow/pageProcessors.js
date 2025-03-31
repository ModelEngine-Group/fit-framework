/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * page 处理器基类.
 *
 * @param pageData 页面数据.
 * @param graph 画布对象.
 * @return {{}} 处理器.
 */
export const pageProcessor = (pageData, graph) => {
  const self = {};
  const shapes = pageData.shapes;

  /**
   * 处理page兼容性问题.
   */
  self.process = () => {
    if (shapes.length === 0) {
      return;
    }
    shapes.map(sd => self.createShapeProcessor(sd, graph)).forEach(p => p.process());
  };

  /**
   * 创建节点的兼容处理器.
   *
   * @param shapeData 节点数据.
   * @param g 画布对象.
   * @return {{}} 处理器.
   */
  self.createShapeProcessor = (shapeData, g) => {
  };

  return self;
};