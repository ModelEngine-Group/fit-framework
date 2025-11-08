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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A default implementation of the MCP client that uses the MCP SDK's streamable HTTP transport.
 *
 * @author 黄可欣
 * @since 2025-11-03
 */
public class DefaultMcpStreamableClient implements McpClient {
    private static final Logger log = Logger.get(DefaultMcpStreamableClient.class);

    private final String clientId;
    private final McpSyncClient mcpSyncClient;
    private volatile boolean initialized = false;
    private volatile boolean closed = false;

    /**
     * Constructs a new instance of the DefaultMcpStreamableClient.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The endpoint for the Server-Sent Events (SSE) connection.
     * @param requestTimeoutSeconds The timeout duration of requests. Units: seconds.
     * @param loggingConsumer The consumer to handle logging messages from the MCP server.
     * @param elicitationHandler The function to handle elicitation requests from the MCP server.
     */
    public DefaultMcpStreamableClient(String baseUri, String sseEndpoint, int requestTimeoutSeconds,
            Consumer<McpSchema.LoggingMessageNotification> loggingConsumer,
            Function<McpSchema.ElicitRequest, McpSchema.ElicitResult> elicitationHandler) {
        this.clientId = UUID.randomUUID().toString();
        notBlank(baseUri, "The MCP server base URI cannot be blank.");
        notBlank(sseEndpoint, "The MCP server SSE endpoint cannot be blank.");
        log.info("Creating MCP client. [clientId={}, baseUri={}]", clientId, baseUri);
        ObjectMapper mapper = new ObjectMapper();
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(baseUri)
                .jsonMapper(new JacksonMcpJsonMapper(mapper))
                .endpoint(sseEndpoint)
                .build();

        if (elicitationHandler != null) {
            this.mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .capabilities(McpSchema.ClientCapabilities.builder().elicitation().build())
                    .loggingConsumer(loggingConsumer)
                    .elicitation(elicitationHandler)
                    .jsonSchemaValidator(new DefaultJsonSchemaValidator(mapper))
                    .build();
        } else {
            this.mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .capabilities(McpSchema.ClientCapabilities.builder().build())
                    .loggingConsumer(loggingConsumer)
                    .jsonSchemaValidator(new DefaultJsonSchemaValidator(mapper))
                    .build();
        }

    }

    @Override
    public String getClientId() {
        return clientId;
    }

    /**
     * Initializes the MCP client connection.
     *
     * @throws IllegalStateException if the client has already been closed.
     */
    @Override
    public void initialize() {
        ensureNotClosed();
        mcpSyncClient.initialize();
        this.initialized = true;
        log.info("MCP client initialized successfully. [clientId={}]", clientId);
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
        ensureReady();
        try {
            McpSchema.ListToolsResult result = this.mcpSyncClient.listTools();
            if (result == null || result.tools() == null) {
                log.warn("Failed to get tools: result is null. [clientId={}]", clientId);
                throw new IllegalStateException("Failed to get tools from MCP server: result is null.");
            }

            List<Tool> tools = result.tools().stream().map(this::convertToFelTool).collect(Collectors.toList());

            log.info("Successfully retrieved tools. [clientId={}, count={}]", clientId, tools.size());
            tools.forEach(tool -> log.debug("Tool information. [name={}, description={}]",
                    tool.getName(),
                    tool.getDescription()));
            return tools;
        } catch (Exception e) {
            log.error("Failed to get tools. [clientId={}, error={}]", clientId, e.getMessage());
            throw new IllegalStateException("Failed to get tools from MCP server. [error=" + e.getMessage() + "]", e);
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
        ensureReady();
        try {
            log.info("Calling tool. [clientId={}, name={}, arguments={}]", clientId, name, arguments);
            McpSchema.CallToolResult result =
                    this.mcpSyncClient.callTool(new McpSchema.CallToolRequest(name, arguments));

            if (result == null) {
                log.error("Failed to call tool: result is null. [clientId={}, name={}]", clientId, name);
                throw new IllegalStateException("Failed to call tool: result is null. [name=" + name + "]");
            }
            return processToolResult(result, name);
        } catch (Exception e) {
            log.error("Failed to call tool. [clientId={}, name={}, error={}]", clientId, name, e.getMessage());
            throw new IllegalStateException("Failed to call tool. [name=" + name + ", error=" + e.getMessage() + "]",
                    e);
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
            String errorDetails = extractErrorDetails(result.content());
            log.error("Tool returned an error. [clientId={}, name={}, details={}]", clientId, name, errorDetails);
            throw new IllegalStateException(
                    "Tool returned an error. [name=" + name + ", details=" + errorDetails + "]");
        }

        if (result.content() == null || result.content().isEmpty()) {
            log.warn("Tool returned empty content. [clientId={}, name={}]", clientId, name);
            return null;
        }

        Object content = result.content().get(0);
        if (content instanceof McpSchema.TextContent textContent) {
            log.info("Successfully called tool. [clientId={}, name={}, result={}]", clientId, name, textContent.text());
            return textContent.text();
        } else if (content instanceof McpSchema.ImageContent imageContent) {
            log.info("Successfully called tool: image content. [clientId={}, name={}]", clientId, name);
            return imageContent;
        } else {
            log.info("Successfully called tool. [clientId={}, name={}, contentType={}]",
                    clientId,
                    name,
                    content.getClass().getSimpleName());
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
        ensureNotClosed();
        this.closed = true;
        this.mcpSyncClient.closeGracefully();
        log.info("MCP client closed. [clientId={}]", clientId);
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

    /**
     * Ensures the MCP client is not closed.
     *
     * @throws IllegalStateException if the client is closed.
     */
    private void ensureNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("The MCP client is closed. [clientId=" + clientId + "]");
        }
    }

    /**
     * Ensures the MCP client is ready for operations (not closed and initialized).
     *
     * @throws IllegalStateException if the client is closed or not initialized.
     */
    private void ensureReady() {
        ensureNotClosed();
        if (!this.initialized) {
            throw new IllegalStateException(
                    "MCP client is not initialized. Please wait a moment. [clientId=" + clientId + "]");
        }
    }

    /**
     * Extracts error details from tool result content.
     *
     * @param content The content list from the tool result.
     * @return The error details as a string.
     */
    private String extractErrorDetails(List<McpSchema.Content> content) {
        if (content != null && !content.isEmpty()) {
            McpSchema.Content errorContent = content.get(0);
            if (errorContent instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            } else {
                return errorContent.toString();
            }
        }
        return "";
    }
}
