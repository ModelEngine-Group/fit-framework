/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {jadeNode} from '../base/jadeNode.jsx';
import {DEFAULT_FLOW_META} from '@/common/Consts.js';
import {variableUpdateDrawer} from '@/components/variableUpdate/variableUpdateDrawer.jsx';

/**
 * 变量更新节点.
 *
 * @override
 */
export const variableUpdateNodeState = (id, x, y, width, height, parent, drawer) => {
  const self = jadeNode(id, x, y, width, height, parent, drawer ? drawer : variableUpdateDrawer);
  self.text = '变量更新';
  self.type = 'variableUpdateNodeState';
  self.componentName = 'variableUpdateComponent';
  self.flowMeta.jober.fitables.push("modelengine.fit.jade.aipp.variable.updater");

  /**
   * @override
   */
  self.isAllowReferenceSystemEnv = () => {
    return false;
  };

  return self;
};