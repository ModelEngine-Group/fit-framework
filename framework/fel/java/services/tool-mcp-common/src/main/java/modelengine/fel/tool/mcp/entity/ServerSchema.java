/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

import static modelengine.fitframework.util.ObjectUtils.cast;

/**
 * Represents a server entity in the MCP framework, encapsulating information about the server's protocol version,
 * capabilities, and additional server details.
 *
 * @author 季聿阶
 * @since 2025-05-22
 */
public record ServerSchema(String protocolVersion, McpSchema.ServerCapabilities capabilities, McpSchema.Implementation serverInfo) {
    /**
     * Creates a new {@link ServerSchema} instance based on the provided map of server information.
     *
     * @param map The map containing server information.
     * @return A new {@link ServerSchema} instance.
     */
    public static ServerSchema create(Map<String, Object> map) {
        String protocolVersion = cast(map.get("protocolVersion"));
        Map<String, Object> capabilitiesMap = cast(map.get("capabilities"));
        Map<String, Object> toolsMap = cast(capabilitiesMap.get("tools"));
        boolean toolsListChanged = cast(toolsMap.getOrDefault("listChanged", false));
        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(toolsListChanged)             // Enable tool support
                .logging()               // Enable logging support
                .completions()           // Enable completions support
                .build();
        Map<String, Object> infoMap = cast(map.get("serverInfo"));
        String name = cast(infoMap.get("name"));
        String version = cast(infoMap.get("version"));
        McpSchema.Implementation serverInfo = new McpSchema.Implementation(name, version);
        return new ServerSchema(protocolVersion, capabilities, serverInfo);
    }
}