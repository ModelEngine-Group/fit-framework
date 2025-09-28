package modelengine.fel.tool.mcp.server;


import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Component;
import io.modelcontextprotocol.server.McpSyncServer;

import java.time.Duration;

@Component
public class SdkMcpServer {
    private final McpSyncServer mcpSyncServer;

    public SdkMcpServer(DefaultMcpStreamableServerTransportProvider transportProvider) {
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
