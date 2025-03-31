/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ChatbotIcon from '../asserts/icon-chatbot-header.svg?react';
import AgentIcon from '../asserts/icon-agent-header.svg?react';
import WorkflowIcon from '../asserts/icon-workflow-header.svg?react';
import {baseToolNodeDrawer} from '@/components/base/baseToolNodeDrawer.jsx';

/**
 * 应用和工具流节点绘制器
 *
 * @override
 */
export const appNodeDrawer = (shape, div, x, y) => {
  const self = baseToolNodeDrawer(shape, div, x, y);
  self.type = 'appNodeDrawer';

  /**
   * @override
   */
  self.getHeaderIcon = () => {
    let IconComponent;
    switch (shape.flowMeta.jober.entity.appCategory) {
      case 'chatbot':
        IconComponent = <ChatbotIcon className='jade-node-custom-header-icon'/>;
        break;
      case 'agent':
        IconComponent = <AgentIcon className='jade-node-custom-header-icon'/>;
        break;
      case 'workflow':
        IconComponent = <WorkflowIcon className='jade-node-custom-header-icon'/>;
        break;
      default:
        IconComponent = <ChatbotIcon className='jade-node-custom-header-icon'/>;
        break;
    }
    return (<>
      {IconComponent}
    </>);
  };

  return self;
};