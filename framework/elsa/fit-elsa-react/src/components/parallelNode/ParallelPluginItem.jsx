/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Form, Row} from 'antd';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useState} from 'react';
import JadePanelCollapse from '@/components/manualCheck/JadePanelCollapse.jsx';
import {useTranslation} from 'react-i18next';
import {MinusCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {InvokeInput} from '@/components/common/InvokeInput.jsx';

/**
 * 并行节点插件配置组件
 *
 * @param plugin 插件信息.
 * @param data 数据信息，用于删除监听使用.
 * @param handlePluginDelete 选项删除后的回调.
 * @param shapeStatus 图形状态.
 * @return {JSX.Element}
 * @constructor
 */
const _ParallelPluginItem = ({plugin, data, handlePluginDelete, shapeStatus}) => {
  const shape = useShapeContext();
  const [pluginInValid, setPluginInValid] = useState(false);
  const {t} = useTranslation();
  const args = plugin.value?.find(arg => arg.name === 'args').value;
  const outputData = data && data.outputParams;

  const recursive = (params, parent, action) => {
    params.forEach(p => {
      if (p.type === 'Object') {
        recursive(p.value, p, action);
        action(p, parent);
      } else {
        action(p, parent);
      }
    });
  };

  const deregisterObservables = () => {
    if (data) {
      recursive(data, null, (p) => {
        shape.page.removeObservable(shape.id, p.id);
      });
    }
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
        {args.length > 0 && <InvokeInput inputData={args} shapeStatus={shapeStatus}/>}
        {outputData.length > 0 && <InvokeOutput outputData={outputData}/>}
      </JadePanelCollapse>
    </Form.Item>
  </>);
};

_ParallelPluginItem.propTypes = {
  plugin: PropTypes.object.isRequired,
  data: PropTypes.object.isRequired,
  handlePluginDelete: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.plugin === nextProps.plugin &&
    prevProps.data === nextProps.data &&
    prevProps.handlePluginDelete === nextProps.handlePluginDelete &&
    prevProps.shapeStatus === nextProps.shapeStatus;
};

export const ParallelPluginItem = React.memo(_ParallelPluginItem, areEqual);