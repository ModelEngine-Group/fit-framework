/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.eco;

import modelengine.fel.tool.Tool;

/**
 * 表示 {@link Tool} 的生态任务型工具实现。
 *
 * @since 2024-06-04
 */
public interface TaskTool extends Tool {
    /**
     * 调用任务工具。
     *
     * @param taskId 表示具体任务的唯一标识的 {@link String}。
     * @param args 表示调用工具的参数列表的 {@link Object}{@code []}。
     * @return 表示调用工具的结果的 {@link Object}。
     */
    Object executeWithTask(String taskId, Object... args);
}
