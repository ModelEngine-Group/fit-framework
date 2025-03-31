/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {OBSERVER_STATUS} from "@/common/Consts.js";

/**
 * 监听代理.
 *
 * @param referenceId 引用的唯一标识.
 * @param nodeId 被监听节点的id.
 * @param observableId 待监听的id.
 * @param observer 监听器.
 * @param shape 图形.
 * @return {{}}
 * @constructor
 */
export const ObserverProxy = (referenceId, nodeId, observableId, observer, shape) => {
    const self = {};
    self.id = referenceId;
    self.nodeId = nodeId;
    self.observableId = observableId;
    self.status = OBSERVER_STATUS.ENABLE;
    self.observer = observer;
    shape.observed.push(self);

    /**
     * 禁用Observer.
     */
    self.disable = () => {
        // 利用刷新机制置空
        self.notify({value: null, type: null});
        self.status = OBSERVER_STATUS.DISABLE;
    };

    /**
     * 启动Observer.
     */
    self.enable = () => {
        self.status = OBSERVER_STATUS.ENABLE;
        // 刷新
        const observable = shape.page.getObservable(self.nodeId, self.observableId);
        self.notify({value: observable.value, type: observable.type});
    };

    /**
     * 触发监听.
     *
     * @param args 参数.
     */
    self.notify = (args) => {
        if (self.status === OBSERVER_STATUS.ENABLE) {
            self.observer(args);
        }
    };

    /**
     * 停止观察.
     * 删除shape中的observed，删除page中的监听.
     */
    self.stopObserve = () => {
        const index = shape.observed.findIndex(o => o === self);
        // 说明已经被删除.
        if (index !== -1) {
            shape.observed.splice(index, 1);
        }
        shape.page.stopObserving(nodeId, observableId, self);
    };

    /**
     * 清空监听方的监听数据
     */
    self.cleanObserve = () => {
        self.notify({value: null, type: null});
        self.status = OBSERVER_STATUS.ENABLE;
    };

    return self;
};