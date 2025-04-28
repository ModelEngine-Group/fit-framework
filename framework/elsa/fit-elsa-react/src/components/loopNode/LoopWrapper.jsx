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

/**
 * 循环节点Wrapper
 *
 * @param shapeStatus 图形状态
 * @returns {JSX.Element} 循环节点Wrapper的DOM
 */
const LoopWrapper = ({shapeStatus}) => {
  const data = useDataContext();
  const dispatch = useDispatch();
  const inputData = data && data.inputParams;
  const args = inputData && inputData.find(value => value.name === 'args').value;
  const radioValue = inputData?.find(value => value.name === 'config')?.value?.loopKeys?.[0] ?? null;
  const toolInfo = inputData && inputData.find(value => value.name === 'toolInfo').value;
  const outputData = data && data.outputParams;
  const isWaterFlow = toolInfo?.tags?.some(tag => tag === TOOL_TYPE.WATER_FLOW)
  const filterArgs = isWaterFlow ? args.find(arg => arg.name === 'inputParams')?.value ?? args : args;
  const filterRadioValue = isWaterFlow && radioValue ? radioValue.replace(/^inputParams\./, '') : radioValue;


  const handlePluginChange = (entity, uniqueName, name, tags) => {
    dispatch({
      type: 'changePluginByMetaData',
      entity: entity,
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

  /**
   * 组装插件对象。
   */
  const plugin = {name: toolInfo?.pluginName ?? undefined, id: toolInfo?.uniqueName ?? undefined};

  return (<>
    <div>
      {args.length > 0 && <InvokeInput inputData={filterArgs} shapeStatus={shapeStatus} showRadio={true} radioValue={filterRadioValue} radioTitle={'chooseToBeLoopParam'} radioRuleMessage={'loopRadioIsRequired'}/>}
      <SkillForm plugin={plugin} data={outputData} handlePluginChange={handlePluginChange} handlePluginDelete={handlePluginDelete} disabled={shapeStatus.disabled}/>
      {outputData.length > 0 && <InvokeOutput outputData={outputData}/>}
    </div>
  </>);
};

export default LoopWrapper;