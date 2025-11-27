/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.elicitation;

import java.util.Map;

/**
 * Represents an elicitation request from an MCP server.
 * This is a simplified version that doesn't depend on MCP SDK types.
 *
 * @param message The {@link String} message describing what information is needed from the user.
 * @param requestedSchema The {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >} JSON schema defining
 * the elicitation request data structure.
 * @author 黄可欣
 * @see <a href=https://modelcontextprotocol.io/specification/2025-06-18/client/elicitation#protocol-messages>MCP
 * Protocol</a>
 * @since 2025-11-25
 */
public record ElicitRequest(String message, Map<String, Object> requestedSchema) {}
