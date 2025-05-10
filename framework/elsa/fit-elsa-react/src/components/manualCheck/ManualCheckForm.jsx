/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Form, Image, Row} from 'antd';
import {OriginForm} from '@/components/manualCheck/OriginForm.jsx';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useState} from 'react';
import JadePanelCollapse from '@/components/manualCheck/JadePanelCollapse.jsx';
import {convertParameter, convertReturnFormat} from '@/components/util/MethodMetaDataParser.js';
import {useTranslation} from 'react-i18next';
import {EyeOutlined, MinusCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {recursive} from '@/components/util/ReferenceUtil.js';

/**
 * 人工检查节点折叠区域组件
 *
 * @param form 表单信息.
 * @param data 数据信息，用于删除监听使用.
 * @param handleFormChange 选项修改后的回调.
 * @param handleFormDelete 选项删除后的回调.
 * @param disabled 禁用状态.
 * @return {JSX.Element}
 * @constructor
 */
const _ManualCheckForm = ({form, data = undefined, handleFormChange, handleFormDelete, disabled}) => {
  const shape = useShapeContext();
  const [formInValid, setFormInValid] = useState(false);
  const {t} = useTranslation();

  // 根据不同的值渲染不同的组件
  const renderComponent = () => {
    if (!form || !form.id || form.id === '') {
      return <OriginForm text='plsSelectAFormPreview'/>;
    }
    if (form.imgUrl) {
      return (<div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
        <div className={'jade-custom-image-container'}>
          <Image
            src={form.imgUrl} style={{borderRadius: '4px'}}
            preview={{
              mask: (
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100%',
                    backgroundColor: 'transparent',
                  }}
                >
                  <EyeOutlined style={{fontSize: '20px', color: '#000'}}/> {/* 自定义图标 */}
                </div>
              ),
            }}/>
        </div>
      </div>);
    }
    return <OriginForm text='noPreviewImg'/>;
  };

  const onSelect = (selectedData) => {
    // 每次切换表单，需要先清除之前注册的observables.
    deregisterObservables();
    const entity = {};
    const orderProperties = selectedData.appearance.schema.parameters.order ?
      selectedData.appearance.schema.parameters.order : Object.keys(selectedData.appearance.schema.parameters.properties);
    entity.inputParams = orderProperties.map(key => {
      return convertParameter({
        propertyName: key,
        property: selectedData.appearance.schema.parameters.properties[key],
        isRequired: Array.isArray(selectedData.appearance.schema.parameters.required) ? selectedData.appearance.schema.parameters.required.includes(key) : false,
      });
    });
    entity.outputParams = [convertReturnFormat(selectedData.appearance.schema.return)];
    handleFormChange(selectedData.name, selectedData.id, selectedData.appearance?.imgUrl ?? undefined, entity);
  };

  const FormSelectEvent = {
    type: 'SELECT_FORM_BASE',
    value: {
      shapeId: shape.id,
      selectedForm: form?.id ?? undefined,
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
    shape.page.triggerEvent(FormSelectEvent);
    e.stopPropagation(); // 阻止事件冒泡
  };

  const renderDeleteIcon = (id) => {
    return (<>
      <Button disabled={disabled}
              type='text'
              className='icon-button'
              style={{height: '100%', marginLeft: 'auto', padding: '0 4px'}}
              onClick={() => {
                handleFormDelete(id);
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
            if (!form || !form.id) {
              return Promise.reject(new Error(t('formCannotBeEmpty')));
            }
            return Promise.resolve();
          },
        },
      ]}
      validateTrigger='onBlur' // 或者使用 "onChange" 进行触发校验
    >
      <JadePanelCollapse
        defaultActiveKey={['manualCheckFormPanel']}
        panelKey='manualCheckFormPanel'
        headerText={t('form')}
        panelStyle={{marginBottom: 8, borderRadius: '8px', width: '100%'}}
        disabled={disabled}
        triggerSelect={triggerSelect}
      >
        <div className={'jade-custom-panel-content'}>

          <Form.Item
            name={`formRow-${shape.id}`}
            rules={[
              {
                validator: () => {
                  const validateInfo = shape.graph.validateInfo?.find(node => node?.nodeId === shape.id);
                  if (!(validateInfo?.isValid ?? true)) {
                    const modelConfigCheck = validateInfo.configChecks?.find(configCheck => configCheck.configName === 'formId');
                    if (modelConfigCheck && modelConfigCheck.formId === form?.id) {
                      setFormInValid(true);
                      return Promise.reject(new Error(`${form?.name} ${t('selectedValueNotExist')}`));
                    }
                  }
                  setFormInValid(false);
                  return Promise.resolve();
                },
              },
            ]}
            validateTrigger='onBlur' // 或者使用 "onChange" 进行触发校验
          >
            {form && form.id && <Row key={`formRow-${form.id}`}>
              <div className={`jade-custom-multi-select-with-slider-div item-hover ${formInValid ? 'jade-error-border' : ''}`}>
            <span className={'jade-custom-multi-select-item'}>
                {form?.name ?? ''}
            </span>
                {renderDeleteIcon(form.id)}
              </div>
            </Row>}
          </Form.Item>

          {renderComponent()} {/* 渲染对应的组件 */}
        </div>
      </JadePanelCollapse>
    </Form.Item>
  </>);
};

_ManualCheckForm.propTypes = {
  form: PropTypes.object.isRequired,
  handleFormChange: PropTypes.func.isRequired,
  handleFormDelete: PropTypes.object.isRequired,
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.form === nextProps.form &&
    prevProps.data === nextProps.data &&
    prevProps.disabled === nextProps.disabled;
};

export const ManualCheckForm = React.memo(_ManualCheckForm, areEqual);