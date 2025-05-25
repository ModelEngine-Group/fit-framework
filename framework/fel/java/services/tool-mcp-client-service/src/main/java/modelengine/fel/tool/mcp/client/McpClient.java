/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client;

import modelengine.fel.tool.mcp.entity.Tool;

import java.util.List;
import java.util.Map;

/**
 * The {@code McpClient} interface defines the contract for interacting with the MCP server.
 * It provides methods to retrieve available tools and execute specific tools with provided arguments.
 * This interface is designed to facilitate communication between the client application and the MCP server,
 * enabling seamless integration and tool invocation.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
public interface McpClient {
    /**
     * Initializes the MCP Client.
     */
    void initialize();

    /**
     * Gets MCP Server Tools.
     *
     * @return The MCP Server Tools as a {@link List}{@code <}{@link Tool}{@code >}.
     */
    List<Tool> getTools();

    /**
     * Calls MCP Server Tool.
     *
     * @param name The tool name as a {@link String}.
     * @param arguments The tool arguments as a {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     * @return The tool result as a {@link Object}.
     */
    Object callTool(String name, Map<String, Object> arguments);
}