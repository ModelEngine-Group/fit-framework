/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Popover} from 'antd';
import NoteCopyIcon from '../asserts/icon-note-copy.svg?react';
import NoteDeleteIcon from '../asserts/icon-note-delete.svg?react';
import NoteCustomIcon from '../asserts/icon-note-custom.svg?react';
import PropTypes from 'prop-types';
import {CUSTOM_ACTION} from '@/components/note/const.js';
import {useShapeContext} from '@/components/DefaultRoot.jsx';


/**
 * 自定义按钮组件
 *
 * @param style 样式
 * @param open 二级菜单key
 * @param setOpen 设置二级菜单key
 * @returns {Element}
 * @private
 */
const _CustomButton = ({open, setOpen}) => {
  const shape = useShapeContext();

  /**
   * 自定义按钮点击后的回调
   *
   * @param key 按钮对应的key
   */
  const onAction = (key) => {
    if (key.key === 'copy') {
      shape.duplicate();
    } else {
      shape.remove();
    }
    key.domEvent.stopPropagation();
  };

  const customMap = {
    [CUSTOM_ACTION.COPY]: {
      icon: <NoteCopyIcon style={{width: '16px', height: '16px'}}/>,

    }, [CUSTOM_ACTION.DELETE]: {
      icon: <NoteDeleteIcon style={{width: '16px', height: '16px'}}/>,
    },
  };

  const customMenu = (<div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  }}>
    {Object.keys(customMap).map(key => (<div
      className='custom-option' // 添加类名
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
        onAction({key, domEvent: e});
        setOpen('');
      }}>
      {customMap[key].icon}
    </div>))}
  </div>);

  return (<>
    {/* 自定义按钮 */}
    <Popover
      content={customMenu} // 使用content属性来定义弹出的内容，等价于Dropdown的menu属性
      title={null} // 如果不需要标题，可以设置为null
      trigger='click' // 设置触发方式为点击
      open={open === 'custom'} // 控制Popover的显示状态
      overlayClassName={'custom-menu'}
      placement='bottom'
    >
      <Button
        className={'toolbar-custom-btn'}
        onClick={(e) => {
          e.stopPropagation(); // 防止事件冒泡影响Popover的显示状态
          if (open === 'custom') {
            setOpen('');
          } else {
            setOpen('custom');
          }
        }}
        icon={<NoteCustomIcon/>}
      />
    </Popover>
  </>);
};

_CustomButton.propTypes = {
  setOpen: PropTypes.func.isRequired,
  open: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.setOpen === nextProps.setOpen &&
    prevProps.open === nextProps.open;
};

export const CustomButton = React.memo(_CustomButton, areEqual);