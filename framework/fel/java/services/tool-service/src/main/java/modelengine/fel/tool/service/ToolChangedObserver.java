/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.service;

import java.util.Map;

/**
 * Represents an observer for tool change events.
 *
 * @author 季聿阶
 * @since 2025-05-11
 */
public interface ToolChangedObserver {
    /**
     * Method called when a tool has been added.
     *
     * @param name The name of the added tool, as a {@link String}.
     * @param description A description of the added tool, as a {@link String}.
     * @param schema The schema associated with the added tool, as a
     * {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     */
    void onToolAdded(String name, String description, Map<String, Object> schema);

    /**
     * Method called when a tool has been removed.
     *
     * @param name The name of the removed tool, as a {@link String}.
     */
    void onToolRemoved(String name);
}
