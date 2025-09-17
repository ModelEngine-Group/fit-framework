/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

/**
 * Represents different methods used in MCP, which are essential for communication and interaction.
 *
 * @since 2025-05-23
 */
public enum Method {
    /**
     * Represents the initialization method, used to set up the environment or system.
     */
    INITIALIZE("initialize"),

    /**
     * Represents the ping method, used to check the availability or connectivity of a service.
     */
    PING("ping"),

    /**
     * Represents the method to retrieve a list of tools, typically used in tool management.
     */
    TOOLS_LIST("tools/list"),

    /**
     * Represents the method to call a specific tool, used for executing tool functions.
     */
    TOOLS_CALL("tools/call"),

    /**
     * Represents the notification method indicating that the system has been initialized.
     */
    NOTIFICATION_INITIALIZED("notifications/initialized"),

    /**
     * Represents the notification method indicating a change in the list of tools.
     */
    NOTIFICATION_TOOLS_CHANGED("notifications/tools/list_changed"),

    /**
     * Represents the method to set logging level.
     * TODO The naming need to be standardized as snake_case.
     */
    LOGGING_SET_LEVEL("logging/setLevel");

    private final String code;

    Method(String code) {
        this.code = code;
    }

    /**
     * Returns the code associated with the method.
     *
     * @return The code of the method.
     */
    public String code() {
        return this.code;
    }
}