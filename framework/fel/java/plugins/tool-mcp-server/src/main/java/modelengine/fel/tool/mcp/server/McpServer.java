/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import modelengine.fel.tool.mcp.server.entity.ToolEntity;

import java.util.List;
import java.util.Map;

/**
 * Represents the MCP Server.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
public interface McpServer {
    /**
     * Gets MCP Server Info.
     *
     * @return The MCP Server Info as a {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     */
    Map<String, Object> getInfo();

    /**
     * Gets MCP Server Tools.
     *
     * @return The MCP Server Tools as a {@link List}{@code <}{@link ToolEntity}{@code >}.
     */
    List<ToolEntity> getTools();

    /**
     * Calls MCP Server Tool.
     *
     * @param name The tool name as a {@link String}.
     * @param arguments The tool arguments as a {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     * @return The tool result as a {@link Object}.
     */
    Object callTool(String name, Map<String, Object> arguments);

    /**
     * Registers MCP Server Tools Changed Observer.
     *
     * @param observer The MCP Server Tools Changed Observer as a {@link ToolsChangedObserver}.
     */
    void registerToolsChangedObserver(ToolsChangedObserver observer);

    /**
     * Represents the MCP Server Tools Changed Observer.
     */
    interface ToolsChangedObserver {
        /**
         * Called when MCP Server Tools changed.
         */
        void onToolsChanged();
    }
}
