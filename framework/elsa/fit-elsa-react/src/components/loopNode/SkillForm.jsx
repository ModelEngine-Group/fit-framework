/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Form, Row} from 'antd';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useState} from 'react';
import JadePanelCollapse from '@/components/manualCheck/JadePanelCollapse.jsx';
import {convertParameter, convertReturnFormat} from '@/components/util/MethodMetaDataParser.js';
import {useTranslation} from 'react-i18next';
import {MinusCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {recursive} from '@/components/util/ReferenceUtil.js';

/**
 * 循环节点插件折叠区域组件
 *
 * @param plugin 插件信息.
 * @param data 数据信息，用于删除监听使用.
 * @param handlePluginChange 选项修改后的回调.
 * @param handlePluginDelete 选项删除后的回调.
 * @param disabled 禁用状态.
 * @return {JSX.Element}
 * @constructor
 */
const _SkillForm = ({plugin, data = undefined, handlePluginChange, handlePluginDelete, disabled}) => {
  const shape = useShapeContext();
  const [pluginInValid, setPluginInValid] = useState(false);
  const {t} = useTranslation();

  const onSelect = (selectedData) => {
    // 每次切换表单，需要先清除之前注册的observables.
    deregisterObservables();
    const inputProperties = selectedData.schema?.parameters?.properties?.inputParams?.properties;
    if (inputProperties) {
      delete inputProperties.traceId;
      delete inputProperties.callbackId;
      delete inputProperties.userId;
    }
    const entity = {};
    const orderProperties = selectedData.schema.parameters.order ?
      selectedData.schema.parameters.order : Object.keys(selectedData.schema.parameters.properties);
    entity.inputParams = orderProperties.map(key => {
      return convertParameter({
        propertyName: key,
        property: selectedData.schema.parameters.properties[key],
        isRequired: Array.isArray(selectedData.schema.parameters.required) ? selectedData.schema.parameters.required.includes(key) : false,
      });
    });
    const outputParams = convertReturnFormat(selectedData.schema.return);
    outputParams.type = 'Array';
    entity.outputParams = [outputParams];
    handlePluginChange(entity, selectedData.uniqueName, selectedData.name, selectedData.tags);
  };

  const pluginSelectEvent = {
    type: 'SELECT_LOOP_PLUGIN',
    value: {
      shapeId: shape.id,
      selectedPlugin: plugin?.id ?? undefined,
      onSelect: onSelect,
    },
  };

  const deregisterObservables = () => {
    if (data) {
      recursive(data, null, (p) => {
        shape.page.removeObservable(shape.id, p.id);
      });
    }
  };

  const triggerSelect = (e) => {
    e.preventDefault();
    shape.page.triggerEvent(pluginSelectEvent);
    e.stopPropagation(); // 阻止事件冒泡
  };

  const renderDeleteIcon = (id) => {
    return (<>
      <Button disabled={disabled}
              type='text'
              className='icon-button'
              style={{height: '100%', marginLeft: 'auto', padding: '0 4px'}}
              onClick={() => {
                handlePluginDelete(id);
                deregisterObservables();
              }}>
        <MinusCircleOutlined/>
      </Button>
    </>);
  };

  return (<>
    <Form.Item
      name={`form-${shape.id}`}
      rules={[
        {
          validator: () => {
            if (!plugin || !plugin.id) {
              return Promise.reject(new Error(t('pluginCannotBeEmpty')));
            }
            return Promise.resolve();
          },
        },
      ]}
      validateTrigger='onBlur' // 或者使用 "onChange" 进行触发校验
    >
      <JadePanelCollapse
        defaultActiveKey={['loopSkillPanel']}
        panelKey='loopSkillPanel'
        headerText={t('tool')}
        panelStyle={{marginBottom: 8, borderRadius: '8px', width: '100%'}}
        disabled={disabled}
        triggerSelect={triggerSelect}
        popoverContent={t('loopSkillPopover')}
      >
        <div className={'jade-custom-panel-content'}>
          <Form.Item
            name={`formRow-${shape.id}`}
            rules={[
              {
                validator: () => {
                  const validateInfo = shape.graph.validateInfo?.find(node => node?.nodeId === shape.id);
                  if (!(validateInfo?.isValid ?? true)) {
                    const modelConfigCheck = validateInfo.configChecks?.find(configCheck => configCheck.configName === 'pluginId');
                    if (modelConfigCheck && modelConfigCheck.pluginId === plugin?.id) {
                      setPluginInValid(true);
                      return Promise.reject(new Error(`${plugin?.name} ${t('selectedValueNotExist')}`));
                    }
                  }
                  setPluginInValid(false);
                  return Promise.resolve();
                },
              },
            ]}
            validateTrigger='onBlur' // 或者使用 "onChange" 进行触发校验
          >
            {plugin && plugin.id && <Row key={`pluginRow-${plugin.id}`}>
              <div className={`jade-custom-multi-select-with-slider-div item-hover ${pluginInValid ? 'jade-error-border' : ''}`}>
                <span className={'jade-custom-multi-select-item'}>
                    {plugin?.name ?? ''}
                </span>
                {renderDeleteIcon(plugin.id)}
              </div>
            </Row>}
          </Form.Item>
        </div>
      </JadePanelCollapse>
    </Form.Item>
  </>);
};

_SkillForm.propTypes = {
  plugin: PropTypes.object.isRequired,
  handlePluginChange: PropTypes.func.isRequired,
  handlePluginDelete: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.plugin === nextProps.plugin &&
    prevProps.data === nextProps.data &&
    prevProps.disabled === nextProps.disabled;
};

export const SkillForm = React.memo(_SkillForm, areEqual);