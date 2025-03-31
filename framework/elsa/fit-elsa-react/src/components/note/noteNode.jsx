/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import './style.css';
import {jadeNode} from '@/components/base/jadeNode.jsx';
import {noteNodeDrawer} from '@/components/note/noteNodeDrawer.jsx';

/**
 * 注释节点.
 *
 * @override
 */
export const noteNode = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : noteNodeDrawer);
  self.type = 'noteNode';
  self.text = '注释';
  self.componentName = 'noteComponent';
  self.runnable = false;
  self.autoWidth = true;
  self.autoHeight = false;
  self.width = 286;
  self.height = 194;
  self.minWidth = 192;
  self.minHeight = 92;
  self.cornerRadius = 12;
  self.backColor = 'rgb(234, 243, 255)';
  self.focusBackColor = 'rgb(234, 243, 255)';
  self.borderColor = 'rgb(171, 203, 249)';
  self.mouseInBorderColor = 'rgb(171, 203, 249)';

  /**
   * 删除所有连接点
   *
   * @override
   */
  const initConnectors = self.initConnectors;
  self.initConnectors = () => {
    initConnectors.apply(self);
    self.connectors.remove(c => c);
  };

  /**
   * 当节点被取消选中时，更改shape在react组件种的状态.
   */
  const unSelect = self.unSelect;
  self.unSelect = () => {
    unSelect.apply(self);
    if (self.drawer.setShapeStatus) {
      self.drawer.setShapeStatus({isFocused: false});
    }
  };

  /**
   * 节点被选中时，更改shape在react组件种的状态.
   */
  const select = self.select;
  self.select = () => {
    select.apply(self);
    if (self.drawer.setShapeStatus) {
      self.drawer.setShapeStatus({isFocused: true});
    }
  };

  /**
   * 拖拽过程设置图形的isInDragging为true
   *
   * @type {function(*): any}
   */
  const onMouseDrag = self.onMouseDrag;
  self.onMouseDrag = (position) => {
    if (self.drawer.setShapeStatus) {
      self.drawer.setShapeStatus({isInDragging: true});
    }
    return onMouseDrag.apply(self, [position]);
  };

  /**
   * 抬手时设置图形的isInDragging为false
   *
   * @type {function(*): any}
   */
  const onMouseUp = self.onMouseUp;
  self.onMouseUp = (position) => {
    if (self.drawer.setShapeStatus) {
      self.drawer.setShapeStatus({isInDragging: false});
    }
    return onMouseUp.apply(self, [position]);
  };

  return self;
};