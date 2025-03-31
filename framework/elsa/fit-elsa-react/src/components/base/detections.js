/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * 添加监听.
 *
 * @param node 节点对象.
 */
export const addDetections = (node) => {
    // 可实现动态替换其中react组件的能力.
    node.addDetection(["componentName"], (property, value, preValue) => {
        if (value === preValue) {
            return;
        }
        node.drawer.unmountReact();
        node.invalidateAlone();
    });
};