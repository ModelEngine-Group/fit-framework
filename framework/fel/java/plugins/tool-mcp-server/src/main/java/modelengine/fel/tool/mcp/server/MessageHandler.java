/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import java.util.Map;

/**
 * A functional interface for handling messages in the MCP server.
 * Implementations of this interface are responsible for processing incoming message requests
 * and returning an appropriate response object.
 *
 * @since 2025-05-15
 */
public interface MessageHandler {
    /**
     * Handles the given message request.
     *
     * @param request A map containing the request parameters and data as a
     * {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     * @return The result of processing the request as an {@link Object}, which can be any type of object.
     */
    Object handle(Map<String, Object> request);
}
