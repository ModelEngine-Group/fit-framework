/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;

import java.time.Duration;

/**
 * Mcp Server Bean implemented with MCP SDK.
 *
 * @author 黄可欣
 * @since 2025-10-22
 */
@Component
public class DefaultMcpServerBean {
    @Bean
    public DefaultMcpStreamableServerTransportProvider defaultMcpStreamableServerTransportProvider() {
        return DefaultMcpStreamableServerTransportProvider.builder()
                .objectMapper(new ObjectMapper())
                .build();
    }

    @Bean
    public McpSyncServer mcpSyncServer(DefaultMcpStreamableServerTransportProvider transportProvider) {
        return io.modelcontextprotocol.server.McpServer.sync(transportProvider)
                .serverInfo("FIT Store MCP Server", "3.6.0-SNAPSHOT")
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
