/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useEffect, useRef, useState} from 'react';
import {Button, Dropdown, Form, Input, message} from 'antd';
import {HEADER_TOOL_MENU_ICON} from '@/components/asserts/svgIcons.jsx';
import './headerStyle.css';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import TextDisplay from '@/components/common/TextDisplay.jsx';
import {AdvancedConfiguration} from '@/components/AdvancedConfiguration.jsx';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {EVENT_NAME} from '@/common/Consts.js';

/**
 * 头部.
 *
 * @param shape 图形.
 * @param data 数据.
 * @param shapeStatus 图形状态集合.
 * @return {JSX.Element}
 * @constructor
 */
export const Header = ({shape, data, shapeStatus}) => {
  const {t} = useTranslation();
  const [edit, setEdit] = useState(false);
  const [isAdvancedConfigurationOpen, setAdvancedConfigurationOpen] = useState(false);
  const inputRef = useRef(null);
  const dispatch = useDispatch();

  useEffect(() => {
    inputRef.current && inputRef.current.focus({
      cursor: 'end',
    });
  });

  const onInputBlur = () => {
    if (inputRef.current.input.value === '') {
      return;
    }
    if (shape.page.sm.getShapes(s => s.id !== shape.id).some(s => s.text === inputRef.current.input.value)) {
      message.error(t('nodeTextDuplicate'));
      return;
    }
    shape.text = inputRef.current.input.value;
    shape.page.triggerEvent({
      type: EVENT_NAME.NODE_NAME_CHANGED,
      value: {
        id: shape.id,
        name: shape.text,
      },
    });
    setEdit(false);
  };

  /**
   * menu点击事件.
   *
   * @param e 事件对象.
   */
  const onMenuClick = (e) => {
    const m = shape.drawer.getToolMenus().find(t => t.key === e.key);
    if (m.action) {
      if (e.key === 'rename') {
        m.action(setEdit);
      } else if (e.key === 'advancedConfiguration') {
        m.action(setAdvancedConfigurationOpen);
      } else {
        m.action();
      }
    }
  };

  /**
   * 获取文本组件，处于编辑态时，需要能修改标题；否则只展示标题.
   *
   * @return {JSX.Element} 标题组件.
   */
  const getTitle = () => {
    if (edit) {
      return (<>
        <Form.Item
          name='title'
          rules={[{required: true, message: t('pleaseInsertName')}]}
          initialValue={shape.text}>
          <Input
            onBlur={(e) => onInputBlur(e)}
            ref={inputRef}
            onMouseDown={(e) => e.stopPropagation()}
            placeholder={t('pleaseInsertName')}
            style={{height: '24px', borderColor: shape.focusBorderColor}}/>
        </Form.Item>
      </>);
    } else {
      return <TextDisplay text={shape.text} lineHeight={19} width={200} fontSize={16} fontWeight={700}/>;
    }
  };

  const onOpenChange = (openKeys) => {
    shape.drawer.getToolMenus().forEach(m => {
      if (openKeys.includes(m.key)) {
        m.onOpen && m.onOpen();
      }
    });
  };

  /**
   * 获取菜单项.
   *
   * @return {*} 菜单项
   */
  const getMenu = () => {
    const items = shape.drawer.getToolMenus().map(toolMenu => {
      const menu = {
        key: toolMenu.key,
        label: t(toolMenu.label),
        popupClassName: 'react-node-header-menu-sub',
        popupOffset: [10, 0],
      };
      toolMenu.children && (menu.children = toolMenu.children);
      toolMenu.onTitleClick && (menu.onTitleClick = toolMenu.onTitleClick);
      return menu;
    });
    return {items, onClick: (e) => onMenuClick(e), onOpenChange, triggerSubMenuAction: 'click'};
  };

  /**
   * 展示菜单.
   *
   * @return {JSX.Element}
   */
  const showMenus = () => {
    if (shape.drawer.getToolMenus().length > 0) {
      return (<>
        <div onMouseDown={e => e.stopPropagation()} className={'jade-menus'}>
          <Dropdown
            getPopupContainer={trigger => trigger.parentNode}
            disabled={shapeStatus.disabled}
            menu={getMenu()}
            trigger='click'
            placement='bottomRight'>
            <Button type='text' size='small' className={'react-node-header-button icon-button'}>
              {HEADER_TOOL_MENU_ICON}
            </Button>
          </Dropdown>
        </div>
      </>);
    }
    return <></>;
  };

  const onConfirm = (newData) => {
    dispatch({type: 'changeFlowMeta', actionType: 'changeFlowMeta', data: newData});
  };

  return (<>
    <div className='react-node-header'>
      <div className='react-node-toolbar' style={{alignItems: 'center'}}>
        <div style={{display: 'flex', alignItems: 'center'}}>
          {shape.drawer.getHeaderIcon()}
        </div>
        <div className='react-node-toolbar-name'>
          {getTitle()}
          {shape.drawer.getHeaderTypeIcon()}
        </div>
        {showMenus()}
      </div>
      <span className='react-node-header-description'>{shape.description}</span>
      <AdvancedConfiguration
        data={data}
        disabled={shapeStatus.disabled}
        onConfirm={onConfirm}
        isAdvancedConfigurationOpen={isAdvancedConfigurationOpen}
        setAdvancedConfigurationOpen={setAdvancedConfigurationOpen}/>
    </div>
  </>);
};

Header.propTypes = {
  shape: PropTypes.object,
  data: PropTypes.object,
  shapeStatus: PropTypes.object,
};
