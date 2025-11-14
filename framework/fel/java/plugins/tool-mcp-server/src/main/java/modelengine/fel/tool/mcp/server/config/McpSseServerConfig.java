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
import modelengine.fel.tool.mcp.server.support.DefaultMcpServer;
import modelengine.fel.tool.mcp.server.transport.FitMcpSseServerTransportProvider;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.annotation.Value;

import java.time.Duration;

/**
 * Mcp Server Bean implemented with MCP SDK.
 *
 * @author 黄可欣
 * @since 2025-11-10
 */
@Component
public class McpSseServerConfig {
    @Bean
    public FitMcpSseServerTransportProvider fitMcpSseServerTransportProvider() {
        return FitMcpSseServerTransportProvider.builder().jsonMapper(McpJsonMapper.getDefault()).build();
    }

    @Bean("McpSyncSseServer")
    public McpSyncServer mcpSyncSseServer(FitMcpSseServerTransportProvider transportProvider,
            @Value("${mcp.server.request.timeout-seconds}") int requestTimeoutSeconds) {
        return McpServer.sync(transportProvider)
                .serverInfo("FIT Store MCP Server", "3.6.1-SNAPSHOT")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).logging().build())
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .build();
    }

    @Bean("DefaultMcpSseServer")
    public DefaultMcpServer defaultMcpSseServer(ToolExecuteService toolExecuteService,
            @Fit(alias = "McpSyncSseServer") McpSyncServer mcpSyncServer) {
        return new DefaultMcpServer(toolExecuteService, mcpSyncServer);
    }
}
