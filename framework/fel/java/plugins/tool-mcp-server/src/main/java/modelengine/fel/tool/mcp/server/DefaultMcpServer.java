/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.entity.ServerSchema;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fel.tool.service.ToolChangedObserver;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Component;
import io.modelcontextprotocol.server.McpSyncServer;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static modelengine.fel.tool.info.schema.PluginSchema.TYPE;
import static modelengine.fel.tool.info.schema.ToolsSchema.PROPERTIES;
import static modelengine.fel.tool.info.schema.ToolsSchema.REQUIRED;
import static modelengine.fitframework.inspection.Validation.notNull;

/**
 * Mcp Server implementing interface {@link McpServer}, {@link ToolChangedObserver}
 * with MCP Server Bean {@link McpSyncServer}.
 *
 * @author 黄可欣
 * @since 2025-09-30
 */
@Component
public class DefaultMcpServer implements McpServer, ToolChangedObserver {
    private static final Logger log = Logger.get(DefaultMcpServer.class);
    private final McpSyncServer mcpSyncServer;

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    private final ToolExecuteService toolExecuteService;
    private final List<ToolsChangedObserver> toolsChangedObservers = new ArrayList<>();

    /**
     * Constructs a new instance of the DefaultMcpServer class.
     *
     * @param toolExecuteService The service used to execute tools when handling tool call requests.
     * @throws IllegalArgumentException If {@code toolExecuteService} is null.
     */
    public DefaultMcpServer(ToolExecuteService toolExecuteService, McpSyncServer mcpSyncServer) {
        this.toolExecuteService = notNull(toolExecuteService, "The tool execute service cannot be null.");
        this.mcpSyncServer = mcpSyncServer;
    }

    @Override
    public ServerSchema getSchema() {
        ServerSchema.Info info = new ServerSchema.Info("FIT Store MCP Server", "3.6.0-SNAPSHOT");
        ServerSchema.Capabilities.Logging logging = new ServerSchema.Capabilities.Logging();
        ServerSchema.Capabilities.Tools tools = new ServerSchema.Capabilities.Tools(true);
        ServerSchema.Capabilities capabilities = new ServerSchema.Capabilities(logging, tools);
        return new ServerSchema("2025-06-18", capabilities, info);
    }

    @Override
    public List<Tool> getTools() {
        return List.copyOf(this.tools.values());
    }

    @Override
    public void registerToolsChangedObserver(ToolsChangedObserver observer) {
        if (observer != null) {
            this.toolsChangedObservers.add(observer);
        }
    }

    @Override
    public void onToolAdded(String name, String description, Map<String, Object> parameters) {
        if (StringUtils.isBlank(name)) {
            log.warn("Tool addition is ignored: tool name is blank.");
            return;
        }
        if (StringUtils.isBlank(description)) {
            log.warn("Tool addition is ignored: tool description is blank. [toolName={}]", name);
            return;
        }
        if (MapUtils.isEmpty(parameters)) {
            log.warn("Tool addition is ignored: tool schema is null or empty. [toolName={}]", name);
            return;
        }
        if (!(parameters.get(TYPE) instanceof String)
                || parameters.get(PROPERTIES) != null && !(parameters.get(PROPERTIES) instanceof Map)
                || parameters.get(REQUIRED) != null && !(parameters.get(REQUIRED) instanceof List)) {
            log.warn("Invalid parameter schema. [toolName={}]", name);
            return;
        }
        @SuppressWarnings("unchecked")
        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema((String) parameters.get(TYPE),
                (Map<String, Object>) parameters.get(PROPERTIES), (List<String>) parameters.get(REQUIRED),
                null, null,null);
        McpServerFeatures.SyncToolSpecification toolSpecification = McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpSchema.Tool.builder()
                        .name(name)
                        .description(description)
                        .inputSchema(inputSchema)
                        .build())
                .callHandler((exchange, request) -> {
                    Map<String, Object> args = request.arguments();
                    String result = this.toolExecuteService.execute(name, args);
                    return new McpSchema.CallToolResult(result, false);
                })
                .build();
        this.mcpSyncServer.addTool(toolSpecification);

        Tool tool = new Tool();
        tool.setName(name);
        tool.setDescription(description);
        tool.setInputSchema(parameters);
        this.tools.put(name, tool);
        log.info("Tool added to MCP server. [toolName={}, description={}, schema={}]", name, description, inputSchema);
        this.toolsChangedObservers.forEach(ToolsChangedObserver::onToolsChanged);
    }

    @Override
    public void onToolRemoved(String name) {
        if (StringUtils.isBlank(name)) {
            log.warn("Tool removal is ignored: tool name is blank.");
            return;
        }
        this.mcpSyncServer.removeTool(name);
        this.tools.remove(name);
        log.info("Tool removed from MCP server. [toolName={}]", name);
        this.toolsChangedObservers.forEach(ToolsChangedObserver::onToolsChanged);
    }
}
