package modelengine.fel.tool.mcp.client.support;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fitframework.annotation.Bean;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Component
public class DefaultMcpStreamableClient {
    private static final Logger log = Logger.get(DefaultMcpStreamableClient.class);

    @Bean
    public HttpClientStreamableHttpTransport mcpTransport() {
        return HttpClientStreamableHttpTransport.builder("http://localhost:9000")
                .jsonMapper(McpJsonMapper.getDefault())
                .endpoint("/mcp")
                .build();
    }

    @Bean
    public McpSyncClient mcpSyncClient(HttpClientStreamableHttpTransport transport) {
        return McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .roots(true)      // Enable roots capability
                        .elicitation()
                        .build())
                .loggingConsumer(DefaultMcpStreamableClient::handleLoggingMessage)
                .elicitation(DefaultMcpStreamableClient::handleElicitationRequest)
                .build();
    }

    public static void handleLoggingMessage(McpSchema.LoggingMessageNotification notification) {
        System.out.println("[Received log] " + notification.level() +
                " - " + notification.data());
    }

    public static McpSchema.ElicitResult handleElicitationRequest(McpSchema.ElicitRequest request) {
        Map<String, Object> schema = request.requestedSchema();
        Map<String, Object> userData = new HashMap<>();

        System.out.println("[ElicitationMessage] "+ request.message());

        // Check what information is being requested
        if (schema != null && schema.containsKey("properties")) {
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            if (properties.containsKey("message")) {
                System.out.print("[ElicitationRequest] Input additional message: ");
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                userData.put("message", input);
            }
        }
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, userData);
    }
}
