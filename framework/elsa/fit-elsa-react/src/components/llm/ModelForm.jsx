/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Collapse, Form, Row, Slider} from 'antd';
import {useDispatch, useFormContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {Trans, useTranslation} from 'react-i18next';
import React, {useEffect, useRef, useState} from 'react';
import {Model} from '@/components/llm/Model.jsx';
import {Prompt} from '@/components/common/prompt/Prompt.jsx';
import AiPromptIcon from '../asserts/icon-ai-prompt.svg?react';
import FullScreenIcon from '../asserts/icon-full-screen.svg?react';
import {DEFAULT_AP_PROMPT_MODEL_CONFIG} from '../../common/Consts.js';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';

const {Panel} = Collapse;

/**
 * 大模型节点模型表单。
 *
 * @param shapeId 所属图形唯一标识。
 * @param modelData 数据.
 * @param modelOptions 模型选项。
 * @param disabled 是否禁用.
 * @returns {JSX.Element} 大模型节点模型表单的DOM。
 */
const _ModelForm = ({shapeId, modelData, modelOptions, disabled}) => {
  const shape = useShapeContext();
  const dispatch = useDispatch();
  const form = useFormContext();
  const {t} = useTranslation();
  const startNode = shape.page.sm.findShapeBy(s => s.type === 'startNodeStart');
  const maxMemoryRounds = modelData.maxMemoryRounds;
  const maxMemoryRoundsValue = parseInt(maxMemoryRounds.value);
  const [maxTurnsOfStartNode, setMaxTurnsOfStartNode] = useState(startNode.getConversationTurn());
  const [promptOpen, setPromptOpen] = useState(false);
  const [systemPromptOpen, setSystemPromptOpen] = useState(false);

  // 实时记录轮次数，用于比较.
  const maxMemoryRoundsValueRef = useRef(maxMemoryRoundsValue);

  const promptContent = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='promptPopover' components={{p: <p/>}}/>
  </div>);

  // 当轮次改变时触发.
  const onConversationTurnChange = (e) => {
    dispatch({type: 'changeConfig', id: maxMemoryRounds.id, value: String(e)});
    maxMemoryRoundsValueRef.current = e;
  };

  // 监听开始节点中，对话轮数的修改.
  useEffect(() => {
    shape.observeTo('start_node_conversation_turn_count', startNode.id, 'start_node_conversation_turn_count',
      (args) => {
        if (args.value === null || args.value === undefined) {
          return;
        }
        setMaxTurnsOfStartNode(args.value);
        if (args.value < maxMemoryRoundsValueRef.current) {
          dispatch({type: 'changeConfig', id: maxMemoryRounds.id, value: String(args.value)});
          maxMemoryRoundsValueRef.current = args.value;
        }
      });
  }, []);

  const systemPromptName = `system-prompt-${shapeId}`;

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
    <JadeCollapse defaultActiveKey={['modelPanel']}>
      {<Panel
        key={'modelPanel'}
        header={<div className='panel-header'>
          <span className='jade-panel-header-font'>{t('llm')}</span>
        </div>}
        className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          <Model
            disabled={disabled} model={modelData.model} serviceName={modelData.serviceName} tag={modelData.tag} shapeId={shapeId}
            modelOptions={modelOptions}
            temperature={modelData.temperature}/>
          <DialogueRound
            maxMemoryRoundsValue={maxMemoryRoundsValue} disabled={disabled} maxTurnsOfStartNode={maxTurnsOfStartNode}
            shapeId={shapeId} onConversationTurnChange={onConversationTurnChange}/>
          <Prompt
            prompt={modelData.prompt}
            rules={[{required: true, message: t('paramCannotBeEmpty')}]}
            name={`prompt-${shapeId}`}
            onChange={(promptText) => {
              dispatch({type: 'changePrompt', id: modelData.prompt.id, value: promptText});
            }}
            buttonConfigs={[{
              icon: <FullScreenIcon/>, onClick: () => {
                setPromptOpen(true);
              },
            }]}
            tips={promptContent}
            disabled={disabled}
            placeHolder={t('promptPlaceHolder')}
            open={promptOpen}
            setOpen={setPromptOpen}
            title={t('prompt')}/>
          <Prompt
            prompt={modelData.systemPrompt}
            rules={[]}
            name={systemPromptName}
            allowAIGenerate={true}
            onAIGenerate={triggerAIGenerateEvent}
            onChange={(promptText) => {
              dispatch({type: 'changeConfig', id: modelData.systemPrompt.id, value: promptText});
            }}
            buttonConfigs={[{
              icon: <AiPromptIcon/>, onClick: triggerAIGenerateEvent((promptText) => {
                if (promptText === '') {
                  return;
                }
                dispatch({type: 'changeConfig', id: modelData.systemPrompt.id, value: promptText});
                form.setFieldsValue({[systemPromptName]: promptText});
              }),
            }, {
              icon: <FullScreenIcon/>, onClick: () => {
                setSystemPromptOpen(true);
              },
            }]}
            disabled={disabled}
            placeHolder={t('systemPromptPlaceHolder')}
            open={systemPromptOpen}
            setOpen={setSystemPromptOpen}
            title={t('systemPrompt')}/>
        </div>
      </Panel>}
    </JadeCollapse>
  </>);
};

_ModelForm.propTypes = {
  shapeId: PropTypes.string.isRequired, // 确保 shapeId 是一个必需的string类型
  modelData: PropTypes.object.isRequired, // 确保 modelData 是一个必需的object类型
  modelOptions: PropTypes.array.isRequired, // 确保 modelOptions 是一个必需的array类型
  disabled: PropTypes.bool, // 确保 modelOptions 是一个必需的array类型
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.modelData.model === nextProps.modelData.model &&
    prevProps.modelData.temperature === nextProps.modelData.temperature &&
    prevProps.modelData.systemPrompt === nextProps.modelData.systemPrompt &&
    prevProps.modelData.maxMemoryRounds === nextProps.modelData.maxMemoryRounds &&
    prevProps.modelData.prompt === nextProps.modelData.prompt &&
    prevProps.modelOptions === nextProps.modelOptions &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.modelData.serviceName === nextProps.modelData.serviceName;
};

/**
 * 对话轮次组件
 *
 * @param t 国际化
 * @param shapeId 图形id
 * @param maxMemoryRoundsValue 轮次值
 * @param maxTurnsOfStartNode 开始节点最大轮次
 * @param disabled 是否禁用
 * @param onConversationTurnChange 轮次更改后的回调
 * @return {JSX.Element} 对话轮次组件
 * @constructor
 */
const DialogueRound = ({shapeId, maxMemoryRoundsValue, maxTurnsOfStartNode, disabled, onConversationTurnChange}) => {
  const {t} = useTranslation();

  return (<>
    <Row gutter={16}>
      <Col span={24}>
        <Form.Item
          className='jade-form-item'
          label={t('pleaseSelectADialogueRound')}
          name={`llm-conversation-turn-${shapeId}`}
          rules={[{required: true, message: t('conversationTurnCannotBeEmpty')}]}
          validateTrigger='onBlur'
          initialValue={maxMemoryRoundsValue}
        >
          <div style={{display: 'flex', alignItems: 'center'}}>
            <Slider style={{width: '95%'}} // 设置固定宽度
                    min={0}
                    max={maxTurnsOfStartNode}
                    disabled={disabled}
                    marks={{[0]: 0, [maxTurnsOfStartNode]: maxTurnsOfStartNode}}
                    defaultValue={maxMemoryRoundsValue}
                    step={1} // 设置步长为1
                    onChange={onConversationTurnChange}
                    value={maxMemoryRoundsValue}
            />
          </div>
        </Form.Item>
      </Col>
    </Row>
  </>);
};

DialogueRound.propTypes = {
  shapeId: PropTypes.string.isRequired,
  maxMemoryRoundsValue: PropTypes.number.isRequired,
  maxTurnsOfStartNode: PropTypes.number.isRequired,
  disabled: PropTypes.bool,
  onConversationTurnChange: PropTypes.func,
};

export const ModelForm = React.memo(_ModelForm, areEqual);