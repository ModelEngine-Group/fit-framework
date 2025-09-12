/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

/**
 * Represents different logging level in MCP server, following the RFC-5424 severity scale.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5424#section-6.2.1">RFC 5424</a>
 * @since 2025-09-10
 */
public enum LoggingLevel {
    /**
     * Detailed debugging information (function entry/exit points).
     */
    DEBUG("debug"),

    /**
     * General informational messages (operation progress updates).
     */
    INFO("info"),

    /**
     * Normal but significant events (configuration changes).
     */
    NOTICE("notice"),

    /**
     * Warning conditions (deprecated feature usage).
     */
    WARNING("warning"),

    /**
     * Error conditions (operation failures).
     */
    ERROR("error"),

    /**
     * Critical conditions (system component failures).
     */
    CRITICAL("critical"),

    /**
     * Action must be taken immediately (data corruption detected).
     */
    ALERT("alert"),

    /**
     * System is unusable (complete system failure).
     */
    EMERGENCY("emergency");

    private final String code;

    LoggingLevel(String code) { this.code = code; }

    /**
     * Returns the code associated with the logging level.
     *
     * @return The code of the logging level.
     */
    public String code() { return this.code; }

    /**
     * Reverse lookup by code (ignore case).
     *
     * @param code The external code
     * @return Corresponding enum or {@code null}
     */
    public static LoggingLevel fromCode(String code) {
        if (code == null) return null;
        for (LoggingLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        return null;
    }
}
