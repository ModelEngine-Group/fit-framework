/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.mcp.server.entity.ToolEntity;
import modelengine.fel.tool.service.ToolChangedObserver;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default implementation of {@link McpServer}.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
@Component
public class DefaultMcpServer implements McpServer, ToolChangedObserver {
    private static final Logger log = Logger.get(DefaultMcpServer.class);

    private final ToolExecuteService toolExecuteService;
    private final Map<String, ToolEntity> tools = new ConcurrentHashMap<>();
    private final List<ToolsChangedObserver> toolsChangedObservers = new ArrayList<>();

    /**
     * Constructs a new instance of the DefaultMcpServer class.
     *
     * @param toolExecuteService The service used to execute tools when handling tool call requests.
     * @throws IllegalArgumentException If {@code toolExecuteService} is null.
     */
    public DefaultMcpServer(ToolExecuteService toolExecuteService) {
        this.toolExecuteService = notNull(toolExecuteService, "The tool execute service cannot be null.");
    }

    @Override
    public Map<String, Object> getInfo() {
        return MapBuilder.<String, Object>get()
                .put("protocolVersion", "2025-03-26")
                .put("capabilities",
                        MapBuilder.get()
                                .put("logging", MapBuilder.get().build())
                                .put("tools", MapBuilder.get().put("listChanged", true).build())
                                .build())
                .put("serverInfo",
                        MapBuilder.get().put("name", "FIT Store MCP Server").put("version", "3.5.0-SNAPSHOT").build())
                .build();
    }

    @Override
    public List<ToolEntity> getTools() {
        return List.copyOf(this.tools.values());
    }

    @Override
    public Object callTool(String name, Map<String, Object> arguments) {
        log.info("Calling tool. [toolName={}, arguments={}]", name, arguments);
        String result = this.toolExecuteService.execute(name, arguments);
        log.info("Tool called. [result={}]", result);
        return result;
    }

    @Override
    public void registerToolsChangedObserver(ToolsChangedObserver observer) {
        if (observer != null) {
            this.toolsChangedObservers.add(observer);
        }
    }

    @Override
    public void onToolAdded(String name, String description, Map<String, Object> schema) {
        if (StringUtils.isBlank(name)) {
            log.warn("Tool addition is ignored: tool name is blank.");
            return;
        }
        if (StringUtils.isBlank(description)) {
            log.warn("Tool addition is ignored: tool description is blank. [toolName={}]", name);
            return;
        }
        if (MapUtils.isEmpty(schema)) {
            log.warn("Tool addition is ignored: tool schema is null or empty. [toolName={}]", name);
            return;
        }
        ToolEntity tool = new ToolEntity();
        tool.setName(name);
        tool.setDescription(description);
        tool.setInputSchema(schema);
        this.tools.put(name, tool);
        log.info("Tool added to MCP server. [toolName={}, description={}, schema={}]", name, description, schema);
        this.toolsChangedObservers.forEach(ToolsChangedObserver::onToolsChanged);
    }

    @Override
    public void onToolRemoved(String name) {
        if (StringUtils.isBlank(name)) {
            log.warn("Tool removal is ignored: tool name is blank.");
            return;
        }
        this.tools.remove(name);
        log.info("Tool removed from MCP server. [toolName={}]", name);
        this.toolsChangedObservers.forEach(ToolsChangedObserver::onToolsChanged);
    }
}
