/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

import modelengine.fitframework.inspection.Nonnull;

/**
 * Represents different logging level in MCP server, following the RFC-5424 severity scale.
 *
 * @author 黄可欣
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5424#section-6.2.1">RFC 5424</a>
 * @since 2025-09-10
 */
public enum LoggingLevel {
    /**
     * Detailed debugging information (function entry/exit points).
     */
    DEBUG(0, "debug"),

    /**
     * General informational messages (operation progress updates).
     */
    INFO(1, "info"),

    /**
     * Normal but significant events (configuration changes).
     */
    NOTICE(2, "notice"),

    /**
     * Warning conditions (deprecated feature usage).
     */
    WARNING(3, "warning"),

    /**
     * Error conditions (operation failures).
     */
    ERROR(4, "error"),

    /**
     * Critical conditions (system component failures).
     */
    CRITICAL(5, "critical"),

    /**
     * Action must be taken immediately (data corruption detected).
     */
    ALERT(6, "alert"),

    /**
     * System is unusable (complete system failure).
     */
    EMERGENCY(7, "emergency");

    private final int level;
    private final String code;

    LoggingLevel(int level, String code) {
        this.level = level;
        this.code = code;
    }

    /**
     * Returns the level number associated with the logging level.
     *
     * @return The number of the logging level as an {@code int}.
     */
    public int level() {
        return this.level;
    }

    /**
     * Returns the code associated with the logging level.
     *
     * @return The code of the logging level as a {@link String}.
     */
    public String code() {
        return this.code;
    }

    /**
     * Returns the default logging level which is INFO level.
     *
     * @return The default INFO logging level as a {@link LoggingLevel}.
     */
    public static LoggingLevel getDefault() {
        return LoggingLevel.INFO;
    }

    /**
     * Return the corresponding {@link LoggingLevel} from the logging level code.
     * If there is no corresponding logging level, return the default logging level.
     *
     * @param code The code of logging level as a {@link String}.
     * @return The corresponding or default logging level as a {@link LoggingLevel}.
     */
    @Nonnull
    public static LoggingLevel fromCode(String code) {
        if (code == null) {
            return LoggingLevel.getDefault();
        }
        for (LoggingLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        return LoggingLevel.getDefault();
    }
}
