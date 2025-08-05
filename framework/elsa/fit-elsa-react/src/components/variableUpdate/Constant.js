/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {DATA_TYPES, FROM_TYPE} from '@/common/Consts.js';

export const createDefaultVariable = () => ({
  id: uuidv4(),
  type: DATA_TYPES.OBJECT,
  from: FROM_TYPE.EXPAND,
  value: [{
    id: uuidv4(),
    name: 'key',
    type: DATA_TYPES.ARRAY,
    dataType: '',
    from: FROM_TYPE.INPUT,
    value: [],
    referenceNode: '',
    referenceId: '',
    referenceKey: '',
  }, {
    id: uuidv4(),
    name: 'value',
    type: '',
    from: FROM_TYPE.REFERENCE,
    value: '',
    referenceNode: '',
    referenceId: '',
    referenceKey: '',
  }],
});