/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import modelengine.fel.tool.mcp.server.MessageRequest;

import java.util.Collections;
import java.util.Map;

/**
 * @author 季聿阶
 * @since 2025-05-15
 */
public class PingHandler extends AbstractMessageHandler<PingHandler.PingRequest> {
    private static final Map<Object, Object> PING_RESULT = Collections.emptyMap();

    /**
     * Constructs a new instance of the PingHandler class.
     */
    public PingHandler() {
        super(PingRequest.class);
    }

    @Override
    public Object handle(PingRequest request) {
        return PING_RESULT;
    }

    /**
     * @author 季聿阶
     * @since 2025-05-15
     */
    public static class PingRequest extends MessageRequest {}
}
