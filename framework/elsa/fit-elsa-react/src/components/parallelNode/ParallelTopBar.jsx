/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {Button} from 'antd';
import {useShapeContext} from '@/components/DefaultRoot.jsx';
import React from 'react';
import {convertParameter, convertReturnFormat} from '@/components/util/MethodMetaDataParser.js';
import {PlusOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';

/**
 * 并行节点头部工具栏组件
 *
 * @param handlePluginChange 选项修改后的回调.
 * @param disabled 禁用状态.
 * @return {JSX.Element}
 * @constructor
 */
const _ParallelTopBar = ({handlePluginChange, disabled}) => {
  const shape = useShapeContext();

  const onSelect = (selectedData) => {
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
        isRequired: selectedData.schema.parameters.required.some(item => item === key),
      });
    });
    entity.outputParams = [convertReturnFormat(selectedData.schema.return)];
    handlePluginChange(entity, selectedData.uniqueName, selectedData.name, selectedData.tags);
  };

  const triggerSelect = (e) => {
    e.preventDefault();
    shape.page.triggerEvent({
      type: 'SELECT_PARALLEL_PLUGINS',
      value: {
        shapeId: shape.id,
        onSelect: onSelect,
      },
    });
    e.stopPropagation(); // 阻止事件冒泡
  };

  return (<>
    <div style={{position: 'relative'}}>
      <Button
        disabled={disabled}
        type="text" className="icon-button jade-panel-header-icon-position"
        onClick={(event) => triggerSelect(event)}>
        <PlusOutlined/>
      </Button>
    </div>
  </>);
};

_ParallelTopBar.propTypes = {
  handlePluginChange: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
};

const areEqual = (prevProps, nextProps) => {
  return prevProps.handlePluginChange === nextProps.handlePluginChange && prevProps.disabled === nextProps.disabled;
};

export const ParallelTopBar = React.memo(_ParallelTopBar, areEqual);