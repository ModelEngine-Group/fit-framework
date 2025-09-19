/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Drawer, Form, Input} from 'antd';
import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {CloseOutlined} from '@ant-design/icons';
import {useTranslation} from 'react-i18next';
import AiPromptIcon from '../../asserts/icon-ai-prompt.svg?react';
import {useFormContext} from '@/components/DefaultRoot.jsx';

const TOP_ALIGN = '60px';

/**
 * 提示词抽屉组件.
 *
 * @param title 抽屉大标题.
 * @param name formItem名称.
 * @param value 抽屉中TextArea值.
 * @param rules 表单验证规则.
 * @param placeHolder TextArea的PlaceHolder.
 * @param getContainer 获取容器dom.
 * @param width 宽度.
 * @param open 打开抽屉.
 * @param onClose 抽屉关闭时的回调.
 * @param onConfirm 确认时的回调.
 * @param allowAIGenerate 允许AI生成.
 * @param onAIGenerate 抽屉中AI生成调用的函数
 * @return {JSX.Element}
 * @constructor
 */
const _PromptDrawer = (
  {
    title,
    name,
    value,
    rules,
    placeHolder,
    container,
    width = 570,
    open,
    onClose,
    onConfirm,
    allowAIGenerate,
    onAIGenerate,
    maxLength = 2000,
    labelName
  }) => {
  const {t} = useTranslation();
  const form = useFormContext();
  const rootStyle = {};
  const drawerName = `${name}-drawer`;
  const [currentPrompt, setCurrentPrompt] = useState(value);

  useEffect(() => {
    setCurrentPrompt(value);
    form.setFieldsValue({[drawerName]: value});
  }, [value, open]);

  if (container) {
    rootStyle.position = 'absolute';
  }

  const _onClose = () => {
    if (onClose) {
      onClose();
    }
  };

  const _onConfirm = () => {
    if (onConfirm) {
      onConfirm(currentPrompt);
    }
    _onClose();
  };

  return (<>
    <Drawer
      title={title}
      className={'jade-prompt-drawer'}
      rootStyle={{...rootStyle}}
      width={width}
      maskClosable={false}
      closable={false}
      open={open}
      extra={
        <CloseOutlined
          onClick={_onClose}
        />
      }
      getContainer={container ? container : false}
      footer={
        <div className='jade-prompt-drawer-footer'>
          <Button className='jade-prompt-drawer-footer-btn' onClick={_onClose}>
            {t('cancel')}
          </Button>
          <Button type='primary' className='jade-prompt-drawer-footer-btn' onClick={_onConfirm}>
            {t('ok')}
          </Button>
        </div>
      }
    >
      <Form.Item
        className='jade-form-item'
        label={
          <div style={{display: 'flex', alignItems: 'center', width: '100%'}}>
            <span>{labelName}</span>
            {allowAIGenerate && <div className='jade-prompt-drawer-btn' onClick={onAIGenerate((promptText) => {
              if (promptText === '') {
                return;
              }
              setCurrentPrompt(promptText);
              form.setFieldsValue({[drawerName]: promptText});
            })}>
              <AiPromptIcon style={{marginRight: 5}}/>
              <span>{`AI${t('generate')}`}</span>
            </div>
            }
          </div>
        }
        name={drawerName}
        rules={rules}
        labelCol={{span: 24}}
        wrapperCol={{span: 24}}
      >
        <Input.TextArea
          className={'jade-prompt-drawer-textarea'}
          value={currentPrompt}
          style={{resize: 'none'}}
          showCount
          maxLength={maxLength}
          placeholder={placeHolder}
          autoSize={false}
          onChange={(e) => setCurrentPrompt(e.target.value)}
        />
      </Form.Item>
    </Drawer>
  </>);
};

_PromptDrawer.propTypes = {
  title: PropTypes.string,
  name: PropTypes.string,
  value: PropTypes.string,
  placeHolder: PropTypes.string,
  rules: PropTypes.array,
  container: PropTypes.object.isRequired,
  width: PropTypes.number,
  open: PropTypes.bool,
  onClose: PropTypes.func,
  onConfirm: PropTypes.func,
  allowAIGenerate: PropTypes.bool,
  onAIGenerate: PropTypes.func,
  maxLength: PropTypes.number,
  labelName: PropTypes.string,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.title === nextProps.title &&
    prevProps.name === nextProps.name &&
    prevProps.value === nextProps.value &&
    prevProps.placeHolder === nextProps.placeHolder &&
    prevProps.container === nextProps.container &&
    prevProps.width === nextProps.width &&
    prevProps.open === nextProps.open &&
    prevProps.onClose === nextProps.onClose &&
    prevProps.onConfirm === nextProps.onConfirm &&
    prevProps.allowAIGenerate === nextProps.allowAIGenerate &&
    prevProps.onAIGenerate === nextProps.onAIGenerate &&
    prevProps.maxLength === nextProps.maxLength;
};

export const PromptDrawer = React.memo(_PromptDrawer, areEqual);