/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {END_NODE_TYPE} from '@/common/Consts.js';

export const getEndNodeType = (inputParams) => {
  return inputParams.find(item => item.name === 'finalOutput') ? END_NODE_TYPE.VARIABLES : END_NODE_TYPE.MANUAL_CHECK;
};