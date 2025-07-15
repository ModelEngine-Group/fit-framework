/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {DATA_TYPES, DEFAULT_ADD_TOOL_NODE_CONTEXT, FROM_TYPE} from '@/common/Consts.js';
import {v4 as uuidv4} from 'uuid';

export const DEFAULT_INPUT_PARAMS = [
  {
    id: uuidv4(),
    name: 'args',
    type: DATA_TYPES.OBJECT,
    from: FROM_TYPE.EXPAND,
    value: [],
  },
  {
    id: uuidv4(),
    name: 'config',
    type: DATA_TYPES.OBJECT,
    from: FROM_TYPE.INPUT,
    value: {},
  },
  {
    id: uuidv4(),
    name: 'toolInfo',
    type: DATA_TYPES.OBJECT,
    from: FROM_TYPE.INPUT,
    value: {},
  },
  JSON.parse(JSON.stringify(DEFAULT_ADD_TOOL_NODE_CONTEXT)),
];
