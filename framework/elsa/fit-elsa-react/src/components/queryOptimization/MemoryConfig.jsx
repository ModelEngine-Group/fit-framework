/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Form, Radio, Slider} from 'antd';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import React, {useEffect, useState} from 'react';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';
import {useTranslation} from 'react-i18next';
import {getConfigValue} from '@/components/util/JadeConfigUtils.js';

const BUFFER_WINDOW = 'buffer_window';

/**
 * 配置复选框组件，切换问题数/对话数或者token
 *
 * @param disabled 是否禁用
 * @param memoryConfig 历史配置
 * @param templateType 模板类型
 * @param selectedOption option类型
 * @param useMemoryType 历史类型
 * @returns {React.JSX.Element} 配置复选框组件，切换问题数/对话数或者token
 * @constructor
 */
export const RadioGroup = ({disabled, memoryConfig, templateType, selectedOption, useMemoryType}) => {
  const itemId = memoryConfig.id;
  const dispatch = useDispatch();
  const {t} = useTranslation();

  /**
   * 获取对话或者问题配置的label
   *
   * @return {string} 对话或者问题配置的label
   */
  const getLabel = () => {
    if (templateType === 'custom' && useMemoryType === 'question_only') {
      return t('queryConfig');
    } else {
      return t('conversationConfig');
    }
  };

  /**
   * 获取radio单选框的选项
   *
   * @return {[{label: string, value: string},{label: string, value: string}]}
   */
  const getWindowOptions = () => {
    if (useMemoryType === 'full') {
      return [{label: t('conversationNumber'), value: BUFFER_WINDOW}, {label: 'token', value: 'token_window'}];
    } else {
      return [{label: t('queryNumber'), value: BUFFER_WINDOW}, {label: 'token', value: 'token_window'}];
    }
  };

  /**
   * 切换单选框
   *
   * @param e event事件
   */
  const handleOptionChange = (e) => {
    // 更改单选框选择的类型（问题数或token）
    dispatch({actionType: 'changeWindowType', value: e.target.value});
  };

  return (<>
    <Form.Item
      className='jade-form-item'
      label={getLabel()}
      name={`byConversationTurn-${itemId}`}
      rules={[{required: true, message: t('conversationTurnCannotBeEmpty')}]}
      validateTrigger='onBlur'
      initialValue={selectedOption}
    >
      <Radio.Group
        disabled={disabled}
        options={getWindowOptions()}
        onChange={handleOptionChange}
        value={selectedOption}
        buttonStyle='solid'
        style={{marginLeft: 8}}
      />
    </Form.Item>
  </>);
};

RadioGroup.propTypes = {
  disabled: PropTypes.bool,
  memoryConfig: PropTypes.object,
  templateType: PropTypes.string,
  selectedOption: PropTypes.object,
  useMemoryType: PropTypes.string,
};

/**
 * 配置值组件
 *
 * @param disabled 是否禁用
 * @param propertyValue 属性值
 * @param sliderConfig 配置.
 * @returns {React.JSX.Element} 配置值组件
 * @constructor
 */
export const ConfigSlider = ({disabled, propertyValue, sliderConfig}) => {
  const dispatch = useDispatch();

  const getDefaultRecalls = () => {
    const recalls = {};
    recalls[sliderConfig.min] = `${sliderConfig.min}`;
    recalls[sliderConfig.default] = `${sliderConfig.default}`;
    recalls[sliderConfig.max] = `${sliderConfig.max}`;
    return recalls;
  };

  return (<>
    <Slider disabled={disabled}
            className='jade-slider'
            style={{width: '90%'}}
            min={sliderConfig.min}
            max={sliderConfig.max}
            value={propertyValue}
            marks={getDefaultRecalls()}
            defaultValue={sliderConfig.default}
            onChange={(value) => dispatch({actionType: 'changeWindowValue', value: value})}
    />
  </>);
};

ConfigSlider.propTypes = {
  disabled: PropTypes.bool,
  propertyValue: PropTypes.number,
  sliderConfig: PropTypes.object,
};

/**
 * 记忆设置组件，包括了选择使用历史记录方式下拉框（可能不展示）、切换按钮和滑块组件
 *
 * @param memoryConfig 记忆设置对象
 * @param disabled 是否禁用
 * @param templateType 模板类型
 * @param isShowUseMemoryType 是否展示切换历史记录使用方式的下拉框
 * @param historyOption 历史记录消费方式选项
 * @return {JSX.Element} 记忆设置组件
 * @private
 */
const _MemoryConfig = ({memoryConfig, disabled, templateType, isShowUseMemoryType, historyOption}) => {
  const itemId = memoryConfig.id;
  const useMemoryType = getConfigValue(memoryConfig, ['serializeAlg']);
  const propertyValue = getConfigValue(memoryConfig, ['property']);
  const selectedOption = getConfigValue(memoryConfig, ['windowAlg']);
  const dispatch = useDispatch();
  const {t} = useTranslation();

  const shape = useShapeContext();
  const startNode = shape.page.sm.findShapeBy(s => s.type === 'startNodeStart');
  const [maxConversationTurn, setMaxConversationTurn] = useState(startNode.getConversationTurn());

  /**
   * 选择历史记录方式下拉框click回调
   *
   * @param event event事件
   */
  const handleSelectClick = (event) => {
    event.stopPropagation(); // 阻止事件冒泡
  };

  /**
   * 切换历史记录方式回调函数
   *
   * @param e value
   */
  const changeHistoryType = (e) => {
    // 更改使用历史记录方式的类型
    dispatch({actionType: 'changeHistoryType', value: e});
  };

  // 监听开始节点中，对话轮数的修改.
  useEffect(() => {
    const cancel = shape.observeTo('start_node_conversation_turn_count', startNode.id, 'start_node_conversation_turn_count',
        (args) => {
          if (args.value === null || args.value === undefined) {
            return;
          }
          setMaxConversationTurn(args.value);
        });
    return () => {
      cancel();
    };
  }, []);

  // 对话轮数变化时，如果最大轮次数小于当前的值，将值修改为最大轮次数.
  useEffect(() => {
    if (selectedOption === BUFFER_WINDOW) {
      if (maxConversationTurn < propertyValue) {
        dispatch({actionType: 'changeWindowValue', value: maxConversationTurn});
      }
    }
  }, [maxConversationTurn]);

  /**
   * 获取slider的配置数据.
   *
   * @return 配置数据.
   */
  const getSliderConfig = () => {
    if (selectedOption !== BUFFER_WINDOW) {
      return {
        min: 1,
        max: 3000,
        default: 1500,
      };
    }
    return {
      min: 1,
      max: maxConversationTurn,
      default: maxConversationTurn < 3 ? maxConversationTurn : 3,
    };
  };

  return (<>
    <div className={`jade-multi-conversation}`}>
      {isShowUseMemoryType && <Form.Item
        key={itemId}
        className='jade-form-item'
        label={t('selectHistoryRecordMode')}
        name={`multiConversationType-${itemId}`}
        rules={[{required: true}]}
        validateTrigger='onBlur'
        initialValue={useMemoryType}
      >
        <JadeStopPropagationSelect
          className='jade-select'
          disabled={disabled}
          style={{width: '100%', marginBottom: '8px', marginTop: '8px'}}
          onClick={handleSelectClick} // 点击下拉框时阻止事件冒泡
          onChange={e => changeHistoryType(e)}
          options={historyOption}
          value={useMemoryType}
        />
      </Form.Item>}
      <RadioGroup memoryConfig={memoryConfig} disabled={disabled} selectedOption={selectedOption}
                  templateType={templateType} useMemoryType={useMemoryType}/>
      <ConfigSlider
          selectedOption={selectedOption}
          disabled={disabled}
          sliderConfig={getSliderConfig()}
          propertyValue={propertyValue}/>
    </div>
  </>);
};

_MemoryConfig.propTypes = {
  memoryConfig: PropTypes.object.isRequired, // 确保 memoryConfig 是一个必需的object类型
  templateType: PropTypes.string.isRequired, // 确保  是一个必需的函数类型
  disabled: PropTypes.bool.isRequired,
  historyOption: PropTypes.array.isRequired,
  isShowUseMemoryType: PropTypes.bool.isRequired,
};

/**
 *
 *
 * @param prevProps
 * @param nextProps
 * @return {boolean}
 */
const areEqual = (prevProps, nextProps) => {
  return prevProps.memoryConfig === nextProps.memoryConfig &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.templateType === nextProps.templateType &&
    prevProps.historyOption === nextProps.historyOption &&
    prevProps.isShowUseMemoryType === nextProps.isShowUseMemoryType;
};

export const MemoryConfig = React.memo(_MemoryConfig, areEqual);