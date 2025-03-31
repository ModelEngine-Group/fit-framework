/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import React, {useState} from 'react';
import {Button, Collapse, Popover} from 'antd';
import {useTranslation} from 'react-i18next';
import {QuestionCircleOutlined} from '@ant-design/icons';
import TextArea from 'antd/es/input/TextArea.js';
import {useFormContext, useShapeContext} from '../DefaultRoot.jsx';
import '../common/style.css';
import {useDispatch} from '@/components/DefaultRoot.jsx';
import {PromptDrawer} from '../common/prompt/PromptDrawer.jsx';
import AiPromptIcon from '../asserts/icon-ai-prompt.svg?react';
import FullScreenIcon from '../asserts/icon-full-screen.svg?react';
import {DEFAULT_AP_PROMPT_MODEL_CONFIG} from '@/common/Consts.js';

const {Panel} = Collapse;

const maxPromptLength = 2000;

/**
 * AI提示词组件
 *
 * @param disabled 是否禁用
 * @param prompt 提示词
 * @param name formItem名称
 * @param popoverContent 折叠标题popover内容
 * @param header 折叠区域标题文字
 * @param drawerTitle 弹窗标题
 * @return {JSX.Element}
 * @private
 */
const _AiPromptPanel = ({disabled, prompt, name, popoverContent = null, header, drawerTitle}) => {
  const shape = useShapeContext();
  const shapeId = shape.id;
  const {t} = useTranslation();
  const form = useFormContext();
  const dispatch = useDispatch();
  const [systemPromptOpen, setSystemPromptOpen] = useState(false);

  /**
   * 提示词修改后的回调
   *
   * @param promptText 提示词
   * @private
   */
  const onChange = (promptText) => {
    dispatch({actionType: 'changePrompt', id: prompt.id, value: promptText});
    form.setFieldsValue({[name]: promptText});
  };

  /**
   * 点击生成提示词后的回调
   *
   * @param applyPrompt 弹窗回调函数
   * @return {(function(): void)|*}
   */
  const triggerAIGenerateEvent = (applyPrompt) => {
    return () => {
      shape.page.triggerEvent({
        type: 'GENERATE_AI_PROMPT',
        value: {
          model: DEFAULT_AP_PROMPT_MODEL_CONFIG.MODEL,
          temperature: DEFAULT_AP_PROMPT_MODEL_CONFIG.TEMPERATURE,
          type: 'system',
          applyPrompt: (promptText) => {
            applyPrompt(promptText);
          },
        },
      });
    };
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={[`AiPromptPanel-${shapeId}`]}>
      {<Panel
        key={`AiPromptPanel-${shapeId}`}
        header={<AiPromptPanelHeader
          triggerAIGenerateEvent={triggerAIGenerateEvent}
          onChange={onChange}
          disabled={disabled}
          setSystemPromptOpen={setSystemPromptOpen}
          popoverContent={popoverContent}
          header={header}
          t={t}/>}
        className='jade-panel'
      >
        <div className={'ai-prompt-panel-content'}>
          <div style={{position: 'relative'}}>
            <TextArea
              disabled={disabled}
              className='ai-prompt-input'
              maxLength={maxPromptLength}
              onChange={(e) => onChange(e.target.value)}
              value={prompt.value}
              style={{paddingBottom: '24px'}} // 为了避免文字被计数器覆盖，增加底部内边距
            />
          </div>
          <PromptDrawer
            maxLength={maxPromptLength}
            value={prompt.value}
            name={name}
            title={drawerTitle}
            rules={[]}
            placeHolder={''}
            container={shape.page.graph.div.parentElement}
            open={systemPromptOpen}
            onClose={() => setSystemPromptOpen(false)}
            onConfirm={(v) => onChange(v)}
            allowAIGenerate={true}
            onAIGenerate={triggerAIGenerateEvent}/>
        </div>
      </Panel>}
    </Collapse>
  </>);
};

_AiPromptPanel.propTypes = {
  disabled: PropTypes.bool,
  prompt: PropTypes.object.isRequired,
  popoverContent: PropTypes.element.isRequired,
  name: PropTypes.string.isRequired,
  header: PropTypes.string.isRequired,
  drawerTitle: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.prompt === nextProps.prompt &&
    prevProps.popoverContent === nextProps.popoverContent &&
    prevProps.name === nextProps.name &&
    prevProps.header === nextProps.header &&
    prevProps.drawerTitle === nextProps.drawerTitle;
};

export const AiPromptPanel = React.memo(_AiPromptPanel, areEqual);

/**
 * AI提示词组件配置面板header
 *
 * @param disabled 是否禁用
 * @param triggerAIGenerateEvent AI生成提示词弹窗
 * @param onChange 提示词修改后的回调
 * @param setSystemPromptOpen 设置提示词弹窗是否打开的方法
 * @param header 折叠区域标题文字
 * @param popoverContent popover内容
 * @returns {Element}
 * @constructor
 */
const AiPromptPanelHeader = ({
  disabled,
  triggerAIGenerateEvent,
  onChange,
  setSystemPromptOpen,
  popoverContent,
  header,
  }) => {
  /**
   * 创建button
   *
   * @param buttonConfig button配置
   * @return {JSX.Element}
   */
  const createButton = (buttonConfig) => {
    return (<>
      <Button
        disabled={disabled}
        type='text'
        className='icon-button'
        style={{height: '100%'}}
        onClick={(e) => {
          e.stopPropagation();
          buttonConfig.onClick();
        }}>
        {buttonConfig.icon}
      </Button>
    </>);
  };

  return (<>
    <div className='panel-header'>
      <span className='jade-panel-header-font'
            style={{whiteSpace: 'nowrap', textOverflow: 'ellipsis'}}>{header}</span>
      {popoverContent !== null && <Popover
        content={popoverContent}
        align={{offset: [0, 3]}}
        overlayClassName={'jade-custom-popover'}
      >
        <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
      </Popover>}
      <div className={'prompt-panel-header-buttons'}>
        {[{
          icon: <AiPromptIcon/>, onClick: triggerAIGenerateEvent((promptText) => {
            onChange(promptText);
          }),
        }, {
          icon: <FullScreenIcon/>, onClick: () => {
            setSystemPromptOpen(true);
          },
        }].map(bc => createButton(bc))}
      </div>
    </div>
  </>);
};

AiPromptPanelHeader.propTypes = {
  disabled: PropTypes.bool,
  triggerAIGenerateEvent: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  setSystemPromptOpen: PropTypes.func.isRequired,
  header: PropTypes.string.isRequired,
  popoverContent: PropTypes.string.isRequired,
};