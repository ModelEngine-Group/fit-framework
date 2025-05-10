/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button, Collapse, Form} from 'antd';
import {useDataContext, useShapeContext} from '@/components/DefaultRoot.jsx';
import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {MinusCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {InvokeInput} from '@/components/common/InvokeInput.jsx';
import {JadeCollapse} from '@/components/common/JadeCollapse.jsx';
import {TOOL_TYPE} from '@/common/Consts.js';
import {v4 as uuidv4} from 'uuid';
import {recursive} from '@/components/util/ReferenceUtil.js';

const {Panel} = Collapse;

/**
 * 并行节点插件配置组件
 *
 * @param plugin 插件信息.
 * @param handlePluginDelete 选项删除后的回调.
 * @param shapeStatus 图形状态.
 * @return {JSX.Element}
 * @constructor
 */
const _ParallelPluginItem = ({plugin, handlePluginDelete, shapeStatus}) => {
  const shape = useShapeContext();
  const data = useDataContext();
  const [pluginInValid, setPluginInValid] = useState(false);
  const {t} = useTranslation();
  const args = plugin?.value?.find(arg => arg.name === 'args')?.value ?? [];
  const isWaterFlow = plugin?.value?.find(arg => arg.name === 'tags')?.value ?? [].some(tag => tag === TOOL_TYPE.WATER_FLOW)
  const filterArgs = isWaterFlow ? args.find(arg => arg.name === 'inputParams')?.value ?? args : args;
  const outputName = plugin?.value?.find(item => item.name === 'outputName')?.value ?? '';
  const output = data?.outputParams?.find(arg => arg.name === 'output') ?? {};
  const registryOutputObject = output?.value?.find(arg => arg.name === outputName) ?? {};
  const virtualPluginOutputData = [{
    id: "output_" + uuidv4(),
    name: "output",
    type: registryOutputObject?.type,
    value: registryOutputObject?.value ?? []
  }];

  const registryNode = (nodeData, parent, shape) => {
    shape.page.registerObservable({
      nodeId: shape.id,
      observableId: nodeData.id,
      value: nodeData.name,
      type: nodeData.type,
      parentId: parent ? parent.id : null
    });
    if (nodeData.type === "Object") {
      nodeData?.value?.map(v => registryNode(v, nodeData, shape));
    }
  };

  useEffect(() => {
    if (!registryOutputObject) {
      return;
    }
    registryNode(registryOutputObject, output, shape);
  }, [registryOutputObject]);

  const deregisterObservables = () => {
    if (registryOutputObject) {
      recursive([registryOutputObject], output, (p) => {
        shape.page.removeObservable(shape.id, p.id);
      });
    }
  };

  const renderDeleteIcon = (id, outputName) => {
    return (<>
      <Button disabled={shapeStatus.disabled}
              type="text"
              className="icon-button"
              style={{height: '100%', marginLeft: 'auto', padding: '0 4px'}}
              onClick={() => {
                handlePluginDelete(id, outputName);
                deregisterObservables();
              }}>
        <MinusCircleOutlined/>
      </Button>
    </>);
  };

  return (<>
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
      validateTrigger="onBlur" // 或者使用 "onChange" 进行触发校验
    >
      <div className={`item-hover ${pluginInValid ? 'jade-error-border' : ''}`}>
        <JadeCollapse defaultActiveKey={['parallelPanel']}>
          <Panel
            className="jade-panel"
            header={<div style={{display: 'flex', alignItems: 'center'}}>
              <span className="jade-panel-header-font">{plugin?.value?.find(item => item.name === 'outputName')?.value ?? ''}</span>
              {renderDeleteIcon(plugin.id, outputName)}
            </div>}
            key="parallelPanel">
            <div className={'jade-custom-panel-content'}>
              {filterArgs.length > 0 && <InvokeInput inputData={filterArgs} shapeStatus={shapeStatus} parentId={plugin.id}/>}
              {virtualPluginOutputData.length > 0 && <InvokeOutput outputData={virtualPluginOutputData} isObservableTree={false}/>}
            </div>
          </Panel>
        </JadeCollapse>
      </div>
    </Form.Item>
  </>);
};

_ParallelPluginItem.propTypes = {
  plugin: PropTypes.object.isRequired,
  handlePluginDelete: PropTypes.func.isRequired,
  shapeStatus: PropTypes.object.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.plugin === nextProps.plugin &&
    prevProps.handlePluginDelete === nextProps.handlePluginDelete &&
    prevProps.shapeStatus === nextProps.shapeStatus;
};

export const ParallelPluginItem = React.memo(_ParallelPluginItem, areEqual);