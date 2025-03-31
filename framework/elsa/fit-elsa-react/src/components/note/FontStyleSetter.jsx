/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React from 'react';
import {Button, Tooltip} from 'antd';
import FontBoldIcon from '../asserts/icon-font-bold.svg?react';
import FontItalicIcon from '../asserts/icon-font-italic.svg?react';
import FontUnderlineIcon from '../asserts/icon-font-underline.svg?react';
import PropTypes from 'prop-types';

/**
 * 字体样式设置组件
 *
 * @param setOpen 设置二级菜单key
 * @param handleFontStyleChange 设置字体样式后的回调
 * @returns {Element}
 * @private
 */
const _FontStyleSetter = ({setOpen, handleFontStyleChange}) => {
  return (<>
    {/* 字体样式 */}
    <Tooltip title='加粗'>
      <Button className={'toolbar-font-font-style-btn'}
              icon={<FontBoldIcon/>}
              onClick={(e) => {
                setOpen('');
                return handleFontStyleChange('Bold');
              }}
      />
    </Tooltip>
    <Tooltip title='斜体'>
      <Button className={'toolbar-font-font-style-btn'}
              icon={<FontItalicIcon/>}
              onClick={(e) => {
                setOpen('');
                return handleFontStyleChange('Italic');
              }}
      />
    </Tooltip>
    <Tooltip title='下划线'>
      <Button className={'toolbar-font-font-style-btn'}
              icon={<FontUnderlineIcon/>}
              onClick={(e) => {
                setOpen('');
                return handleFontStyleChange('Underline');
              }}
      />
    </Tooltip>
  </>);
};

_FontStyleSetter.propTypes = {
  handleFontStyleChange: PropTypes.func.isRequired,
  setOpen: PropTypes.func.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.handleFontStyleChange === nextProps.handleFontStyleChange &&
    prevProps.setOpen === nextProps.setOpen;
};

export const FontStyleSetter = React.memo(_FontStyleSetter, areEqual);