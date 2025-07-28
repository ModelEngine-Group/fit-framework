/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

import static modelengine.fitframework.util.ObjectUtils.cast;

import java.util.Map;

/**
 * Represents a server entity in the MCP framework, encapsulating information about the server's protocol version,
 * capabilities, and additional server details.
 *
 * @since 2025-05-22
 */
public record ServerSchema(String protocolVersion, Capabilities capabilities, Info serverInfo) {
    /**
     * Creates a new {@link ServerSchema} instance based on the provided map of server information.
     *
     * @param map The map containing server information.
     * @return A new {@link ServerSchema} instance.
     */
    public static ServerSchema create(Map<String, Object> map) {
        String protocolVersion = cast(map.get("protocolVersion"));
        Map<String, Object> capabilitiesMap = cast(map.get("capabilities"));
        Capabilities.Logging logging = new Capabilities.Logging();
        Map<String, Object> toolsMap = cast(capabilitiesMap.get("tools"));
        boolean toolsListChanged = cast(toolsMap.getOrDefault("listChanged", false));
        Capabilities.Tools tools = new Capabilities.Tools(toolsListChanged);
        Capabilities capabilities = new Capabilities(logging, tools);
        Map<String, Object> infoMap = cast(map.get("serverInfo"));
        String name = cast(infoMap.get("name"));
        String version = cast(infoMap.get("version"));
        Info serverInfo = new Info(name, version);
        return new ServerSchema(protocolVersion, capabilities, serverInfo);
    }

    /**
     * Represents the capabilities supported by the server, including logging and tool-related functionalities.
     */
    public record Capabilities(Logging logging, Tools tools) {
        /**
         * Represents the logging capabilities of the server.
         */
        public record Logging() {}

        /**
         * Represents the tool-related capabilities of the server, including whether the tool list has changed.
         */
        public record Tools(boolean listChanged) {}
    }

    /**
     * Represents additional information about the server, such as its name and version.
     */
    public record Info(String name, String version) {}
}