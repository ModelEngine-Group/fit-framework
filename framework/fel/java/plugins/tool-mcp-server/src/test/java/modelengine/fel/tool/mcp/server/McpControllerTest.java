/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;

import modelengine.fitframework.serialization.ObjectSerializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link McpController}.
 *
 * @author 季聿阶
 * @since 2025-05-20
 */
@DisplayName("Unit tests for McpController")
public class McpControllerTest {
    private ObjectSerializer objectSerializer;
    private McpServer mcpServer;
    private String baseUrl;

    @BeforeEach
    void setup() {
        this.objectSerializer = mock(ObjectSerializer.class);
        this.mcpServer = mock(McpServer.class);
        this.baseUrl = "http://localhost:8080";
    }

    @Nested
    @DisplayName("Constructor Tests")
    class GivenConstructor {
        @Test
        @DisplayName("Should throw exception when base URL is null or blank")
        void shouldThrowExceptionWhenBaseUrlIsNullOrEmpty() {
            // Null
            var exception1 = catchThrowableOfType(IllegalArgumentException.class,
                    () -> new McpController(null, objectSerializer, mcpServer));
            assertThat(exception1).hasMessage("The base URL for MCP server cannot be blank.");

            // Blank
            var exception2 = catchThrowableOfType(IllegalArgumentException.class,
                    () -> new McpController("", objectSerializer, mcpServer));
            assertThat(exception2).hasMessage("The base URL for MCP server cannot be blank.");
        }

        @Test
        @DisplayName("Should throw exception when serializer is null")
        void shouldThrowExceptionWhenSerializerIsNull() {
            var exception = catchThrowableOfType(IllegalArgumentException.class,
                    () -> new McpController(baseUrl, null, mcpServer));
            assertThat(exception).hasMessage("The json serializer cannot be null.");
        }

        @Test
        @DisplayName("Should throw exception when mcpServer is null")
        void shouldThrowExceptionWhenMcpServerIsNull() {
            var exception = catchThrowableOfType(IllegalArgumentException.class,
                    () -> new McpController(baseUrl, objectSerializer, null));
            assertThat(exception).hasMessage("The MCP server cannot be null.");
        }
    }
}
