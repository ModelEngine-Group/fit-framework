/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.model.transfer;

import java.util.List;

/**
 * 表示工具组的数据。
 *
 * @author 王攀博
 * @since 2024-10-25
 */
public class ToolGroupData extends GroupData {
    private String defGroupName;
    private List<ToolData> tools;

    /**
     * 获取定义组名。
     *
     * @return 表示获取定义组名的 {@link String}。
     */
    public String getDefGroupName() {
        return this.defGroupName;
    }

    /**
     * 设置定义组名。
     *
     * @param name 表示设置定义组名的 {@link String}。
     */
    public void setDefGroupName(String name) {
        this.defGroupName = name;
    }

    /**
     * 获取工具数据列表。
     *
     * @return 表示工具数据列表的 {@link List}{@code <}{@link ToolData}{@code >}。
     */
    public List<ToolData> getTools() {
        return this.tools;
    }

    /**
     * 设置工具数据列表。
     *
     * @param tools 表示要设置的工具数据列表的 {@link List}{@code <}{@link ToolData}{@code >}。
     */
    public void setTools(List<ToolData> tools) {
        this.tools = tools;
    }
}
