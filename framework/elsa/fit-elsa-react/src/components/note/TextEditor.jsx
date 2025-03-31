/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useRef, useState} from 'react';
import contentCSS from '../note/editor.css?raw';
import tinymce from 'tinymce';
import 'tinymce/models/dom/index.js';
import 'tinymce/icons/default/index.js';
import 'tinymce/plugins/lists/index.js';
import 'tinymce/skins/ui/oxide/skin.min.css';
import 'tinymce/themes/silver/theme.min.js';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import {EditorToolbar} from '@/components/note/EditorToolBar.jsx';
import PropTypes from 'prop-types';
import {TOOL_BAR_SIZE} from '@/components/note/const.js';
import {useTranslation} from 'react-i18next';
import {ResizeButton} from '@/components/note/ResizeButton.jsx';
import {EVENT_TYPE, isPointInRect} from '@fit-elsa/elsa-core';

/**
 * 文本编辑组件
 *
 * @param text 文本内容（呗tinyMCE编辑器格式化后的文本内容）
 * @param style css样式记录值
 * @param dispatch 发送事件的方法，用于组件数据更新
 * @param isFocused 图形的选中状态
 * @param isInDragging 图形是否在拖拽中
 * @returns {React.JSX.Element}
 * @constructor
 */
const _TextEditor = ({text, style, dispatch, isFocused, isInDragging}) => {
  const shape = useShapeContext();
  const {t} = useTranslation();
  const shapeDiv = shape.drawer.parent;
  const [isEditing, setIsEditing] = useState(false); // 控制编辑状态
  const editorRef = useRef(null); // 创建一个ref
  const [open, setOpen] = useState(''); // 控制菜单显示状态
  const toolBarRef = useRef(null); // 创建一个ref
  const [forceUpdate, setForceUpdate] = useState(false); // 强制组件刷新
  const bgColor = style.value.find(item => item.name === 'backgroundColor');
  const height = shape.height - 32;
  const width = shape.width - 22;
  const editorId = `jade-node-editor-${shape.id}`;

  /**
   * 文本修改的回调
   *
   * @param value 新的文本内容
   */
  const handleTextChange = (value) => {
    dispatch({actionType: 'textChange', value: value});
  };

  /**
   * 背景颜色修改后的回调
   *
   * @param value 背景颜色值
   */
  const handleBgColorChange = (value) => {
    dispatch({actionType: 'styleChange', item: 'backgroundColor', value: value});
  };

  /**
   * 字体大小修改回调
   *
   * @param value 字号
   */
  const handleFontSizeChange = (value) => {
    tinymce.activeEditor.execCommand('FontSize', false, `${value}px`);
    handleTextChange(tinymce.activeEditor.getContent());
    dispatch({actionType: 'styleChange', item: 'fontSize', value: value});
  };

  /**
   * 字体颜色修改回调
   *
   * @param value 字体颜色
   */
  const handleFontColorChange = (value) => {
    tinymce.activeEditor.execCommand('ForeColor', false, value);
    handleTextChange(tinymce.activeEditor.getContent());
    dispatch({actionType: 'styleChange', item: 'fontColor', value: value});
  };

  /**
   * 文字样式修改回调（加粗、斜体、下划线）
   *
   * @param value 样式类型（加粗、斜体、下划线）
   */
  const handleFontStyleChange = (value) => {
    tinymce.activeEditor.execCommand(value);
    handleTextChange(tinymce.activeEditor.getContent());
  };

  /**
   * 对齐方式修改回调
   *
   * @param value 对齐方式
   */
  const handleAlignChange = (value) => {
    tinymce.activeEditor.execCommand(value);
    handleTextChange(tinymce.activeEditor.getContent());
    dispatch({actionType: 'styleChange', item: 'align', value: value});
  };

  /**
   * 列表样式修改回调
   *
   * @param value 列表样式
   */
  const handleListStyleChange = (value) => {
    tinymce.activeEditor.execCommand(value);
    handleTextChange(tinymce.activeEditor.getContent());
    dispatch({actionType: 'styleChange', item: 'listStyle', value: value});
  };

  /**
   * 是否点击到编辑器空白区域
   *
   * @param event 事件
   * @returns {null|*|this is Node[]} true/false
   */
  const isEmptyAreaClicked = event => {
    let textNodes = editorRef.current.childNodes;
    let minX = textNodes[0].getBoundingClientRect().x;
    let minY = textNodes[0].getBoundingClientRect().y;
    let maxY = 0;
    let textPartWidth = textNodes[0].getBoundingClientRect().width;
    let textPartHeight = textNodes[0].getBoundingClientRect().height;
    // 计算文本所在实际区域
    Array.from(textNodes).forEach(textPart => {
      const rect = textPart.getBoundingClientRect();
      minX = Math.min(minX, rect.x);
      minY = Math.min(minY, rect.y);
      maxY = Math.max(maxY, rect.y);
      textPartWidth = Math.max(textPartWidth, rect.width);
      textPartHeight = Math.max(textPartHeight, maxY - minY + rect.height);
    });

    return editorRef.current && editorRef.current.contains(event.target) &&
      (textNodes[0] && !isPointInRect({x: event.clientX, y: event.clientY}, {
        x: minX,
        y: minY,
        width: textPartWidth,
        height: textPartHeight,
      }));
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      // 点击文本区域之外图形区域，比如边框
      if (editorRef.current && !editorRef.current.contains(event.target)) {
        setOpen('');
        event.preventDefault();
        document.activeElement.blur(); // 在选择后取消焦点
      }
      // 文本为空点击空白区域可唤起工具栏，不为空需要点击到具体的文本区域
      if (text !== '' && isEmptyAreaClicked(event)) {
        event.preventDefault();
        document.activeElement.blur(); // 在选择后取消焦点
      }
    };

    const handleSelectionChange = () => {
      // 强制重新渲染以更新工具栏状态
      setForceUpdate((prev) => !prev);
    };

    shape.page.addEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, handleSelectionChange);

    // 监听mousedown事件来确保触发blur
    shapeDiv.addEventListener('mousedown', handleClickOutside); // 在点击时判断是否是外部点击
    return () => {
      shapeDiv.removeEventListener('mousedown', handleClickOutside);
      shape.page.removeEventListener(EVENT_TYPE.FOCUSED_SHAPES_CHANGE, handleSelectionChange);
    };
  }, [text]);

  // 编辑器初始化
  useEffect(() => {
    if (tinymce.get(editorId)) {
      tinymce.get(editorId).destroy();
    }
    tinymce.init({
      selector: `#jade-node-editor-${shape.id}`,
      plugins: 'lists',
      content_style: contentCSS,
      min_height: '60px',
      min_width: '160px',
      height: `${height}px`,
      width: `${width}px`,
      inline: true,
      resize: true,
      menubar: false,
      statusbar: false,
      toolbar: false,
      setup: (editor) => {
        // 监听文本变化
        editor.on('Change', () => {
          const content = editor.getContent();
          handleTextChange(content);
        });

        // 显示弹窗
        editor.on('focus', event => {
          if (isInDragging) {
            event.preventDefault(); // 阻止默认行为
            return;
          }
          setIsEditing(true);
        });

        editor.on('click', () => {
          setOpen('');
        });
      },
      init_instance_callback: (editor) => {
        // 初始内容设置
        editor.setContent(text);
      },
    });

    return () => {
      if (tinymce.get(editorId)) {
        tinymce.get(editorId).destroy();
      }
    };
  }, []);

  // 当图形不是 focus状态，关闭工具栏
  useEffect(() => {
    if (!isFocused) {
      setIsEditing(false);
      setOpen('');
    }
  }, [isFocused]);

  // 移动过程中，设置编辑器为readonly，防止鼠标进入到编辑器区域
  useEffect(() => {
    if (isInDragging) {
      editorRef.current.style.pointerEvents = 'none';
    } else {
      editorRef.current.style.pointerEvents = 'auto';
    }
    setOpen('');
  }, [isInDragging]);

  /**
   * 计算工具栏的top
   *
   * @returns {number} 工具栏top数值
   */
  const getTop = () => {
    return -16 - 8 - TOOL_BAR_SIZE.HEIGHT;
  };

  /**
   * 计算工具栏的left属性
   *
   * @returns {number} 工具栏left数值
   */
  const getLeft = () => {
    if (shape.width >= TOOL_BAR_SIZE.WIDTH) {
      return ((shape.width - TOOL_BAR_SIZE.WIDTH) / 2) - 16;
    } else {
      return (-(TOOL_BAR_SIZE.WIDTH - shape.width) / 2) - 16;
    }
  };

  /**
   * 是否展示工具栏
   *
   * @returns {boolean} 是否展示工具栏
   */
  const isShowToolBar = () => {
    // 额外场景：1.ctrl按下的情况下，点击编辑器不出现工具栏；2.超过一个节点被选中，不展示工具栏
    return isEditing && isFocused && !isInDragging && !shape.page.ctrlKeyPressed && shape.page.getFocusedShapes().length <= 1;
  };

  return (<>
    {isShowToolBar() && (<div
      ref={toolBarRef}
      className='toolbar'
      style={{
        height: '40px',
        position: 'absolute',
        top: `${getTop()}px`,
        left: `${getLeft()}px`,
        display: 'flex',
        gap: '8px',
        zIndex: 1000,
      }}
    >
      <EditorToolbar
        style={style}
        bgColor={bgColor}
        handleBgColorChange={handleBgColorChange}
        handleFontColorChange={handleFontColorChange}
        handleFontSizeChange={handleFontSizeChange}
        handleFontStyleChange={handleFontStyleChange}
        handleAlignChange={handleAlignChange}
        handleListStyleChange={handleListStyleChange}
        setOpen={setOpen}
        open={open}
      />
    </div>)}

    <div
      ref={editorRef} // 应用ref
      className={'editor-wrapper'}
      style={{
        position: 'relative',
        backgroundColor: `${bgColor.value}`,
        minHeight: '62px',
        minWidth: '160px',
        paddingRight: '12px',
        marginRight: '-12px', // 保证滚动条相对于有边界只有4px
        height: `${height}px`,
        width: `${width}px`,
        resize: 'none', // 禁用用浏览器默认resize
        overflowY: 'auto', // 必须启用overflow属性，否则resize不会生效
        overflowX: 'hidden', // 隐藏横向滚动条
        border: 'none',
      }}
      contentEditable={true}
      id={`jade-node-editor-${shape.id}`}>
    </div>
    <ResizeButton editorRef={editorRef} toolBarRef={toolBarRef} getLeft={getLeft} getTop={getTop}/>
  </>);
};

_TextEditor.propTypes = {
  text: PropTypes.string.isRequired,
  style: PropTypes.object.isRequired,
  dispatch: PropTypes.func.isRequired,
  isFocused: PropTypes.bool.isRequired,
  isInDragging: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.text === nextProps.text &&
    prevProps.style === nextProps.style &&
    prevProps.dispatch === nextProps.dispatch &&
    prevProps.isInDragging === nextProps.isInDragging &&
    prevProps.isFocused === nextProps.isFocused;
};

export const TextEditor = React.memo(_TextEditor, areEqual);