/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import modelengine.fitframework.annotation.Property;

import java.util.List;
import java.util.Map;

/**
 * 表示定义组和实现组的结构类。
 *
 * @since 2024-10-26
 */
public class SchemaEntity {
    private String name;
    private String description;
    private ParameterEntity parameters;
    private List<String> order;
    @Property(name = "return")
    private Map<String, Object> ret;
    private Map<String, Object> parameterExtensions;

    /**
     * 获取结构的名称。
     *
     * @return 表示结构的名称的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置结构的名称。
     *
     * @param name 表示结构的名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取结构的描述。
     *
     * @return 表示结构的描述的 {@link String}。
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置结构的描述。
     *
     * @param description 表示结构的描述的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取结构的参数。
     *
     * @return 表示结构的参数的 {@link ParameterEntity}。
     */
    public ParameterEntity getParameters() {
        return this.parameters;
    }

    /**
     * 设置结构的参数。
     *
     * @param parameters 表示结构的参数的 {@link ParameterEntity}。
     */
    public void setParameters(ParameterEntity parameters) {
        this.parameters = parameters;
    }

    /**
     * 获取参数的执行顺序。
     *
     * @return 表示参数的执行顺序的 {@link List}{@code <}{@link String}{@code >}。
     */
    public List<String> getOrder() {
        return this.order;
    }

    /**
     * 设置参数的执行顺序。
     *
     * @param order 表示参数的执行顺序的 {@link List}{@code <}{@link String}{@code >}。
     */
    public void setOrder(List<String> order) {
        this.order = order;
    }

    /**
     * 获取方法的返回值。
     *
     * @return 表示方法的返回值的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getRet() {
        return this.ret;
    }

    /**
     * 设置方法的返回值。
     *
     * @param ret 表示方法的返回值的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setRet(Map<String, Object> ret) {
        this.ret = ret;
    }

    /**
     * 获取扩展参数的信息。
     *
     * @return 表示扩展参数的信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getParameterExtensions() {
        return this.parameterExtensions;
    }

    /**
     * 设置扩展参数的信息。
     *
     * @param parameterExtensions 表示扩展参数的信息的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setParameterExtensions(Map<String, Object> parameterExtensions) {
        this.parameterExtensions = parameterExtensions;
    }
}
