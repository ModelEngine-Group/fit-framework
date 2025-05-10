/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {useDataContext, useDispatch, useShapeContext} from '@/components/DefaultRoot.jsx';
import {ParallelTopBar} from '@/components/parallelNode/ParallelTopBar.jsx';
import {ParallelPluginItem} from '@/components/parallelNode/ParallelPluginItem.jsx';
import {Form} from 'antd';
import {useTranslation} from 'react-i18next';
import PropTypes from 'prop-types';
import {useEffect, useMemo} from 'react';
import {OUTPUT, OUTPUT_NAME, TOOL_CALLS} from '@/components/parallelNode/consts.js';

/**
 * 并行节点Wrapper
 *
 * @param shapeStatus 图形状态
 * @returns {JSX.Element} 循环节点Wrapper的DOM
 */
const ParallelWrapper = ({shapeStatus}) => {
  const shape = useShapeContext();
  const data = useDataContext();
  const dispatch = useDispatch();
  const {t} = useTranslation();

  const tools = useMemo(
    () => data?.inputParams?.find(value => value.name === TOOL_CALLS)?.value ?? [],
    [data?.inputParams]
  );

  useEffect(() => {
    const output = data?.outputParams?.find(item => item.name === OUTPUT) ?? {};
    shape.page.registerObservable({
      nodeId: shape.id,
      observableId: output.id,
      value: output.name,
      type: output.type,
      parentId: null,
    });
  }, [data?.outputParams]);

  const handlePluginAdd = (entity, uniqueName, name, tags) => {
    dispatch({
      type: 'addPluginByMetaData',
      entity: entity,
      uniqueName: uniqueName,
      pluginName: name,
      tags: tags,
    });
  };

  const handlePluginDelete = (deletePluginId, outputName) => {
    dispatch({
      type: 'deletePlugin', id: deletePluginId, outputName: outputName,
    });
  };

  return (<>
    <div>
      <ParallelTopBar handlePluginAdd={handlePluginAdd} disabled={shapeStatus.disabled}/>
      <Form.Item
        name={`form-${shape.id}`}
        rules={[
          {
            validator: () => {
              if (tools.length < 1) {
                return Promise.reject(new Error(t('pluginCannotBeEmpty')));
              }
              return Promise.resolve();
            },
          },
        ]}
        validateTrigger="onBlur"
      >
        {tools.map((tool) => (
          <ParallelPluginItem key={tool?.value?.find(item => item.name === OUTPUT_NAME)?.value ?? ''} plugin={tool} handlePluginDelete={handlePluginDelete} shapeStatus={shapeStatus}/>
        ))}
      </Form.Item>
    </div>
  </>);
};

ParallelWrapper.propTypes = {
  shapeStatus: PropTypes.object,
};

export default ParallelWrapper;