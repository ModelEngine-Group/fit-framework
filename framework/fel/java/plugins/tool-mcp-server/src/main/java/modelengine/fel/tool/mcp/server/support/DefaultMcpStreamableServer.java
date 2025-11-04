/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.support;

import static modelengine.fel.tool.info.schema.PluginSchema.TYPE;
import static modelengine.fel.tool.info.schema.ToolsSchema.PROPERTIES;
import static modelengine.fel.tool.info.schema.ToolsSchema.REQUIRED;
import static modelengine.fitframework.inspection.Validation.notNull;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import modelengine.fel.tool.mcp.entity.Tool;
import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.service.ToolChangedObserver;
import modelengine.fel.tool.service.ToolExecuteService;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mcp Server implementing interface {@link McpServer}, {@link ToolChangedObserver}
 * with MCP Server Bean {@link McpSyncServer}.
 *
 * @author 季聿阶
 * @since 2025-05-15
 */
@Component
public class DefaultMcpStreamableServer implements McpServer, ToolChangedObserver {
    private static final Logger log = Logger.get(DefaultMcpStreamableServer.class);
    private final McpSyncServer mcpSyncServer;

    private final ToolExecuteService toolExecuteService;
    private final List<ToolsChangedObserver> toolsChangedObservers = new ArrayList<>();

    /**
     * Constructs a new instance of the DefaultMcpServer class.
     *
     * @param toolExecuteService The service used to execute tools when handling tool call requests.
     * @throws IllegalArgumentException If {@code toolExecuteService} is null.
     */
    public DefaultMcpStreamableServer(ToolExecuteService toolExecuteService, McpSyncServer mcpSyncServer) {
        this.toolExecuteService = notNull(toolExecuteService, "The tool execute service cannot be null.");
        this.mcpSyncServer = mcpSyncServer;
    }

    @Override
    public List<Tool> getTools() {
        return this.mcpSyncServer.listTools().stream().map(this::convertToFelTool).collect(Collectors.toList());
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
        if (!isValidParameterSchema(parameters)) {
            log.warn("Invalid parameter schema. [toolName={}]", name);
            return;
        }

        McpServerFeatures.SyncToolSpecification toolSpecification =
                createToolSpecification(name, description, parameters);

        this.mcpSyncServer.addTool(toolSpecification);
        log.info("Tool added to MCP server. [toolName={}, description={}, schema={}]", name, description, parameters);
        this.toolsChangedObservers.forEach(ToolsChangedObserver::onToolsChanged);
    }

    @Override
    public void onToolRemoved(String name) {
        if (StringUtils.isBlank(name)) {
            log.warn("Tool removal is ignored: tool name is blank.");
            return;
        }
        this.mcpSyncServer.removeTool(name);
        log.info("Tool removed from MCP server. [toolName={}]", name);
        this.toolsChangedObservers.forEach(ToolsChangedObserver::onToolsChanged);
    }

    /**
     * Creates a tool specification for the MCP server.
     * <p>
     * This method constructs a {@link McpServerFeatures.SyncToolSpecification} that includes:
     * <ul>
     *     <li>Tool metadata (name, description, input schema)</li>
     *     <li>Call handler that executes the tool and handles exceptions</li>
     * </ul>
     *
     * @param name The name of the tool.
     * @param description The description of the tool.
     * @param parameters The parameter schema containing type, properties, and required fields.
     * @return A fully configured {@link McpServerFeatures.SyncToolSpecification}.
     */
    private McpServerFeatures.SyncToolSpecification createToolSpecification(String name, String description,
            Map<String, Object> parameters) {
        @SuppressWarnings("unchecked") McpSchema.JsonSchema inputSchema =
                new McpSchema.JsonSchema((String) parameters.get(TYPE),
                        (Map<String, Object>) parameters.get(PROPERTIES),
                        (List<String>) parameters.get(REQUIRED),
                        null,
                        null,
                        null);

        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpSchema.Tool.builder().name(name).description(description).inputSchema(inputSchema).build())
                .callHandler((exchange, request) -> executeToolWithErrorHandling(name, request))
                .build();
    }

    /**
     * Executes a tool and handles any exceptions that may occur.
     * <p>
     * This method handles two types of exceptions:
     * <ul>
     *     <li>{@link IllegalArgumentException}: Invalid tool arguments (logged as warning)</li>
     *     <li>{@link Exception}: Any other execution failure (logged as error)</li>
     * </ul>
     *
     * @param toolName The name of the tool to execute.
     * @param request The tool call request containing arguments.
     * @return A {@link McpSchema.CallToolResult} with the execution result or error message.
     */
    private McpSchema.CallToolResult executeToolWithErrorHandling(String toolName, McpSchema.CallToolRequest request) {
        try {
            Map<String, Object> args = request.arguments();
            String result = this.toolExecuteService.execute(toolName, args);
            return new McpSchema.CallToolResult(result, false);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid arguments for tool execution. [toolName={}, error={}]", toolName, e.getMessage());
            return new McpSchema.CallToolResult("Error: Invalid arguments - " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("Failed to execute tool. [toolName={}]", toolName, e);
            return new McpSchema.CallToolResult("Error: Tool execution failed - " + e.getMessage(), true);
        }
    }

    /**
     * Converts an MCP SDK Tool to a FEL Tool entity.
     *
     * @param mcpTool The MCP SDK tool to convert.
     * @return A FEL Tool entity with the corresponding name, description, and input schema.
     */
    private Tool convertToFelTool(McpSchema.Tool mcpTool) {
        Tool tool = new Tool();
        tool.setName(mcpTool.name());
        tool.setDescription(mcpTool.description());

        // Convert JsonSchema to Map<String, Object>
        McpSchema.JsonSchema inputSchema = mcpTool.inputSchema();
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put(TYPE, inputSchema.type());
        if (inputSchema.properties() != null) {
            schemaMap.put(PROPERTIES, inputSchema.properties());
        }
        if (inputSchema.required() != null) {
            schemaMap.put(REQUIRED, inputSchema.required());
        }
        tool.setInputSchema(schemaMap);

        return tool;
    }

    /**
     * Validates the structure of the parameter schema to ensure it conforms to the expected format.
     *
     * @param parameters The parameter schema to validate, represented as a Map with String keys and Object values.
     * @return {@code true} if the parameter schema is valid; {@code false} otherwise.
     */
    private boolean isValidParameterSchema(Map<String, Object> parameters) {
        Object type = parameters.get(TYPE);
        if (!(type instanceof String)) {
            return false;
        }

        Object props = parameters.get(PROPERTIES);
        if (!(props instanceof Map<?, ?> propsMap)) {
            return false;
        }
        if (propsMap.keySet().stream().anyMatch(k -> !(k instanceof String))) {
            return false;
        }

        Object reqs = parameters.get(REQUIRED);
        if (!(reqs instanceof List<?> reqsList)) {
            return false;
        }
        return reqsList.stream().allMatch(v -> v instanceof String);
    }
}
