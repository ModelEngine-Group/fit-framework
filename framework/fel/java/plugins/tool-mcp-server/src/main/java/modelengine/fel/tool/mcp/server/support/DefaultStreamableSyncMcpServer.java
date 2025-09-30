package modelengine.fel.tool.mcp.server.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fitframework.annotation.Component;
import io.modelcontextprotocol.server.McpSyncServer;

import java.time.Duration;

@Component
public class DefaultStreamableSyncMcpServer {
    private final McpSyncServer mcpSyncServer;

    public DefaultStreamableSyncMcpServer(ObjectMapper mapper) {
        DefaultMcpStreamableServerTransportProvider transportProvider = DefaultMcpStreamableServerTransportProvider.builder()
                .objectMapper(mapper)
                .build();
        this.mcpSyncServer = McpServer.sync(transportProvider)
                .serverInfo("hkx-server", "1.0.0")
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
