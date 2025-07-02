/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service.server;

import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fitframework.annotation.Genericable;

import java.util.List;

/**
 * 表示通知服务，用于更新Fitables实例信息。
 *
 * @author 董智豪
 * @since 2025-06-20
 */
public interface Notify {

    /**
     * 通知更新Fitables 实例。
     *
     * @param fitableInstances 表示指定服务实现的所有实例信息的 {@link List}{@code <}{@link FitableAddressInstance}{@code >}。
     */
    @Genericable(id = "b69df5e8cbcd4166aa5029602e7a58cf")
    void notifyFitables(List<FitableAddressInstance> fitableInstances);
}
