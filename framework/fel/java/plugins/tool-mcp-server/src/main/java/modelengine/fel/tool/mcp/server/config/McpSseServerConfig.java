/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.config;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.server.transport.FitMcpSseServerTransportProvider;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;

import java.time.Duration;

/**
 * MCP SSE Server Bean implemented with MCP SDK.
 *
 * @author 黄可欣
 * @since 2025-11-10
 */
@Component
public class McpSseServerConfig {
    @Bean
    public FitMcpSseServerTransportProvider fitMcpSseServerTransportProvider(
            @Value("${mcp.server.keep-alive-interval-seconds}") int keepAliveIntervalSeconds) {
        return FitMcpSseServerTransportProvider.builder()
                .jsonMapper(McpJsonMapper.getDefault())
                .keepAliveInterval(Duration.ofSeconds(keepAliveIntervalSeconds))
                .build();
    }

    @Bean("McpSyncSseServer")
    public McpSyncServer mcpSyncSseServer(FitMcpSseServerTransportProvider transportProvider,
            @Value("${mcp.server.request.timeout-seconds}") int requestTimeoutSeconds) {
        return McpServer.sync(transportProvider)
                .serverInfo("FIT Store MCP SSE Server", "3.6.1-SNAPSHOT")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).logging().build())
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .build();
    }
}
