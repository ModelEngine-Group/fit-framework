/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import React, {useState} from 'react';
import {Button, Col, Form, Input, Popover, Row} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import './prompt.css';
import PropTypes from 'prop-types';
import {useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import {PromptDrawer} from '@/components/common/prompt/PromptDrawer.jsx';
import {useTranslation} from "react-i18next";

const {TextArea} = Input;

/**
 * 提示词组件
 *
 * @param name 表单名称
 * @param rules 表单验证规则
 * @param prompt 提示词对象
 * @param tips 提示词提示信息
 * @param onChange 提示词变化时触发
 * @param disabled 是否禁用
 * @param title 提示词组件标题
 * @param placeHolder 文本输入框placeHolder
 * @param open 抽屉是否打开
 * @param setOpen 修改抽屉状态
 * @param allowAIGenerate 抽屉中允许AI生成
 * @param onAIGenerate 抽屉中AI生成调用的函数
 * @param buttonConfigs 按钮配置.
 * @return {JSX.Element} 提示词组件
 * @constructor
 */
export const Prompt = (
  {
    name, rules, prompt, tips, onChange, disabled, title, placeHolder, open, setOpen, allowAIGenerate = false, onAIGenerate, buttonConfigs = [],
  }) => {
  const shape = useShapeContext();
  const form = useFormContext();
  const {t} = useTranslation();
  const [promptContent, setPromptContent] = useState(prompt.value);

  const _onChange = (promptText) => {
    onChange(promptText);
    form.setFieldsValue({[name]: promptText});
    setPromptContent(promptText);
  };

  /**
   * 失焦时才设置值，对于必填项.若为空，则不设置
   *
   * @param e event事件
   */
  const changeOnBlur = (e) => {
    if (prompt.value !== e.target.value) {
      _onChange(e.target.value);
    }
  };

  const createButton = (buttonConfig) => {
    return (<>
      <Button
        disabled={disabled}
        type='text'
        className='icon-button'
        style={{height: '100%'}}
        onClick={buttonConfig.onClick}>
        {buttonConfig.icon}
      </Button>
    </>);
  };

  const getLabel = () => {
    return (<>
      <div className={'required-after'} style={{display: 'flex', alignItems: 'center', width: '100%'}}>
        <span className='jade-second-title'>{title}</span>
        {tips && <Popover
          content={[tips]}
          align={{offset: [0, 3]}}
          overlayClassName={'jade-custom-popover'}
        >
          <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
        </Popover>}
        <div className={'prompt-title-buttons'}>
          {buttonConfigs.map(bc => createButton(bc))}
        </div>
      </div>
    </>);
  };

  return (<>
    <Row gutter={16}>
      <Col span={24}>
        <div className={'prompt-container'}>
          <Form.Item
            className='jade-form-item'
            name={name}
            label={getLabel()}
            rules={rules}
            initialValue={promptContent}
            validateTrigger='onBlur'
          >
            <TextArea
              disabled={disabled}
              maxLength={2000}
              className='jade-textarea-input jade-font-size'
              onBlur={(e) => changeOnBlur(e)}
              placeholder={placeHolder}
            />
          </Form.Item>
          <PromptDrawer
            value={promptContent}
            name={name}
            title={title}
            rules={rules}
            placeHolder={placeHolder}
            container={shape.page.graph.div.parentElement}
            open={open}
            onClose={() => setOpen(false)}
            onConfirm={(v) => _onChange(v)}
            allowAIGenerate={allowAIGenerate}
            onAIGenerate={onAIGenerate}
            labelName={t('promptName')}
          />
        </div>
      </Col>
    </Row>
  </>);
};
Prompt.propTypes = {
  name: PropTypes.string.isRequired,
  rules: PropTypes.array.isRequired,
  prompt: PropTypes.object.isRequired,
  tips: PropTypes.object,
  onChange: PropTypes.func,
  actionName: PropTypes.string,
  disabled: PropTypes.bool,
  title: PropTypes.string,
  placeHolder: PropTypes.string,
  open: PropTypes.bool,
  setOpen: PropTypes.func,
  allowAIGenerate: PropTypes.bool,
  onAIGenerate: PropTypes.func,
  buttonConfigs: PropTypes.array,
};