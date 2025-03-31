/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDispatch, useFormContext} from '@/components/DefaultRoot.jsx';
import {Trans, useTranslation} from 'react-i18next';
import React from 'react';
import {Form, Popover, Select, Typography} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';
import TextArea from 'antd/es/input/TextArea.js';
import PropTypes from 'prop-types';

const {Text} = Typography;

/**
 * 模板组件header
 *
 * @param templateType 模板类型
 * @param disabled 是否禁用
 * @param shapeId 图形id
 * @returns {React.JSX.Element} 模板组件header
 * @constructor
 */
const PanelHeader = ({templateType, disabled, shapeId}) => {
  const {t} = useTranslation();
  const form = useFormContext();
  const dispatch = useDispatch();

  /**
   * 更改模板类型的回调函数（对话背景/提示词）
   *
   * @param e 动作事件
   */
  const onChange = (e) => {
    // 切换对话背景或提示词
    form.setFields([{name: `Prompt-${shapeId}`, value: ''}]);
    dispatch({actionType: 'changeTemplateType', value: e});
  };

  /**
   * 根据当前类型获取文字输入区域的标题
   *
   * @return {string} 标题
   */
  const getTitle = () => {
    if (templateType === 'custom') {
      return t('userPromptTemplate');
    } else {
      return t('conversationDescription');
    }
  };

  /**
   * 根据是提示词或者对话背景，获取模板的提示信息
   *
   * @return {JSX.Element} 国际化后的提示信息
   */
  const getPromptContent = () => {
    if (templateType === 'custom') {
      return (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='optimizationPromptPopover' components={{p: <p/>}}/>
      </div>);
    } else {
      return (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
        <Trans i18nKey='conversationBackgroundPopover' components={{p: <p/>}}/>
      </div>);
    }
  };

  return (<div className='panel-header custom-header'>
            <span style={{display: 'flex', alignItems: 'center'}}>
                {templateType === 'custom' && <Text type='danger'>*</Text>}
              <span className='jade-panel-header-font'>{getTitle()}</span>
            </span>
    <Popover
      content={getPromptContent()}
      align={{offset: [0, 3]}}
      overlayClassName={'jade-custom-popover'}
    >
      <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
    </Popover>
    <Select
      disabled={disabled}
      id={`template-model-select-${shapeId}`}
      className='select-value-custom jade-select'
      options={[{value: 'custom', label: t('optimizationPrompt')}, {
        value: 'builtin', label: t('conversationBackground'),
      }]}
      onChange={onChange}
      value={templateType}
    />
  </div>);
};

/**
 * 模板文本区域组件
 *
 * @param shapeId 图形id
 * @param disabled 是否禁用
 * @param template 模板对象
 * @param getRules 根据类型获取校验规则
 * @param getPlaceHolder 根据类型获取模板placeHolder
 * @returns {React.JSX.Element} 模板文本区域组件
 * @constructor
 */
const TemplateTextArea = ({shapeId, disabled, template, getRules, getPlaceHolder}) => {
  const dispatch = useDispatch();

  /**
   * 失焦时才设置值，对于必填项.若为空，则不设置
   *
   * @param e event
   * @param actionType 事件类型
   * @param id config的id
   * @param required 是否必需
   */
  const changeOnBlur = (e, actionType, id, required) => {
    if (required && e.target.value === '') {
      return;
    }
    dispatch({actionType: actionType, id: id, value: e.target.value});
  };

  return (<Form.Item
    className='jade-form-item'
    id={`Prompt-${shapeId}`}
    name={`Prompt-${shapeId}`}
    rules={getRules()}
    initialValue={template.value}
    validateTrigger='onBlur'
  >
    <TextArea
      disabled={disabled}
      maxLength={10000}
      className='jade-textarea-input jade-font-size'
      onBlur={(e) => changeOnBlur(e, 'changeTemplateValue', template.id, true)}
      placeholder={getPlaceHolder()}
    />
  </Form.Item>);
};

/**
 * 问题改写节点模板
 *
 * @param shapeId 图形id
 * @param disabled 是否禁用
 * @param template 提示词对象
 * @param templateType 模板类型
 * @return {JSX.Element} 模板组件
 * @private
 */
const _Template = ({shapeId, disabled, template, templateType}) => {
  const {t} = useTranslation();

  /**
   * 根据模板类型，返回校验规则
   *
   * @return {[{message: TFuncReturn<'translation', string, string, undefined>, required: boolean}]|*[]}
   */
  const getRules = () => {
    if (templateType === 'custom') {
      return [{message: t('paramCannotBeEmpty')}];
    } else {
      return [];
    }
  };

  /**
   * 根据模板类型获取placeHolder
   *
   * @returns {TFuncReturn<'translation', string, string, undefined>}
   */
  const getPlaceHolder = () => {
    let i18Key;
    i18Key = templateType === 'custom' ? 'systemPromptPlaceHolder' : '';
    return t(i18Key);
  };

  return (<>
    <PanelHeader
      templateType={templateType}
      disabled={disabled}
      shapeId={shapeId}/>
    <TemplateTextArea
      shapeId={shapeId}
      disabled={disabled}
      template={template}
      getRules={getRules}
      getPlaceHolder={getPlaceHolder}/>
  </>);
};

_Template.propTypes = {
  shapeId: PropTypes.string.isRequired, // 确保 shapeId 是一个必需的string类型
  disabled: PropTypes.bool, // 确保 modelOptions 是一个必需的array类型
  template: PropTypes.object.isRequired, templateType: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.shapeId === nextProps.shapeId &&
    prevProps.templateType === nextProps.templateType &&
    prevProps.template === nextProps.template;
};

export const Template = React.memo(_Template, areEqual);