/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PropTypes from 'prop-types';
import React from 'react';
import {Collapse} from 'antd';
import {Model} from '@/components/llm/Model.jsx';
import {Template} from '@/components/queryOptimization/Template.jsx';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import {MemoryConfig} from '@/components/queryOptimization/MemoryConfig.jsx';
import {useTranslation} from 'react-i18next';

const {Panel} = Collapse;

/**
 * 优化配置组件
 *
 * @param disabled 是否禁用
 * @param modelOptions 模板选项
 * @param template 提示词
 * @param memoryConfig 记忆设置对象
 * @param temperature 温度对象
 * @param strategy 策略，表示模板类型
 * @param serviceName 模型对象
 * @param tag 模型对象标签
 * @return {JSX.Element} 优化配置组件
 * @private
 */
const _OptimizationConfig = ({disabled, modelOptions, template, memoryConfig, temperature, strategy, serviceName, tag}) => {
  const shape = useShapeContext();
  const shapeId = shape.id;
  const {t} = useTranslation();

  /**
   * 历史记录消费方式options
   *
   * @type {[{label: string, value: string},{label: string, value: string}]}
   */
  const historyOption = [{label: t('byConversation'), value: 'full'}, {label: t('byQuery'), value: 'question_only'}];

  return (<>
    <Collapse bordered={false} className='jade-custom-collapse' defaultActiveKey={[`optimizationPanel${shapeId}`]}>
      {<Panel
        key={`optimizationPanel${shapeId}`}
        header={<div className='panel-header'>
          <span className='jade-panel-header-font'>{t('optimizationConfig')}</span>
        </div>}
        className='jade-panel'
      >
        <div className={'jade-custom-panel-content'}>
          <Model
            disabled={disabled} shapeId={shapeId} modelOptions={modelOptions} temperature={temperature}
            serviceName={serviceName} tag={tag}/>
          <Template shapeId={shapeId} template={template} templateType={strategy.value} disabled={disabled}/>
          <MemoryConfig memoryConfig={memoryConfig} disabled={disabled} templateType={strategy.value}
                        isShowUseMemoryType={strategy.value === 'custom'} historyOption={historyOption}/>
        </div>
      </Panel>}
    </Collapse>
  </>);
};

_OptimizationConfig.propTypes = {
  disabled: PropTypes.bool,
  modelOptions: PropTypes.array.isRequired,
  model: PropTypes.object.isRequired,
  template: PropTypes.object.isRequired,
  memoryConfig: PropTypes.object.isRequired,
  temperature: PropTypes.object.isRequired,
  strategy: PropTypes.object.isRequired,
  serviceName: PropTypes.object.isRequired,
  tag: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.model === nextProps.model &&
    prevProps.disabled === nextProps.disabled &&
    prevProps.modelOptions === nextProps.modelOptions &&
    prevProps.memoryConfig === nextProps.memoryConfig &&
    prevProps.temperature === nextProps.temperature &&
    prevProps.template === nextProps.template &&
    prevProps.strategy === nextProps.strategy &&
    prevProps.serviceName === nextProps.serviceName &&
    prevProps.tag === nextProps.tag;
};

export const OptimizationConfig = React.memo(_OptimizationConfig, areEqual);