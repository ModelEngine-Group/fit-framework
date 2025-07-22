/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

/**
 * Represents different types of events used in MCP.
 *
 * @since 2025-05-22
 */
public enum Event {
    /**
     * Represents an endpoint event.
     */
    ENDPOINT("endpoint"),

    /**
     * Represents a message event.
     */
    MESSAGE("message");

    private final String code;

    /**
     * Constructor to initialize the event with a specific code.
     *
     * @param code The code associated with the event.
     */
    Event(String code) {
        this.code = code;
    }

    /**
     * Returns the code associated with the event.
     *
     * @return The code of the event.
     */
    public String code() {
        return this.code;
    }
}