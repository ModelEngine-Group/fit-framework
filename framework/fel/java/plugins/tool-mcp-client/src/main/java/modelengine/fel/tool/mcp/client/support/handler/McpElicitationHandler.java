/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support.handler;

import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.client.elicitation.ElicitRequest;
import modelengine.fel.tool.mcp.client.elicitation.ElicitResult;
import modelengine.fitframework.log.Logger;

import java.util.function.Function;

/**
 * MCP elicitation handler that delegates to an external handler function.
 *
 * <p>Converts {@link McpSchema.ElicitRequest} to {@link ElicitRequest},
 * calls the user's handler, and converts {@link ElicitResult} back to {@link McpSchema.ElicitResult}.</p>
 *
 * @author 黄可欣
 * @since 2025-11-25
 */
public class McpElicitationHandler {
    private static final Logger log = Logger.get(McpElicitationHandler.class);
    private final String clientId;
    private final Function<ElicitRequest, ElicitResult> elicitationHandler;

    /**
     * Constructs a new handler.
     *
     * @param clientId The client ID.
     * @param elicitationHandler The user's handler function that processes {@link ElicitRequest}
     * and returns {@link ElicitResult}.
     */
    public McpElicitationHandler(String clientId, Function<ElicitRequest, ElicitResult> elicitationHandler) {
        this.clientId = clientId;
        this.elicitationHandler = elicitationHandler;
    }

    /**
     * Handles an elicitation request by converting {@link McpSchema.ElicitRequest} to {@link ElicitRequest},
     * delegating to the user's handler, and converting {@link ElicitResult} back to {@link McpSchema.ElicitResult}.
     *
     * @param request The {@link McpSchema.ElicitRequest} from MCP server.
     * @return The {@link McpSchema.ElicitResult} to send back to MCP server.
     */
    public McpSchema.ElicitResult handleElicitationRequest(McpSchema.ElicitRequest request) {
        log.info("Received elicitation request from MCP server. [clientId={}, message={}, requestSchema={}]",
                this.clientId,
                request.message(),
                request.requestedSchema());

        try {
            ElicitRequest elicitRequest = new ElicitRequest(request.message(), request.requestedSchema());
            ElicitResult result = this.elicitationHandler.apply(elicitRequest);
            log.info("Successfully handled elicitation request. [clientId={}, action={}, content={}]",
                    this.clientId,
                    result.action(),
                    result.content());

            McpSchema.ElicitResult.Action mcpAction = switch (result.action()) {
                case ACCEPT -> McpSchema.ElicitResult.Action.ACCEPT;
                case DECLINE -> McpSchema.ElicitResult.Action.DECLINE;
                case CANCEL -> McpSchema.ElicitResult.Action.CANCEL;
            };
            return new McpSchema.ElicitResult(mcpAction, result.content());
        } catch (Exception e) {
            log.error("Failed to handle elicitation request. [clientId={}, error={}]",
                    this.clientId,
                    e.getMessage(),
                    e);
            throw new IllegalStateException("Failed to handle elicitation request: " + e.getMessage(), e);
        }
    }
}
