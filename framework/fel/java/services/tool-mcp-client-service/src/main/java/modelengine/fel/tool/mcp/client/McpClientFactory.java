/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Indicates the factory of {@link McpClient}.
 * <p>
 * Each {@link McpClient} instance created by this factory is designed to connect to a single specified MCP server.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
public interface McpClientFactory {
    /**
     * Creates a {@link McpClient} instance with default logging consumer but without elicitation ability.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @return The connected {@link McpClient} instance with default logging consumer but without elicitation ability.
     */
    McpClient create(String baseUri, String sseEndpoint);

    /**
     * Creates a {@link McpClient} instance with custom logging consumer but without elicitation ability.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @param loggingConsumer The consumer to handle logging messages from the MCP server.
     * @return The connected {@link McpClient} instance with custom logging consumer but without elicitation ability.
     */
    McpClient create(String baseUri, String sseEndpoint,
            Consumer<McpSchema.LoggingMessageNotification> loggingConsumer);

    /**
     * Creates a {@link McpClient} instance with default logging consumer and elicitation ability.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @param elicitationHandler The function to handle elicitation requests from the MCP server.
     * @return The connected {@link McpClient} instance with default logging consumer and elicitation ability.
     */
    McpClient create(String baseUri, String sseEndpoint,
            Function<McpSchema.ElicitRequest, McpSchema.ElicitResult> elicitationHandler);

    /**
     * Creates a {@link McpClient} instance with custom message handlers and elicitation ability.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @param loggingConsumer The consumer to handle logging messages from the MCP server.
     * @param elicitationHandler The function to handle elicitation requests from the MCP server.
     * @return The connected {@link McpClient} instance with custom message handlers and elicitation ability.
     */
    McpClient create(String baseUri, String sseEndpoint, Consumer<McpSchema.LoggingMessageNotification> loggingConsumer,
            Function<McpSchema.ElicitRequest, McpSchema.ElicitResult> elicitationHandler);
}