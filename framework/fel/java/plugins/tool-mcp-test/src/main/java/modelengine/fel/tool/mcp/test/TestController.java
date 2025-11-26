/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.test;

import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.client.McpClientFactory;
import modelengine.fel.tool.mcp.client.elicitation.ElicitResult;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fitframework.annotation.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a test controller for interacting with the MCP (Model Communication Protocol) client.
 * This class provides methods to initialize the MCP client and retrieve a list of available tools.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
@Component
@RequestMapping(path = "/mcp-test")
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
     * Initializes the MCP client by creating an instance using the provided factory and initializing it.
     * This method sets up the connection to the MCP server and prepares it for further interactions.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @return A string indicating that the initialization was successful.
     */
    @PostMapping(path = "/initialize")
    public String initializeStreamable(@RequestQuery(name = "baseUri") String baseUri,
            @RequestQuery(name = "sseEndpoint") String sseEndpoint) {
        this.client = this.mcpClientFactory.createStreamable(baseUri, sseEndpoint, null);
        this.client.initialize();
        return "Initialized";
    }

    @PostMapping(path = "/initialize-sse")
    public String initializeSse(@RequestQuery(name = "baseUri") String baseUri,
            @RequestQuery(name = "sseEndpoint") String sseEndpoint) {
        this.client = this.mcpClientFactory.createSse(baseUri, sseEndpoint, null);
        this.client.initialize();
        return "Initialized";
    }

    @PostMapping(path = "/initialize-elicitation")
    public String initializeElicitation(@RequestQuery(name = "baseUri") String baseUri,
            @RequestQuery(name = "sseEndpoint") String sseEndpoint) {
        this.client = this.mcpClientFactory.createStreamable(baseUri,
                sseEndpoint,
                request -> new ElicitResult(ElicitResult.Action.ACCEPT, Collections.emptyMap()));
        this.client.initialize();
        return "Initialized";
    }

    /**
     * Closes the MCP client and releases any resources associated with it.
     * This method ensures that the MCP client is properly closed and resources are released.
     *
     * @return A string indicating that the close operation was successful.
     */
    @PostMapping(path = "/close")
    public String close() {
        try {
            this.client.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close.", e);
        }
        return "Closed";
    }

    /**
     * Retrieves a list of available tools from the MCP server.
     * This method calls the MCP client to fetch the list of tools and returns it to the caller.
     *
     * @return A list of {@link Tool} objects representing the available tools.
     */
    @GetMapping(path = "/tools/list")
    public List<Tool> toolsList() {
        return this.client.getTools();
    }

    /**
     * Calls a specific tool with the given name and JSON arguments.
     * This method invokes the specified tool on the MCP server and returns the result.
     *
     * @param name The name of the tool to be called.
     * @param jsonArgs The JSON arguments to be passed to the tool.
     * @return The result of the tool execution.
     */
    @PostMapping(path = "/tools/call")
    public Object toolsCall(@RequestQuery(name = "name") String name, @RequestBody Map<String, Object> jsonArgs) {
        return this.client.callTool(name, jsonArgs);
    }
}