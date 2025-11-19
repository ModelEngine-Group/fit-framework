/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

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
    public McpClient create(String baseUri, String sseEndpoint) {
        return new DefaultMcpStreamableClient(baseUri, sseEndpoint, requestTimeoutSeconds);
    }
}
