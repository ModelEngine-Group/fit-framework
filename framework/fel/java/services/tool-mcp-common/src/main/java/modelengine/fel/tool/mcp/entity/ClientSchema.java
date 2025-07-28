/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

/**
 * Represents a client entity in the MCP framework, encapsulating information about the client's protocol version,
 * capabilities, and additional client details.
 *
 * @since 2025-05-22
 */
public record ClientSchema(String protocolVersion, Capabilities capabilities, Info clientInfo) {
    /**
     * Represents the capabilities supported by the client.
     */
    public record Capabilities() {}

    /**
     * Represents additional information about the client, such as its name and version.
     */
    public record Info(String name, String version) {}
}