/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {NODE_STATUS} from "@";

/**
 * 节点状态管理器.
 *
 * @param node 节点.
 * @return {{}} 管理器对象.
 */
export const statusManager = (node) => {
    const self = new Proxy({}, {
        get: function (target, propKey) {
            const t = Reflect.get(target, propKey);
            if (typeof t !== "function") {
                return t;
            }
            return (args) => {
                t(args);
                flush();
            };
        }
    });

    /**
     * 设置runnable状态.
     *
     * @param runnable 状态.
     */
    self.setRunnable = (runnable) => {
        node.runnable = runnable;
    };

    /**
     * 设置disabled状态.
     *
     * @param disabled 状态.
     */
    self.setDisabled = (disabled) => {
        node.disabled = disabled;
    };

    /**
     * 设置调试运行状态.
     *
     * @param runStatus 状态.
     */
    self.setRunStatus = (runStatus) => {
        node.runStatus = runStatus;
        node.emphasized = runStatus === NODE_STATUS.RUNNING;
        const focused = node.page.getFocusedShapes();
        if (focused.length === 0) {
            node.isFocused = runStatus === NODE_STATUS.RUNNING;
        }
    };

    /**
     * 是否启动mask.
     *
     * @param enableMask mask状态.
     */
    self.setEnableMask = (enableMask) => {
        node.enableMask = enableMask;
    };

    /**
     * 引用disabled状态.
     *
     * @param referenceDisabled 引用disabled状态.
     */
    self.setReferenceDisabled = (referenceDisabled) => {
        node.referenceDisabled = referenceDisabled;
    };

    const flush = () => {
        if (!self.flushing) {
            self.flushing = true;
            Promise.resolve().then(() => {
                node.drawer.setShapeStatus && node.drawer.setShapeStatus({
                    runnable: node.runnable,
                    runStatus: node.runStatus,
                    disabled: node.disabled,
                    enableMask: node.enableMask,
                    referenceDisabled: node.referenceDisabled
                });
                self.flushing = false;
            });
        }
    };

    return self;
};