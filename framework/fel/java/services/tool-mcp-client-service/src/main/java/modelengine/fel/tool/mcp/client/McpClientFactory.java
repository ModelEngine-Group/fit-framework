/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client;

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
     * Creates a {@link McpClient} instance.
     *
     * @param connectionString The connection {@link String}.
     * @return The connected {@link McpClient} instance.
     */
    McpClient create(String connectionString);
}