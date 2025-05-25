/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.client.support;

import modelengine.fel.tool.mcp.client.McpClient;
import modelengine.fel.tool.mcp.client.McpClientFactory;
import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * Represents a factory for creating instances of the DefaultMcpClient.
 * This class is responsible for initializing and configuring the HTTP client and JSON serializer
 * required by the DefaultMcpClient.
 *
 * @author 季聿阶
 * @since 2025-05-21
 */
@Component
public class DefaultMcpClientFactory implements McpClientFactory {
    private final HttpClassicClient client;
    private final ObjectSerializer jsonSerializer;

    /**
     * Constructs a new instance of the DefaultMcpClientFactory.
     *
     * @param clientFactory The factory used to create the HTTP client.
     * @param jsonSerializer The JSON serializer used for serialization and deserialization.
     */
    public DefaultMcpClientFactory(HttpClassicClientFactory clientFactory,
            @Fit(alias = "json") ObjectSerializer jsonSerializer) {
        this.client = clientFactory.create(HttpClassicClientFactory.Config.builder()
                .connectTimeout(30_000)
                .socketTimeout(60_000)
                .connectionRequestTimeout(60_000)
                .build());
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public McpClient create(String connectionString) {
        return new DefaultMcpClient(this.jsonSerializer, this.client, connectionString);
    }
}
