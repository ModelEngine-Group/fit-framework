/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import modelengine.fel.tool.mcp.entity.Tool;

import java.util.List;

/**
 * Represents the MCP Server.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
public interface McpServer {
    /**
     * Gets MCP server tools.
     *
     * @return The MCP server tools as a {@link List}{@code <}{@link Tool}{@code >}.
     */
    List<Tool> getTools();
}
