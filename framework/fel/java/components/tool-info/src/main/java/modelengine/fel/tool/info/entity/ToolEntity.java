/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import java.util.List;
import java.util.Map;

/**
 * 表示工具的实体类。
 *
 * @author 曹嘉美
 * @author 李金绪
 * @since 2024-10-26
 */
public class ToolEntity {
    private SchemaEntity schema;
    private Map<String, Object> runnables;
    private Map<String, Object> extensions;
    private List<String> tags;
    private String definitionName;

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
}
