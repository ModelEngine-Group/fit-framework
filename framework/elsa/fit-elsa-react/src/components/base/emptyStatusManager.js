/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {statusManager} from '@/components/base/statusManager.js';

/**
 * 节点状态管理器空实现.
 *
 * @param node 节点.
 * @return {{}} 管理器对象.
 */
export const emptyStatusManager = (node) => {
    const self = statusManager(node);

    /**
     * 设置runnable状态.
     *
     * @override
     */
    self.setRunnable = (runnable) => {};

    /**
     * 设置disabled状态.
     *
     * @override
     */
    self.setDisabled = (disabled) => {};

    /**
     * 设置调试运行状态.
     *
     * @override
     */
    self.setRunStatus = (runStatus) => {};

    /**
     * 是否启动mask.
     *
     * @override
     */
    self.setEnableMask = (enableMask) => {};

    /**
     * 引用disabled状态.
     *
     * @override
     */
    self.setReferenceDisabled = (referenceDisabled) => {};

    return self;
};