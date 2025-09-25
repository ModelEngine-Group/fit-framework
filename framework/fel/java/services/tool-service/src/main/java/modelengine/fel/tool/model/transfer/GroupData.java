/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model.transfer;

import java.util.Map;

/**
 * 表示组的基本内容。
 *
 * @author 李金绪
 * @since 2024-12-09
 */
public class GroupData {
    private String name;
    private String summary;
    private String description;
    private Map<String, Object> extensions;

    /**
     * 获取组名。
     *
     * @return 表示组名的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置组名。
     *
     * @param name 表示组名的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取摘要。
     *
     * @return 表示摘要的 {@link String}。
     */
    public String getSummary() {
        return this.summary;
    }

    /**
     * 设置摘要
     *
     * @param summary 表示要设置的摘要的 {@link String}。
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 获取描述。
     *
     * @return 表示描述的 {@link String}。
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置描述。
     *
     * @param description 表示要设置的描述的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取扩展属性。
     *
     * @return 表示扩展属性的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public Map<String, Object> getExtensions() {
        return this.extensions;
    }

    /**
     * 设置扩展属性。
     *
     * @param extensions 扩展属性的 {@link Map}{@code <}{@link String}{@code , }{@link Object}{@code >}。
     */
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
}
