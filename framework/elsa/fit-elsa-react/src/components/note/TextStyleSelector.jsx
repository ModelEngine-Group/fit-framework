/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Popover} from 'antd';
import AlignLeft from '../asserts/icon-note-align-left.svg?react';
import AlignCenter from '../asserts/icon-note-align-center.svg?react';
import AlignRight from '../asserts/icon-note-align-right.svg?react';
import UnorderedList from '../asserts/icon-unordered-list.svg?react';
import OrderedList from '../asserts/icon-ordered-list.svg?react';
import {ALIGN, LIST_STYLE} from '@/components/note/const.js';
import PropTypes from 'prop-types';

/**
 * 文本样式设置组件
 *
 * @param style 样式
 * @param open 二级菜单key
 * @param setOpen 设置二级菜单key
 * @param handleAlignChange 对齐方式修改回调
 * @param handleListStyleChange 编号凡是修改回调
 * @returns {Element}
 * @private
 */
const _TextStyleSelector = ({style, open, setOpen, handleAlignChange, handleListStyleChange}) => {
  const align = style.value.find(item => item.name === 'align');
  const listStyle = style.value.find(item => item.name === 'listStyle');

  const alignMap = {
    [ALIGN.ALIGN_LEFT]: {
      icon: <AlignLeft style={{width: '16px', height: '16px'}}/>,

    }, [ALIGN.ALIGN_CENTER]: {
      icon: <AlignCenter style={{width: '16px', height: '16px'}}/>,

    }, [ALIGN.ALIGN_RIGHT]: {
      icon: <AlignRight style={{width: '16px', height: '16px'}}/>,
    },
  };

  const listStyleMap = {
    [LIST_STYLE.UNORDERED_LIST]: {
      icon: <UnorderedList style={{width: '16px', height: '16px'}}/>,

    }, [LIST_STYLE.ORDERED_LIST]: {
      icon: <OrderedList style={{width: '16px', height: '16px'}}/>,
    },
  };

  /**
   * 根据对齐方式选择展示的按钮
   *
   * @param alignStyleKey 对齐方式
   * @returns {*}
   */
  const getAlignIcon = (alignStyleKey) => {
    return alignMap[alignStyleKey]?.icon;
  };

  /**
   * 根据编号方式展示按钮
   *
   * @param listStyleKey 编号方式
   * @returns {*}
   */
  const getListStyleIcon = (listStyleKey) => {
    return listStyleMap[listStyleKey]?.icon;
  };

  // 对齐方式菜单
  const alignmentMenu = (<div
    style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
    }}>
    {Object.keys(alignMap).map(key => (<div
      className='align-option' // 添加类名
      style={{
        width: '32px',
        height: '32px',
        marginRight: '4px',
        marginLeft: '4px',
        display: 'flex', // 应用Flexbox布局以中心对齐SVG
        justifyContent: 'center', // 水平居中
        alignItems: 'center', // 垂直居中
      }}
      key={key}
      onClick={(e) => {
        onAlignClick({key, domEvent: e});
        setOpen('');
      }}>
      {alignMap[key].icon}
    </div>))}
  </div>);

  // 项目列表菜单
  const listMenu = (<div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  }}>
    {Object.keys(listStyleMap).map(key => (<div
      className='list-option' // 添加类名
      style={{
        width: '32px',
        height: '32px',
        marginRight: '4px',
        marginLeft: '4px',
        display: 'flex', // 应用Flexbox布局以中心对齐SVG
        justifyContent: 'center', // 水平居中
        alignItems: 'center', // 垂直居中
      }}
      key={key}
      onClick={(e) => {
        onListStyleClick({key, domEvent: e});
        setOpen('');
      }}>
      {listStyleMap[key].icon}
    </div>))}
  </div>);

  /**
   * 对齐方式点击的回调
   *
   * @param key 按钮对应的key
   */
  const onAlignClick = (key) => {
    handleAlignChange(key.key);
    key.domEvent.stopPropagation();
  };

  /**
   * 项目编号点击的回调
   *
   * @param key 按钮对应的编号方式
   */
  const onListStyleClick = (key) => {
    handleListStyleChange(key.key);
    key.domEvent.stopPropagation();
  };

  /**
   * 点击对齐按钮
   */
  const onToolBarAlignClick = (e) => {
    if (open === 'align') {
      setOpen('');
    } else {
      setOpen('align');
    }
  };

  /**
   * 点击编号按钮
   */
  const onToolBarListClick = (e) => {
    if (open === 'listStyle') {
      setOpen('');
    } else {
      setOpen('listStyle');
    }
  };

  return (<>
    {/* 对齐方式 */}
    <Popover
      overlayClassName={'align-menu'}
      content={alignmentMenu}
      trigger='click'
      open={open === 'align'}
      placement='bottom'>
      <Button onClick={onToolBarAlignClick}
              className={'toolbar-paragraph-style-btn'}
              icon={getAlignIcon(align.value)}/>
    </Popover>

    <Popover
      overlayClassName={'list-style-menu'}
      open={open === 'listStyle'}
      content={listMenu}
      trigger='click'
      placement='bottom'>
      <Button onClick={onToolBarListClick}
              icon={getListStyleIcon(listStyle.value)}
              className={'toolbar-paragraph-style-btn'}/>
    </Popover>
  </>);
};

_TextStyleSelector.propTypes = {
  style: PropTypes.object.isRequired,
  handleAlignChange: PropTypes.func.isRequired,
  handleListStyleChange: PropTypes.func.isRequired,
  setOpen: PropTypes.func.isRequired,
  open: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.style === nextProps.style &&
    prevProps.handleAlignChange === nextProps.handleAlignChange &&
    prevProps.handleListStyleChange === nextProps.handleListStyleChange &&
    prevProps.setOpen === nextProps.setOpen &&
    prevProps.open === nextProps.open;
};

export const TextStyleSelector = React.memo(_TextStyleSelector, areEqual);