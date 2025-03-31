/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {rectangle} from '@fit-elsa/elsa-core';
import {VIRTUAL_CONTEXT_NODE} from '@/common/Consts.js';
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
    {observableId: 'instanceId', value: 'instanceId', type: 'String'},
    {observableId: 'appId', value: 'appId', type: 'String'},
    {observableId: 'memories', value: 'memories', type: 'Array'},
    {observableId: 'useMemory', value: 'useMemory', type: 'Boolean'},
    {observableId: 'userId', value: 'userId', type: 'String'},
    {observableId: 'fileUrls', value: 'fileUrls', type: 'Array'},
    {observableId: 'chatId', value: 'chatId', type: 'String'},
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