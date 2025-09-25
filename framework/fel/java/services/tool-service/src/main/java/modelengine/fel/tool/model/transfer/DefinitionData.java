/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model.transfer;

import modelengine.fel.tool.Tool;

import java.util.Map;

/**
 * 表示定义的基本内容。
 *
 * @author 王攀博
 * @since 2024-10-25
 */
public class DefinitionData {
    private Map<String, Object> schema;
    private String name;
    private String groupName;
    private String description;

    /**
     * 获取工具定义的摘要信息。
     *
     * @return 表示工具定义的摘要信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getSchema() {
        return this.schema;
    }

    /**
     * 设置工具定义的摘要信息。
     *
     * @param schema 表示工具定义的摘要信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

    /**
     * 获取定义名称。
     *
     * @return 表示定义名称的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置定义名称。
     *
     * @param name 表示定义名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取定义组名称。
     *
     * @return 表示定义组名称的 {@link String}。
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * 设置定义组名称。
     *
     * @param groupName 表示定义组名称的 {@link String}。
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * 获取定义的描述信息。
     *
     * @return 表示定义的描述信息的 {@link String}。
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置定义的描述信息。
     *
     * @param description 表示定义的描述信息的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 将工具元数据转换为工具定义数据。
     *
     * @param metadata 表示工具元数据的 {@link Tool.Metadata}
     * @return 表示工具定义数据的 {@link DefinitionData}。
     */
    public static DefinitionData from(Tool.Metadata metadata) {
        DefinitionData definitionData = new DefinitionData();
        definitionData.setGroupName(metadata.definitionGroupName());
        definitionData.setName(metadata.definitionName());
        definitionData.setSchema(metadata.schema());
        definitionData.setDescription(metadata.description());
        return definitionData;
    }
}
