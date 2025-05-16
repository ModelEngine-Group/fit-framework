/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

export const setBranchDisabled = (node, disabled) => {
  const flowMeta = node.getFlowMeta();
  node.drawer.dispatch({
    actionType: 'changeBranchesStatus',
    changes: [
      {key: 'ids', value: flowMeta.conditionParams.branches.map(b => b.id)},
      {key: 'disabled', value: disabled},
      {key: 'jadeNodeConfigChangeIgnored', value: true},
    ],
  });
};