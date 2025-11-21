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
import modelengine.fel.tool.mcp.server.FitMcpServer;
import modelengine.fel.tool.mcp.server.transport.FitMcpStreamableServerTransportProvider;
import modelengine.fel.tool.service.ToolChangedObserverRegistry;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.annotation.Value;

import java.time.Duration;

/**
 * MCP Streamable Server Bean implemented with MCP SDK.
 *
 * @author 黄可欣
 * @since 2025-10-22
 */
@Component
public class McpStreamableServerConfig {
    @Bean
    public FitMcpStreamableServerTransportProvider fitMcpStreamableServerTransportProvider(
            @Value("${mcp.server.ping.interval-seconds}") int keepAliveIntervalSeconds,
            @Value("${mcp.server.streamable.disallow-delete}") boolean disallowDelete) {
        return FitMcpStreamableServerTransportProvider.builder()
                .jsonMapper(McpJsonMapper.getDefault())
                .keepAliveInterval(Duration.ofSeconds(keepAliveIntervalSeconds))
                .disallowDelete(disallowDelete)
                .build();
    }

    @Bean("McpSyncStreamableServer")
    public McpSyncServer mcpSyncStreamableServer(FitMcpStreamableServerTransportProvider transportProvider,
            @Value("${mcp.server.request.timeout-seconds}") int requestTimeoutSeconds) {
        return McpServer.sync(transportProvider)
                .serverInfo("FIT Store MCP Streamable Server", "3.6.1-SNAPSHOT")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).logging().build())
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .build();
    }

    @Bean("McpStreamableServer")
    public FitMcpServer defaultMcpStreamableServer(ToolExecuteService toolExecuteService,
            @Fit(alias = "McpSyncStreamableServer") McpSyncServer mcpSyncServer,
            ToolChangedObserverRegistry toolChangedObserverRegistry) {
        return new FitMcpServer(toolExecuteService, mcpSyncServer, toolChangedObserverRegistry);
    }
}
