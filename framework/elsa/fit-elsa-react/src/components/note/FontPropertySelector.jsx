/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useState} from 'react';
import {Button, Dropdown, Popover} from 'antd';
import FontColorIcon from '@/components/note/FontColorIcon.jsx';
import {FONT_COLOR, FONT_SIZE_OPTIONS} from '@/components/note/const.js';
import PropTypes from 'prop-types';

/**
 * 字体属性设置组件
 *
 * @param style 样式对象
 * @param open 二级菜单key
 * @param setOpen 设置二级菜单key
 * @param handleFontSizeChange 字体大小设置回调
 * @param handleFontColorChange 字体颜色设置回调
 * @returns {Element}
 * @private
 */
const _FontPropertySelector = ({style, open, setOpen, handleFontSizeChange, handleFontColorChange}) => {
  const fontSize = style.value.find(item => item.name === 'fontSize');
  const fontColor = style.value.find(item => item.name === 'fontColor');
  const [selectedColor, setSelectedColor] = useState('#000000');

  /**
   * 选择颜色后的回调
   *
   * @param color 颜色
   */
  const handleColorSelect = (color) => {
    setSelectedColor(color);
    handleFontColorChange(color);
    setOpen(''); // 点击后关闭菜单
  };

  /**
   * 对齐方式点击的回调
   *
   * @param key 按钮对应的key
   */
  const onFontSizeSelect = (key) => {
    handleFontSizeChange(key.key);
    key.domEvent.stopPropagation();
    setOpen('');
  };

  // 字体大小菜单
  const fontSizeMenu = {
    className: 'ant-font-dropdown-menu-custom', // 应用了自定义样式类
    onClick: onFontSizeSelect,
    items: FONT_SIZE_OPTIONS.map((size) => ({
      key: size, label: size,
    })),
  };

  // 渲染颜色选择器
  const colorMenu = (<div style={{display: 'grid', gridTemplateColumns: 'repeat(6, 16px)', gap: 4}}>
    {FONT_COLOR.map((color) => (<div
      key={color}
      className='color-option' // 添加类名
      style={{
        backgroundColor: color,
        width: 16,
        height: 16,
        borderRadius: 2,
        border: color === selectedColor ? '2px solid #1890FF' : '1px solid #E8E8E8',
        cursor: 'pointer',
      }}
      onClick={(e) => {
        handleColorSelect(color);
        e.stopPropagation();
      }}
    />))}
  </div>);

  /**
   * 点击字号按钮
   */
  const onToolBarFontSizeClick = (e) => {
    if (open === 'fontSize') {
      setOpen('');
    } else {
      setOpen('fontSize');
    }
  };

  /**
   * 点击字体颜色按钮
   */
  const onToolBarFontColorClick = (e) => {
    if (open === 'fontColor') {
      setOpen('');
    } else {
      setOpen('fontColor');
    }
  };

  return (<>
    {/* 字号选择 */}
    <Dropdown menu={fontSizeMenu}
              open={open === 'fontSize'}
              trigger={['click']}>
      <Button className={'tool-bar-font-size-btn'}
              onClick={onToolBarFontSizeClick}
      >
        <span className={'tool-bar-font-size-font-style'}>{fontSize.value}</span></Button>
    </Dropdown>

    {/* 字体颜色 */}
    <Popover
      overlayClassName='font-color-menu' // 添加自定义类名
      content={colorMenu}
      trigger='click'
      placement='bottom'
      open={open === 'fontColor'}
      onClick={(e) => e.stopPropagation()}
    >
      <Button
        onClick={onToolBarFontColorClick}
        open={open === 'fontColor'}
        icon={<FontColorIcon color={fontColor.value}/>}
        className={'toolbar-font-color-btn'}/>
    </Popover>
  </>);
};

_FontPropertySelector.propTypes = {
  style: PropTypes.object.isRequired,
  handleFontSizeChange: PropTypes.func.isRequired,
  handleFontColorChange: PropTypes.func.isRequired,
  setOpen: PropTypes.func.isRequired,
  open: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.style === nextProps.style &&
    prevProps.handleFontSizeChange === nextProps.handleFontSizeChange &&
    prevProps.handleFontColorChange === nextProps.handleFontColorChange &&
    prevProps.setOpen === nextProps.setOpen &&
    prevProps.open === nextProps.open;
};

export const FontPropertySelector = React.memo(_FontPropertySelector, areEqual);