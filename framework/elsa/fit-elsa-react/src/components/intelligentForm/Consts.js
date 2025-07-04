/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {RENDER_TYPE} from '@/common/Consts.js';
import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

export const RENDER_OPTIONS_TYPE = new Set([RENDER_TYPE.RADIO, RENDER_TYPE.SELECT, RENDER_TYPE.CHECK_BOX]);

export const FORM_TYPE = {
  ORCHESTRATION: 'orchestration',
  MANUAL: 'manual',
};

export const ORCHESTRATION_INIT_ENTITY = {
  inputParams: [{
    id: uuidv4(),
    name: 'data',
    type: DATA_TYPES.OBJECT,
    from: FROM_TYPE.EXPAND,
    value: [],
  }, {
    id: uuidv4(),
    name: 'schema',
    type: DATA_TYPES.OBJECT,
    from: FROM_TYPE.INPUT,
    value: {
      parameters: [],
    },
  }],
  outputParams: [{
    id: uuidv4(),
    name: 'output',
    type: DATA_TYPES.OBJECT,
    value: [],
  }],
};

export const MANUAL_INIT_ENTITY = {
  inputParams: [],
  outputParams: [],
};

export const ORCHESTRATION_TASK_ID = 'a910a3d38a4549eda1112beee008419d';