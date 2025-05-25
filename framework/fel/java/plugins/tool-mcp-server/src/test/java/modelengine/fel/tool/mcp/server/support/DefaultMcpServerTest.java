/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import modelengine.fel.tool.mcp.entity.Server;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.util.MapBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link DefaultMcpServer}.
 *
 * @author 季聿阶
 * @since 2025-05-20
 */
@DisplayName("Unit tests for DefaultMcpServer")
public class DefaultMcpServerTest {
    private ToolExecuteService toolExecuteService;

    @BeforeEach
    void setup() {
        this.toolExecuteService = mock(ToolExecuteService.class);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class GivenConstructor {
        @Test
        @DisplayName("Should throw IllegalArgumentException when toolExecuteService is null")
        void throwIllegalArgumentExceptionWhenToolExecuteServiceIsNull() {
            IllegalArgumentException exception =
                    catchThrowableOfType(IllegalArgumentException.class, () -> new DefaultMcpServer(null));
            assertThat(exception).isNotNull().hasMessage("The tool execute service cannot be null.");
        }
    }

    @Nested
    @DisplayName("getInfo Method Tests")
    class GivenGetInfo {
        @Test
        @DisplayName("Should return expected server information")
        void returnExpectedServerInfo() {
            McpServer server = new DefaultMcpServer(toolExecuteService);
            Server info = server.getInfo();

            assertThat(info).returns("2025-03-26", Server::getProtocolVersion);

            Server.Capabilities capabilities = info.getCapabilities();
            assertThat(capabilities).isNotNull();

            Server.Capabilities.Tools toolsCapability = capabilities.getTools();
            assertThat(toolsCapability).returns(true, Server.Capabilities.Tools::isListChanged);

            Server.Info serverInfo = info.getServerInfo();
            assertThat(serverInfo).returns("FIT Store MCP Server", Server.Info::getName)
                    .returns("3.5.0-SNAPSHOT", Server.Info::getVersion);
        }
    }

    @Nested
    @DisplayName("registerToolsChangedObserver and Notification Tests")
    class GivenRegisterAndNotify {
        @Test
        @DisplayName("Should notify observers when tools are added or removed")
        void notifyObserversOnToolAddOrRemove() {
            DefaultMcpServer server = new DefaultMcpServer(toolExecuteService);
            McpServer.ToolsChangedObserver observer = mock(McpServer.ToolsChangedObserver.class);
            server.registerToolsChangedObserver(observer);

            server.onToolAdded("tool1",
                    "description1",
                    MapBuilder.<String, Object>get().put("schema", "value1").build());
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
            DefaultMcpServer server = new DefaultMcpServer(toolExecuteService);
            String name = "tool1";
            String description = "description1";
            Map<String, Object> schema = MapBuilder.<String, Object>get().put("input", "value").build();

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
            DefaultMcpServer server = new DefaultMcpServer(toolExecuteService);

            server.onToolAdded("", "description", MapBuilder.<String, Object>get().put("input", "value").build());
            assertThat(server.getTools()).isEmpty();

            server.onToolAdded("tool1", "", MapBuilder.<String, Object>get().put("input", "value").build());
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
            DefaultMcpServer server = new DefaultMcpServer(toolExecuteService);
            server.onToolAdded("tool1", "desc", MapBuilder.<String, Object>get().put("input", "value").build());

            server.onToolRemoved("tool1");

            assertThat(server.getTools()).isEmpty();
        }

        @Test
        @DisplayName("Should ignore removal if name is blank")
        void ignoreBlankName() {
            DefaultMcpServer server = new DefaultMcpServer(toolExecuteService);
            server.onToolAdded("tool1", "desc", MapBuilder.<String, Object>get().put("input", "value").build());

            server.onToolRemoved("");

            assertThat(server.getTools()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("callTool Method Tests")
    class GivenCallTool {
        @Test
        @DisplayName("Should call the tool and return correct result")
        void callToolSuccessfully() {
            when(toolExecuteService.execute(anyString(), anyMap())).thenReturn("result");
            McpServer server = new DefaultMcpServer(toolExecuteService);

            Object result = server.callTool("tool1", Map.of("arg1", "value1"));

            assertThat(result).isEqualTo("result");
            verify(toolExecuteService, times(1)).execute(eq("tool1"), anyMap());
        }
    }
}