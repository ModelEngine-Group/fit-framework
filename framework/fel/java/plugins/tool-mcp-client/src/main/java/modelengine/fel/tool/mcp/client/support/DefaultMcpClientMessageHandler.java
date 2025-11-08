/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles MCP client messages received from MCP server,
 * including logging notifications and elicitation requests.
 *
 * @author 黄可欣
 * @since 2025-11-03
 */
@Component
public class DefaultMcpClientMessageHandler {
    private static final Logger log = Logger.get(DefaultMcpClientMessageHandler.class);

    /**
     * Handles logging messages received from the MCP server.
     *
     * @param notification The {@link McpSchema.LoggingMessageNotification} containing the log level and data.
     */
    public static void defaultLoggingMessageHandler(McpSchema.LoggingMessageNotification notification) {
        log.info("Received logging message from MCP server. [level={}, data={}]",
                notification.level(),
                notification.data());
    }
}
