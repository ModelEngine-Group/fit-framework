/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

/**
 * 表示参数属性的实体类。
 *
 * @author 曹嘉美
 * @author 李金绪
 * @since 2024-10-26
 */
public class PropertyEntity {
    private String defaultValue;
    private String description;
    private String name;
    private String type;
    private Object items;
    private Object properties;
    private String examples;
    private boolean required;

    /**
     * 获取参数的默认值。
     *
     * @return 表示参数的默认值的 {@link String}。
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * 设置参数的默认值。
     *
     * @param defaultValue 表示参数的默认值的 {@link String}。
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * 获取参数的描述信息。
     *
     * @return 表示参数的描述信息的 {@link String}。
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置参数的描述信息。
     *
     * @param description 表示参数的描述信息的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取参数是否是必需的标志。
     *
     * @return 如果参数是必需的，则返回 {@code true}，否则返回 {@code false}。
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * 设置参数是否是必需的标志。
     *
     * @param required 表示参数是否是必需的 {@link boolean}。
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * 获取参数的名称。
     *
     * @return 表示参数的名称的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置参数的名称。
     *
     * @param name 表示参数的名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取参数的类型。
     *
     * @return 表示参数的类型的 {@link String}。
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置参数的类型。
     *
     * @param type 表示参数的类型的 {@link String}。
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取参数的子类型。
     *
     * @return 表示参数的子类型的 {@link Object}。
     */
    public Object getItems() {
        return items;
    }

    /**
     * 设置参数的子类型。
     *
     * @param items 表示参数的子类型的 {@link Object}。
     */
    public void setItems(Object items) {
        this.items = items;
    }

    /**
     * 获取参数的属性。
     *
     * @return 表示参数的属性的 {@link Object}。
     */
    public Object getProperties() {
        return properties;
    }

    /**
     * 设置参数的属性。
     *
     * @param properties 表示参数的属性的 {@link Object}。
     */
    public void setProperties(Object properties) {
        this.properties = properties;
    }

    /**
     * 获取参数的示例值。
     *
     * @return 表示参数的示例值的 {@link String}。
     */
    public String getExamples() {
        return this.examples;
    }

    /**
     * 设置参数的示例值。
     *
     * @param examples 表示参数的示例值的 {@link String}。
     */
    public void setExamples(String examples) {
        this.examples = examples;
    }
}
