/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.tool.info.entity;

import java.util.List;

/**
 * 表示实现组的实体类。
 *
 * @author 曹嘉美
 * @author 李金绪
 * @since 2024-10-26
 */
public class ToolGroupEntity extends GroupEntity {
    private String definitionGroupName;
    private List<ToolEntity> tools;

    /**
     * 获取定义组名称。
     *
     * @return 表示定义组名称的 {@link String}。
     */
    public String getDefinitionGroupName() {
        return this.definitionGroupName;
    }

    /**
     * 设置定义组名称。
     *
     * @param definitionGroupName 表示定义组名称的 {@link String}。
     */
    public void setDefinitionGroupName(String definitionGroupName) {
        this.definitionGroupName = definitionGroupName;
    }

    /**
     * 获取工具列表。
     *
     * @return 表示工具列表的 {@link List}{@code <}{@link ToolEntity}{@code >}。
     */
    public List<ToolEntity> getTools() {
        return this.tools;
    }

    /**
     * 设置工具列表。
     *
     * @param tools 表示工具列表的 {@link List}{@code <}{@link ToolEntity}{@code >}。
     */
    public void setTools(List<ToolEntity> tools) {
        this.tools = tools;
    }
}
