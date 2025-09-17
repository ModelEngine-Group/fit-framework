/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import modelengine.fel.tool.mcp.entity.LoggingLevel;
import modelengine.fel.tool.mcp.server.MessageRequest;
import modelengine.fitframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 *  A handler for processing logging set level requests in the MCP server.
 *  This class extends {@link AbstractMessageHandler} and is responsible for handling
 *  {@link LoggingSetLevelRequest} messages.
 *
 * @author 黄可欣
 * @since 2025-09-10
 */
public class LoggingSetLevelHandler extends AbstractMessageHandler<LoggingSetLevelHandler.LoggingSetLevelRequest> {
    private static final Map<Object, Object> SET_LEVEL_RESULT = Collections.emptyMap();

    /**
     * Constructs a new instance of the LoggingSetLevelHandler class.
     */
    public LoggingSetLevelHandler() {
        super(LoggingSetLevelHandler.LoggingSetLevelRequest.class);
    }

    @Override
    public Object handle(LoggingSetLevelHandler.LoggingSetLevelRequest request) {
        if (request == null) {
            throw new IllegalStateException("No logging set level request.");
        }
        if (StringUtils.isBlank(request.getLevel())) {
            throw new IllegalStateException("No logging level in request.");
        }
        String loggingLevelString =  request.getLevel();
        LoggingLevel loggingLevel = LoggingLevel.fromCode(loggingLevelString);
        // TODO change the logging level of corresponding session.
        return SET_LEVEL_RESULT;
    }

    /**
     * Represents a request to set the logging level in the MCP server.
     * This request is handled by {@link LoggingSetLevelHandler} to set the logging level in the MCP server.
     *
     * @since 2025-09-10
     */
    public static class LoggingSetLevelRequest extends MessageRequest {
        private String level;

        /**
         * Gets the level of server logging.
         *
         * @return The level of server logging as a {@link String}.
         */
        public String getLevel() {
            return this.level;
        }

        /**
         * Sets the level of server logging .
         *
         * @param level The level of server logging as a {@link String}.
         */
        public void setLevel(String level) {
            this.level = level;
        }
    }
}
