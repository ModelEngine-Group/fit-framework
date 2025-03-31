/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useRef} from 'react';
import {SplitLine} from '@/components/note/SplitLine.jsx';
import PropTypes from 'prop-types';
import {BgColorSelector} from '@/components/note/BgColorSelector.jsx';
import {FontPropertySelector} from '@/components/note/FontPropertySelector.jsx';
import {FontStyleSetter} from '@/components/note/FontStyleSetter.jsx';
import {TextStyleSelector} from '@/components/note/TextStyleSelector.jsx';
import {CustomButton} from '@/components/note/CustomButton.jsx';

/**
 * 编辑器工具栏
 *
 * @param style 编辑器样式，需要使得工具栏展示对应的样式
 * @param bgColor 背景颜色
 * @param handleBgColorChange 更改背景颜色的回调
 * @param handleFontSizeChange 更改字体大小的回调
 * @param handleFontColorChange 更改字体颜色的回调
 * @param handleFontStyleChange 更改字体样式的回调
 * @param handleAlignChange 更改对齐方式的回调
 * @param handleListStyleChange 更改编号方式的回调
 * @param setOpen 设置二级菜单key
 * @param open 二级菜单的key
 * @returns {Element}
 * @constructor
 */
const _EditorToolbar = ({
                          style,
                          bgColor,
                          handleBgColorChange,
                          handleFontSizeChange,
                          handleFontColorChange,
                          handleFontStyleChange,
                          handleAlignChange,
                          handleListStyleChange,
                          setOpen,
                          open,
                        }) => {
  const editorRef = useRef(null);

  // 点击工具栏区域，阻止事件冒泡，从而解决点击工具栏选中下面图形的问题
  useEffect(() => {
    const handleClickToolBar = (event) => {
      event.stopPropagation();
    };

    editorRef.current.addEventListener('mousedown', handleClickToolBar);
    return () => {
      return editorRef.current && editorRef.current.removeEventListener('mousedown', handleClickToolBar);
    };
  });

  return (<div ref={editorRef} className={'tool-bar'}>
    <BgColorSelector bgColor={bgColor} open={open} setOpen={setOpen} handleBgColorChange={handleBgColorChange}/>
    <SplitLine/>
    <FontPropertySelector open={open}
                          setOpen={setOpen}
                          style={style}
                          handleFontColorChange={handleFontColorChange}
                          handleFontSizeChange={handleFontSizeChange}/>
    <SplitLine/>
    <FontStyleSetter handleFontStyleChange={handleFontStyleChange} setOpen={setOpen}/>
    <SplitLine/>
    <TextStyleSelector
      style={style}
      open={open}
      setOpen={setOpen}
      handleAlignChange={handleAlignChange}
      handleListStyleChange={handleListStyleChange}/>
    <SplitLine/>
    <CustomButton setOpen={setOpen} open={open}/>
  </div>);
};

_EditorToolbar.propTypes = {
  style: PropTypes.object.isRequired,
  bgColor: PropTypes.object.isRequired,
  handleBgColorChange: PropTypes.func.isRequired,
  handleFontSizeChange: PropTypes.func.isRequired,
  handleFontColorChange: PropTypes.func.isRequired,
  handleFontStyleChange: PropTypes.func.isRequired,
  handleAlignChange: PropTypes.func.isRequired,
  handleListStyleChange: PropTypes.func.isRequired,
  setOpen: PropTypes.func.isRequired,
  open: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.style === nextProps.style &&
    prevProps.bgColor === nextProps.bgColor &&
    prevProps.handleBgColorChange === nextProps.handleBgColorChange &&
    prevProps.handleFontSizeChange === nextProps.handleFontSizeChange &&
    prevProps.handleFontColorChange === nextProps.handleFontColorChange &&
    prevProps.handleFontStyleChange === nextProps.handleFontStyleChange &&
    prevProps.handleAlignChange === nextProps.handleAlignChange &&
    prevProps.setOpen === nextProps.setOpen &&
    prevProps.open === nextProps.open &&
    prevProps.handleListStyleChange === nextProps.handleListStyleChange;
};

export const EditorToolbar = React.memo(_EditorToolbar, areEqual);