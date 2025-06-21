/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service.server;

/**
 * @author pc
 * @description
 * @created 2025/6/20
 */

import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fitframework.annotation.Genericable;

import java.util.List;

/**
 * 表示运行时 {@code 'worker.'} 前缀的配置项。
 *
 * @author 董智豪
 * @since 2025-06-20
 */
public interface Notify {

    @Genericable(id = "b69df5e8cbcd4166aa5029602e7a58cf")
    void notifyFitables(List<FitableAddressInstance> fitableInstances);
}
