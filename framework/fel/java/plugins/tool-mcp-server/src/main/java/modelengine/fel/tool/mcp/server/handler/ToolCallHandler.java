/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.server.handler;

import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.mcp.server.McpServer;
import modelengine.fel.tool.mcp.server.MessageRequest;
import modelengine.fel.tool.mcp.server.MessageResponse;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * A handler for processing tool call requests in the MCP server.
 * This class extends {@link AbstractMessageHandler} and is responsible for handling
 * {@link ToolCallRequest} messages by invoking the specified tool via the associated {@link McpServer}.
 * It serializes the result using the provided {@link ObjectSerializer} and returns a structured
 * response through the {@link ToolCallResponse} class.
 *
 * @since 2025-05-15
 */
public class ToolCallHandler extends AbstractMessageHandler<ToolCallHandler.ToolCallRequest> {
    private final McpServer mcpServer;
    private final ObjectSerializer jsonSerializer;

    /**
     * Constructs a new instance of the ToolCallHandler class.
     *
     * @param mcpServer The MCP server instance used to invoke tools during request handling.
     * @param jsonSerializer The serializer used to convert non-string results into JSON strings.
     * @throws IllegalArgumentException If {@code mcpServer} or {@code jsonSerializer} is null.
     */
    public ToolCallHandler(McpServer mcpServer, ObjectSerializer jsonSerializer) {
        super(ToolCallRequest.class);
        this.mcpServer = notNull(mcpServer, "The MCP server cannot be null.");
        this.jsonSerializer = notNull(jsonSerializer, "The json serializer cannot be null.");
    }

    @Override
    protected Object handle(ToolCallRequest request) {
        if (request == null) {
            throw new IllegalStateException("No tool call request.");
        }
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalStateException("No tool name to call.");
        }
        ToolCallResponse response = new ToolCallResponse();
        ToolCallResponse.Content content = new ToolCallResponse.Content();
        response.setContents(List.of(content));
        content.setType("text");
        try {
            Object result = this.mcpServer.callTool(request.getName(), request.getArguments());
            if (result instanceof String) {
                content.setText(cast(result));
            } else {
                content.setText(this.jsonSerializer.serialize(result));
            }
            response.setError(false);
        } catch (Exception e) {
            content.setText(e.getMessage());
            response.setError(true);
        }
        return response;
    }

    /**
     * Represents a tool call request in the MCP server.
     * This request contains the name of the tool to be invoked and a map of arguments
     * to be passed to the tool. It is handled by {@link ToolCallHandler} to execute the tool
     * and return the result.
     *
     * @since 2025-05-15
     */
    public static class ToolCallRequest extends MessageRequest {
        private String name;
        private Map<String, Object> arguments;

        /**
         * Gets the name of the tool to be called.
         *
         * @return The name of the tool as a {@link String}.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Sets the name of the tool to be called.
         *
         * @param name The name of the tool as a {@link String}.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the arguments to be passed to the tool.
         *
         * @return A map containing the arguments as a {@link Map}{@code <}{@link String}{@code ,
         * }{@link Object}{@code >}.
         */
        public Map<String, Object> getArguments() {
            return this.arguments;
        }

        /**
         * Sets the arguments to be passed to the tool.
         *
         * @param arguments A map containing the arguments as a
         * {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
         */
        public void setArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }
    }

    /**
     * Represents the structured response returned after executing a tool call.
     * This class includes a list of content items and an error flag indicating
     * whether the execution was successful.
     *
     * <p>Each content item has a type and text value, which can be used to represent
     * the result or error message from the tool execution.</p>
     *
     * @since 2025-05-15
     */
    public static class ToolCallResponse extends MessageResponse {
        @Property(name = "content")
        private List<Content> contents;
        private boolean isError;

        /**
         * Gets the list of content items included in the response.
         *
         * @return A list of content items as a {@link List}{@code <}{@link Content}{@code >}.
         */
        public List<Content> getContents() {
            return this.contents;
        }

        /**
         * Sets the list of content items included in the response.
         *
         * @param contents A list of content items as a {@link List}{@code <}{@link Content}{@code >}.
         */
        public void setContents(List<Content> contents) {
            this.contents = contents;
        }

        /**
         * Checks whether the tool execution resulted in an error.
         *
         * @return true if an error occurred; false otherwise.
         */
        public boolean isError() {
            return this.isError;
        }

        /**
         * Sets the error flag indicating whether the tool execution resulted in an error.
         *
         * @param error true if an error occurred; false otherwise.
         */
        public void setError(boolean error) {
            this.isError = error;
        }

        /**
         * Represents a single content item within the tool call response.
         * Each content item has a type (e.g., "text", "json") and a text value,
         * typically used to describe the result or error message from the tool execution.
         *
         * <p>This class supports multiple content formats, allowing flexible representation
         * of the tool's output.</p>
         *
         * @since 2025-05-15
         */
        public static class Content {
            private String type;
            private String text;

            /**
             * Gets the type of the content item.
             *
             * @return The type of the content as a {@link String}.
             */
            public String getType() {
                return this.type;
            }

            /**
             * Sets the type of the content item.
             *
             * @param type The type of the content as a {@link String}.
             */
            public void setType(String type) {
                this.type = type;
            }

            /**
             * Gets the text value of the content item.
             *
             * @return The text value as a {@link String}.
             */
            public String getText() {
                return this.text;
            }

            /**
             * Sets the text value of the content item.
             *
             * @param text The text value as a {@link String}.
             */
            public void setText(String text) {
                this.text = text;
            }
        }
    }
}
