/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ObserverProxy} from "@/components/base/observerProxy.js";
import {OBSERVER_STATUS} from "@/common/Consts.js";

/**
 * 引用相关方法.
 *
 * @param node 节点.
 */
export const referenceDecorate = (node) => {
    /**
     * 清空所有当前节点可被其他节点引用的observable.
     */
    node.cleanObservables = () => {
        // 清除我提供的observable
        node.page.removeObservable(node.id);

        // 清除我的observer.
        node.observed.forEach(o => node.page.stopObserving(o.nodeId, o.observableId, o));
    };

    /**
     * 监听.
     *
     * @param referenceId 引用的唯一标识.
     * @param nodeId 被监听节点的id.
     * @param observableId 待监听的id.
     * @param observer 监听器.
     * @return {(function(): void)|*}
     */
    node.observeTo = (referenceId, nodeId, observableId, observer) => {
        const preNodeInfos = node.getPreReferencableNodeInfos();
        const observerProxy = ObserverProxy(referenceId, nodeId, observableId, observer, node);
        if (node.isReferenceAvailable(preNodeInfos, observerProxy)) {
            observerProxy.status = OBSERVER_STATUS.ENABLE;
        } else {
            observerProxy.disable();
        }
        node.page.observeTo(nodeId, observableId, observerProxy);

        // 返回取消监听的方法.
        return () => {
            observerProxy.stopObserve();
        };
    };

    /**
     * 判断引用是否可用.
     *
     * @param preNodeInfos 前序节点信息.
     * @param observerProxy 观察者.
     * @return {*} true/false.
     */
    node.isReferenceAvailable = (preNodeInfos, observerProxy) => {
        return preNodeInfos.some(pre => pre.id === observerProxy.nodeId && pre.runnable === node.runnable);
    };

    /**
     * 被监听的值发生变化时触发.
     *
     * @param observableId 被监听的id.
     * @param value 值.
     * @param type 类型.
     */
    node.emit = (observableId, {value, type}) => {
        const observable = node.page.getObservable(node.id, observableId);
        if (!observable) {
            return;
        }

        observable.observers.forEach(o => {
            if (o.status === OBSERVER_STATUS.ENABLE) {
                o.notify({
                    value: value !== null && value !== undefined ? value : observable.value,
                    type: type !== null && type !== undefined ? type : observable.type
                });
            }
        });

        // 是空字符串的情况下，需要修改值.
        if (value !== null && value !== undefined) {
            observable.value = value;
        }

        // 是空字符串的情况下，需要修改值.
        if (type !== null && type !== undefined) {
            observable.type = type;
        }
    };

    /**
     * 清空当前节点所有对其他节点的引用.
     */
    node.cleanObserved = () => {
        while (node.observed.length > 0) {
            const o = node.observed.pop();
            o.stopObserve();
        }
    };

    /**
     * disable所有的observed.
     */
    node.disableObserved = () => {
        node.observed.forEach(o => o.disable());
    };
};