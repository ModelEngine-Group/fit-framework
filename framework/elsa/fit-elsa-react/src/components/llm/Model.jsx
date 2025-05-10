/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Col, Form, InputNumber, Popover, Row} from 'antd';
import {JadeStopPropagationSelect} from '@/components/common/JadeStopPropagationSelect.jsx';
import {QuestionCircleOutlined} from '@ant-design/icons';
import React from 'react';
import {Trans, useTranslation} from 'react-i18next';
import {useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import PropTypes from 'prop-types';

/**
 * 模型选择组件
 *
 * @param shapeId 图形id
 * @param model 选择的模型
 * @param serviceName 模型名称
 * @param tag 模型标签
 * @param disabled 是否禁用
 * @param modelOptions 模型选项
 * @returns {React.JSX.Element}
 * @constructor
 */
const ModelSelect = ({shapeId, model, serviceName, tag, disabled, modelOptions}) => {
  const shape = useShapeContext();
  const {t} = useTranslation();
  const dispatch = useDispatch();

  const handleSelectClick = (event) => {
    event.stopPropagation(); // 阻止事件冒泡
  };

  return (<>
    <Form.Item
      className='jade-form-item'
      name={`model-${shapeId}`}
      label={t('model')}
      rules={[{required: true, message: t('pleaseSelectTheModelToBeUsed')}, {
        validator: (_, value) => {
          const validateInfo = shape.graph.validateInfo?.find(node => node?.nodeId === shape.id);
          if (value && !(validateInfo?.isValid ?? true)) {
            const modelConfigCheck = validateInfo.configChecks?.find(configCheck => configCheck.configName === 'accessInfo');
            if (modelConfigCheck && modelConfigCheck.serviceName === serviceName?.value && modelConfigCheck.tag === tag?.value) {
              return Promise.reject(new Error(`${modelConfigCheck.serviceName} ${t('selectedValueNotExist')}`));
            }
          }
          return Promise.resolve();
        },
      }]}
      initialValue={(serviceName?.value && tag?.value ? `${serviceName.value}&&${tag.value}` : null) ?? model?.value ?? serviceName?.value ?? ''} // 当组件套在Form.Item中的时候，内部组件的初始值使用Form.Item的initialValue进行赋值
      validateTrigger='onBlur'
    >
      <JadeStopPropagationSelect
        disabled={disabled}
        className='jade-select'
        onClick={handleSelectClick} // 点击下拉框时阻止事件冒泡
        onChange={(e) => dispatch({type: 'changeAccessInfoConfig', value: e})}
        options={modelOptions}
        dropdownMatchSelectWidth={false}
      />
    </Form.Item>
  </>);
};

/**
 * 温度输入组件
 *
 * @param shapeId 图形id
 * @param disabled 是否禁用
 * @param temperature 温度
 * @param inputNumberChangeOnBlur 组件输入回调
 * @returns {React.JSX.Element} 温度输入组件
 * @constructor
 */
const TemperatureInput = ({shapeId, disabled, temperature, inputNumberChangeOnBlur}) => {
  const {t} = useTranslation();
  const content = (<div className={'jade-font-size'} style={{lineHeight: '1.2'}}>
    <Trans i18nKey='llmTemperaturePopover' components={{p: <p/>}}/>
  </div>);

  const onKeyPress = (event) => {
    const {key, target} = event;

    // 只允许输入数字和点，且防止输入多个点
    if (!/[\d.]/.test(key) || (key === '.' && target.value.includes('.'))) {
      event.preventDefault();
    }
  };

  return (<>
    <Form.Item
      className='jade-form-item'
      name={`temperature-${shapeId}`}
      label={<div className={'required-after'} style={{display: 'flex', alignItems: 'center'}}>
        <span className='jade-second-title'>{t('temperature')}</span>
        <Popover
          content={content}
          align={{offset: [0, 3]}}
          overlayClassName={'jade-custom-popover'}
        >
          <QuestionCircleOutlined className='jade-panel-header-popover-content'/>
        </Popover>
      </div>}
      rules={[{required: true, message: t('pleaseEnterAValueRangingFrom0To1')}]}
      initialValue={temperature.value}
      validateTrigger='onBlur'
    >
      <InputNumber
        disabled={disabled}
        className='jade-input'
        style={{width: '100%'}}
        precision={1}
        min={0}
        max={1}
        step={0.1}
        onBlur={(e) => inputNumberChangeOnBlur(e, 'changeConfig', temperature.id, true)}
        stringMode
        onKeyPress={onKeyPress}
      />
    </Form.Item>
  </>);
};

/**
 * 大模型选择组件
 *
 * @param shapeId 图形id
 * @param disabled 是否禁用
 * @param model 选择的模型
 * @param modelOptions 模型选项
 * @param temperature 温度
 * @param serviceName 模型名称
 * @param tag 模型标签
 * @return {JSX.Element} 大模型选择组件
 * @private
 */
const _Model = ({shapeId, disabled, model, modelOptions, temperature, serviceName, tag}) => {
  const dispatch = useDispatch();

  /**
   * 数字输入对应失焦时才设置值，对于必填项.若为空，则不设置。并对其中值进行范围内标准化
   *
   * @param e
   * @param actionType
   * @param id
   * @param required
   */
  const inputNumberChangeOnBlur = (e, actionType, id, required) => {
    let originValue = parseFloat(e.target.value); // 将输入值转换为浮点数
    // 如果转换后的值不是数字（NaN），则将其设为 0
    if (isNaN(originValue)) {
      originValue = 0;
    }
    let changeValue;
    if (originValue <= 0.0) {
      changeValue = 0;
    } else if (originValue >= 1.0) {
      changeValue = 1;
    } else {
      changeValue = Math.round(originValue * 10) / 10; // 保留小数点后一位
    }
    dispatch({type: actionType, id: id, value: changeValue});
  };

  return (<>
    <Row gutter={16}>
      <Col span={12}>
        <ModelSelect
          modelOptions={modelOptions}
          shapeId={shapeId}
          model={model}
          disabled={disabled}
          serviceName={serviceName}
          tag={tag}/>
      </Col>
      <Col span={12}>
        <TemperatureInput temperature={temperature} shapeId={shapeId} disabled={disabled}
                          inputNumberChangeOnBlur={inputNumberChangeOnBlur}/>
      </Col>
    </Row>
  </>);
};

_Model.propTypes = {
  shapeId: PropTypes.string.isRequired, // 确保 shapeId 是一个必需的string类型
  modelOptions: PropTypes.array.isRequired, // 确保 modelOptions 是一个必需的array类型
  disabled: PropTypes.bool, // 确保 modelOptions 是一个必需的array类型
  temperature: PropTypes.object.isRequired,
  serviceName: PropTypes.object.isRequired,
  tag: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.model === nextProps.model &&
    prevProps.shapeId === nextProps.shapeId &&
    prevProps.modelOptions === nextProps.modelOptions &&
    prevProps.temperature === nextProps.temperature &&
    prevProps.serviceName === nextProps.serviceName &&
    prevProps.tag === nextProps.tag &&
    prevProps.disabled === nextProps.disabled;
};

export const Model = React.memo(_Model, areEqual);