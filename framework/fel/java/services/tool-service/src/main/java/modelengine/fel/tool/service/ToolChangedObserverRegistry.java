/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.service;

/**
 * 工具变更观察者注册表接口。
 *
 * @author 黄可欣
 * @since 2025-11-20
 */
public interface ToolChangedObserverRegistry {
    /**
     * 注册工具变更观察者。
     *
     * @param observer 待注册的工具变更观察者。
     */
    void register(ToolChangedObserver observer);

    /**
     * 注销工具变更观察者。
     *
     * @param observer 需要注销的工具变更观察者。
     */
    void unregister(ToolChangedObserver observer);
}
