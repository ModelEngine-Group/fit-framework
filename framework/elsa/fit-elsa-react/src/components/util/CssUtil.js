/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 去除属性组件第一层级之前的虚线，需要配合className打标使用
 */
export const removeFirstLevelLine = () => {
  document.querySelectorAll('.first-level').forEach(firstLevel => {
    const wrapper = firstLevel.closest('.ant-tree-title').closest('.ant-tree-node-content-wrapper'); // 根据实际的父元素类名修改
    if (wrapper) {
      const switcher = wrapper.previousElementSibling; // 选择紧邻的前一个兄弟元素
      if (!(switcher && switcher.classList.contains('ant-tree-switcher'))) {
        return;
      }
      const leafLine = switcher.querySelector('.ant-tree-switcher-leaf-line');
      if (leafLine) {
        leafLine.style.display = 'none'; // 设置样式为 display: none;
      }
    }
  });
};