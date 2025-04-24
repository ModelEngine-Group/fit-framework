/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

/**
 * 表示 http 插件的 json 实体。
 *
 * @author 李金绪
 * @since 2024-11-05
 */
public class HttpJsonEntity extends ToolJsonEntity {
    /**
     * 表示工具的名称。
     */
    private String name;

    /**
     * 表示工具的来源。
     */
    private String source;

    /**
     * 表示工具的图标。
     */
    private String icon;

    /**
     * 表示工具的创建者。
     */
    private String creator;

    /**
     * 表示工具的修改者。
     */
    private String modifier;

    /**
     * 获取工具的来源。
     *
     * @return 表示工具的来源的 {@link String}。
     */
    public String getSource() {
        return this.source;
    }

    /**
     * 设置工具的来源。
     *
     * @param source 表示工具的来源的 {@link String}。
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取工具的图标。
     *
     * @return 表示工具的图标的 {@link String}。
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * 设置工具的图标。
     *
     * @param icon 表示工具的图标的 {@link String}。
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取工具的创建者。
     *
     * @return 表示工具的创建者的 {@link String}。
     */
    public String getCreator() {
        return this.creator;
    }

    /**
     * 设置工具的创建者。
     *
     * @param creator 表示工具的创建者的 {@link String}。
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 获取工具的修改者。
     *
     * @return 表示工具的修改者的 {@link String}。
     */
    public String getModifier() {
        return this.modifier;
    }

    /**
     * 设置工具的修改者。
     *
     * @param modifier 表示工具的修改者的 {@link String}。
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
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
     * 设置工具的名称。
     *
     * @param name 表示工具的名称的 {@link String}。
     */
    public void setName(String name) {
        this.name = name;
    }
}
