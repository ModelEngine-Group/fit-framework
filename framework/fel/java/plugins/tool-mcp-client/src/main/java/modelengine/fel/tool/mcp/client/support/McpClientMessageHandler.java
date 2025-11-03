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
public class McpClientMessageHandler {
    private static final Logger log = Logger.get(McpClientMessageHandler.class);

    /**
     * Handles logging messages received from the MCP server.
     *
     * @param notification The {@link McpSchema.LoggingMessageNotification} containing the log level and data.
     */
    public static void handleLoggingMessage(McpSchema.LoggingMessageNotification notification) {
        log.info("[Client] log: {}-{}", notification.level(), notification.data());
    }

    /**
     * Handles elicitation requests from the MCP server.
     *
     * @param request The {@link McpSchema.ElicitRequest} containing the elicitation message and schema.
     * @return A {@link McpSchema.ElicitResult} with the action {@code ACCEPT} and any collected user data.
     */
    public static McpSchema.ElicitResult handleElicitationRequest(McpSchema.ElicitRequest request) {
        Map<String, Object> schema = request.requestedSchema();
        Map<String, Object> userData = new HashMap<>();

        log.info("[Client]get elicitation: {}", request.message());
        if (schema != null && schema.containsKey("properties")) {
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            if (properties.containsKey("message")) {
                log.info("[ElicitationRequest] Input additional message: ");
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                userData.put("message", input);
            }
        }
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, userData);
    }
}
