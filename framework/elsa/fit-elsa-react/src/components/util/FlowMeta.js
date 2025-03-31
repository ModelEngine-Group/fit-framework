/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * FlowMeta类，用于快速处理FlowMeta。
 */
export default class FlowMeta {
  constructor(flowMeta) {
    this.flowMeta = flowMeta;
  }

  callbackInput() {
    return this.flowMeta?.callback?.converter?.entity?.inputParams ?? [];
  }

  joberInput() {
    return this.flowMeta?.jober?.converter?.entity?.inputParams ?? [];
  }

  taskInput() {
    return this.flowMeta?.task?.converter?.entity?.inputParams ?? [];
  }
}