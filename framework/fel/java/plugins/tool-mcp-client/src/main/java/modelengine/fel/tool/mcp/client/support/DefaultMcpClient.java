/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

import static modelengine.fitframework.inspection.Validation.notBlank;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.json.schema.jackson.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.client.elicitation.ElicitRequest;
import modelengine.fel.tool.mcp.client.elicitation.ElicitResult;
import modelengine.fel.tool.mcp.client.support.handler.McpClientLogHandler;
import modelengine.fel.tool.mcp.client.support.handler.McpElicitationHandler;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fitframework.inspection.Nullable;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.UuidUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A default implementation of the MCP client that uses the MCP SDK's streamable HTTP transport.
 *
 * <p><b>Not thread-safe:</b> This class is <b>not thread-safe</b>. External synchronization is required
 * if instances are to be accessed by multiple threads concurrently.
 *
 * @author 黄可欣
 * @since 2025-11-03
 */
public class DefaultMcpClient implements McpClient {
    private static final Logger log = Logger.get(DefaultMcpClient.class);

    private final String clientId;
    private final McpSyncClient mcpSyncClient;

    private volatile boolean initialized = false;
    private volatile boolean closed = false;

    /**
     * Constructs a new instance of the DefaultMcpClient.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The endpoint for the Server-Sent Events (SSE) connection.
     * @param requestTimeoutSeconds The timeout duration of requests. Units: seconds.
     */
    public DefaultMcpClient(String baseUri, String sseEndpoint, McpClientTransport transport, int requestTimeoutSeconds,
            @Nullable Function<ElicitRequest, ElicitResult> elicitationHandler) {
        this.clientId = UuidUtils.randomUuidString();
        notBlank(baseUri, "The MCP server base URI cannot be blank.");
        notBlank(sseEndpoint, "The MCP server SSE endpoint cannot be blank.");
        log.info("Creating MCP client. [clientId={}, baseUri={}]", this.clientId, baseUri);
        McpClientLogHandler logHandler = new McpClientLogHandler(this.clientId);
        if (elicitationHandler != null) {
            McpElicitationHandler mcpElicitationHandler =
                    new McpElicitationHandler(this.clientId, elicitationHandler);
            this.mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
                    .capabilities(McpSchema.ClientCapabilities.builder().elicitation().build())
                    .loggingConsumer(logHandler::handleLoggingMessage)
                    .elicitation(mcpElicitationHandler::handleElicitationRequest)
                    .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .jsonSchemaValidator(new DefaultJsonSchemaValidator(new ObjectMapper()))
                    .build();
        } else {
            this.mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
                    .capabilities(McpSchema.ClientCapabilities.builder().build())
                    .loggingConsumer(logHandler::handleLoggingMessage)
                    .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .jsonSchemaValidator(new DefaultJsonSchemaValidator(new ObjectMapper()))
                    .build();
        }
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Initializes the MCP client connection.
     *
     * @throws IllegalStateException if the client has already been closed.
     */
    @Override
    public void initialize() {
        this.ensureNotClosed();
        this.mcpSyncClient.initialize();
        this.initialized = true;
        log.info("MCP client initialized successfully. [clientId={}]", this.clientId);
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
        this.ensureReady();
        try {
            McpSchema.ListToolsResult result = this.mcpSyncClient.listTools();
            if (result == null || result.tools() == null) {
                log.warn("Failed to get tools list: result is null. [clientId={}]", this.clientId);
                throw new IllegalStateException("Failed to get tools list from MCP server: result is null.");
            }

            List<Tool> tools = result.tools().stream().map(this::convertToFelTool).collect(Collectors.toList());

            log.info("Successfully retrieved tools list. [clientId={}, count={}]", this.clientId, tools.size());
            tools.forEach(tool -> log.debug("Tool information. [name={}, description={}]",
                    tool.getName(),
                    tool.getDescription()));
            return tools;
        } catch (Exception e) {
            log.error("Failed to get tools list. [clientId={}, error={}]", this.clientId, e.getMessage());
            throw new IllegalStateException(StringUtils.format("Failed to get tools from MCP server. [error={0}]",
                    e.getMessage()), e);
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
        this.ensureReady();
        try {
            log.info("Calling tool. [clientId={}, name={}, arguments={}]", this.clientId, name, arguments);
            McpSchema.CallToolResult result =
                    this.mcpSyncClient.callTool(new McpSchema.CallToolRequest(name, arguments));

            if (result == null) {
                log.error("Failed to call tool: result is null. [clientId={}, name={}]", this.clientId, name);
                throw new IllegalStateException(StringUtils.format("Failed to call tool: result is null. [name={0}]",
                        name));
            }
            return this.processToolResult(result, name);
        } catch (Exception e) {
            log.error("Failed to call tool. [clientId={}, name={}, error={}]", this.clientId, name, e.getMessage());
            throw new IllegalStateException(StringUtils.format("Failed to call tool. [name={0}, error={1}]",
                    name,
                    e.getMessage()), e);
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
            String errorDetails = this.extractErrorDetails(result.content());
            log.error("Tool returned an error. [clientId={}, name={}, details={}]", this.clientId, name, errorDetails);
            throw new IllegalStateException(StringUtils.format("Tool returned an error. [name={0}, details={1}]",
                    name,
                    errorDetails));
        }

        if (result.content() == null || result.content().isEmpty()) {
            log.warn("Tool returned empty content. [clientId={}, name={}]", this.clientId, name);
            return null;
        }

        Object content = result.content().get(0);
        if (content instanceof McpSchema.TextContent textContent) {
            log.info("Successfully called tool. [clientId={}, name={}, result={}]",
                    this.clientId,
                    name,
                    textContent.text());
            return textContent.text();
        } else if (content instanceof McpSchema.ImageContent imageContent) {
            log.info("Successfully called tool: image content. [clientId={}, name={}]", this.clientId, name);
            return imageContent;
        } else {
            log.info("Successfully called tool. [clientId={}, name={}, contentType={}]",
                    this.clientId,
                    name,
                    content.getClass().getSimpleName());
            return content;
        }
    }

    /**
     * Closes the MCP client connection and releases associated resources.
     *
     * <p><b>Note:</b> This method is <b>not thread-safe</b>. Callers should ensure that this method
     * is not invoked concurrently from multiple threads.
     *
     * @throws IOException if an I/O error occurs during the close operation.
     */
    @Override
    public void close() throws IOException {
        this.ensureNotClosed();
        this.closed = true;
        this.mcpSyncClient.closeGracefully();
        log.info("MCP client closed. [clientId={}]", this.clientId);
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
            throw new IllegalStateException(StringUtils.format("The MCP client is already closed. [clientId={0}]",
                    this.clientId));
        }
    }

    /**
     * Ensures the MCP client is ready for operations (not closed and initialized).
     *
     * @throws IllegalStateException if the client is closed or not initialized.
     */
    private void ensureReady() {
        this.ensureNotClosed();
        if (!this.initialized) {
            throw new IllegalStateException(StringUtils.format("MCP client is not initialized. [clientId={0}]",
                    this.clientId));
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
