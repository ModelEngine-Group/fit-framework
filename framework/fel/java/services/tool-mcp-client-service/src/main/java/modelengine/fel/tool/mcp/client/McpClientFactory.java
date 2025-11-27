/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client;

import modelengine.fel.tool.mcp.client.elicitation.ElicitRequest;
import modelengine.fel.tool.mcp.client.elicitation.ElicitResult;
import modelengine.fitframework.inspection.Nullable;

import java.util.function.Function;

/**
 * Factory for creating {@link McpClient} instances with SSE or Streamable HTTP transport.
 * <p>Each client connects to a single MCP server.</p>
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
public interface McpClientFactory {
    /**
     * Creates a client with streamable HTTP transport.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @param elicitationFunction The function to handle {@link ElicitRequest} and return {@link ElicitResult}.
     * If null, elicitation will not be supported in MCP client.
     * @return The created {@link McpClient} instance.
     */
    McpClient createStreamable(String baseUri, String sseEndpoint,
            @Nullable Function<ElicitRequest, ElicitResult> elicitationFunction);

    /**
     * Creates a client with SSE transport.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @param elicitationFunction The function to handle {@link ElicitRequest} and return {@link ElicitResult}.
     * If null, elicitation will not be supported in MCP client.
     * @return The created {@link McpClient} instance.
     */
    McpClient createSse(String baseUri, String sseEndpoint,
            @Nullable Function<ElicitRequest, ElicitResult> elicitationFunction);

    /**
     * Creates a client with streamable HTTP transport (default). No elicitation support.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @return The created {@link McpClient} instance.
     */
    default McpClient create(String baseUri, String sseEndpoint) {
        return this.createStreamable(baseUri, sseEndpoint, null);
    }

    /**
     * Creates a client with streamable HTTP transport. No elicitation support.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @return The created {@link McpClient} instance.
     */
    default McpClient createStreamable(String baseUri, String sseEndpoint) {
        return this.createStreamable(baseUri, sseEndpoint, null);
    }

    /**
     * Creates a client with SSE transport. No elicitation support.
     *
     * @param baseUri The base URI of the MCP server.
     * @param sseEndpoint The SSE endpoint of the MCP server.
     * @return The created {@link McpClient} instance.
     */
    default McpClient createSse(String baseUri, String sseEndpoint) {
        return this.createSse(baseUri, sseEndpoint, null);
    }
}