/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import React from 'react';
import {Collapse, Popover} from 'antd';
import {Model} from '@/components/llm/Model.jsx';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {Trans, useTranslation} from 'react-i18next';
import {QuestionCircleOutlined} from '@ant-design/icons';
import TextArea from 'antd/es/input/TextArea.js';

const {Panel} = Collapse;

/**
 * 优化配置组件
 *
 * @param disabled 是否禁用
 * @param modelOptions 模板选项
 * @param description 提取要求描述
 * @param memoryConfig 记忆设置对象
 * @param temperature 温度对象
 * @param serviceName 模型对象
 * @param tag 模型对象标签
 * @return {JSX.Element} 优化配置组件
 * @private
 */
const _ModelConfig = ({
                        disabled,
                        modelOptions,
                        description,
                        temperature,
                        serviceName,
                        promptPopover,
                        promptTitle,
                        tag,
                      }) => {
  const shape = useShapeContext();
  const shapeId = shape.id;
  const {t} = useTranslation();
  const dispatch = useDispatch();

  /**
   * 根据是提示词或者对话背景，获取模板的提示信息
   *
   * @return {JSX.Element} 国际化后的提示信息
   */
  const getPromptContent = () => {
    return (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
      <Trans i18nKey={promptPopover} components={{p: <p/>}}/>
    </div>);
  };

  /**
   * 失焦时才设置值，对于必填项.若为空，则不设置
   *
   * @param e event
   * @param actionType 事件类型
   * @param id config的id
   */
  const onChange = (e, actionType, id) => {
    dispatch({actionType: actionType, id: id, value: e.target.value});
  };

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse'
              defaultActiveKey={[`commonModelPanel${shapeId}`]}>
      {<Panel
        key={`commonModelPanel${shapeId}`}
        header={<div className='panel-header'>
          <span className='jade-panel-header-font'>{t('llm')}</span>
        </div>}
        className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          <Model
            disabled={disabled} shapeId={shapeId} modelOptions={modelOptions} temperature={temperature}
            serviceName={serviceName} tag={tag}/>
          <span>
            <span className='jade-panel-header-font'>{t(promptTitle)}</span>
          </span>
          <Popover
            content={getPromptContent()}
            align={{offset: [0, 3]}}
            overlayClassName={'jade-custom-popover'}
          >
            <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
          </Popover>
          <TextArea
            value={description.value}
            disabled={disabled}
            maxLength={10000}
            className='jade-textarea-input jade-font-size'
            onChange={(e) => onChange(e, 'changePromptValue', description.id)}
          />
        </div>
      </Panel>}
    </Collapse>
  </>);
};

_ModelConfig.propTypes = {
  disabled: PropTypes.bool,
  modelOptions: PropTypes.array.isRequired,
  description: PropTypes.object.isRequired,
  temperature: PropTypes.object.isRequired,
  serviceName: PropTypes.object.isRequired,
  tag: PropTypes.object.isRequired,
  promptPopover: PropTypes.string.isRequired,
  promptTitle: PropTypes.string.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.disabled === nextProps.disabled &&
    prevProps.modelOptions === nextProps.modelOptions &&
    prevProps.temperature === nextProps.temperature &&
    prevProps.description === nextProps.description &&
    prevProps.serviceName === nextProps.serviceName &&
    prevProps.tag === nextProps.tag &&
    prevProps.promptPopover === nextProps.promptPopover &&
    prevProps.promptTitle === nextProps.promptTitle;
};

export const ModelConfig = React.memo(_ModelConfig, areEqual);