/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from '@fit-elsa/elsa-core';
import {DATA_TYPES, VIRTUAL_CONTEXT_NODE, VIRTUAL_CONTEXT_NODE_VARIABLES} from '@/common/Consts.js';
import {emptyStatusManager} from '@/components/base/emptyStatusManager.js';

/**
 * 系统变量.
 *
 * @overview
 */
export const systemEnv = (id, x, y, width, height, parent) => {
  const self = rectangle(id, x, y, width, height, parent);
  self.type = 'systemEnv';
  self.serializable = false;
  const i18n = self.graph.i18n;
  self.text = i18n?.t(VIRTUAL_CONTEXT_NODE.name) ?? VIRTUAL_CONTEXT_NODE.name;
  self.visible = false;
  self.x = 0;
  self.y = 0;
  self.width = 0;
  self.height = 0;
  self.virtualNodeInfoList = [
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.INSTANCE_ID, value: VIRTUAL_CONTEXT_NODE_VARIABLES.INSTANCE_ID, type: DATA_TYPES.STRING},
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.APP_ID, value: VIRTUAL_CONTEXT_NODE_VARIABLES.APP_ID, type: DATA_TYPES.STRING},
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.MEMORIES, value: VIRTUAL_CONTEXT_NODE_VARIABLES.MEMORIES, type: DATA_TYPES.ARRAY},
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.USE_MEMORY, value: VIRTUAL_CONTEXT_NODE_VARIABLES.USE_MEMORY, type: DATA_TYPES.BOOLEAN},
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.USER_ID, value: VIRTUAL_CONTEXT_NODE_VARIABLES.USER_ID, type: DATA_TYPES.STRING},
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.FILE_URLS, value: VIRTUAL_CONTEXT_NODE_VARIABLES.FILE_URLS, type: DATA_TYPES.ARRAY},
    {observableId: VIRTUAL_CONTEXT_NODE_VARIABLES.CHAT_ID, value: VIRTUAL_CONTEXT_NODE_VARIABLES.CHAT_ID, type: DATA_TYPES.STRING},
  ];
  self.statusManager = emptyStatusManager(self);

  /**
   * 注册可被观察者.
   */
  self.registerObservables = () => {
    self.virtualNodeInfoList.forEach(({observableId, value, type}) => {
      self.page.registerObservable({
        nodeId: VIRTUAL_CONTEXT_NODE.id,
        observableId,
        value,
        type,
        parentId: undefined,
      });
    });
  };

  return self;
};