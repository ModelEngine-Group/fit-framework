/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.mcp.entity;

import java.util.Map;

/**
 * Represents a tool entity with name, description, and schema.
 *
 * @since 2025-05-15
 */
public class Tool {
    /**
     * The name of the tool.
     * This serves as a unique identifier for the tool within the system.
     */
    private String name;

    /**
     * A brief description of the tool.
     * Provides human-readable information about what the tool does.
     */
    private String description;

    /**
     * The input schema that defines the expected parameters when invoking the tool.
     * Typically represented using a map structure, e.g., JSON schema format.
     */
    private Map<String, Object> inputSchema;

    /**
     * Gets the name of the tool.
     *
     * @return The tool's name as a {@link String}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the tool.
     *
     * @param name The new name for the tool, as a {@link String}.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the tool.
     *
     * @return The tool's description as a {@link String}.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of the tool.
     *
     * @param description The new description for the tool, as a {@link String}.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the input schema of the tool.
     * This defines the required and optional parameters for invoking the tool.
     *
     * @return The tool's input schema as a {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     */
    public Map<String, Object> getInputSchema() {
        return this.inputSchema;
    }

    /**
     * Sets the input schema of the tool.
     *
     * @param inputSchema The new input schema for the tool, as a
     * {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}.
     */
    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
}
