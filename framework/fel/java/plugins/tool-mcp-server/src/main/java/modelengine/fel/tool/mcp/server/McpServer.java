/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import modelengine.fel.tool.mcp.entity.ServerSchema;
import modelengine.fel.tool.mcp.entity.Tool;

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
     * Gets MCP server schema.
     *
     * @return The MCP server schema as a {@link ServerSchema}.
     */
    ServerSchema getSchema();

    /**
     * Gets MCP server tools.
     *
     * @return The MCP server tools as a {@link List}{@code <}{@link Tool}{@code >}.
     */
    List<Tool> getTools();

    /**
     * Calls MCP server tool.
     *
     * @param name The tool name as a {@link String}.
     * @param arguments The tool arguments as a {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     * @return The tool result as a {@link Object}.
     */
    Object callTool(String name, Map<String, Object> arguments);

    /**
     * Registers MCP server tools changed observer.
     *
     * @param observer The MCP server tools changed observer as a {@link ToolsChangedObserver}.
     */
    void registerToolsChangedObserver(ToolsChangedObserver observer);

    /**
     * Represents the MCP server tools changed observer.
     */
    interface ToolsChangedObserver {
        /**
         * Called when MCP server tools changed.
         */
        void onToolsChanged();
    }
}
