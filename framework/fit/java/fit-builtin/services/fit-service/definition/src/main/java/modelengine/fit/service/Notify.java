/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service;

import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fitframework.annotation.Genericable;

import java.util.List;

/**
 * Represents notification service for updating Fitables instance information.
 *
 * @author 董智豪
 * @since 2025-06-20
 */
public interface Notify {
    /**
     * Notify to update Fitables instances.
     *
     * @param fitableInstances A {@link List}{@code <}{@link FitableAddressInstance}{@code >} representing all instance
     * information for specified service implementations.
     */
    @Genericable(id = "modelengine.fit.service.registry-listener.notify-fitables")
    void notifyFitables(List<FitableAddressInstance> fitableInstances);
}