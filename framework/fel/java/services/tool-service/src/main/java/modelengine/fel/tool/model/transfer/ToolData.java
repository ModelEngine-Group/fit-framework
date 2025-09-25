/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model.transfer;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolSchema;
import modelengine.fitframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示工具的基本数据内容。
 *
 * @author 鲁为
 * @since 2024-04-18
 */
public class ToolData {
    private String name;
    private String groupName;
    private String defName;
    private String defGroupName;
    private String uniqueName;
    private String description;
    private Map<String, Object> schema;
    private Map<String, Object> runnables;
    private Map<String, Object> extensions;
    private String version;
    private Boolean isLatest;

    /**
     * 获取工具的命名空间。
     *
     * @return 表示工具的命名空间的 {@link String}。
     */
    public String getNamespace() {
        return cast(this.schema.get(ToolSchema.NAME_SPACE));
    }

    /**
     * 获取工具的参数。
     *
     * @return 表示工具的参数的 {@link Map}。
     */
    public Map<String, Object> getParameters() {
        return cast(this.schema.get(ToolSchema.PARAMETERS));
    }

    /**
     * 获取工具的名称。
     *
     * @return 表示工具的名称的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取工具组的名称。
     *
     * @return 表示工具组的名称的 {@link String}。
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * 获取工具定义的名称。
     *
     * @return 表示工具的定义名称的 {@link String}。
     */
    public String getDefName() {
        return this.defName;
    }

    /**
     * 获取工具定义组的名称。
     *
     * @return 表示工具的定义组名称的 {@link String}。
     */
    public String getDefGroupName() {
        return defGroupName;
    }

    /**
     * 获取工具的唯一名称。
     *
     * @return 表示工具的唯一名称的 {@link String}。
     */
    public String getUniqueName() {
        return this.uniqueName;
    }

    /**
     * 获取工具的描述信息。
     *
     * @return 表示工具的描述信息的 {@link String}。
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 获取工具的schema信息。
     *
     * @return 表示工具的schema信息的 {@link Map}。
     */
    public Map<String, Object> getSchema() {
        return this.schema;
    }

    /**
     * 获取工具的可运行信息。
     *
     * @return 表示工具的可运行信息的 {@link Map}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getRunnables() {
        return this.runnables;
    }

    /**
     * 获取工具的扩展信息。
     *
     * @return 表示工具的扩展信息的 {@link Map}。
     */
    public Map<String, Object> getExtensions() {
        return this.extensions;
    }

    /**
     * 获取工具的版本信息。
     *
     * @return 表示工具的版本信息的 {@link String}。
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * 获取工具是否是最新版本。
     *
     * @return 表示工具是否是最新版本的 {@link Boolean}。
     */
    public Boolean getLatest() {
        return isLatest;
    }

    /**
     * 设置工具的名称。
     *
     * @param name 表示工具的名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置工具组的名称。
     *
     * @param groupName 表示工具组的名称的 {@link String}。
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * 设置工具定义的名称。
     *
     * @param defName 表示工具定义的名称的 {@link String}。
     */
    public void setDefName(String defName) {
        this.defName = defName;
    }

    /**
     * 设置工具定义组的名称。
     *
     * @param defGroupName 表示工具定义组的名称的 {@link String}。
     */
    public void setDefGroupName(String defGroupName) {
        this.defGroupName = defGroupName;
    }

    /**
     * 设置工具的唯一名称。
     *
     * @param uniqueName 表示工具的唯一名称的 {@link String}。
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    /**
     * 设置工具的描述信息。
     *
     * @param description 表示工具的描述信息的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 设置工具的schema信息。
     *
     * @param schema 表示工具的schema信息的 {@link Map}。
     */
    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

    /**
     * 设置工具的可运行信息。
     *
     * @param runnables 表示工具的可运行信息的 {@link Map}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setRunnables(Map<String, Object> runnables) {
        this.runnables = runnables;
    }

    /**
     * 设置工具的扩展信息。
     *
     * @param extensions 表示工具的扩展信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    /**
     * 设置工具的版本信息。
     *
     * @param version 表示工具的版本信息的 {@link String}。
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 设置工具是否是最新版本。
     *
     * @param latest 表示工具是否是最新版本的 {@link Boolean}。
     */
    public void setLatest(Boolean latest) {
        isLatest = latest;
    }

    /**
     * 将工具数据转换为工具信息。
     *
     * @param toolData 表示工具数据的 {@link ToolData}。
     * @return 表示工具信息的 {@link Tool.Info}。
     */
    public static Tool.Info convertToInfo(ToolData toolData) {
        return Tool.Info.custom()
                .name(toolData.getName())
                .groupName(toolData.getGroupName())
                .definitionName(toolData.getDefName())
                .definitionGroupName(toolData.getDefGroupName())
                .uniqueName(toolData.getUniqueName())
                .description(toolData.getDescription())
                .schema(toolData.getSchema())
                .runnables(toolData.getRunnables())
                .extensions(toolData.getExtensions())
                .version(toolData.getVersion())
                .isLatest(ObjectUtils.nullIf(toolData.getLatest(), true))
                .returnConverter(ObjectUtils.cast(toolData.getSchema().get(ToolSchema.RETURN_CONVERTER)))
                .defaultParameterValues(ToolData.defaultParamValues(toolData.getSchema()))
                .parameters(toolData.getParameters())
                .namespace(toolData.getNamespace())
                .build();
    }

    /**
     * 从工具信息创建工具数据。
     *
     * @param info 表示工具信息的 {@link Tool.Info}。
     * @return 表示工具数据的 {@link ToolData}。
     */
    public static ToolData from(Tool.Info info) {
        ToolData toolData = new ToolData();
        toolData.setName(info.name());
        toolData.setUniqueName(info.uniqueName());
        toolData.setDescription(info.description());
        toolData.setSchema(info.schema());
        toolData.setRunnables(info.runnables());
        toolData.setExtensions(info.extensions());
        toolData.setVersion(info.version());
        toolData.setLatest(info.isLatest());
        toolData.setGroupName(info.groupName());
        toolData.setDefName(info.definitionName());
        toolData.setDefGroupName(info.definitionGroupName());
        return toolData;
    }

    /**
     * 转换工具数据。
     *
     * @param toolData 表示工具数据的 {@link ToolData}。
     * @return 表示转换后的工具数据的 {@link ToolData}。
     */
    public static ToolData transform(ToolData toolData) {
        toolData.setName(ObjectUtils.cast(toolData.getSchema().get(ToolSchema.NAME)));
        if (toolData.getSchema().containsKey(ToolSchema.DESCRIPTION)) {
            toolData.setDescription(ObjectUtils.cast(toolData.getSchema().get(ToolSchema.DESCRIPTION)));
        }
        return toolData;
    }

    /**
     * 解析获取所有参数的默认值。
     *
     * @param schema 表示参数的描述信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     * @return 表示所有参数默认值的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public static Map<String, Object> defaultParamValues(Map<String, Object> schema) {
        Map<String, Object> tempParamValue = new HashMap<>();
        Map<String, Object> parameters = ObjectUtils.cast(schema.get(ToolSchema.PARAMETERS));
        if (parameters == null) {
            return tempParamValue;
        }
        Map<String, Object> properties = ObjectUtils.cast(parameters.get(ToolSchema.PARAMETERS_PROPERTIES));
        if (properties == null) {
            return tempParamValue;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Map<String, Object> property = cast(entry.getValue());
            Object value = property.get(ToolSchema.DEFAULT_PARAMETER);
            if (value != null) {
                tempParamValue.put(entry.getKey(), value);
            }
        }
        return tempParamValue;
    }
}