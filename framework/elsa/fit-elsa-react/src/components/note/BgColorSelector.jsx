/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Popover} from 'antd';
import DefaultColorIcon from '../asserts/icon-note-default-color.svg?react'; // 导入背景图片
import SecondColorIcon from '../asserts/icon-note-second-color.svg?react'; // 导入背景图片
import ThirdColorIcon from '../asserts/icon-note-third-color.svg?react'; // 导入背景图片
import FourthColorIcon from '../asserts/icon-note-fourth-color.svg?react';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {NOTE_NODE_COLOR} from '@/components/note/const.js';

/**
 * 背景颜色选择器
 *
 * @param bgColor 背景颜色
 * @param handleBgColorChange 背景颜色更改回调
 * @param open 打开的二级菜单key
 * @param setOpen 设置key
 * @returns {Element}
 * @private
 */
const _BgColorSelector = ({bgColor, handleBgColorChange, open, setOpen}) => {
  const shape = useShapeContext();

  const bgColorMap = {
    [NOTE_NODE_COLOR.DEFAULT.BACKCOLOR]: {
      icon: <DefaultColorIcon/>,

    }, [NOTE_NODE_COLOR.SECOND.BACKCOLOR]: {
      icon: <SecondColorIcon/>,

    }, [NOTE_NODE_COLOR.THIRD.BACKCOLOR]: {
      icon: <ThirdColorIcon/>,

    }, [NOTE_NODE_COLOR.FOURTH.BACKCOLOR]: {
      icon: <FourthColorIcon/>,
    },
  };

  // 背景颜色选择器
  const backColorMenu = (<div
    style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
    }}>
    {['DEFAULT', 'SECOND', 'THIRD', 'FOURTH'].map((key) => (
      <div
        key={key}
        className='color-option' // 添加类名
        style={{
          width: '32px',
          height: '32px',
          marginRight: '4px',
          marginLeft: '4px',
          display: 'flex', // 应用Flexbox布局以中心对齐SVG
          justifyContent: 'center', // 水平居中
          alignItems: 'center', // 垂直居中
        }}
        onClick={(e) => handleMenuItemClick({key, domEvent: e})}>
        {bgColorMap[NOTE_NODE_COLOR[key].BACKCOLOR].icon}
      </div>))}
  </div>);

  /**
   * 切换背景颜色的回调
   *
   * @param key 选项对应的key
   */
  const handleMenuItemClick = (key) => {
    shape.backColor = NOTE_NODE_COLOR[key.key].BACKCOLOR;
    shape.outlineColor = NOTE_NODE_COLOR[key.key].OUTLINE_COLOR;
    shape.focusBackColor = NOTE_NODE_COLOR[key.key].BACKCOLOR;
    shape.borderColor = NOTE_NODE_COLOR[key.key].BORDER_COLOR;
    shape.focusBorderColor = NOTE_NODE_COLOR[key.key].FOCUSED_BORDER_COLOR;
    shape.mouseInBorderColor = NOTE_NODE_COLOR[key.key].BORDER_COLOR;
    shape.invalidateAlone();
    // 更改jadeConfig种的背景颜色值
    handleBgColorChange(NOTE_NODE_COLOR[key.key].BACKCOLOR);
    key.domEvent.stopPropagation();
    setOpen('');
  };

  /**
   * 根据背景颜色获取工具栏背景颜色展示按钮
   *
   * @param color 背景颜色
   * @returns {*}
   */
  const getBgColorIcon = (color) => {
    return bgColorMap[color]?.icon;
  };

  /**
   * 点击背景颜色按钮
   *
   * @param e 动作事件
   */
  const onToolbarBgColorClick = e => {
    if (open === 'backcolor') {
      setOpen('');
    } else {
      setOpen('backcolor');
    }
  };

  return (<>
    <Popover
      content={backColorMenu}
      trigger='click'
      placement='bottom'
      open={open === 'backcolor'}
      overlayClassName={'back-color-menu'}>
      <Button onClick={onToolbarBgColorClick}
              icon={getBgColorIcon(bgColor.value)}
              className={'toolbar-backcolor-btn'}/>
    </Popover>
  </>);
};

_BgColorSelector.propTypes = {
  bgColor: PropTypes.object.isRequired,
  handleBgColorChange: PropTypes.func.isRequired,
  setOpen: PropTypes.func.isRequired,
  open: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.bgColor === nextProps.bgColor &&
    prevProps.handleBgColorChange === nextProps.handleBgColorChange &&
    prevProps.setOpen === nextProps.setOpen &&
    prevProps.open === nextProps.open &&
    prevProps.handleListStyleChange === nextProps.handleListStyleChange;
};

export const BgColorSelector = React.memo(_BgColorSelector, areEqual);