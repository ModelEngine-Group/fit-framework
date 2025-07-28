/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import java.util.List;
import java.util.Map;

/**
 * 表示参数的实体类。
 *
 * @since 2024-10-26
 */
public class ParameterEntity {
    private String type;
    private Map<String, Object> properties;
    private List<String> required;

    /**
     * 获取参数类型。
     *
     * @return 表示参数类型的 {@link String}。
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置参数类型。
     *
     * @param type 表示参数类型的 {@link String}。
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取参数的属性。
     *
     * @return 表示参数属性的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * 设置参数的属性。
     *
     * @param properties 表示参数属性的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * 获取参数的必填项。
     *
     * @return 表示参数必填项的 {@link List}{@code <}{@link String}{@code >}。
     */
    public List<String> getRequired() {
        return this.required;
    }

    /**
     * 设置参数的必填项。
     *
     * @param required 表示参数必填项的 {@link List}{@code <}{@link String}{@code >}。
     */
    public void setRequired(List<String> required) {
        this.required = required;
    }
}
