/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import modelengine.fel.tool.mcp.server.MessageRequest;
import modelengine.fel.tool.mcp.server.MessageResponse;

/**
 * Represents a request for an unsupported method in the MCP server.
 * This request is handled by {@link UnsupportedMethodHandler} to indicate that the
 * corresponding operation is not implemented or supported.
 *
 * @since 2025-05-15
 */
public class UnsupportedMethodHandler
        extends AbstractMessageHandler<UnsupportedMethodHandler.UnsupportedMethodRequest> {
    /**
     * Constructs a new instance of the UnsupportedMethodHandler class.
     *
     * <p>This handler is used to handle requests for methods that are not supported or implemented.</p>
     */
    public UnsupportedMethodHandler() {
        super(UnsupportedMethodRequest.class);
    }

    @Override
    public MessageResponse handle(UnsupportedMethodRequest request) {
        throw new UnsupportedOperationException("Not supported request method.");
    }

    /**
     * Represents a request for an operation that is not supported by the current handler.
     * This class is used in conjunction with {@link UnsupportedMethodHandler} to signal
     * that the requested method has no implementation.
     *
     * @since 2025-05-15
     */
    public static class UnsupportedMethodRequest extends MessageRequest {}
}
