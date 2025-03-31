/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import 'tinymce/models/dom/index.js';
import 'tinymce/icons/default/index.js';
import 'tinymce/plugins/lists/index.js';
import 'tinymce/skins/ui/oxide/skin.min.css';
import 'tinymce/themes/silver/theme.min.js';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import ResizeIcon from '../asserts/icon-resize.svg?react';
import {Button} from 'antd';
import PropTypes from 'prop-types';

/**
 * 缩放按钮
 *
 * @param editorRef 编辑器区域引用
 * @param toolBarRef 工具栏区域引用
 * @param getLeft 计算工具栏left
 * @param getTop 计算工具栏top
 * @returns {Element}
 * @private
 */
const _ResizeButton = ({editorRef, toolBarRef, getLeft, getTop}) => {
  const shape = useShapeContext();
  let lastWidth = shape.width - 22;
  let lastHeight = shape.height - 32;

  // 处理鼠标拖动事件
  const handleMouseDown = (e) => {
    e.stopPropagation(); // 防止缩放时导致移动图形
    const startX = e.clientX;
    const startY = e.clientY;
    const startWidth = editorRef.current.offsetWidth;
    const startHeight = editorRef.current.offsetHeight;

    const onMouseMove = (moveEvent) => {
      const currentWidth = startWidth + (moveEvent.clientX - startX);
      const currentHeight = startHeight + (moveEvent.clientY - startY);
      const newWidth = currentWidth <= 160 ? 160 : currentWidth;
      const newHeight = currentHeight <= 60 ? 60 : currentHeight;
      // 实时调整缩放
      if (editorRef.current) {
        editorRef.current.style.width = `${newWidth}px`;
        editorRef.current.style.height = `${newHeight}px`;
      }
      let shapeWidth = shape.width;
      let shapeHeight = shape.height;

      if (lastWidth !== newWidth) {
        shapeWidth = newWidth + 22;
      }
      if (lastHeight !== newHeight) {
        shapeHeight = newHeight + 32;
      }

      // 仅在尺寸发生实际变化时执行
      shape.resize(shapeWidth, shapeHeight);
      shape.invalidateAlone();
      toolBarRef.current && (toolBarRef.current.style.left = `${getLeft()}px`);
      toolBarRef.current && (toolBarRef.current.style.top = `${getTop()}px`);

      // 更新最后记录的尺寸
      lastWidth = newWidth;
      lastHeight = newHeight;
    };

    const onMouseUp = () => {
      // 停止监听鼠标移动和鼠标弹起事件
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
    };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
  };

  return (<>
    <Button
      className={'resize-button no-click-effect'}
      style={{
        background: 'transparent',
        border: 'none',
        position: 'absolute',
        width: '8px', // 明确设置宽度
        height: '8px', // 明确设置高度
        padding: 0, // 移除默认的内边距
        bottom: '-8px',
        right: '-8px',
        zIndex: '99999',
        cursor: 'se-resize', // 表示是一个缩放按钮
        display: 'flex', // 使用Flexbox布局来居中图标
        alignItems: 'center', // 垂直居中
        justifyContent: 'center', // 水平居中
      }}
      icon={<ResizeIcon/>}
      onMouseDown={handleMouseDown}/>
  </>);
};

_ResizeButton.propTypes = {
  editorRef: PropTypes.element.isRequired,
  toolBarRef: PropTypes.element.isRequired,
  getLeft: PropTypes.func.isRequired,
  getTop: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.editorRef === nextProps.editorRef &&
    prevProps.toolBarRef === nextProps.toolBarRef &&
    prevProps.getLeft === nextProps.getLeft &&
    prevProps.getTop === nextProps.getTop;
};

export const ResizeButton = React.memo(_ResizeButton, areEqual);