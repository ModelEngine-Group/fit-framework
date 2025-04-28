/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {InvokeInput} from '@/components/common/InvokeInput.jsx';
import {InvokeOutput} from '@/components/common/InvokeOutput.jsx';
import {SkillForm} from '@/components/loopNode/SkillForm.jsx';
import {useDataContext, useDispatch} from '@/components/DefaultRoot.jsx';
import {TOOL_TYPE} from '@/common/Consts.js';
import {ParallelTopBar} from '@/components/parallelNode/ParallelTopBar.jsx';
import {IntelligentInputFormItem} from '@/components/intelligentForm/IntelligentInputFormItem.jsx';
import {ParallelPluginItem} from '@/components/parallelNode/ParallelPluginItem.jsx';

/**
 * 并行节点Wrapper
 *
 * @param shapeStatus 图形状态
 * @returns {JSX.Element} 循环节点Wrapper的DOM
 */
const ParallelWrapper = ({shapeStatus}) => {
  const data = useDataContext();
  const dispatch = useDispatch();
  const inputData = data && data.inputParams;
  const tools = inputData && inputData.find(value => value.name === 'tools').value;

  const handlePluginChange = (entity, returnSchema, uniqueName, name, tags) => {
    dispatch({
      type: 'changePluginByMetaData',
      entity: entity,
      returnSchema: returnSchema,
      uniqueName: uniqueName,
      pluginName: name,
      tags: tags,
    });
  };

  const handlePluginDelete = (deletePluginId) => {
    dispatch({
      type: 'deletePlugin', formId: deletePluginId,
    });
  };

  return (<>
    <div>
      <ParallelTopBar handlePluginChange={handlePluginChange} disabled={shapeStatus.disabled}/>
      {tools.map((tool) => (
        <ParallelPluginItem key={tool.outputName} plugin={tool} handlePluginDelete={handlePluginDelete} shapeStatus={shapeStatus}/>
      ))}
    </div>
  </>);
};

export default ParallelWrapper;