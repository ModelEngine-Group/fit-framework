/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.modelcontextprotocol.server.McpSyncServer;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.mcp.server.McpServerConfig;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link DefaultMcpStreamableServer}.
 *
 * @author 季聿阶
 * @since 2025-05-20
 */
@DisplayName("Unit tests for DefaultMcpStreamableServer")
public class DefaultMcpStreamableServerTest {
    private ToolExecuteService toolExecuteService;
    private McpSyncServer mcpSyncServer;

    @BeforeEach
    void setup() {
        this.toolExecuteService = mock(ToolExecuteService.class);
        McpServerConfig config = new McpServerConfig();
        this.mcpSyncServer = config.mcpSyncServer(config.fitMcpStreamableServerTransportProvider(), 10);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class GivenConstructor {
        @Test
        @DisplayName("Should throw IllegalArgumentException when toolExecuteService is null")
        void throwIllegalArgumentExceptionWhenToolExecuteServiceIsNull() {
            IllegalArgumentException exception = catchThrowableOfType(IllegalArgumentException.class,
                    () -> new DefaultMcpStreamableServer(null, mcpSyncServer));
            assertThat(exception).isNotNull().hasMessage("The tool execute service cannot be null.");
        }
    }

    @Nested
    @DisplayName("registerToolsChangedObserver and Notification Tests")
    class GivenRegisterAndNotify {
        @Test
        @DisplayName("Should notify observers when tools are added or removed")
        void notifyObserversOnToolAddOrRemove() {
            DefaultMcpStreamableServer server = new DefaultMcpStreamableServer(toolExecuteService, mcpSyncServer);
            McpServer.ToolsChangedObserver observer = mock(McpServer.ToolsChangedObserver.class);
            server.registerToolsChangedObserver(observer);

            Map<String, Object> schema = MapBuilder.<String, Object>get()
                    .put("type", "object")
                    .put("properties", Collections.emptyMap())
                    .put("required", Collections.emptyList())
                    .build();
            server.onToolAdded("tool1", "description1", schema);
            verify(observer, times(1)).onToolsChanged();

            server.onToolRemoved("tool1");
            verify(observer, times(2)).onToolsChanged();
        }
    }

    @Nested
    @DisplayName("onToolAdded Method Tests")
    class GivenOnToolAdded {
        @Test
        @DisplayName("Should add tool successfully with valid parameters")
        void addToolSuccessfully() {
            DefaultMcpStreamableServer server = new DefaultMcpStreamableServer(toolExecuteService, mcpSyncServer);
            String name = "tool1";
            String description = "description1";
            Map<String, Object> schema = MapBuilder.<String, Object>get()
                    .put("type", "object")
                    .put("properties", Collections.emptyMap())
                    .put("required", Collections.emptyList())
                    .build();

            server.onToolAdded(name, description, schema);

            List<Tool> tools = server.getTools();
            assertThat(tools).hasSize(1);

            Tool tool = tools.get(0);
            assertThat(tool.getName()).isEqualTo(name);
            assertThat(tool.getDescription()).isEqualTo(description);
            assertThat(tool.getInputSchema()).isEqualTo(schema);
        }

        @Test
        @DisplayName("Should ignore invalid parameters and not add any tool")
        void ignoreInvalidParameters() {
            DefaultMcpStreamableServer server = new DefaultMcpStreamableServer(toolExecuteService, mcpSyncServer);
            Map<String, Object> schema = MapBuilder.<String, Object>get()
                    .put("type", "object")
                    .put("properties", Collections.emptyMap())
                    .put("required", Collections.emptyList())
                    .build();

            server.onToolAdded("", "description", schema);
            assertThat(server.getTools()).isEmpty();

            server.onToolAdded("tool1", "", schema);
            assertThat(server.getTools()).isEmpty();

            server.onToolAdded("tool1", "description", null);
            assertThat(server.getTools()).isEmpty();
        }
    }

    @Nested
    @DisplayName("onToolRemoved Method Tests")
    class GivenOnToolRemoved {
        @Test
        @DisplayName("Should remove an added tool correctly")
        void removeToolSuccessfully() {
            DefaultMcpStreamableServer server = new DefaultMcpStreamableServer(toolExecuteService, mcpSyncServer);
            Map<String, Object> schema = MapBuilder.<String, Object>get()
                    .put("type", "object")
                    .put("properties", Collections.emptyMap())
                    .put("required", Collections.emptyList())
                    .build();
            server.onToolAdded("tool1", "desc", schema);

            server.onToolRemoved("tool1");

            assertThat(server.getTools()).isEmpty();
        }

        @Test
        @DisplayName("Should ignore removal if name is blank")
        void ignoreBlankName() {
            DefaultMcpStreamableServer server = new DefaultMcpStreamableServer(toolExecuteService, mcpSyncServer);
            Map<String, Object> schema = MapBuilder.<String, Object>get()
                    .put("type", "object")
                    .put("properties", Collections.emptyMap())
                    .put("required", Collections.emptyList())
                    .build();
            server.onToolAdded("tool1", "desc", schema);

            server.onToolRemoved("");

            assertThat(server.getTools()).hasSize(1);
        }
    }
}