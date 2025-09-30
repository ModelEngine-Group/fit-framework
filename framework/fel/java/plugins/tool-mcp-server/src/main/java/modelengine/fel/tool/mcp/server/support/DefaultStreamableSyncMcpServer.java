package modelengine.fel.tool.mcp.server.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;
import io.modelcontextprotocol.server.McpSyncServer;
import modelengine.fitframework.annotation.ImportConfigs;

import java.time.Duration;

@Component
public class DefaultStreamableSyncMcpServer {
    @Bean
    public DefaultMcpStreamableServerTransportProvider defaultMcpStreamableServerTransportProvider() {
        return DefaultMcpStreamableServerTransportProvider.builder()
                .objectMapper(new ObjectMapper())
                .build();
    }

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
