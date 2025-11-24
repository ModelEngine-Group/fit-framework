/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.client.McpClientFactory;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;

/**
 * Represents a factory for creating instances of the {@link DefaultMcpStreamableClient}.
 * This class is responsible for initializing and configuring.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
@Component
public class DefaultMcpClientFactory implements McpClientFactory {
    private final int requestTimeoutSeconds;

    /**
     * Constructs a new instance of the DefaultMcpClientFactory.
     *
     * @param requestTimeoutSeconds The timeout duration of requests. Units: seconds.
     */
    public DefaultMcpClientFactory(@Value("${mcp.client.request.timeout-seconds}") int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds > 0 ? requestTimeoutSeconds : 180;
    }

    @Override
    public McpClient createStreamable(String baseUri, String sseEndpoint) {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(baseUri)
                .jsonMapper(new JacksonMcpJsonMapper(new ObjectMapper()))
                .endpoint(sseEndpoint)
                .build();
        return new DefaultMcpStreamableClient(baseUri, sseEndpoint, this.requestTimeoutSeconds, transport);
    }

    @Override
    public McpClient createSse(String baseUri, String sseEndpoint) {
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(baseUri)
                .jsonMapper(new JacksonMcpJsonMapper(new ObjectMapper()))
                .sseEndpoint(sseEndpoint)
                .build();
        return new DefaultMcpStreamableClient(baseUri, sseEndpoint, this.requestTimeoutSeconds, transport);
    }

    @Override
    @Deprecated
    public McpClient create(String baseUri, String sseEndpoint) {
        return this.createStreamable(baseUri, sseEndpoint);
    }
}
