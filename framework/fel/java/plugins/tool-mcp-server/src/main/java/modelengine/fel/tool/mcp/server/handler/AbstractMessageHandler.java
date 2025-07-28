/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.mcp.server.MessageHandler;
import modelengine.fel.tool.mcp.server.MessageRequest;
import modelengine.fitframework.util.ObjectUtils;

import java.util.Map;

/**
 * The abstract parent class of {@link MessageHandler}.
 *
 * @since 2025-05-15
 */
public abstract class AbstractMessageHandler<Req extends MessageRequest> implements MessageHandler {
    private final Class<Req> requestClass;

    AbstractMessageHandler(Class<Req> requestClass) {
        this.requestClass = notNull(requestClass, "The request class cannot be null.");
    }

    @Override
    public Object handle(Map<String, Object> request) {
        Req req = ObjectUtils.toCustomObject(request, this.requestClass);
        return this.handle(req);
    }

    /**
     * Handles the request.
     *
     * @param request The request as a {@link Req}.
     * @return The response as a {@link Object}.
     */
    abstract Object handle(Req request);
}
