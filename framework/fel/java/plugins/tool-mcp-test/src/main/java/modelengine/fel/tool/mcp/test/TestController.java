/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.test;

import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.client.McpClientFactory;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fitframework.annotation.Component;

import java.util.List;

/**
 * Represents a test controller for interacting with the MCP (Model Communication Protocol) client.
 * This class provides methods to initialize the MCP client and retrieve a list of available tools.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
@Component
public class TestController {
    private final McpClientFactory mcpClientFactory;
    private McpClient client;

    /**
     * Constructs a new instance of the TestController.
     *
     * @param mcpClientFactory The factory used to create instances of the MCP client.
     */
    public TestController(McpClientFactory mcpClientFactory) {
        this.mcpClientFactory = mcpClientFactory;
    }

    /**
     * Handles the HTTP GET request to "/test/mcp".
     * Initializes the MCP client and retrieves a list of available tools from the server.
     *
     * @return A list of tools retrieved from the MCP server.
     */
    @GetMapping(path = "/test/mcp")
    public List<Tool> testMcp() {
        this.client = this.mcpClientFactory.create("http://localhost:8080/sse");
        this.client.initialize();
        return this.client.getTools();
    }
}