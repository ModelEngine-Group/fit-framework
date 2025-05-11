/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.mcp.server.MessageRequest;
import modelengine.fitframework.util.MapBuilder;

/**
 * A handler for processing tool list requests in the MCP server.
 * This class extends {@link AbstractMessageHandler} and is responsible for handling
 * {@link ToolListRequest} messages by retrieving the list of tools from the associated {@link McpServer}
 * and returning them in a structured map format.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
public class ToolListHandler extends AbstractMessageHandler<ToolListHandler.ToolListRequest> {
    private final McpServer mcpServer;

    /**
     * Constructs a new instance of the ToolListHandler class.
     *
     * @param mcpServer The MCP server instance used to retrieve the list of tools during request handling.
     * @throws IllegalStateException If {@code mcpServer} is null.
     */
    public ToolListHandler(McpServer mcpServer) {
        super(ToolListRequest.class);
        this.mcpServer = notNull(mcpServer, "The MCP server cannot be null.");
    }

    @Override
    public Object handle(ToolListRequest request) {
        return MapBuilder.get().put("tools", this.mcpServer.getTools()).build();
    }

    /**
     * Represents a tool list request in the MCP server.
     * This request is handled by {@link ToolListHandler} to retrieve the list of available tools
     * from the server and return them in a structured format.
     *
     * @author 季聿阶
     * @since 2025-05-15
     */
    public static class ToolListRequest extends MessageRequest {}
}
