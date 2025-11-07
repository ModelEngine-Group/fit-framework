/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fitframework.log.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A default implementation of the MCP client that uses the MCP SDK's streamable HTTP transport.
 *
 * @author 黄可欣
 * @since 2025-11-03
 */
public class DefaultMcpStreamableClient implements McpClient {
    private static final Logger log = Logger.get(DefaultMcpStreamableClient.class);

    private final McpSyncClient mcpSyncClient;
    private volatile boolean initialized = false;
    private volatile boolean closed = false;

    /**
     * Constructs a new instance of the DefaultMcpStreamableClient.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The endpoint for the Server-Sent Events (SSE) connection.
     * @param requestTimeoutSeconds The timeout duration of requests. Units: seconds.
     */
    public DefaultMcpStreamableClient(String baseUri, String sseEndpoint, int requestTimeoutSeconds) {
        notBlank(baseUri, "The MCP server base URI cannot be blank.");
        notBlank(sseEndpoint, "The MCP server SSE endpoint cannot be blank.");
        ObjectMapper mapper = new ObjectMapper();
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(baseUri)
                .jsonMapper(new JacksonMcpJsonMapper(mapper))
                .endpoint(sseEndpoint)
                .build();
        this.mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds > 0 ? requestTimeoutSeconds : 5))
                .capabilities(McpSchema.ClientCapabilities.builder().elicitation().build())
                .loggingConsumer(McpClientMessageHandler::handleLoggingMessage)
                .elicitation(McpClientMessageHandler::handleElicitationRequest)
                .jsonSchemaValidator(new DefaultJsonSchemaValidator(mapper))
                .build();
    }

    /**
     * Initializes the MCP client connection.
     *
     * @throws IllegalStateException if the client has already been closed.
     */
    @Override
    public void initialize() {
        if (this.closed) {
            throw new IllegalStateException("The MCP client is closed.");
        }
        mcpSyncClient.initialize();
        this.initialized = true;
        log.info("MCP client initialized successfully.");
    }

    /**
     * Retrieves the list of available tools from the MCP server.
     *
     * @return A {@link List} of {@link Tool} objects representing the available tools.
     * @throws IllegalStateException if the client is closed, not initialized, or if
     * the server request fails.
     */
    @Override
    public List<Tool> getTools() {
        if (this.closed) {
            throw new IllegalStateException("The MCP client is closed.");
        }
        if (!this.initialized) {
            throw new IllegalStateException("MCP client is not initialized. Please wait a moment.");
        }

        try {
            McpSchema.ListToolsResult result = this.mcpSyncClient.listTools();
            if (result == null || result.tools() == null) {
                log.warn("Failed to get tools from MCP server: result is null.");
                throw new IllegalStateException("Failed to get tools from MCP server: result is null.");
            }

            List<Tool> tools = result.tools().stream().map(this::convertToFelTool).collect(Collectors.toList());

            log.info("Successfully retrieved {} tools from MCP server.", tools.size());
            tools.forEach(tool -> log.info("Tool - Name: {}, Description: {}", tool.getName(), tool.getDescription()));
            return tools;
        } catch (Exception e) {
            log.error("Failed to get tools from MCP server: {}", e);
            throw new IllegalStateException("Failed to get tools from MCP server: " + e.getMessage(), e);
        }
    }

    /**
     * Invokes a specific tool on the MCP server with the provided arguments.
     *
     * @param name The name of the tool to invoke, as a {@link String}.
     * @param arguments The arguments to pass to the tool, as a {@link Map} of parameter names to values.
     * @return The result of the tool invocation. For text content, returns the text as a {@link String}.
     * For image content, returns the {@link McpSchema.ImageContent} object.
     * Returns {@code null} if the tool returns empty content.
     * @throws IllegalStateException if the client is closed, not initialized, if the tool
     * returns an error, or if the server request fails.
     */
    @Override
    public Object callTool(String name, Map<String, Object> arguments) {
        if (this.closed) {
            throw new IllegalStateException("The MCP client is closed.");
        }
        if (!this.initialized) {
            throw new IllegalStateException("MCP client is not initialized. Please wait a moment.");
        }
        try {
            log.info("Calling tool: {} with arguments: {}", name, arguments);
            McpSchema.CallToolResult result =
                    this.mcpSyncClient.callTool(new McpSchema.CallToolRequest(name, arguments));

            if (result == null) {
                log.error("Failed to call tool '{}': result is null.", name);
                throw new IllegalStateException("Failed to call tool '" + name + "': result is null.");
            }
            return processToolResult(result, name);
        } catch (Exception e) {
            log.error("Failed to call tool '{}' from MCP server.", name, e);
            throw new IllegalStateException("Failed to call tool '" + name + "': " + e.getMessage(), e);
        }
    }

    /**
     * Processes the tool call result and extracts the content.
     * Handles error cases and different content types (text, image, etc.).
     *
     * @param result The {@link McpSchema.CallToolResult} returned from the tool call.
     * @param name The name of the tool that was called.
     * @return The extracted content. For text content, returns the text as a {@link String}.
     * For image content, returns the {@link McpSchema.ImageContent} object.
     * Returns {@code null} if the tool returns empty content.
     * @throws IllegalStateException if the tool returns an error.
     */
    private Object processToolResult(McpSchema.CallToolResult result, String name) {
        if (result.isError() != null && result.isError()) {
            String errorMsg = "Tool '" + name + "' returned an error";
            if (result.content() != null && !result.content().isEmpty()) {
                Object errorContent = result.content().get(0);
                if (errorContent instanceof McpSchema.TextContent textContent) {
                    errorMsg += ": " + textContent.text();
                } else {
                    errorMsg += ": " + errorContent;
                }
            }
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (result.content() == null || result.content().isEmpty()) {
            log.warn("Tool '{}' returned empty content.", name);
            return null;
        }

        Object content = result.content().get(0);
        if (content instanceof McpSchema.TextContent textContent) {
            log.info("Successfully called tool '{}', result: {}", name, textContent.text());
            return textContent.text();
        } else if (content instanceof McpSchema.ImageContent imageContent) {
            log.info("Successfully called tool '{}', returned image content.", name);
            return imageContent;
        } else {
            log.info("Successfully called tool '{}', content type: {}", name, content.getClass().getSimpleName());
            return content;
        }
    }

    /**
     * Closes the MCP client connection and releases associated resources.
     *
     * @throws IOException if an I/O error occurs during the close operation.
     */
    @Override
    public void close() throws IOException {
        this.closed = true;
        this.mcpSyncClient.closeGracefully();
        log.info("MCP client closed.");
    }

    /**
     * Converts an MCP SDK Tool to a FEL Tool entity.
     *
     * @param mcpTool The MCP SDK {@link McpSchema.Tool} to convert.
     * @return A FEL {@link Tool} entity with the corresponding name, description, and input schema.
     */
    private Tool convertToFelTool(McpSchema.Tool mcpTool) {
        Tool tool = new Tool();
        tool.setName(mcpTool.name());
        tool.setDescription(mcpTool.description());

        // Convert JsonSchema to Map<String, Object>
        McpSchema.JsonSchema inputSchema = mcpTool.inputSchema();
        if (inputSchema != null) {
            Map<String, Object> schemaMap = new HashMap<>();
            schemaMap.put("type", inputSchema.type());
            if (inputSchema.properties() != null) {
                schemaMap.put("properties", inputSchema.properties());
            }
            if (inputSchema.required() != null) {
                schemaMap.put("required", inputSchema.required());
            }
            tool.setInputSchema(schemaMap);
        }

        return tool;
    }
}
