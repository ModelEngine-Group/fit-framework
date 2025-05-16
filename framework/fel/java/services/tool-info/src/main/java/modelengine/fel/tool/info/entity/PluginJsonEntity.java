/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import java.util.Map;

/**
 * 用于 plugin.json 的序列化与反序列化的实体类。
 *
 * @author 李金绪
 * @since 2024-10-28
 */
public class PluginJsonEntity {
    private String checksum;
    private String name;
    private String description;
    private String type;
    private Map<String, String> uniqueness;

    /**
     * 获取插件的校验和。
     *
     * @return 表示校验和的 {@link String}。
     */
    public String getChecksum() {
        return this.checksum;
    }

    /**
     * 设置插件的校验和。
     *
     * @param checksum 表示校验和的 {@link String}。
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * 获取插件的名称。
     *
     * @return 表示插件名称的 {@link String}。
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置插件的名称。
     *
     * @param name 表示插件名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取插件的描述信息。
     *
     * @return 表示插件描述信息的 {@link String}。
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置插件的描述信息。
     *
     * @param description 表示插件描述信息的 {@link String}。
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取插件的唯一性信息。
     *
     * @return 表示插件唯一性信息的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    public Map<String, String> getUniqueness() {
        return this.uniqueness;
    }

    /**
     * 设置插件的唯一性信息。
     *
     * @param uniqueness 表示插件唯一性信息的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    public void setUniqueness(Map<String, String> uniqueness) {
        this.uniqueness = uniqueness;
    }

    /**
     * 获取插件的类型。
     *
     * @return 表示插件类型的 {@link String}。
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置插件的类型。
     *
     * @param type 表示插件类型的 {@link String}。
     */
    public void setType(String type) {
        this.type = type;
    }
}
