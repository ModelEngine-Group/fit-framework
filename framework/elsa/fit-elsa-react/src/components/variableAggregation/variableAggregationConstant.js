/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';
import {FROM_TYPE} from '@/common/Consts.js';

export const DEFAULT_VARIABLE = {
  id: uuidv4(),
  name: null,
  type: null,
  from: FROM_TYPE.REFERENCE,
  value: [],
};