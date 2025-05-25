/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

/**
 * Represents a server entity in the MCP framework, encapsulating information about the server's protocol version,
 * capabilities, and additional server details.
 *
 * @author 季聿阶
 * @since 2025-05-22
 */
public class Server {
    private String protocolVersion;
    private Capabilities capabilities;
    private Info serverInfo;

    /**
     * Returns the protocol version used by the server.
     *
     * @return The protocol version.
     */
    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    /**
     * Sets the protocol version used by the server.
     *
     * @param protocolVersion The protocol version to set.
     */
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Returns the capabilities supported by the server.
     *
     * @return The server capabilities.
     */
    public Capabilities getCapabilities() {
        return this.capabilities;
    }

    /**
     * Sets the capabilities supported by the server.
     *
     * @param capabilities The server capabilities to set.
     */
    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Returns additional information about the server.
     *
     * @return The server information.
     */
    public Info getServerInfo() {
        return this.serverInfo;
    }

    /**
     * Sets additional information about the server.
     *
     * @param serverInfo The server information to set.
     */
    public void setServerInfo(Info serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public String toString() {
        return "Server{" + "protocolVersion='" + protocolVersion + '\'' + ", capabilities=" + capabilities
                + ", serverInfo=" + serverInfo + '}';
    }

    /**
     * Represents the capabilities supported by the server, including logging and tool-related functionalities.
     */
    public static class Capabilities {
        private Logging logging;
        private Tools tools;

        /**
         * Returns the logging capabilities of the server.
         *
         * @return The logging capabilities.
         */
        public Logging getLogging() {
            return this.logging;
        }

        /**
         * Sets the logging capabilities of the server.
         *
         * @param logging The logging capabilities to set.
         */
        public void setLogging(Logging logging) {
            this.logging = logging;
        }

        /**
         * Returns the tool-related capabilities of the server.
         *
         * @return The tool-related capabilities.
         */
        public Tools getTools() {
            return this.tools;
        }

        /**
         * Sets the tool-related capabilities of the server.
         *
         * @param tools The tool-related capabilities to set.
         */
        public void setTools(Tools tools) {
            this.tools = tools;
        }

        @Override
        public String toString() {
            return "Capabilities{" + "logging=" + logging + ", tools=" + tools + '}';
        }

        /**
         * Represents the logging capabilities of the server.
         */
        public static class Logging {}

        /**
         * Represents the tool-related capabilities of the server, including whether the tool list has changed.
         */
        public static class Tools {
            private boolean listChanged;

            /**
             * Returns whether the tool list has changed.
             *
             * @return True if the tool list has changed, false otherwise.
             */
            public boolean isListChanged() {
                return this.listChanged;
            }

            /**
             * Sets whether the tool list has changed.
             *
             * @param listChanged The change status of the tool list.
             */
            public void setListChanged(boolean listChanged) {
                this.listChanged = listChanged;
            }

            @Override
            public String toString() {
                return "Tools{" + "listChanged=" + listChanged + '}';
            }
        }
    }

    /**
     * Represents additional information about the server, such as its name and version.
     */
    public static class Info {
        private String name;
        private String version;

        /**
         * Returns the name of the server.
         *
         * @return The server name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Sets the name of the server.
         *
         * @param name The server name to set.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the version of the server.
         *
         * @return The server version.
         */
        public String getVersion() {
            return this.version;
        }

        /**
         * Sets the version of the server.
         *
         * @param version The server version to set.
         */
        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "Info{" + "name='" + name + '\'' + ", version='" + version + '\'' + '}';
        }
    }
}