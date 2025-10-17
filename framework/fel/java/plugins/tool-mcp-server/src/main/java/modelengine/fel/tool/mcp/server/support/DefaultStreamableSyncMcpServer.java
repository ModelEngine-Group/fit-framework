/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;
import io.modelcontextprotocol.server.McpSyncServer;

import java.time.Duration;

/**
 * Mcp Server implemented with MCP SDK.
 *
 * @author 黄可欣
 * @since 2025-09-30
 */
@Component
public class DefaultStreamableSyncMcpServer {
    /**
     * Construct a transport provider Bean for MCP Server.
     */
    @Bean
    public DefaultMcpStreamableServerTransportProvider defaultMcpStreamableServerTransportProvider() {
        return DefaultMcpStreamableServerTransportProvider.builder()
                .objectMapper(new ObjectMapper())
                .build();
    }

    /**
     * Construct a synchronized MCP Server Bean with MCP SDK.
     *
     * @param transportProvider The bean of {@link DefaultMcpStreamableServerTransportProvider}.
     */
    @Bean
    public McpSyncServer mcpSyncServer(DefaultMcpStreamableServerTransportProvider transportProvider) {
        return McpServer.sync(transportProvider)
                .serverInfo("fit-mcp-streamable-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .resources(false, true)  // Enable resource support
                        .tools(true)             // Enable tool support
                        .prompts(true)           // Enable prompt support
                        .logging()               // Enable logging support
                        .completions()           // Enable completions support
                        .build())
                .requestTimeout(Duration.ofSeconds(10))
                .build();
    }
}
