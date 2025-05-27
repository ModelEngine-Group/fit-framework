/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.mcp.server.MessageRequest;

/**
 * A handler for processing initialization requests in the MCP server.
 * This class extends {@link AbstractMessageHandler} and is responsible for handling
 * {@link InitializeRequest} messages by retrieving server information via the associated {@link McpServer}.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
public class InitializeHandler extends AbstractMessageHandler<InitializeHandler.InitializeRequest> {
    private final McpServer mcpServer;

    /**
     * Constructs a new instance of the InitializeHandler class.
     *
     * @param mcpServer The MCP server instance used to retrieve server information during request handling.
     * @throws IllegalArgumentException If {@code mcpServer} is null.
     */
    public InitializeHandler(McpServer mcpServer) {
        super(InitializeRequest.class);
        this.mcpServer = notNull(mcpServer, "The MCP server cannot be null.");
    }

    @Override
    protected Object handle(InitializeRequest request) {
        return this.mcpServer.getSchema();
    }

    /**
     * Represents an initialization request in the MCP server.
     * This request is handled by {@link InitializeHandler} to retrieve server information.
     *
     * @author 季聿阶
     * @since 2025-05-15
     */
    public static class InitializeRequest extends MessageRequest {}
}
