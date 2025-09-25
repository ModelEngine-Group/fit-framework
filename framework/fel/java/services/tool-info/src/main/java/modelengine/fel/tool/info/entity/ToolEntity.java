/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import static modelengine.fel.tool.info.schema.PluginSchema.DESCRIPTION;
import static modelengine.fel.tool.info.schema.PluginSchema.TYPE;
import static modelengine.fel.tool.info.schema.ToolsSchema.NAME;
import static modelengine.fel.tool.info.schema.ToolsSchema.ORDER;
import static modelengine.fel.tool.info.schema.ToolsSchema.PARAMETERS;
import static modelengine.fel.tool.info.schema.ToolsSchema.PARAMETER_EXTENSIONS;
import static modelengine.fel.tool.info.schema.ToolsSchema.PROPERTIES;
import static modelengine.fel.tool.info.schema.ToolsSchema.REQUIRED;
import static modelengine.fel.tool.info.schema.ToolsSchema.RETURN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示工具的实体类。
 *
 * @author 曹嘉美
 * @author 李金绪
 * @author 杭潇
 * @since 2024-10-26
 */
public class ToolEntity {
    private String namespace;
    private SchemaEntity schema;
    private Map<String, Object> runnables;
    private Map<String, Object> extensions;
    private List<String> tags;
    private String definitionName;

    /**
     * 获取工具的命名空间。
     *
     * @return 表示工具命名空间的 {@link String}。
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * 配置工具的命名空间。
     *
     * @param namespace 表示工具命名空间的 {@link String}。
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * 获取 schema 对象。
     *
     * @return 表示 schema 对象的 {@link SchemaEntity}。
     */
    public SchemaEntity getSchema() {
        return this.schema;
    }

    /**
     * 设置 schema 对象。
     *
     * @param schema 表示 schema 对象的 {@link SchemaEntity}。
     */
    public void setSchema(SchemaEntity schema) {
        this.schema = schema;
    }

    /**
     * 获取 runnables 对象。
     *
     * @return 表示 runnables 对象的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getRunnables() {
        return this.runnables;
    }

    /**
     * 设置 runnables 对象。
     *
     * @param runnables 表示 runnables 对象的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */

    public void setRunnables(Map<String, Object> runnables) {
        this.runnables = runnables;
    }

    /**
     * 获取扩展信息。
     *
     * @return 表示扩展信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getExtensions() {
        return this.extensions;
    }

    /**
     * 设置扩展信息。
     *
     * @param extensions 表示扩展信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    /**
     * 获取标签列表。
     *
     * @return 表示标签列表的 {@link List}{@code <}{@link String}{@code >}。
     */
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * 设置标签列表。
     *
     * @param tags 表示标签列表的 {@link List}{@code <}{@link String}{@code >}。
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * 获取工具的名称。
     *
     * @return 表示工具名称的 {@link String}。
     */
    public String getDefinitionName() {
        return this.definitionName;
    }

    /**
     * 设置工具的名称。
     *
     * @param definitionName 表示工具名称的 {@link String}。
     */
    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    /**
     * 将 Schema 中的 Parameters 转换为 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >} 结构。
     *
     * @return 转换后的数据的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> parameters() {
        Map<String, Object> paramsMap = new HashMap<>();
        ParameterEntity paramEntity = this.schema.getParameters();
        paramsMap.put(TYPE, paramEntity.getType());
        paramsMap.put(PROPERTIES, paramEntity.getProperties());
        paramsMap.put(REQUIRED, paramEntity.getRequired());
        return paramsMap;
    }

    /**
     * 将 Schema 中的数据转换为 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >} 结构。
     *
     * @return 转换后的数据的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> schema() {
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put(NAME, this.schema.getName());
        schemaMap.put(DESCRIPTION, this.schema.getDescription());
        schemaMap.put(PARAMETERS, this.parameters());
        schemaMap.put(ORDER, this.schema.getOrder());
        schemaMap.put(RETURN, this.schema.getRet());
        schemaMap.put(PARAMETER_EXTENSIONS, this.schema.getParameterExtensions());
        return schemaMap;
    }
}
