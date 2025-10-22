/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.entity.ServerSchema;
import modelengine.fel.tool.mcp.entity.Tool;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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
     * Add a tool.
     *
     * @param name The name of the added tool, as a {@link String}.
     * @param description A description of the added tool, as a {@link String}.
     * @param inputSchema The parameters associated with the added tool, as a {@link McpSchema.JsonSchema}.
     * @param callHandler The tool call handler as a {@link BiFunction}
     */
    void addTool(String name, String description, McpSchema.JsonSchema inputSchema,
                    BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler);

    /**
     * Remove a tool.
     *
     * @param name The name of the removed tool, as a {@link String}.
     */
    void removeTool(String name);

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
