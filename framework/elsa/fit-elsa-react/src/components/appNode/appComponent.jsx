/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {AppWrapper} from '@/components/appNode/AppWrapper.jsx';
import {updateInput} from '@/components/util/JadeConfigUtils.js';
import {defaultComponent} from '@/components/defaultComponent.js';

/**
 * 应用和工具流调用节点组件
 *
 * @param jadeConfig
 */
export const appComponent = (jadeConfig) => {
  const self = defaultComponent(jadeConfig);

  /**
   * 必填
   *
   * @return 组件信息
   */
  self.getJadeConfig = () => {
    return jadeConfig ? jadeConfig : {
      inputParams: [],
      outputParams: [],
    };
  };

  /**
   * 获取当前节点的所有组件
   *
   * @return {JSX.Element}
   */
  self.getReactComponents = (shapeStatus, data) => {
    return (<>
      <AppWrapper shapeStatus={shapeStatus} data={data}/>
    </>);
  };

  /**
   * @override
   */
  const reducers = self.reducers;
  self.reducers = (config, action) => {
    let newConfig = {...config};

    switch (action.type) {
      case 'update': {
        newConfig.inputParams.find(item => item.name === 'inputParams').value =
          updateInput(config.inputParams.find(item => item.name === 'inputParams').value, action.id, action.changes);
        return newConfig;
      }
      case 'changeFlowMeta': {
        newConfig.enableStageDesc = action.data.enableStageDesc;
        newConfig.stageDesc = action.data.stageDesc;
        return newConfig;
      }
      default: {
        return reducers.apply(self, [config, action]);
      }
    }
  };

  return self;
};